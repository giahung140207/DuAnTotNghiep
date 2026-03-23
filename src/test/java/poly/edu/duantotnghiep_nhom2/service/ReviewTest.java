package poly.edu.duantotnghiep_nhom2.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import poly.edu.duantotnghiep_nhom2.entity.Booking;
import poly.edu.duantotnghiep_nhom2.entity.Review;
import poly.edu.duantotnghiep_nhom2.entity.enums.BookingStatus;
import poly.edu.duantotnghiep_nhom2.repository.BookingRepository;
import poly.edu.duantotnghiep_nhom2.repository.ReviewRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Booking booking;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatus.COMPLETED);
    }

    // ==============================
    // 1. Happy case
    // ==============================
    @Test
    void testCreateReview_Success() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBookingId(1L)).thenReturn(false);

        Review review = new Review();
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        Review result = reviewService.createReview(1L, 5, "Good");

        assertNotNull(result);
        verify(reviewRepository).save(any(Review.class));
    }

    // ==============================
    // 2. Booking not found
    // ==============================
    @Test
    void testCreateReview_BookingNotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(RuntimeException.class, () ->
                reviewService.createReview(1L, 5, "Good")
        );

        assertEquals("Booking not found", ex.getMessage());
    }

    // ==============================
    // 3. Booking chưa completed
    // ==============================
    @Test
    void testCreateReview_NotCompleted() {
        booking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        Exception ex = assertThrows(RuntimeException.class, () ->
                reviewService.createReview(1L, 5, "Good")
        );

        assertEquals("Chỉ được đánh giá khi trận đấu đã hoàn tất.", ex.getMessage());
    }

    // ==============================
    // 4. Đã review rồi
    // ==============================
    @Test
    void testCreateReview_AlreadyReviewed() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBookingId(1L)).thenReturn(true);

        Exception ex = assertThrows(RuntimeException.class, () ->
                reviewService.createReview(1L, 5, "Good")
        );

        assertEquals("Bạn đã đánh giá trận đấu này rồi.", ex.getMessage());
    }

    // ==============================
    // 5. Test hasReviewed
    // ==============================
    @Test
    void testHasReviewed() {
        when(reviewRepository.existsByBookingId(1L)).thenReturn(true);

        boolean result = reviewService.hasReviewed(1L);

        assertTrue(result);
    }
}