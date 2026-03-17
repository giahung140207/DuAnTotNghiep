package poly.edu.duantotnghiep_nhom2.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.duantotnghiep_nhom2.entity.User;
import poly.edu.duantotnghiep_nhom2.entity.enums.Role;
import poly.edu.duantotnghiep_nhom2.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // Tạo mã OTP ngẫu nhiên 6 số
    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    // 1. Đăng ký tài khoản mới (Cập nhật logic OTP)
    @Transactional
    public User register(User user) {
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Họ tên không được để trống!");
        }
        
        if (user.getUsername() == null || user.getUsername().trim().length() < 4) {
            throw new RuntimeException("Tên đăng nhập phải từ 4 ký tự trở lên!");
        }

        if (user.getPassword() == null || user.getPassword().length() < 6) {
            throw new RuntimeException("Mật khẩu phải từ 6 ký tự trở lên!");
        }

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!Pattern.matches(emailRegex, user.getEmail())) {
            throw new RuntimeException("Email không đúng định dạng!");
        }

        String phoneRegex = "^\\d{10,11}$";
        if (!Pattern.matches(phoneRegex, user.getPhone())) {
            throw new RuntimeException("Số điện thoại phải là số và có 10-11 chữ số!");
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user.setRole(Role.CUSTOMER);
        user.setBalance(BigDecimal.ZERO);
        
        // Thiết lập trạng thái chưa kích hoạt và tạo OTP
        user.setIsActive(false);
        String otp = generateOtp();
        user.setOtpCode(otp);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(5)); // Hết hạn sau 5 phút

        User savedUser = userRepository.save(user);

        // Gửi email OTP
        String emailContent = "Xin chào " + user.getFullName() + ",\n\n"
                + "Mã xác nhận (OTP) để kích hoạt tài khoản của bạn là: " + otp + "\n"
                + "Mã này sẽ hết hạn sau 5 phút.\n\n"
                + "Cảm ơn bạn đã sử dụng dịch vụ!";
        emailService.sendSimpleMessage(user.getEmail(), "Mã xác nhận kích hoạt tài khoản", emailContent);

        return savedUser;
    }

    // Xác thực mã OTP
    @Transactional
    public boolean verifyOtp(String username, String otpCode) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        if (user.getIsActive()) {
            throw new RuntimeException("Tài khoản đã được kích hoạt từ trước!");
        }

        if (user.getOtpCode() == null || !user.getOtpCode().equals(otpCode)) {
            throw new RuntimeException("Mã xác nhận không chính xác!");
        }

        if (user.getOtpExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã xác nhận đã hết hạn! Vui lòng yêu cầu mã mới.");
        }

        // Kích hoạt tài khoản
        user.setIsActive(true);
        user.setOtpCode(null);
        user.setOtpExpiryTime(null);
        userRepository.save(user);
        return true;
    }

    // Gửi lại mã OTP
    @Transactional
    public void resendOtp(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        if (user.getIsActive()) {
            throw new RuntimeException("Tài khoản đã được kích hoạt, không cần gửi lại mã.");
        }

        String newOtp = generateOtp();
        user.setOtpCode(newOtp);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        String emailContent = "Xin chào " + user.getFullName() + ",\n\n"
                + "Bạn vừa yêu cầu gửi lại mã xác nhận. Mã OTP mới của bạn là: " + newOtp + "\n"
                + "Mã này sẽ hết hạn sau 5 phút.\n\n"
                + "Vui lòng không chia sẻ mã này cho bất kỳ ai!";
        emailService.sendSimpleMessage(user.getEmail(), "Mã xác nhận kích hoạt tài khoản (Mới)", emailContent);
    }
    
    // Tạo lại OTP kích hoạt từ Identifier (Username hoặc Email)
    @Transactional
    public String generateResendActivationOtp(String identifier) {
        User user = userRepository.findByUsername(identifier)
                .orElseGet(() -> userRepository.findByEmail(identifier)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản nào khớp với thông tin bạn cung cấp!")));

        if (user.getIsActive()) {
            throw new RuntimeException("Tài khoản này đã được kích hoạt rồi. Bạn có thể đăng nhập bình thường.");
        }

        String newOtp = generateOtp();
        user.setOtpCode(newOtp);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        String emailContent = "Xin chào " + user.getFullName() + ",\n\n"
                + "Bạn vừa yêu cầu gửi lại mã xác nhận. Mã OTP kích hoạt tài khoản của bạn là: " + newOtp + "\n"
                + "Mã này sẽ hết hạn sau 5 phút.\n\n"
                + "Vui lòng không chia sẻ mã này cho bất kỳ ai!";
        emailService.sendSimpleMessage(user.getEmail(), "Mã xác nhận kích hoạt tài khoản (Mới)", emailContent);
        
        return user.getUsername();
    }

    // 2. Nạp tiền vào ví
    @Transactional
    public void topUpBalance(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền nạp phải lớn hơn 0");
        }
        User user = getUserById(userId);
        user.setBalance(user.getBalance().add(amount));
        userRepository.save(user);
    }

    // 3. Trừ tiền ví
    @Transactional
    public void deductBalance(Long userId, BigDecimal amount) {
        User user = getUserById(userId);
        if (user.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Số dư ví không đủ để thanh toán.");
        }
        user.setBalance(user.getBalance().subtract(amount)); // Trừ tiền
        userRepository.save(user);
    }

    // 4. Cập nhật thông tin User
    @Transactional
    public void updateUser(User user) {
        // Validate lại khi cập nhật
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Họ tên không được để trống!");
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!Pattern.matches(emailRegex, user.getEmail())) {
            throw new RuntimeException("Email không đúng định dạng!");
        }
        String phoneRegex = "^\\d{10,11}$";
        if (!Pattern.matches(phoneRegex, user.getPhone())) {
            throw new RuntimeException("Số điện thoại phải là số và có 10-11 chữ số!");
        }
        
        userRepository.save(user);
    }

    // 5. Đổi mật khẩu (Cũ - dành cho bước 1 của Khách hàng: check pass cũ)
    public void checkOldPassword(Long userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không chính xác!");
        }
        
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("Mật khẩu mới phải từ 6 ký tự trở lên!");
        }
    }

    // 5.1. Tạo OTP Đổi mật khẩu cho khách hàng
    @Transactional
    public void generatePasswordOtp(Long userId) {
        User user = getUserById(userId);
        
        String otp = generateOtp();
        user.setOtpCode(otp);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        String emailContent = "Xin chào " + user.getFullName() + ",\n\n"
                + "Bạn vừa yêu cầu đổi mật khẩu. Mã xác nhận (OTP) của bạn là: " + otp + "\n"
                + "Mã này sẽ hết hạn sau 5 phút.\n\n"
                + "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email và bảo mật tài khoản của mình.";
        emailService.sendSimpleMessage(user.getEmail(), "Mã xác nhận đổi mật khẩu", emailContent);
    }
    
    // 5.2 Gửi lại mã OTP đổi mật khẩu
    @Transactional
    public void resendPasswordOtp(Long userId) {
        User user = getUserById(userId);
        
        String otp = generateOtp();
        user.setOtpCode(otp);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        String emailContent = "Xin chào " + user.getFullName() + ",\n\n"
                + "Đây là mã xác nhận (OTP) mới để đổi mật khẩu của bạn: " + otp + "\n"
                + "Mã này sẽ hết hạn sau 5 phút.\n\n"
                + "Vui lòng không chia sẻ mã này cho bất kỳ ai.";
        emailService.sendSimpleMessage(user.getEmail(), "Mã xác nhận đổi mật khẩu (Mới)", emailContent);
    }

    // 5.3 Xác thực OTP đổi mật khẩu
    @Transactional
    public boolean verifyPasswordOtp(Long userId, String otpCode) {
        User user = getUserById(userId);

        if (user.getOtpCode() == null || !user.getOtpCode().equals(otpCode)) {
            throw new RuntimeException("Mã xác nhận không chính xác!");
        }

        if (user.getOtpExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã xác nhận đã hết hạn! Vui lòng yêu cầu mã mới.");
        }

        // Xóa OTP sau khi dùng thành công
        user.setOtpCode(null);
        user.setOtpExpiryTime(null);
        userRepository.save(user);
        return true;
    }

    // 5.4. Ép đổi mật khẩu ngay lập tức (Dành cho Admin hoặc Khách đã qua OTP)
    @Transactional
    public void forceChangePassword(Long userId, String newPassword) {
        User user = getUserById(userId);
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("Mật khẩu mới phải từ 6 ký tự trở lên!");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    // Hàm cũ (tạm giữ để không vỡ các code khác nếu lỡ gọi)
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        checkOldPassword(userId, oldPassword, newPassword);
        forceChangePassword(userId, newPassword);
    }

    // --- QUÊN MẬT KHẨU ---
    @Transactional
    public void generateForgotPasswordOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản nào đăng ký với email này!"));

        String otp = generateOtp();
        user.setOtpCode(otp);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        String emailContent = "Xin chào " + user.getFullName() + ",\n\n"
                + "Bạn vừa yêu cầu đặt lại mật khẩu do quên mật khẩu. Mã xác nhận (OTP) của bạn là: " + otp + "\n"
                + "Mã này sẽ hết hạn sau 5 phút.\n\n"
                + "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email và bảo mật tài khoản của mình.";
        emailService.sendSimpleMessage(user.getEmail(), "Mã xác nhận quên mật khẩu", emailContent);
    }

    @Transactional
    public void verifyAndResetPassword(String email, String otpCode, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy tài khoản!"));

        if (user.getOtpCode() == null || !user.getOtpCode().equals(otpCode)) {
            throw new RuntimeException("Mã xác nhận không chính xác!");
        }

        if (user.getOtpExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã xác nhận đã hết hạn! Vui lòng yêu cầu lại mã mới.");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("Mật khẩu mới phải từ 6 ký tự trở lên!");
        }

        // Đặt lại mật khẩu và xóa OTP
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setOtpCode(null);
        user.setOtpExpiryTime(null);
        userRepository.save(user);
    }

    // Các hàm tra cứu
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng ID: " + id));
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    // Lấy danh sách khách hàng (trừ Admin)
    public List<User> getAllCustomers() {
        return userRepository.findByRole(Role.CUSTOMER);
    }
}