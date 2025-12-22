# 🤖 Hướng dẫn tích hợp Chatbot - Frontend Guide

## 📋 Mục lục
1. [Tổng quan](#tổng-quan)
2. [WebSocket Setup](#websocket-setup)
3. [Gửi & Nhận tin nhắn](#gửi--nhận-tin-nhắn)
4. [Chuyển đổi chế độ Bot/Staff](#chuyển-đổi-chế-độ)
5. [Format dữ liệu](#format-dữ-liệu)
6. [UI Components](#ui-components)
7. [Code ví dụ hoàn chỉnh](#code-ví-dụ-hoàn-chỉnh)
8. [Troubleshooting](#troubleshooting)

---

## 🎯 Tổng quan

### Chatbot Hybrid System
Hệ thống chat đã được nâng cấp với AI chatbot tự động. Khách hàng có thể chuyển đổi linh hoạt giữa 2 chế độ:

| Chế độ | Mô tả | Khi nào dùng |
|--------|-------|--------------|
| **🤖 Bot mode** | AI tự động phân tích & gợi ý sản phẩm | Tư vấn nhanh, gợi ý sản phẩm cơ bản |
| **👤 Staff mode** | Nhân viên trả lời trực tiếp | Yêu cầu phức tạp, tư vấn chuyên sâu |

### Luồng hoạt động:
```
User gửi tin nhắn → Backend check chatMode
                          ↓
        ┌─────────────────┴─────────────────┐
        ↓                                   ↓
    Bot Mode                           Staff Mode
    (Chatbot API)                      (Nhân viên)
        ↓                                   ↓
    Trả lời tự động +                  Trả lời thủ công
    Gợi ý sản phẩm
        ↓                                   ↓
    ←───────────── WebSocket ─────────────→
                    User nhận tin nhắn
```

---

## 📡 WebSocket Setup

### Bước 1: Kết nối WebSocket
```javascript
// Import thư viện (CDN hoặc npm)
// <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
// <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>

let stompClient = null;

function connectWebSocket() {
  // Tạo kết nối SockJS
  const socket = new SockJS('http://localhost:8080/ws/customer');
  
  // Tạo STOMP client
  stompClient = Stomp.over(socket);
  
  // Tắt debug log (optional)
  stompClient.debug = null;
  
  // Kết nối với authentication
  const headers = {
    'Authorization': 'Bearer ' + localStorage.getItem('token')
  };
  
  stompClient.connect(headers, onConnected, onConnectionError);
}

function onConnected() {
  console.log('✅ WebSocket connected successfully');
  
  // Subscribe các channel để nhận tin nhắn
  subscribeToChannels();
}

function onConnectionError(error) {
  console.error('❌ WebSocket connection error:', error);
  // Retry sau 3s
  setTimeout(connectWebSocket, 3000);
}
```

### Bước 2: Subscribe các channel
```javascript
function subscribeToChannels() {
  // Channel 1: Nhận tin nhắn chat (quan trọng nhất)
  stompClient.subscribe('/user/queue/chat', function(message) {
    const data = JSON.parse(message.body);
    handleIncomingMessage(data);
  });
  
  // Channel 2: Nhận thông báo thay đổi mode
  stompClient.subscribe('/user/queue/chat_mode_changed', function(notification) {
    const data = JSON.parse(notification.body);
    handleModeChanged(data);
  });
  
  // Channel 3: Nhận thông báo lỗi
  stompClient.subscribe('/user/queue/chat_error', function(errorMsg) {
    const data = JSON.parse(errorMsg.body);
    showErrorNotification(data.error);
  });
  
  // Sau khi subscribe xong, load lịch sử chat
  loadChatHistory();
}
```

---

## 💬 Gửi & Nhận tin nhắn

### Gửi tin nhắn từ User
```javascript
function sendMessage(messageText) {
  if (!messageText || !messageText.trim()) {
    return; // Không gửi tin nhắn rỗng
  }
  
  if (!stompClient || !stompClient.connected) {
    console.error('WebSocket chưa kết nối');
    return;
  }
  
  // Hiển thị tin nhắn ngay lập tức (optimistic UI)
  displayUserMessage(messageText);
  
  // Gửi qua WebSocket
  stompClient.send('/app/chat.send', {}, JSON.stringify({
    content: messageText
  }));
  
  // Clear input
  document.getElementById('messageInput').value = '';
  
  // Show typing indicator (nếu ở bot mode)
  if (currentChatMode === 'bot') {
    showTypingIndicator();
  }
}

// Ví dụ gửi tin nhắn khi nhấn Enter
document.getElementById('messageInput').addEventListener('keypress', function(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault();
    sendMessage(this.value);
  }
});

// Hoặc khi click nút Gửi
document.getElementById('sendBtn').addEventListener('click', function() {
  const input = document.getElementById('messageInput');
  sendMessage(input.value);
});
```

### Nhận tin nhắn từ Backend
```javascript
function handleIncomingMessage(message) {
  console.log('📨 Received message:', message);
  
  // Ẩn typing indicator
  hideTypingIndicator();
  
  /*
   * message có cấu trúc:
   * {
   *   id: 124,
   *   senderType: "BOT" | "USER" | "STAFF",
   *   senderKey: "BOT" | "USER_xxx" | "STAFF_xxx",
   *   content: "...",  // Có thể là text hoặc JSON string
   *   type: "text" | "chatbot-response",
   *   createdAt: "2025-12-18T10:30:00"
   * }
   */
  
  // Kiểm tra loại tin nhắn
  if (message.type === 'chatbot-response') {
    // Tin nhắn từ Bot - content là JSON string
    handleBotResponse(message);
  } else if (message.senderType === 'STAFF') {
    // Tin nhắn từ Nhân viên
    displayStaffMessage(message.content, message.createdAt);
  } else if (message.senderType === 'USER') {
    // Tin nhắn từ chính mình (đã gửi từ thiết bị khác)
    displayUserMessage(message.content, message.createdAt);
  } else {
    // Tin nhắn hệ thống
    displaySystemMessage(message.content, message.createdAt);
  }
}

function handleBotResponse(message) {
  try {
    // ⚠️ QUAN TRỌNG: content là JSON string, cần parse
    const botData = JSON.parse(message.content);
    
    /*
     * botData có cấu trúc:
     * {
     *   reply_text: "Dạ, em xin gợi ý một số mẫu iPhone...",
     *   suggested_products: [
     *     {
     *       name: "iPhone 15 Pro Max",
     *       price: 29990000,
     *       image_url: "https://...",
     *       short_desc: "Chip A17 Pro, Camera 48MP",
     *       highlights: ["Màn hình 6.7 inch", "RAM 8GB", "Pin 4422mAh"],
     *       link: "/products/iphone-15-pro-max"
     *     }
     *   ]
     * }
     */
    
    // 1. Hiển thị text trả lời
    displayBotMessage(botData.reply_text, message.createdAt);
    
    // 2. Hiển thị sản phẩm gợi ý (nếu có)
    if (botData.suggested_products && botData.suggested_products.length > 0) {
      displayProductSuggestions(botData.suggested_products);
    }
    
  } catch (error) {
    console.error('❌ Error parsing bot response:', error);
    // Fallback: hiển thị raw content
    displayBotMessage(message.content, message.createdAt);
  }
}
```

---

## 🔄 Chuyển đổi chế độ

### Gửi yêu cầu chuyển mode
```javascript
let currentChatMode = 'bot'; // Track mode hiện tại

function switchToBotMode() {
  stompClient.send('/app/chat.switch_mode', {}, JSON.stringify({
    chatMode: 'bot'
  }));
  
  // UI feedback ngay lập tức
  showLoadingMode();
}

function switchToStaffMode() {
  stompClient.send('/app/chat.switch_mode', {}, JSON.stringify({
    chatMode: 'staff'
  }));
  
  showLoadingMode();
}

// Event handlers cho toggle buttons
document.getElementById('botModeBtn').addEventListener('click', switchToBotMode);
document.getElementById('staffModeBtn').addEventListener('click', switchToStaffMode);
```

### Nhận kết quả chuyển mode
```javascript
function handleModeChanged(data) {
  console.log('🔄 Chat mode changed:', data);
  
  /*
   * data có cấu trúc:
   * {
   *   chatMode: "bot" | "staff",
   *   message: "Đã chuyển sang chế độ Bot tự động"
   * }
   */
  
  // 1. Cập nhật biến tracking
  currentChatMode = data.chatMode;
  
  // 2. Cập nhật UI buttons
  updateModeButtons(data.chatMode);
  
  // 3. Hiển thị thông báo cho user
  showSuccessNotification(data.message);
  
  // 4. Lưu preference (optional)
  localStorage.setItem('preferredChatMode', data.chatMode);
  
  // 5. Cập nhật giao diện chat
  if (data.chatMode === 'staff') {
    showStaffModeIndicator(); // Hiển thị "Đang kết nối với nhân viên..."
  } else {
    showBotModeIndicator(); // Hiển thị "Bot đang sẵn sàng hỗ trợ"
  }
}

function updateModeButtons(mode) {
  const botBtn = document.getElementById('botModeBtn');
  const staffBtn = document.getElementById('staffModeBtn');
  
  if (mode === 'bot') {
    botBtn.classList.add('active');
    staffBtn.classList.remove('active');
  } else {
    staffBtn.classList.add('active');
    botBtn.classList.remove('active');
  }
}
```

---

## 📦 Format dữ liệu

### 1. Tin nhắn thường (USER/STAFF/SYSTEM)
```json
{
  "id": 123,
  "room": {
    "id": 1,
    "chatMode": "bot",
    "conversationId": "conv_abc123"
  },
  "senderType": "USER",
  "senderKey": "USER_0123456789",
  "content": "Tư vấn iPhone giá rẻ",
  "type": "text",
  "createdAt": "2025-12-18T10:30:00"
}
```

**Fields giải thích:**
- `id`: ID của tin nhắn trong database
- `senderType`: Loại người gửi - `"USER"` | `"BOT"` | `"STAFF"` | `"SYSTEM"`
- `senderKey`: Mã định danh người gửi
- `content`: Nội dung tin nhắn (text hoặc JSON string)
- `type`: Loại tin nhắn - `"text"` | `"chatbot-response"` | `"image"` | `"file"`
- `createdAt`: Thời gian tạo (ISO 8601 format)

### 2. Tin nhắn từ Bot (⚠️ Đặc biệt)
```json
{
  "id": 124,
  "room": {...},
  "senderType": "BOT",
  "senderKey": "BOT",
  "content": "{\"reply_text\":\"Dạ, em xin gợi ý...\",\"suggested_products\":[...]}",
  "type": "chatbot-response",
  "createdAt": "2025-12-18T10:30:05"
}
```

**⚠️ QUAN TRỌNG - ĐỌC KỸ:**
- Khi `type === "chatbot-response"`, field `content` là **JSON STRING** (không phải object)
- Bạn **BẮT BUỘC** phải `JSON.parse()` trước khi sử dụng
- Nếu không parse sẽ không lấy được sản phẩm gợi ý

### 3. Cấu trúc content của Bot (sau khi parse)
```javascript
const botData = JSON.parse(message.content);

// botData có cấu trúc:
{
  "reply_text": "Dạ, em xin gợi ý một số mẫu iPhone phù hợp với nhu cầu của anh/chị...",
  "suggested_products": [
    {
      "name": "iPhone 15 Pro Max 256GB",
      "price": 29990000,
      "image_url": "https://cdn.techshop.vn/iphone-15-pro-max.jpg",
      "short_desc": "Chip A17 Pro, Camera 48MP, Titan Design",
      "highlights": [
        "Màn hình Super Retina XDR 6.7 inch",
        "RAM 8GB, Chip A17 Pro 3nm",
        "Camera chính 48MP + Telephoto 12MP",
        "Pin 4422mAh, sạc nhanh 27W"
      ],
      "link": "/products/iphone-15-pro-max-256gb"
    },
    {
      "name": "iPhone 14 Pro 128GB",
      "price": 22990000,
      "image_url": "https://cdn.techshop.vn/iphone-14-pro.jpg",
      "short_desc": "Dynamic Island, Camera 48MP",
      "highlights": [
        "Màn hình OLED 6.1 inch 120Hz",
        "Chip A16 Bionic",
        "Camera 48MP ProRAW",
        "Pin 3200mAh"
      ],
      "link": "/products/iphone-14-pro-128gb"
    }
  ]
}
```

**Fields trong suggested_products:**
- `name`: Tên sản phẩm
- `price`: Giá (VNĐ, kiểu number)
- `image_url`: Link ảnh sản phẩm
- `short_desc`: Mô tả ngắn (1-2 dòng)
- `highlights`: Mảng các điểm nổi bật (specs)
- `link`: Đường dẫn đến trang chi tiết sản phẩm

### 4. Code xử lý tin nhắn Bot (Chi tiết)
```javascript
function handleBotResponse(message) {
  try {
    // Bước 1: Parse JSON string → object
    const botData = JSON.parse(message.content);
    
    // Bước 2: Kiểm tra dữ liệu hợp lệ
    if (!botData.reply_text) {
      throw new Error('Missing reply_text');
    }
    
    // Bước 3: Hiển thị text trả lời của bot
    const botMessageHTML = `
      <div class="message bot-message">
        <div class="avatar">
          <img src="/assets/bot-avatar.png" alt="Bot">
        </div>
        <div class="message-content">
          <div class="message-text">${escapeHtml(botData.reply_text)}</div>
          <div class="message-time">${formatTime(message.createdAt)}</div>
        </div>
      </div>
    `;
    document.getElementById('chatMessages').insertAdjacentHTML('beforeend', botMessageHTML);
    
    // Bước 4: Hiển thị sản phẩm gợi ý (nếu có)
    if (botData.suggested_products && Array.isArray(botData.suggested_products)) {
      if (botData.suggested_products.length > 0) {
        displayProductSuggestions(botData.suggested_products);
      }
    }
    
    // Bước 5: Scroll xuống cuối
    scrollToBottom();
    
  } catch (error) {
    console.error('❌ Lỗi parse bot response:', error);
    console.error('Raw content:', message.content);
    
    // Fallback: Hiển thị raw text
    displayBotMessage('Xin lỗi, bot gặp lỗi khi xử lý. Vui lòng thử lại.', message.createdAt);
  }
}

// Hàm escape HTML để tránh XSS
function escapeHtml(text) {
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}

// Format thời gian
function formatTime(isoString) {
  const date = new Date(isoString);
  return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
}
```

### 5. Thông báo chuyển mode
```json
{
  "chatMode": "staff",
  "message": "Đã chuyển sang chế độ Nhân viên tư vấn"
}
```

### 6. Thông báo lỗi
```json
{
  "error": "Room not found",
  "timestamp": "2025-12-18T10:30:00"
}
```

---

## 🎨 UI Components

### 1. Chat Mode Switcher (Toggle Button)
```html
<div class="chat-mode-switcher">
  <button id="botModeBtn" class="mode-btn active" title="Chuyển sang Bot tự động">
    <i class="icon-robot"></i>
    <span>🤖 Bot</span>
  </button>
  <button id="staffModeBtn" class="mode-btn" title="Chuyển sang Nhân viên">
    <i class="icon-user"></i>
    <span>👤 Nhân viên</span>
  </button>
</div>

<style>
.chat-mode-switcher {
  display: flex;
  gap: 8px;
  padding: 12px;
  background: #f5f5f5;
  border-bottom: 1px solid #e0e0e0;
}

.mode-btn {
  flex: 1;
  padding: 10px 16px;
  border: 2px solid #ddd;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  font-weight: 500;
}

.mode-btn:hover {
  border-color: #007bff;
  background: #f0f7ff;
}

.mode-btn.active {
  border-color: #007bff;
  background: #007bff;
  color: white;
}

.mode-btn.loading {
  opacity: 0.6;
  cursor: wait;
}
</style>

<script>
// Event handlers
document.getElementById('botModeBtn').addEventListener('click', function() {
  if (!this.classList.contains('active')) {
    switchToBotMode();
  }
});

document.getElementById('staffModeBtn').addEventListener('click', function() {
  if (!this.classList.contains('active')) {
    switchToStaffMode();
  }
});
</script>
```

### 2. Product Suggestion Cards (Chi tiết)
```javascript
function displayProductSuggestions(products) {
  // Tạo container cho sản phẩm
  const productsHTML = `
    <div class="products-suggestion-wrapper">
      <div class="products-header">
        <span class="icon">💡</span>
        <span class="text">Sản phẩm gợi ý cho bạn</span>
      </div>
      <div class="products-grid" id="productsGrid"></div>
    </div>
  `;
  
  document.getElementById('chatMessages').insertAdjacentHTML('beforeend', productsHTML);
  
  // Render từng sản phẩm
  const grid = document.getElementById('productsGrid');
  products.forEach(product => {
    const card = createProductCard(product);
    grid.appendChild(card);
  });
  
  scrollToBottom();
}

function createProductCard(product) {
  const card = document.createElement('div');
  card.className = 'product-card';
  
  card.innerHTML = `
    <div class="product-image">
      <img src="${product.image_url}" 
           alt="${product.name}"
           onerror="this.src='/assets/no-image.png'">
      ${product.discount ? `<span class="discount-badge">-${product.discount}%</span>` : ''}
    </div>
    
    <div class="product-info">
      <h4 class="product-name" title="${product.name}">
        ${product.name}
      </h4>
      
      <div class="product-price">
        <span class="current-price">${formatPrice(product.price)}đ</span>
        ${product.original_price ? 
          `<span class="original-price">${formatPrice(product.original_price)}đ</span>` 
          : ''}
      </div>
      
      <p class="product-desc">${product.short_desc}</p>
      
      ${product.highlights && product.highlights.length > 0 ? `
        <ul class="product-highlights">
          ${product.highlights.slice(0, 3).map(h => `<li>✓ ${h}</li>`).join('')}
        </ul>
      ` : ''}
      
      <div class="product-actions">
        <a href="${product.link}" 
           class="btn-view" 
           target="_blank"
           onclick="trackProductClick('${product.name}')">
          Xem chi tiết
        </a>
        <button class="btn-quick-buy" 
                onclick="quickBuy('${product.link}')">
          Mua ngay
        </button>
      </div>
    </div>
  `;
  
  return card;
}

// Format giá VNĐ
function formatPrice(price) {
  return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
}

// Track click (optional - for analytics)
function trackProductClick(productName) {
  console.log('User clicked product:', productName);
  // TODO: Send to analytics
}

// Quick buy (optional)
function quickBuy(productLink) {
  window.open(productLink, '_blank');
  // TODO: Add to cart directly
}
```

### 3. Product Card CSS
```css
.products-suggestion-wrapper {
  margin: 16px 0;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
}

.products-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  color: white;
  font-weight: 600;
  font-size: 16px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.product-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.15);
}

.product-image {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: #f8f8f8;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.discount-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #ff4444;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.product-info {
  padding: 16px;
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: 44px;
}

.product-price {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.current-price {
  font-size: 20px;
  font-weight: 700;
  color: #ff4444;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

.product-desc {
  font-size: 14px;
  color: #666;
  margin: 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-highlights {
  list-style: none;
  padding: 0;
  margin: 12px 0;
}

.product-highlights li {
  font-size: 13px;
  color: #555;
  padding: 4px 0;
  border-bottom: 1px dashed #eee;
}

.product-highlights li:last-child {
  border-bottom: none;
}

.product-actions {
  display: flex;
  gap: 8px;
  margin-top: 16px;
}

.btn-view {
  flex: 1;
  padding: 10px 16px;
  border: 2px solid #007bff;
  background: white;
  color: #007bff;
  text-align: center;
  text-decoration: none;
  border-radius: 6px;
  font-weight: 600;
  transition: all 0.3s;
}

.btn-view:hover {
  background: #007bff;
  color: white;
}

.btn-quick-buy {
  flex: 1;
  padding: 10px 16px;
  border: none;
  background: #28a745;
  color: white;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-quick-buy:hover {
  background: #218838;
}

/* Responsive */
@media (max-width: 768px) {
  .products-grid {
    grid-template-columns: 1fr;
  }
}
```

### 4. Typing Indicator (Bot đang trả lời)
```html
<div class="typing-indicator" id="typingIndicator" style="display: none;">
  <div class="avatar">
    <img src="/assets/bot-avatar.png" alt="Bot">
  </div>
  <div class="typing-dots">
    <span></span>
    <span></span>
    <span></span>
  </div>
</div>

<style>
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  margin: 8px 0;
}

.typing-indicator .avatar img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
}

.typing-dots {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
  background: #e9ecef;
  border-radius: 18px;
}

.typing-dots span {
  width: 8px;
  height: 8px;
  background: #666;
  border-radius: 50%;
  animation: typing 1.4s infinite;
}

.typing-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    opacity: 0.3;
    transform: scale(0.8);
  }
  30% {
    opacity: 1;
    transform: scale(1);
  }
}
</style>

<script>
function showTypingIndicator() {
  document.getElementById('typingIndicator').style.display = 'flex';
  scrollToBottom();
}

function hideTypingIndicator() {
  document.getElementById('typingIndicator').style.display = 'none';
}
</script>
```

### 5. Mode Indicator (Hiển thị chế độ hiện tại)
```html
<div class="chat-mode-indicator" id="modeIndicator">
  <span class="icon">🤖</span>
  <span class="text">Bot đang sẵn sàng hỗ trợ bạn</span>
</div>

<style>
.chat-mode-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #e7f3ff;
  border-left: 4px solid #007bff;
  font-size: 13px;
  color: #0056b3;
}

.chat-mode-indicator.staff-mode {
  background: #fff3e0;
  border-left-color: #ff9800;
  color: #e65100;
}
</style>

<script>
function showBotModeIndicator() {
  const indicator = document.getElementById('modeIndicator');
  indicator.className = 'chat-mode-indicator';
  indicator.innerHTML = `
    <span class="icon">🤖</span>
    <span class="text">Bot đang sẵn sàng hỗ trợ bạn</span>
  `;
}

function showStaffModeIndicator() {
  const indicator = document.getElementById('modeIndicator');
  indicator.className = 'chat-mode-indicator staff-mode';
  indicator.innerHTML = `
    <span class="icon">👤</span>
    <span class="text">Nhân viên sẽ trả lời trong giây lát...</span>
  `;
}
</script>
```

---

## 📋 Code ví dụ hoàn chỉnh

### File: chat.js (Complete Implementation)
```javascript
/**
 * TechShop - Chatbot Integration
 * Frontend WebSocket Chat Client
 */

// ==================== CONFIGURATION ====================
const CONFIG = {
  WS_ENDPOINT: 'http://localhost:8080/ws/customer',
  TOKEN_KEY: 'auth_token',
  CHAT_MODE_KEY: 'preferred_chat_mode'
};

// ==================== STATE MANAGEMENT ====================
let stompClient = null;
let currentChatMode = 'bot';
let isConnected = false;

// ==================== WEBSOCKET CONNECTION ====================

/**
 * Khởi tạo kết nối WebSocket
 */
function initWebSocket() {
  console.log('🔌 Đang kết nối WebSocket...');
  
  const socket = new SockJS(CONFIG.WS_ENDPOINT);
  stompClient = Stomp.over(socket);
  
  // Tắt debug log (bật lại nếu cần debug)
  stompClient.debug = null;
  
  const token = localStorage.getItem(CONFIG.TOKEN_KEY);
  if (!token) {
    console.error('❌ Token không tồn tại');
    showError('Vui lòng đăng nhập');
    return;
  }
  
  const headers = {
    'Authorization': 'Bearer ' + token
  };
  
  stompClient.connect(headers, onWebSocketConnected, onWebSocketError);
}

/**
 * Callback khi kết nối thành công
 */
function onWebSocketConnected() {
  console.log('✅ WebSocket connected');
  isConnected = true;
  
  // Subscribe các channel
  stompClient.subscribe('/user/queue/chat', handleChatMessage);
  stompClient.subscribe('/user/queue/chat_mode_changed', handleModeChanged);
  stompClient.subscribe('/user/queue/chat_error', handleChatError);
  
  // Load lịch sử chat
  loadChatHistory();
  
  // Restore chat mode preference
  const savedMode = localStorage.getItem(CONFIG.CHAT_MODE_KEY);
  if (savedMode && savedMode !== currentChatMode) {
    switchChatMode(savedMode);
  } else {
    updateModeUI(currentChatMode);
  }
  
  showSuccess('Kết nối chat thành công');
}

/**
 * Callback khi kết nối lỗi
 */
function onWebSocketError(error) {
  console.error('❌ WebSocket error:', error);
  isConnected = false;
  showError('Lỗi kết nối. Đang thử lại...');
  
  // Retry sau 3s
  setTimeout(initWebSocket, 3000);
}

/**
 * Load lịch sử chat
 */
function loadChatHistory() {
  stompClient.send('/app/chat.load_history', {}, JSON.stringify({}));
}

// ==================== MESSAGE HANDLERS ====================

/**
 * Xử lý tin nhắn chat nhận được
 */
function handleChatMessage(payload) {
  const message = JSON.parse(payload.body);
  console.log('📨 Received message:', message);
  
  hideTypingIndicator();
  
  switch (message.senderType) {
    case 'BOT':
      if (message.type === 'chatbot-response') {
        handleBotResponse(message);
      } else {
        displayBotMessage(message.content, message.createdAt);
      }
      break;
      
    case 'STAFF':
      displayStaffMessage(message.content, message.createdAt);
      break;
      
    case 'USER':
      // Tin nhắn từ chính user (có thể từ device khác)
      displayUserMessage(message.content, message.createdAt, true);
      break;
      
    case 'SYSTEM':
      displaySystemMessage(message.content, message.createdAt);
      break;
      
    default:
      console.warn('Unknown sender type:', message.senderType);
  }
  
  scrollToBottom();
}

/**
 * Xử lý phản hồi từ chatbot
 */
function handleBotResponse(message) {
  try {
    // Parse JSON string content
    const botData = JSON.parse(message.content);
    
    if (!botData.reply_text) {
      throw new Error('Missing reply_text in bot response');
    }
    
    // 1. Hiển thị text
    displayBotMessage(botData.reply_text, message.createdAt);
    
    // 2. Hiển thị sản phẩm gợi ý
    if (botData.suggested_products && Array.isArray(botData.suggested_products)) {
      if (botData.suggested_products.length > 0) {
        displayProductSuggestions(botData.suggested_products);
      }
    }
    
  } catch (error) {
    console.error('❌ Error parsing bot response:', error);
    console.error('Raw content:', message.content);
    displayBotMessage('Xin lỗi, bot gặp sự cố. Vui lòng thử lại.', message.createdAt);
  }
}

/**
 * Xử lý thông báo thay đổi mode
 */
function handleModeChanged(payload) {
  const data = JSON.parse(payload.body);
  console.log('🔄 Mode changed:', data);
  
  currentChatMode = data.chatMode;
  localStorage.setItem(CONFIG.CHAT_MODE_KEY, data.chatMode);
  
  updateModeUI(data.chatMode);
  displaySystemMessage(data.message, new Date().toISOString());
  showSuccess(data.message);
}

/**
 * Xử lý lỗi chat
 */
function handleChatError(payload) {
  const data = JSON.parse(payload.body);
  console.error('❌ Chat error:', data);
  showError(data.error || 'Có lỗi xảy ra');
}

// ==================== SEND MESSAGES ====================

/**
 * Gửi tin nhắn
 */
function sendMessage(content) {
  if (!content || !content.trim()) {
    return;
  }
  
  if (!isConnected || !stompClient) {
    showError('Chưa kết nối đến server');
    return;
  }
  
  const messageText = content.trim();
  
  // Hiển thị tin nhắn ngay (optimistic UI)
  displayUserMessage(messageText, new Date().toISOString(), false);
  
  // Gửi qua WebSocket
  stompClient.send('/app/chat.send', {}, JSON.stringify({
    content: messageText
  }));
  
  // Clear input
  document.getElementById('messageInput').value = '';
  
  // Show typing nếu đang ở bot mode
  if (currentChatMode === 'bot') {
    showTypingIndicator();
  }
}

/**
 * Chuyển đổi chế độ chat
 */
function switchChatMode(mode) {
  if (!isConnected || !stompClient) {
    showError('Chưa kết nối đến server');
    return;
  }
  
  if (mode === currentChatMode) {
    return;
  }
  
  console.log('🔄 Switching to mode:', mode);
  
  stompClient.send('/app/chat.switch_mode', {}, JSON.stringify({
    chatMode: mode
  }));
  
  // Show loading state
  setModeLoading(true);
}

// ==================== UI UPDATES ====================

/**
 * Hiển thị tin nhắn từ user
 */
function displayUserMessage(content, timestamp, isEcho = false) {
  const time = formatTime(timestamp);
  const messageHTML = `
    <div class="message user-message ${isEcho ? 'echo' : ''}">
      <div class="message-content">
        <div class="message-text">${escapeHtml(content)}</div>
        <div class="message-time">${time}</div>
      </div>
      <div class="avatar">
        <img src="/assets/user-avatar.png" alt="You">
      </div>
    </div>
  `;
  
  appendMessage(messageHTML);
}

/**
 * Hiển thị tin nhắn từ bot
 */
function displayBotMessage(content, timestamp) {
  const time = formatTime(timestamp);
  const messageHTML = `
    <div class="message bot-message">
      <div class="avatar">
        <img src="/assets/bot-avatar.png" alt="Bot">
      </div>
      <div class="message-content">
        <div class="message-text">${escapeHtml(content)}</div>
        <div class="message-time">${time}</div>
      </div>
    </div>
  `;
  
  appendMessage(messageHTML);
}

/**
 * Hiển thị tin nhắn từ nhân viên
 */
function displayStaffMessage(content, timestamp) {
  const time = formatTime(timestamp);
  const messageHTML = `
    <div class="message staff-message">
      <div class="avatar">
        <img src="/assets/staff-avatar.png" alt="Staff">
      </div>
      <div class="message-content">
        <div class="message-badge">Nhân viên tư vấn</div>
        <div class="message-text">${escapeHtml(content)}</div>
        <div class="message-time">${time}</div>
      </div>
    </div>
  `;
  
  appendMessage(messageHTML);
}

/**
 * Hiển thị tin nhắn hệ thống
 */
function displaySystemMessage(content, timestamp) {
  const time = formatTime(timestamp);
  const messageHTML = `
    <div class="message system-message">
      <div class="system-badge">
        <span class="icon">ℹ️</span>
        <span class="text">${escapeHtml(content)}</span>
        <span class="time">${time}</span>
      </div>
    </div>
  `;
  
  appendMessage(messageHTML);
}

/**
 * Hiển thị sản phẩm gợi ý
 */
function displayProductSuggestions(products) {
  const container = document.createElement('div');
  container.className = 'products-suggestion-wrapper';
  
  container.innerHTML = `
    <div class="products-header">
      <span class="icon">💡</span>
      <span class="text">Sản phẩm gợi ý cho bạn</span>
    </div>
    <div class="products-grid"></div>
  `;
  
  const grid = container.querySelector('.products-grid');
  
  products.forEach(product => {
    const card = createProductCard(product);
    grid.appendChild(card);
  });
  
  document.getElementById('chatMessages').appendChild(container);
  scrollToBottom();
}

/**
 * Tạo product card element
 */
function createProductCard(product) {
  const card = document.createElement('div');
  card.className = 'product-card';
  
  const highlightsHTML = product.highlights && product.highlights.length > 0
    ? `<ul class="product-highlights">
         ${product.highlights.slice(0, 3).map(h => `<li>✓ ${escapeHtml(h)}</li>`).join('')}
       </ul>`
    : '';
  
  card.innerHTML = `
    <div class="product-image">
      <img src="${product.image_url}" 
           alt="${product.name}"
           onerror="this.src='/assets/no-image.png'">
    </div>
    
    <div class="product-info">
      <h4 class="product-name" title="${product.name}">
        ${escapeHtml(product.name)}
      </h4>
      
      <div class="product-price">
        <span class="current-price">${formatPrice(product.price)}đ</span>
      </div>
      
      <p class="product-desc">${escapeHtml(product.short_desc)}</p>
      
      ${highlightsHTML}
      
      <div class="product-actions">
        <a href="${product.link}" class="btn-view" target="_blank">
          Xem chi tiết
        </a>
        <button class="btn-quick-buy" onclick="quickBuy('${product.link}')">
          Mua ngay
        </button>
      </div>
    </div>
  `;
  
  return card;
}

/**
 * Cập nhật UI mode buttons
 */
function updateModeUI(mode) {
  const botBtn = document.getElementById('botModeBtn');
  const staffBtn = document.getElementById('staffModeBtn');
  
  if (mode === 'bot') {
    botBtn.classList.add('active');
    staffBtn.classList.remove('active');
    showBotModeIndicator();
  } else {
    staffBtn.classList.add('active');
    botBtn.classList.remove('active');
    showStaffModeIndicator();
  }
  
  setModeLoading(false);
}

/**
 * Set loading state cho mode buttons
 */
function setModeLoading(isLoading) {
  const botBtn = document.getElementById('botModeBtn');
  const staffBtn = document.getElementById('staffModeBtn');
  
  if (isLoading) {
    botBtn.classList.add('loading');
    staffBtn.classList.add('loading');
    botBtn.disabled = true;
    staffBtn.disabled = true;
  } else {
    botBtn.classList.remove('loading');
    staffBtn.classList.remove('loading');
    botBtn.disabled = false;
    staffBtn.disabled = false;
  }
}

/**
 * Hiển thị typing indicator
 */
function showTypingIndicator() {
  const indicator = document.getElementById('typingIndicator');
  if (indicator) {
    indicator.style.display = 'flex';
    scrollToBottom();
  }
}

/**
 * Ẩn typing indicator
 */
function hideTypingIndicator() {
  const indicator = document.getElementById('typingIndicator');
  if (indicator) {
    indicator.style.display = 'none';
  }
}

/**
 * Hiển thị bot mode indicator
 */
function showBotModeIndicator() {
  const indicator = document.getElementById('modeIndicator');
  if (indicator) {
    indicator.className = 'chat-mode-indicator';
    indicator.innerHTML = `
      <span class="icon">🤖</span>
      <span class="text">Bot đang sẵn sàng hỗ trợ bạn</span>
    `;
  }
}

/**
 * Hiển thị staff mode indicator
 */
function showStaffModeIndicator() {
  const indicator = document.getElementById('modeIndicator');
  if (indicator) {
    indicator.className = 'chat-mode-indicator staff-mode';
    indicator.innerHTML = `
      <span class="icon">👤</span>
      <span class="text">Nhân viên sẽ trả lời trong giây lát...</span>
    `;
  }
}

// ==================== UTILITY FUNCTIONS ====================

/**
 * Append message to chat container
 */
function appendMessage(html) {
  const container = document.getElementById('chatMessages');
  container.insertAdjacentHTML('beforeend', html);
  scrollToBottom();
}

/**
 * Scroll to bottom of chat
 */
function scrollToBottom() {
  const container = document.getElementById('chatMessages');
  setTimeout(() => {
    container.scrollTop = container.scrollHeight;
  }, 100);
}

/**
 * Format timestamp
 */
function formatTime(isoString) {
  const date = new Date(isoString);
  return date.toLocaleTimeString('vi-VN', { 
    hour: '2-digit', 
    minute: '2-digit' 
  });
}

/**
 * Format price VND
 */
function formatPrice(price) {
  return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
}

/**
 * Escape HTML to prevent XSS
 */
function escapeHtml(text) {
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}

/**
 * Show success notification
 */
function showSuccess(message) {
  // TODO: Implement toast notification
  console.log('✅', message);
}

/**
 * Show error notification
 */
function showError(message) {
  // TODO: Implement toast notification
  console.error('❌', message);
}

/**
 * Quick buy product
 */
function quickBuy(productLink) {
  window.open(productLink, '_blank');
  // TODO: Add to cart directly
}

// ==================== EVENT LISTENERS ====================

document.addEventListener('DOMContentLoaded', function() {
  // Khởi tạo WebSocket
  initWebSocket();
  
  // Send button
  document.getElementById('sendBtn').addEventListener('click', function() {
    const input = document.getElementById('messageInput');
    sendMessage(input.value);
  });
  
  // Enter key to send
  document.getElementById('messageInput').addEventListener('keypress', function(e) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage(this.value);
    }
  });
  
  // Mode switcher buttons
  document.getElementById('botModeBtn').addEventListener('click', function() {
    switchChatMode('bot');
  });
  
  document.getElementById('staffModeBtn').addEventListener('click', function() {
    switchChatMode('staff');
  });
});

// ==================== DISCONNECT ====================

/**
 * Ngắt kết nối WebSocket (khi user logout hoặc close tab)
 */
function disconnectWebSocket() {
  if (stompClient && isConnected) {
    stompClient.disconnect(() => {
      console.log('🔌 WebSocket disconnected');
      isConnected = false;
    });
  }
}

// Disconnect khi close tab
window.addEventListener('beforeunload', disconnectWebSocket);
```

---

## 🐛 Troubleshooting

### 1. Bot không trả lời?

**Nguyên nhân có thể:**
- Backend Chatbot API không hoạt động
- Dify API key hết hạn hoặc sai
- Network timeout

**Cách kiểm tra:**
```javascript
// 1. Check WebSocket connection
console.log('WebSocket connected:', isConnected);

// 2. Check current mode
console.log('Current chat mode:', currentChatMode);

// 3. Check console logs
// Xem có error gì từ backend không

// 4. Test gửi tin nhắn trực tiếp
stompClient.send('/app/chat.send', {}, JSON.stringify({
  content: 'test'
}));
```

**Giải pháp:**
- Liên hệ team Backend kiểm tra logs
- Verify Dify API config trong `application.properties`
- Check Network tab xem WebSocket message có đến server không

### 2. Sản phẩm gợi ý không hiển thị?

**Nguyên nhân:**
- Quên parse `JSON.parse(message.content)`
- Field `suggested_products` bị `null` hoặc empty array
- CSS chưa load

**Cách fix:**
```javascript
// Thêm defensive checks
function handleBotResponse(message) {
  try {
    const botData = JSON.parse(message.content);
    
    // Check reply_text
    if (botData.reply_text) {
      displayBotMessage(botData.reply_text, message.createdAt);
    }
    
    // Check suggested_products
    if (botData.suggested_products) {
      if (Array.isArray(botData.suggested_products)) {
        if (botData.suggested_products.length > 0) {
          displayProductSuggestions(botData.suggested_products);
        }
      }
    }
    
  } catch (e) {
    console.error('Parse error:', e);
    console.log('Raw content:', message.content);
  }
}
```

### 3. Switch mode không hoạt động?

**Nguyên nhân:**
- Payload format sai
- Chưa subscribe channel `/user/queue/chat_mode_changed`
- Token hết hạn

**Cách fix:**
```javascript
// 1. Verify payload format
const payload = {
  chatMode: 'bot' // hoặc 'staff', phải lowercase
};

// 2. Check subscription
stompClient.subscribe('/user/queue/chat_mode_changed', function(msg) {
  console.log('Mode change notification:', msg.body);
  handleModeChanged(msg);
});

// 3. Verify token còn hạn
const token = localStorage.getItem('auth_token');
if (!token) {
  console.error('Token missing');
}
```

### 4. WebSocket liên tục disconnect?

**Nguyên nhân:**
- Token không hợp lệ
- Backend restart
- Network không ổn định

**Giải pháp:**
```javascript
// Implement reconnection logic
function initWebSocket() {
  // ... existing code ...
  
  socket.onclose = function() {
    console.log('Connection closed, reconnecting...');
    setTimeout(initWebSocket, 3000);
  };
}
```

### 5. Tin nhắn bị duplicate?

**Nguyên nhân:**
- Subscribe nhiều lần
- Optimistic UI + Server echo

**Giải pháp:**
```javascript
// Track message IDs để tránh duplicate
const displayedMessageIds = new Set();

function handleChatMessage(payload) {
  const message = JSON.parse(payload.body);
  
  // Check duplicate
  if (displayedMessageIds.has(message.id)) {
    return;
  }
  
  displayedMessageIds.add(message.id);
  
  // Display message...
}
```

---

## 📞 Liên hệ & Hỗ trợ

### Team Backend Contact:
- **Email**: backend@techshop.vn
- **Slack**: #pbl6-backend-support

### Các vấn đề thường gặp cần liên hệ Backend:
1. ✅ Bot không trả lời sau 30s
2. ✅ Switch mode không hoạt động
3. ✅ WebSocket connection error 401/403
4. ✅ Tin nhắn không được lưu vào database
5. ✅ Product links không đúng format

### Resources:
- 📄 Backend API Docs: `docs/CHATBOT_BACKEND_README.md`
- 🗂️ Database Migration: `docs/migration_add_chatbot.sql`
- 🧪 Test File: `test-chatbot.html`

---

## ✅ Checklist tích hợp

Khi bắt đầu tích hợp, hãy đảm bảo:

- [ ] Đã có token authentication hợp lệ
- [ ] Đã include SockJS và STOMP libraries
- [ ] Đã implement WebSocket connection logic
- [ ] Đã subscribe đủ 3 channels (chat, chat_mode_changed, chat_error)
- [ ] Đã xử lý parse JSON cho bot response
- [ ] Đã implement UI cho product suggestions
- [ ] Đã có mode switcher buttons
- [ ] Đã test với cả bot mode và staff mode
- [ ] Đã handle các trường hợp lỗi (connection, parse, timeout)
- [ ] Đã implement typing indicator
- [ ] Đã test trên mobile responsive

---

**🎉 Chúc bạn tích hợp thành công!**
