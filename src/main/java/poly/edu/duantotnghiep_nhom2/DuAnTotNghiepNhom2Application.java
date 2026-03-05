package poly.edu.duantotnghiep_nhom2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Kích hoạt tác vụ định kỳ
public class DuAnTotNghiepNhom2Application {

    public static void main(String[] args) {
        SpringApplication.run(DuAnTotNghiepNhom2Application.class, args);
    }

}
