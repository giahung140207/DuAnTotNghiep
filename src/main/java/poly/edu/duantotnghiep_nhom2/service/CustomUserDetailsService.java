package poly.edu.duantotnghiep_nhom2.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import poly.edu.duantotnghiep_nhom2.entity.User;
import poly.edu.duantotnghiep_nhom2.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với username: " + username));

        // CHẶN ĐĂNG NHẬP NẾU TÀI KHOẢN CHƯA ĐƯỢC KÍCH HOẠT QUA EMAIL
        if (user.getIsActive() != null && !user.getIsActive()) {
            throw new org.springframework.security.authentication.DisabledException("Tài khoản của bạn chưa được kích hoạt. Vui lòng kiểm tra email.");
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}
