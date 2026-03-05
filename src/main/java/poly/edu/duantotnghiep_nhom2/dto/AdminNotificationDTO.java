package poly.edu.duantotnghiep_nhom2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminNotificationDTO {
    private long unreadMessages;
    private long pendingBookings;
    
    // Constructor thủ công
    public AdminNotificationDTO(long unreadMessages, long pendingBookings) {
        this.unreadMessages = unreadMessages;
        this.pendingBookings = pendingBookings;
    }
    
    // Getters Setters thủ công
    public long getUnreadMessages() { return unreadMessages; }
    public void setUnreadMessages(long unreadMessages) { this.unreadMessages = unreadMessages; }
    public long getPendingBookings() { return pendingBookings; }
    public void setPendingBookings(long pendingBookings) { this.pendingBookings = pendingBookings; }
}
