package poly.edu.duantotnghiep_nhom2.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Dùng cho các lỗi như: Ví không đủ tiền, Hủy sau giờ quy định
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessLogicException extends RuntimeException {
    public BusinessLogicException(String message) {
        super(message);
    }
}
