package poly.edu.duantotnghiep_nhom2.entity.enums;

public enum BookingStatus {
    PENDING,    // Chờ duyệt
    CONFIRMED,  // Đã duyệt (Đang chờ đá hoặc đang đá)
    COMPLETED,  // Đã hoàn thành (Đã đá xong)
    CANCELLED,  // Đã hủy
    ADMINCANCELLED, // ADMIN hủy
    REFUNDED,   // Đã hoàn tiền (Thêm trạng thái này)
    SWAPPED     // Đã đổi sân
}
