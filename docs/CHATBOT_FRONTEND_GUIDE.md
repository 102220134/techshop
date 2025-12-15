# Hướng dẫn tích hợp Chatbot - Frontend Guide

## 🤖 Tổng quan
Hệ thống chat đã được nâng cấp với chatbot tự động. User có thể chuyển đổi giữa 2 chế độ:
- **Bot mode** (mặc định): Chatbot tự động trả lời
- **Staff mode**: Nhân viên trả lời thủ công

---

## 📡 WebSocket Endpoints

### 1. Kết nối WebSocket
```javascript
const socket = new SockJS('/ws/customer');
const stompClient = Stomp.over(socket);

stompClient.connect({
  'Authorization': 'Bearer ' + token
}, onConnected, onError);
```

### 2. Subscribe để nhận tin nhắn
```javascript
// Nhận tin nhắn chat
stompClient.subscribe('/user/queue/chat', onMessageReceived);

// Nhận thông báo chuyển mode
stompClient.subscribe('/user/queue/chat_mode_changed', onModeChanged);

// Nhận lỗi
stompClient.subscribe('/user/queue/chat_error', onError);
```

---

## 💬 Gửi tin nhắn

### Gửi tin nhắn thường
```javascript
stompClient.send('/app/chat.send', {}, JSON.stringify({
  content: 'Tư vấn iPhone giá rẻ'
}));
```

---

## 🔄 Chuyển đổi chế độ chat

### Switch sang Bot mode
```javascript
stompClient.send('/app/chat.switch_mode', {}, JSON.stringify({
  chatMode: 'bot'
}));
```

### Switch sang Staff mode
```javascript
stompClient.send('/app/chat.switch_mode', {}, JSON.stringify({
  chatMode: 'staff'
}));
```

### Nhận kết quả chuyển đổi
```javascript
function onModeChanged(payload) {
  const data = JSON.parse(payload.body);
  console.log(data.message); // "Đã chuyển sang chế độ Bot tự động"
  console.log(data.chatMode); // "bot" hoặc "staff"
  
  // Update UI
  updateChatModeButton(data.chatMode);
}
```

---

## 📦 Format tin nhắn

### Tin nhắn thường (USER/SYSTEM)
```json
{
  "id": 123,
  "room": {...},
  "senderType": "USER",
  "senderKey": "USER_0123456789",
  "content": "Tư vấn iPhone giá rẻ",
  "type": "text",
  "createdAt": "2025-12-15T10:30:00"
}
```

### Tin nhắn từ Bot (senderType = "BOT")
```json
{
  "id": 124,
  "room": {...},
  "senderType": "BOT",
  "senderKey": "BOT",
  "content": "{\"reply_text\":\"Dạ, em xin gợi ý...\",\"suggested_products\":[...]}",
  "type": "chatbot-response",
  "createdAt": "2025-12-15T10:30:05"
}
```

**⚠️ Quan trọng**: Khi `type = "chatbot-response"`, field `content` là **JSON string**, cần parse trước khi dùng:

```javascript
function onMessageReceived(payload) {
  const message = JSON.parse(payload.body);
  
  if (message.type === 'chatbot-response') {
    // Parse content thành object
    const botResponse = JSON.parse(message.content);
    
    // Hiển thị text
    displayText(botResponse.reply_text);
    
    // Hiển thị sản phẩm gợi ý
    if (botResponse.suggested_products && botResponse.suggested_products.length > 0) {
      displayProductCards(botResponse.suggested_products);
    }
  } else {
    // Tin nhắn thường
    displayText(message.content);
  }
}
```

---

## 🎨 UI Components

### 1. Chat Mode Switcher (Toggle Button)
```html
<div class="chat-mode-switcher">
  <button id="botModeBtn" class="active">
    🤖 Bot
  </button>
  <button id="staffModeBtn">
    👤 Nhân viên
  </button>
</div>
```

```javascript
document.getElementById('botModeBtn').onclick = () => {
  stompClient.send('/app/chat.switch_mode', {}, JSON.stringify({
    chatMode: 'bot'
  }));
};

document.getElementById('staffModeBtn').onclick = () => {
  stompClient.send('/app/chat.switch_mode', {}, JSON.stringify({
    chatMode: 'staff'
  }));
};
```

