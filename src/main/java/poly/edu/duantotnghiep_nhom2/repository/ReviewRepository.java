package poly.edu.duantotnghiep_nhom2.repository;
import poly.edu.duantotnghiep_nhom2.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Lấy tất cả review của một sân bóng cụ thể
    @Query("SELECT r FROM Review r WHERE r.booking.pitch.id = :pitchId ORDER BY r.createdAt DESC")
    List<Review> findByPitchId(@Param("pitchId") Long pitchId);

    // Tính điểm đánh giá trung bình
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.booking.pitch.id = :pitchId")
    Double getAverageRatingByPitch(@Param("pitchId") Long pitchId);
    
    // Kiểm tra xem booking đã được đánh giá chưa
    boolean existsByBookingId(Long bookingId);

    // Lấy 4 bài đánh giá mới nhất để hiển thị ở trang chủ
    List<Review> findTop4ByOrderByCreatedAtDesc();
}
