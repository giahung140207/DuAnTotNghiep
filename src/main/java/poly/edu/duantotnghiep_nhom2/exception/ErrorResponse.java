package poly.edu.duantotnghiep_nhom2.exception;

import java.time.LocalDateTime;
import java.util.Map;

public class ErrorResponse {
    private int status;            // HTTP Status Code (400, 404, 409...)
    private String error;          // Tên lỗi (e.g., Conflict, Not Found)
    private String message;        // Thông báo chi tiết cho người dùng
    private LocalDateTime timestamp;
    private String path;           // API nào bị lỗi

    // Dùng để chứa chi tiết lỗi validate (VD: "email": "Không đúng định dạng")
    private Map<String, String> validationErrors;

    // Constructor đầy đủ
    public ErrorResponse(int status, String error, String message, LocalDateTime timestamp, String path, Map<String, String> validationErrors) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
        this.path = path;
        this.validationErrors = validationErrors;
    }

    // Constructor rút gọn
    public ErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    // --- GETTERS AND SETTERS ---

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(Map<String, String> validationErrors) {
        this.validationErrors = validationErrors;
    }
}