### 2. Hiển thị sản phẩm gợi ý
```javascript
function displayProductCards(products) {
  const container = document.getElementById('product-suggestions');
  container.innerHTML = '';
  
  products.forEach(product => {
    const card = `
      <div class="product-card">
        <img src="${product.image_url}" alt="${product.name}">
        <h4>${product.name}</h4>
        <p class="price">${formatPrice(product.price)} đ</p>
        <p class="desc">${product.short_desc}</p>
        <ul class="highlights">
          ${product.highlights.map(h => `<li>${h}</li>`).join('')}
        </ul>
        <a href="${product.link}" class="btn-view">Xem chi tiết</a>
      </div>
    `;
    container.innerHTML += card;
  });
}

function formatPrice(price) {
  return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
}
```

---

## 📋 Complete Example

```javascript
let stompClient = null;
let currentChatMode = 'bot';

function connect() {
  const socket = new SockJS('/ws/customer');
  stompClient = Stomp.over(socket);
  
  stompClient.connect({
    'Authorization': 'Bearer ' + getToken()
  }, () => {
    console.log('Connected');
    
    // Subscribe
    stompClient.subscribe('/user/queue/chat', onMessageReceived);
    stompClient.subscribe('/user/queue/chat_mode_changed', onModeChanged);
    stompClient.subscribe('/user/queue/chat_error', onError);
    
    // Load history
    stompClient.send('/app/chat.load_history', {}, '{}');
  });
}

function onMessageReceived(payload) {
  const message = JSON.parse(payload.body);
  
  if (message.type === 'chatbot-response') {
    const botResponse = JSON.parse(message.content);
    
    // Hiển thị text
    appendMessage('bot', botResponse.reply_text, message.createdAt);
    
    // Hiển thị sản phẩm
    if (botResponse.suggested_products?.length > 0) {
      displayProductCards(botResponse.suggested_products);
    }
  } else {
    appendMessage(
      message.senderType.toLowerCase(), 
      message.content, 
      message.createdAt
    );
  }
}

function onModeChanged(payload) {
  const data = JSON.parse(payload.body);
  currentChatMode = data.chatMode;
  
  // Update UI
  document.getElementById('botModeBtn').classList.toggle('active', data.chatMode === 'bot');
  document.getElementById('staffModeBtn').classList.toggle('active', data.chatMode === 'staff');
  
  // Show notification
  showNotification(data.message);
}

function sendMessage() {
  const input = document.getElementById('messageInput');
  const content = input.value.trim();
  
  if (content) {
    stompClient.send('/app/chat.send', {}, JSON.stringify({
      content: content
    }));
    input.value = '';
  }
}

function switchMode(mode) {
  stompClient.send('/app/chat.switch_mode', {}, JSON.stringify({
    chatMode: mode
  }));
}

// Initialize
connect();
```

---

## 🎯 Best Practices

1. **Always check message type**
   ```javascript
   if (message.type === 'chatbot-response') {
     const data = JSON.parse(message.content);
     // Handle bot response
   }
   ```

2. **Handle errors gracefully**
   ```javascript
   stompClient.subscribe('/user/queue/chat_error', (payload) => {
     const error = JSON.parse(payload.body);
     showErrorToast(error.error);
   });
   ```

3. **Show loading state**
   ```javascript
   function sendMessage() {
     showTypingIndicator();
     stompClient.send('/app/chat.send', {}, ...);
   }
   ```

4. **Cache chat mode**
   ```javascript
   localStorage.setItem('preferredChatMode', currentChatMode);
   ```

---

## 🐛 Troubleshooting

### Bot không trả lời?
- Check console logs
- Verify Dify API config trong `application.properties`
- Check network tab for WebSocket connection

### Sản phẩm không hiển thị?
- Verify JSON parse: `JSON.parse(message.content)`
- Check `suggested_products` array exists
- Validate product links

### Switch mode không hoạt động?
- Check payload format: `{ "chatMode": "bot" }`
- Subscribe đúng channel: `/user/queue/chat_mode_changed`
- Check user authentication

---

## 📞 Support
Liên hệ team Backend nếu có vấn đề về API hoặc WebSocket connection.
