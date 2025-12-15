-- Migration: Add chatbot support to chat system
-- Date: 2025-12-15

-- 1. Thêm cột chatMode và conversationId vào bảng rooms
ALTER TABLE rooms 
ADD COLUMN chat_mode VARCHAR(20) DEFAULT 'bot',
ADD COLUMN conversation_id VARCHAR(100);

-- 2. Update tất cả room hiện tại sang chế độ bot
UPDATE rooms SET chat_mode = 'bot' WHERE chat_mode IS NULL;

-- 3. (Optional) Thêm index để tăng performance
CREATE INDEX idx_rooms_chat_mode ON rooms(chat_mode);
CREATE INDEX idx_rooms_user_key ON rooms(user_key);

-- 4. Thêm comment cho documentation
ALTER TABLE rooms MODIFY COLUMN chat_mode VARCHAR(20) DEFAULT 'bot' COMMENT 'Chat mode: bot or staff';
ALTER TABLE rooms MODIFY COLUMN conversation_id VARCHAR(100) COMMENT 'Chatbot conversation ID for context';
