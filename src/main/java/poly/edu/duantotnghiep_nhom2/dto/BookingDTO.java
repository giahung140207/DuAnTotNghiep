package poly.edu.duantotnghiep_nhom2.dto;

import lombok.Data;
import poly.edu.duantotnghiep_nhom2.entity.enums.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookingDTO {
    private Long id;

    // Thông tin người đặt
    private Long userId;
    private String userFullName;

    // Thông tin sân
    private Long pitchId;
    private String pitchName;
    private String facilityName;
    private String facilityAddress;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private String paymentStatus;
}