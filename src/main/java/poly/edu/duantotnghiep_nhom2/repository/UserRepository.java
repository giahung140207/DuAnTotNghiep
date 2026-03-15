package poly.edu.duantotnghiep_nhom2.repository;
import poly.edu.duantotnghiep_nhom2.entity.User;
import poly.edu.duantotnghiep_nhom2.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Dùng cho chức năng đăng nhập (Login)
    Optional<User> findByUsername(String username);

    // Tìm kiếm user theo email (Dùng cho quên mật khẩu)
    Optional<User> findByEmail(String email);

    // Dùng để validate khi đăng ký (tránh trùng lặp)
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Tìm kiếm user theo role (cho Admin quản lý)
    List<User> findByRole(Role role);
}
