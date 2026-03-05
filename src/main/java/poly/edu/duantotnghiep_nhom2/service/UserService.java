package poly.edu.duantotnghiep_nhom2.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.duantotnghiep_nhom2.entity.User;
import poly.edu.duantotnghiep_nhom2.entity.enums.Role;
import poly.edu.duantotnghiep_nhom2.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. Đăng ký tài khoản mới
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

        return userRepository.save(user);
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

    // 5. Đổi mật khẩu
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không chính xác!");
        }
        
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("Mật khẩu mới phải từ 6 ký tự trở lên!");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
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
    
    // Lấy danh sách khách hàng (trừ Admin)
    public List<User> getAllCustomers() {
        return userRepository.findByRole(Role.CUSTOMER);
    }
}
