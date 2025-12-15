# Chatbot Integration - Backend Documentation

## 🎯 Overview
Hệ thống chatbot hybrid tích hợp Dify AI để tự động tư vấn sản phẩm, với khả năng chuyển đổi sang nhân viên khi cần.

---

## 🏗️ Architecture

```
User Message
    ↓
ChatController
    ↓
[Check Room.chatMode]
    ↓
├─ bot → ChatbotService → Dify API → Bot Response
│                                         ↓
│                                    MessageService.saveBotMessage()
│                                         ↓
│                                    WebSocket → User
│
└─ staff → Forward to Staff Dashboard
```

---

## 📁 File Structure

```
src/main/java/com/pbl6/
├── controllers/chat/
│   └── ChatController.java          # WebSocket handlers + routing logic
├── services/
│   ├── ChatbotService.java          # Interface
│   ├── MessageService.java          # Updated with saveBotMessage()
│   ├── RoomService.java             # Updated with switchChatMode()
│   └── impl/
│       ├── ChatbotServiceImpl.java  # Dify API integration
│       ├── MessageServiceImpl.java
│       └── RoomServiceImpl.java
├── entities/
│   ├── MessageEntity.java           # Unchanged (giữ nguyên)
│   └── RoomEntity.java              # + chatMode, conversationId
└── dtos/
    ├── request/
    │   └── ChatbotRequestDTO.java
    └── response/chat/
        ├── ChatbotResponseDTO.java  # Parse từ Dify
        ├── DifyApiResponse.java
        └── MessageDTO.java          # Unchanged (giữ nguyên)
```

---

## ⚙️ Configuration

### application.properties
```properties
# AI Chatbot Config
chatbot.api.url=${CHATBOT_API_URL:https://api.dify.ai/v1}
chatbot.api.key=${CHATBOT_API_KEY:app-xxxxxxxxxxxxx}
```

### Environment Variables (Production)
```bash
CHATBOT_API_URL=https://api.dify.ai/v1
CHATBOT_API_KEY=app-your-dify-api-key-here
```

---

## 🗄️ Database Changes

### Migration SQL
```sql
-- Thêm vào bảng rooms
ALTER TABLE rooms 
ADD COLUMN chat_mode VARCHAR(20) DEFAULT 'bot',
ADD COLUMN conversation_id VARCHAR(100);

UPDATE rooms SET chat_mode = 'bot' WHERE chat_mode IS NULL;
```

### RoomEntity Fields
```java
private String chatMode = "bot";        // "bot" hoặc "staff"
private String conversationId;          // Dify conversation ID
```

---

## 🔌 API Endpoints

### WebSocket Endpoints

#### 1. Send Message (Updated)
**Destination:** `/app/chat.send`

**Payload:**
```json
{
  "content": "Tư vấn iPhone giá rẻ"
}
```

**Logic:**
- Lưu tin nhắn user
- Check `room.chatMode`
- Nếu `"bot"` → Gọi Dify API → Lưu bot response
- Nếu `"staff"` → Forward cho staff dashboard

**Response to User:**
```json
{
  "id": 124,
  "senderType": "BOT",
  "content": "{\"reply_text\":\"...\",\"suggested_products\":[...]}",
  "type": "chatbot-response",
  "createdAt": "2025-12-15T10:30:00"
}
```

#### 2. Switch Chat Mode (New)
**Destination:** `/app/chat.switch_mode`

**Payload:**
```json
{
  "chatMode": "bot"  // hoặc "staff"
}
```

**Response:**
- Channel: `/user/queue/chat_mode_changed`
```json
{
  "chatMode": "bot",
  "message": "Đã chuyển sang chế độ Bot tự động"
}
```

---

## 🤖 Chatbot Integration

### ChatbotServiceImpl

#### Request to Chatbot
```java
POST {chatbot.api.url}/chat-messages
Headers:
  Authorization: Bearer {chatbot.api.key}
  Content-Type: application/json

Body:
{
  "query": "Tư vấn iPhone giá rẻ",
  "user": "USER_0123456789",
  "response_mode": "blocking",
  "conversation_id": "abc-xyz-123" // optional
}
```

#### Response from Chatbot
```json
{
  "answer": "{\"reply_text\":\"Dạ, em xin gợi ý...\",\"suggested_products\":[...]}",
  "conversation_id": "conv_123456",
  "message_id": "msg_789",
  "created_at": 1702641600
}
```

#### Parse Logic
```java
DifyApiResponse difyResponse = objectMapper.readValue(response.getBody(), DifyApiResponse.class);
String answer = difyResponse.getAnswer();

// Parse JSON trong answer
ChatbotResponseDTO chatbotResponse = objectMapper.readValue(answer, ChatbotResponseDTO.class);
```

---

## 💾 Data Flow

### 1. User gửi tin nhắn (Bot mode)
```
User → WebSocket → ChatController.userSendMessage()
  ↓
MessageService.saveUserMessage() → DB
  ↓
ChatbotService.getChatbotResponse() → Dify API
  ↓
Parse ChatbotResponseDTO
  ↓
MessageService.saveBotMessage() → DB
  ↓
WebSocket → User (chatbot-response)
```

