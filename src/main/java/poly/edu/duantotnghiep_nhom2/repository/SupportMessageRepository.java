package poly.edu.duantotnghiep_nhom2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.duantotnghiep_nhom2.entity.SupportMessage;
import poly.edu.duantotnghiep_nhom2.entity.User;

import java.util.List;

@Repository
public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {
    
    List<SupportMessage> findByUserIdOrderByTimestampAsc(Long userId);
    
    // Chỉ lấy User có tin nhắn chưa bị ẩn (isArchived = false)
    @Query("SELECT m.user FROM SupportMessage m " +
           "WHERE m.isArchived = false " +
           "GROUP BY m.user " +
           "ORDER BY MAX(m.timestamp) DESC")
    List<User> findUsersWithMessagesOrderByLatest();

    @Query("SELECT COUNT(m) FROM SupportMessage m WHERE m.isRead = false AND m.admin IS NULL AND m.isArchived = false")
    long countUnreadMessages();

    @Modifying
    @Query("UPDATE SupportMessage m SET m.isRead = true WHERE m.user.id = :userId AND m.admin IS NULL")
    void markMessagesAsRead(@Param("userId") Long userId);

    // Ẩn tất cả tin nhắn của user này khỏi Admin
    @Modifying
    @Query("UPDATE SupportMessage m SET m.isArchived = true WHERE m.user.id = :userId")
    void archiveMessages(@Param("userId") Long userId);
}
