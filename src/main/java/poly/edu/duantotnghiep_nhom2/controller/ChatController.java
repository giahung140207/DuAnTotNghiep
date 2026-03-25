package poly.edu.duantotnghiep_nhom2.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // Endpoint này được gọi khi Client gửi message lên /app/chat
    @MessageMapping("/chat")
    public void processMessage(@Payload Map<String, String> message) {
        String userId = message.get("userId");
        String content = message.get("content");
        String senderType = message.get("senderType"); // "ADMIN" hoặc "CUSTOMER"

        // Trong thực tế, bạn có thể lưu tin nhắn vào DB ở đây bằng SupportMessageService
        // Nhưng do các API Ajax cũ (send-ajax) đã làm việc đó rồi, nên ở đây ta chỉ cần phát tín hiệu (ping)
        
        // Gửi thông báo có tin nhắn mới vào phòng chat riêng của User đó
        // Ví dụ: /topic/messages/5 (Phòng chat của user id 5)
        messagingTemplate.convertAndSend("/topic/messages/" + userId, "NEW_MESSAGE");
    }
}
