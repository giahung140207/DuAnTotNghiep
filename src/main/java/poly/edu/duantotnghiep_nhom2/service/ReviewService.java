package poly.edu.duantotnghiep_nhom2.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.duantotnghiep_nhom2.entity.Booking;
import poly.edu.duantotnghiep_nhom2.entity.Review;
import poly.edu.duantotnghiep_nhom2.entity.enums.BookingStatus;
import poly.edu.duantotnghiep_nhom2.repository.BookingRepository;
import poly.edu.duantotnghiep_nhom2.repository.ReviewRepository;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    public ReviewService(ReviewRepository reviewRepository, BookingRepository bookingRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public Review createReview(Long bookingId, Integer rating, String comment) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new RuntimeException("Chỉ được đánh giá khi trận đấu đã hoàn tất.");
        }
        
        if (hasReviewed(bookingId)) {
            throw new RuntimeException("Bạn đã đánh giá trận đấu này rồi.");
        }

        Review review = new Review();
        review.setBooking(booking);
        review.setRating(rating);
        review.setComment(comment);

        return reviewRepository.save(review);
    }

    public List<Review> getReviewsByPitch(Long pitchId) {
        return reviewRepository.findByPitchId(pitchId);
    }
    
    public boolean hasReviewed(Long bookingId) {
        return reviewRepository.existsByBookingId(bookingId);
    }

    // Lấy 4 bài đánh giá mới nhất
    public List<Review> getLatestReviews() {
        return reviewRepository.findTop4ByOrderByCreatedAtDesc();
    }
}
