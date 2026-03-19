package poly.edu.duantotnghiep_nhom2.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import poly.edu.duantotnghiep_nhom2.entity.Booking;
import poly.edu.duantotnghiep_nhom2.entity.Pitch;
import poly.edu.duantotnghiep_nhom2.entity.User;
import poly.edu.duantotnghiep_nhom2.entity.enums.BookingStatus;
import poly.edu.duantotnghiep_nhom2.repository.BookingRepository;
import poly.edu.duantotnghiep_nhom2.repository.InvoiceRepository;
import poly.edu.duantotnghiep_nhom2.repository.PitchRepository;
import poly.edu.duantotnghiep_nhom2.repository.UserRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApproveBookingTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PitchRepository pitchRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private BookingService bookingService;

    private Booking booking;
    private User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setBalance(new BigDecimal("500000"));

        booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        booking.setStatus(BookingStatus.PENDING);
        booking.setPaymentStatus("UNPAID");
        booking.setTotalPrice(new BigDecimal("200000"));
    }

    // ✅ 1. Duyệt thành công
    @Test
    void testApproveBooking_Success() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.save(any())).thenReturn(user);
        when(bookingRepository.save(any())).thenReturn(booking);

        bookingService.approveBooking(1L);

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals("PAID", booking.getPaymentStatus());
        assertTrue(user.getBalance().compareTo(new BigDecimal("500000")) < 0);

        verify(bookingRepository).save(booking);
    }

    // ❌ 2. Không đủ tiền
    @Test
    void testApproveBooking_NotEnoughBalance() {
        user.setBalance(new BigDecimal("10000"));

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                bookingService.approveBooking(1L)
        );

        assertEquals("Số dư ví khách không đủ.", ex.getMessage());
    }

    // ❌ 3. Sai trạng thái
    @Test
    void testApproveBooking_InvalidStatus() {
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                bookingService.approveBooking(1L)
        );

        assertEquals("Chỉ có thể duyệt đơn đang chờ (PENDING).", ex.getMessage());
    }

    // 🔄 4. Swap thành công
    @Test
    void testApproveBooking_Swap_Success() {
        Booking oldBooking = new Booking();
        oldBooking.setId(2L);
        oldBooking.setStatus(BookingStatus.CONFIRMED);
        oldBooking.setTotalPrice(new BigDecimal("100000"));
        oldBooking.setPaymentStatus("PAID");
        oldBooking.setTicketCode("ABC123");

        Pitch oldPitch = new Pitch();
        oldPitch.setName("Sân A");
        oldBooking.setPitch(oldPitch);

        Pitch newPitch = new Pitch();
        newPitch.setName("Sân B");
        booking.setPitch(newPitch);

        booking.setNote("Yêu cầu đổi từ đơn #2");
        booking.setTotalPrice(new BigDecimal("150000"));

        user.setBalance(new BigDecimal("500000"));

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.findById(2L)).thenReturn(Optional.of(oldBooking));
        when(userRepository.save(any())).thenReturn(user);
        when(bookingRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        bookingService.approveBooking(1L);

        assertEquals("PAID", booking.getPaymentStatus());
        assertTrue(booking.isCheckedIn());

        assertEquals(BookingStatus.SWAPPED, oldBooking.getStatus());
        assertEquals("REFUNDED", oldBooking.getPaymentStatus());

        verify(bookingRepository).save(oldBooking);
    }

    // ❌ 5. Swap không đủ tiền
    @Test
    void testApproveBooking_Swap_NotEnoughBalance() {
        Booking oldBooking = new Booking();
        oldBooking.setId(2L);
        oldBooking.setStatus(BookingStatus.CONFIRMED);
        oldBooking.setTotalPrice(new BigDecimal("100000"));

        booking.setNote("Yêu cầu đổi từ đơn #2");
        booking.setTotalPrice(new BigDecimal("300000"));

        user.setBalance(new BigDecimal("10000"));

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.findById(2L)).thenReturn(Optional.of(oldBooking));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                bookingService.approveBooking(1L)
        );

        assertTrue(ex.getMessage().contains("Số dư ví không đủ"));
    }
}