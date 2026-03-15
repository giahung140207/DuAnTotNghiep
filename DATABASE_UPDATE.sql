-- =========================================================================================
-- FILE NÀY CHỨA CÁC CÂU LỆNH SQL ĐỂ CẬP NHẬT DATABASE
-- Hãy copy toàn bộ nội dung dưới đây và chạy trong SQL Server Management Studio
-- =========================================================================================

-- 1. Thêm 3 cột mới vào bảng 'users' để phục vụ chức năng xác thực qua Email (OTP)
ALTER TABLE users ADD is_active BIT DEFAULT 0;
ALTER TABLE users ADD otp_code VARCHAR(10);
ALTER TABLE users ADD otp_expiry_time DATETIME;

-- 2. Cập nhật tất cả các tài khoản đang có sẵn trong database thành 'đã kích hoạt' (is_active = 1)
-- Việc này giúp các tài khoản Admin hoặc khách hàng cũ (tạo trước khi có chức năng OTP)
-- vẫn có thể đăng nhập bình thường mà không bị hệ thống chặn lại.
UPDATE users SET is_active = 1;
