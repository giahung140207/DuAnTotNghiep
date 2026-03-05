package poly.edu.duantotnghiep_nhom2.util;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;

public class FileUploadUtils {

    // Thư mục gốc lưu ảnh (khớp với cấu hình trong WebMvcConfig)
    private static final String UPLOAD_DIR = "uploads";

    private FileUploadUtils() {}

    /**
     * Lưu file vào thư mục hệ thống
     * @param uploadSubDir thư mục con (ví dụ: "pitches", "users")
     * @param multipartFile file từ request
     * @return Tên file đã lưu (để lưu vào DB)
     */
    public static String saveFile(String uploadSubDir, MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty()) {
            return null; // Hoặc throw Exception tùy logic
        }

        // Tạo thư mục nếu chưa tồn tại
        Path uploadPath = Paths.get(UPLOAD_DIR, uploadSubDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Tạo tên file ngẫu nhiên: UUID + Extension gốc (ví dụ: a1b2-c3d4.jpg)
        String originalFileName = multipartFile.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + fileExtension;

        // Lưu file
        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

            // Trả về đường dẫn tương đối để lưu DB: /uploads/pitches/xyz.jpg
            return "/" + UPLOAD_DIR + "/" + uploadSubDir + "/" + fileName;
        } catch (IOException ioe) {
            throw new IOException("Không thể lưu file: " + fileName, ioe);
        }
    }

    // Xóa file (dùng khi user đổi avatar hoặc xóa sân)
    public static void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) return;

        try {
            // filePath đang lưu dạng "/uploads/..." -> cần bỏ dấu "/" đầu
            Path path = Paths.get(filePath.startsWith("/") ? filePath.substring(1) : filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("Lỗi xóa file: " + filePath);
        }
    }
}
