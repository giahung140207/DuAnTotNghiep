package poly.edu.duantotnghiep_nhom2.dto;

import lombok.Data;
import poly.edu.duantotnghiep_nhom2.entity.enums.PitchStatus;
import poly.edu.duantotnghiep_nhom2.entity.enums.PitchType;
import java.math.BigDecimal;

@Data
public class PitchDTO {
    private Long id;
    private String name;
    private PitchType type; // 5 hoặc 7 người
    private BigDecimal pricePerHour;
    private PitchStatus status;
    private String imageUrl;
    private String description;

    // Thông tin phẳng (Flatten) từ Facility để hiển thị tiện lợi
    private Long facilityId;
    private String facilityName;
    private String facilityAddress;
}