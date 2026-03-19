package poly.edu.duantotnghiep_nhom2.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import poly.edu.duantotnghiep_nhom2.entity.*;
import poly.edu.duantotnghiep_nhom2.entity.enums.BookingStatus;
import poly.edu.duantotnghiep_nhom2.entity.enums.PitchStatus;
import poly.edu.duantotnghiep_nhom2.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingTest {
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PitchRepository pitchRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private BookingService bookingService;

    private User user;
    private Pitch pitch;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setBalance(new BigDecimal("500000"));
        user.setLoyaltyPoints(0);

        pitch = new Pitch();
        pitch.setId(1L);
        pitch.setPricePerHour(new BigDecimal("100000"));
        pitch.setStatus(PitchStatus.ACTIVE);
    }

    // ✅ TEST THÀNH CÔNG
    @Test
    void testCreateBooking_Success() {
        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end = start.plusHours(2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(pitchRepository.findById(1L)).thenReturn(Optional.of(pitch));
        when(bookingRepository.existsByUserIdAndStatusIn(anyLong(), anyList())).thenReturn(false);
        when(bookingRepository.existsByPitchIdAndOverlapTime(anyLong(), any(), any(), anyList())).thenReturn(false);
        when(bookingRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Booking booking = bookingService.createBooking(1L, 1L, start, end);

        assertNotNull(booking);
        assertEquals(BookingStatus.PENDING, booking.getStatus());
        assertEquals("PAID", booking.getPaymentStatus());
        assertTrue(user.getBalance().compareTo(new BigDecimal("500000")) < 0);

        verify(bookingRepository, times(1)).save(any());
    }

    // ❌ TEST TRÙNG LỊCH
    @Test
    void testCreateBooking_OverlapTime() {
        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end = start.plusHours(2);

        when(bookingRepository.existsByUserIdAndStatusIn(anyLong(), anyList())).thenReturn(false);
        when(bookingRepository.existsByPitchIdAndOverlapTime(anyLong(), any(), any(), anyList())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                bookingService.createBooking(1L, 1L, start, end)
        );

        assertEquals("Sân đã có người đặt trong khung giờ này!", ex.getMessage());
    }

    // ❌ TEST KHÔNG ĐỦ TIỀN
    @Test
    void testCreateBooking_NotEnoughBalance() {
        user.setBalance(new BigDecimal("10000"));

        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end = start.plusHours(2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(pitchRepository.findById(1L)).thenReturn(Optional.of(pitch));
        when(bookingRepository.existsByUserIdAndStatusIn(anyLong(), anyList())).thenReturn(false);
        when(bookingRepository.existsByPitchIdAndOverlapTime(anyLong(), any(), any(), anyList())).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                bookingService.createBooking(1L, 1L, start, end)
        );

        assertEquals("Số dư ví không đủ. Vui lòng nạp thêm tiền.", ex.getMessage());
    }

    // ❌ TEST USER ĐANG CÓ BOOKING
    @Test
    void testCreateBooking_UserHasActiveBooking() {
        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end = start.plusHours(2);

        when(bookingRepository.existsByUserIdAndStatusIn(anyLong(), anyList())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                bookingService.createBooking(1L, 1L, start, end)
        );

        assertEquals("Bạn đang có đơn đặt sân chưa hoàn thành.", ex.getMessage());
    }

    // ❌ TEST SÂN BẢO TRÌ
    @Test
    void testCreateBooking_PitchMaintenance() {
        pitch.setStatus(PitchStatus.MAINTENANCE);

        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end = start.plusHours(2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(pitchRepository.findById(1L)).thenReturn(Optional.of(pitch));
        when(bookingRepository.existsByUserIdAndStatusIn(anyLong(), anyList())).thenReturn(false);
        when(bookingRepository.existsByPitchIdAndOverlapTime(anyLong(), any(), any(), anyList())).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                bookingService.createBooking(1L, 1L, start, end)
        );

        assertEquals("Sân đang bảo trì.", ex.getMessage());
    }
}
