package poly.edu.duantotnghiep_nhom2.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cấu hình đường dẫn upload ảnh
        Path uploadDir = Paths.get("./uploads");
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        registry.addResourceHandler("/uploads/**") // URL truy cập: localhost:8080/uploads/ten-anh.jpg
                .addResourceLocations("file:/" + uploadPath + "/");

        // Cấu hình cho static resources mặc định (Bootstrap, Custom CSS/JS)
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
