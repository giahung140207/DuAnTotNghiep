package poly.edu.duantotnghiep_nhom2.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Đường dẫn mà client đăng ký lắng nghe (subscribe)
        config.enableSimpleBroker("/topic");
        
        // Tiền tố cho các request gửi lên server từ client
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Điểm kết nối (endpoint) cho client để bắt đầu bắt tay với WebSocket
        registry.addEndpoint("/chat-websocket").withSockJS();
    }
}
