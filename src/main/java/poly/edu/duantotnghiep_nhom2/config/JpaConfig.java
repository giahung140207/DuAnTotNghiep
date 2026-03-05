package poly.edu.duantotnghiep_nhom2.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaAuditing // Kích hoạt @CreatedDate và @LastModifiedDate
@EnableTransactionManagement // Quản lý transaction chặt chẽ
public class JpaConfig {
    // Có thể thêm Bean AuditorAware<String> ở đây nếu muốn lưu cả "created_by" (người tạo)
}
