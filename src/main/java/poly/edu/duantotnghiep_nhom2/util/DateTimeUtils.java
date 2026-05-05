package poly.edu.duantotnghiep_nhom2.util;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateTimeUtils {

    // Private constructor để ngăn chặn khởi tạo
    private DateTimeUtils() {}

    private static final String DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

    // Format LocalDateTime thành String đẹp (để hiển thị ra View/Email)
    public static String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(FORMATTER) : "";
    }

    // Tính tổng số giờ chơi (trả về số thực, ví dụ: 1h30p = 1.5 giờ)
    // Dùng để nhân với pricePerHour ra tổng tiền
    public static double calculateDurationInHours(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 0;

        long minutes = ChronoUnit.MINUTES.between(start, end);
        if(minutes <= 0) return 0;
        return (double) minutes / 60.0;
    }

    // Validate logic: Giờ kết thúc phải sau giờ bắt đầu
    public static boolean isValidTimeRange(LocalDateTime start, LocalDateTime end) {
        return start != null && end != null && end.isAfter(start);
    }

    // Validate logic: Không được đặt ngày quá khứ (cho phép trễ tối đa 5 phút do độ trễ mạng)
    public static boolean isPastTime(LocalDateTime start) {
        return start != null && start.isBefore(LocalDateTime.now().minusMinutes(5));
    }
}
