package poly.edu.duantotnghiep_nhom2.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.duantotnghiep_nhom2.entity.SupportMessage;
import poly.edu.duantotnghiep_nhom2.entity.User;
import poly.edu.duantotnghiep_nhom2.repository.SupportMessageRepository;
import poly.edu.duantotnghiep_nhom2.repository.UserRepository;

import java.util.List;

@Service
public class SupportMessageService {

    private final SupportMessageRepository supportMessageRepository;
    private final UserRepository userRepository;

    public SupportMessageService(SupportMessageRepository supportMessageRepository, UserRepository userRepository) {
        this.supportMessageRepository = supportMessageRepository;
        this.userRepository = userRepository;
    }

    public SupportMessage sendMessage(Long userId, String content) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        SupportMessage message = new SupportMessage();
        message.setUser(user);
        message.setContent(content);
        message.setRead(false);
        message.setArchived(false); // Tin mới luôn hiện lại
        return supportMessageRepository.save(message);
    }

    @Transactional
    public SupportMessage replyMessage(Long userId, Long adminId, String content) {
        User user = userRepository.findById(userId).orElseThrow();
        User admin = userRepository.findById(adminId).orElseThrow();

        SupportMessage message = new SupportMessage();
        message.setUser(user);
        message.setAdmin(admin);
        message.setContent(content);
        message.setRead(true);
        message.setArchived(false);
        
        return supportMessageRepository.save(message);
    }

    public List<SupportMessage> getMessagesByUser(Long userId) {
        return supportMessageRepository.findByUserIdOrderByTimestampAsc(userId);
    }
    
    public List<User> getUsersWithSupportRequest() {
        return supportMessageRepository.findUsersWithMessagesOrderByLatest();
    }

    @Transactional
    public void markAsRead(Long userId) {
        supportMessageRepository.markMessagesAsRead(userId);
    }

    // Kết thúc hỗ trợ: Ẩn tin nhắn khỏi Admin
    @Transactional
    public void endSupport(Long userId) {
        supportMessageRepository.archiveMessages(userId);
    }
}