### 2. MessageEntity Structure
```java
MessageEntity {
  senderType: "BOT"
  senderKey: "BOT"
  content: "{"reply_text":"...","suggested_products":[...]}"  // JSON string
  type: "chatbot-response"
}
```

**⚠️ Lưu ý:** `content` là JSON string, không phải object. Frontend cần parse.

### 3. Switch Mode Flow
```
User → /app/chat.switch_mode → ChatController
  ↓
RoomService.switchChatMode(userKey, "staff")
  ↓
room.setChatMode("staff") → DB
  ↓
WebSocket → /user/queue/chat_mode_changed
```

---

## 🧪 Testing

### 1. Test Chatbot Service
```java
@SpringBootTest
class ChatbotServiceTest {
    @Autowired
    private ChatbotService chatbotService;
    
    @Test
    void testGetChatbotResponse() {
        ChatbotResponseDTO response = chatbotService.getChatbotResponse(
            "Tư vấn iPhone giá rẻ",
            "USER_TEST",
            null
        );
        
        assertNotNull(response.getReplyText());
        assertNotNull(response.getSuggestedProducts());
    }
}
```

### 2. Test với Postman (WebSocket)
```javascript
// Connect
const socket = new SockJS('http://localhost:8080/ws/customer');
const stompClient = Stomp.over(socket);

// Send message
stompClient.send('/app/chat.send', {}, JSON.stringify({
  content: 'Tư vấn iPhone'
}));

// Switch mode
stompClient.send('/app/chat.switch_mode', {}, JSON.stringify({
  chatMode: 'staff'
}));
```

### 3. cURL test Dify API
```bash
curl -X POST https://api.dify.ai/v1/chat-messages \
  -H "Authorization: Bearer app-xxxxx" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "Tư vấn iPhone giá rẻ",
    "user": "test_user",
    "response_mode": "blocking"
  }'
```

---

## 🐛 Error Handling

### 1. Dify API Error
```java
try {
    ChatbotResponseDTO response = chatbotService.getChatbotResponse(...);
} catch (Exception e) {
    log.error("Chatbot error", e);
    
    // Fallback response
    ChatbotResponseDTO errorResponse = new ChatbotResponseDTO();
    errorResponse.setReplyText("Xin lỗi, bot đang bận...");
    
    MessageDTO errorMessage = messageService.saveBotMessage(roomId, errorResponse);
    // Send to user
}
```

### 2. Invalid Chat Mode
```java
if (!"bot".equalsIgnoreCase(chatMode) && !"staff".equalsIgnoreCase(chatMode)) {
    throw new IllegalArgumentException("Invalid chat mode");
}
```

### 3. Room Not Found
```java
RoomEntity room = roomRepo.findByUserKey(userKey);
if (room == null) {
    throw new RuntimeException("Room not found");
}
```

---

## 📊 Monitoring

### Logs to Monitor
```java
log.info("Bot mode - calling Dify API for user: {}", userKey);
log.info("Bot responded successfully to user: {}", userKey);
log.error("Chatbot error for user: {}", userKey, exception);
log.info("User {} switched to {} mode", userKey, chatMode);
```

### Metrics
- Dify API response time
- Bot response success rate
- Mode switch frequency
- User → Bot vs User → Staff ratio

---

## 🔧 Troubleshooting

### Bot không trả lời
1. Check Dify API key: `application.properties`
2. Check logs: `Chatbot error`
3. Test Dify API directly: `curl`
4. Verify JSON format từ Dify

### JSON Parse Error
1. Check Dify prompt: phải return đúng format JSON
2. Log raw answer: `log.info("Dify raw answer: {}", answer)`
3. Fallback to plain text nếu parse fail

### WebSocket Connection Issue
1. Check CORS config: `WebSocketConfig`
2. Check authentication: `CustomerHandshakeInterceptor`
3. Verify token valid

---

## 🚀 Deployment

### 1. Update Database
```bash
mysql -u root -p techshop < docs/migration_add_chatbot.sql
```

### 2. Build & Run
```bash
mvn clean package
java -jar target/pbl6-0.0.1-SNAPSHOT.jar
```

### 3. Verify
```bash
# Check health
curl http://localhost:8080/actuator/health

# Check WebSocket
wscat -c ws://localhost:8080/ws/customer
```

---

## 📝 Future Enhancements

1. **Conversation History với Dify**
   - Lưu `conversationId` từ Dify response
   - Pass vào request tiếp theo để maintain context

2. **Analytics Dashboard**
   - Track bot success rate
   - Monitor user satisfaction
   - A/B testing bot vs staff

3. **Smart Routing**
   - Auto switch to staff nếu bot không trả lời được
   - Intent detection để forward complex queries

4. **Multi-language Support**
   - Detect user language
   - Switch Dify prompt accordingly

---


