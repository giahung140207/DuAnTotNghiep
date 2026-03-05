package poly.edu.duantotnghiep_nhom2.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.duantotnghiep_nhom2.entity.Booking;
import poly.edu.duantotnghiep_nhom2.entity.Invoice;
import poly.edu.duantotnghiep_nhom2.entity.User;
import poly.edu.duantotnghiep_nhom2.entity.enums.BookingStatus;
import poly.edu.duantotnghiep_nhom2.repository.BookingRepository;
import poly.edu.duantotnghiep_nhom2.repository.InvoiceRepository;
import poly.edu.duantotnghiep_nhom2.repository.UserRepository;
import poly.edu.duantotnghiep_nhom2.service.BookingService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingScheduler {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final BookingService bookingService;

    public BookingScheduler(BookingRepository bookingRepository, UserRepository userRepository, InvoiceRepository invoiceRepository, BookingService bookingService) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.invoiceRepository = invoiceRepository;
        this.bookingService = bookingService;
    }

    // Chạy mỗi phút một lần
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoCancelNoShowBookings() {
        LocalDateTime now = LocalDateTime.now();
        // Thời gian giới hạn: Quá 15 phút so với giờ bắt đầu
        LocalDateTime threshold = now.minusMinutes(15);

        // Tìm các đơn CONFIRMED đã quá giờ mà chưa check-in
        List<Booking> noShowBookings = bookingRepository.findExpiredConfirmedBookings(threshold, BookingStatus.CONFIRMED);

        for (Booking booking : noShowBookings) {
            if (!booking.isCheckedIn()) {
                // Xử lý hủy đơn (NO_SHOW)
                booking.setStatus(BookingStatus.CANCELLED);
                
                // Xử lý hoàn tiền 70% (Phạt 30%)
                if ("PAID".equals(booking.getPaymentStatus())) {
                    BigDecimal totalPaid = booking.getTotalPrice();
                    
                    // Tính 70% hoàn lại
                    BigDecimal refundAmount = totalPaid.multiply(new BigDecimal("0.70")).setScale(0, RoundingMode.HALF_UP);
                    
                    // Tính 30% phí phạt
                    BigDecimal cancellationFee = totalPaid.subtract(refundAmount);

                    // Cộng tiền hoàn vào ví
                    User user = booking.getUser();
                    user.setBalance(user.getBalance().add(refundAmount));
                    userRepository.save(user);
                    
                    // Tạo hóa đơn cho phí phạt (Doanh thu)
                    createCancellationInvoice(booking, cancellationFee);
                    
                    booking.setPaymentStatus("REFUNDED_PARTIAL");
                    booking.setNote("Hủy tự động do không check-in (No-show). Hoàn 70%: " + refundAmount + ". Phí 30%: " + cancellationFee);
                } else {
                    booking.setNote("Hủy tự động do không check-in (No-show).");
                }

                bookingRepository.save(booking);
                System.out.println("Auto-cancelled booking ID: " + booking.getId());
            }
        }
    }
    
    // Tự động hủy đơn PENDING quá hạn (CHỈ HỦY KHI ĐÃ HẾT GIỜ ĐÁ)
    @Scheduled(fixedRate = 300000) // 5 phút chạy 1 lần
    @Transactional
    public void autoCancelExpiredPending() {
        LocalDateTime now = LocalDateTime.now();
        
        // Tìm các đơn PENDING mà endTime < now (Đã kết thúc khung giờ mà vẫn chưa được duyệt)
        List<Booking> expiredPending = bookingRepository.findExpiredPendingBookings(now, BookingStatus.PENDING);
        
        for (Booking booking : expiredPending) {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setNote("Hủy tự động do hết giờ đá mà chưa được duyệt.");
            
            // Hoàn tiền 100% nếu đã lỡ thanh toán
            if ("PAID".equals(booking.getPaymentStatus())) {
                User user = booking.getUser();
                user.setBalance(user.getBalance().add(booking.getTotalPrice()));
                userRepository.save(user);
                booking.setPaymentStatus("REFUNDED");
            }
            bookingRepository.save(booking);
        }
    }

    // TỰ ĐỘNG KẾT THÚC ĐƠN HÀNG ĐÃ HẾT GIỜ
    @Scheduled(fixedRate = 60000) // Chạy mỗi phút
    @Transactional
    public void autoCompleteFinishedBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> finishedBookings = bookingRepository.findFinishedBookings(now, BookingStatus.CONFIRMED);

        for (Booking booking : finishedBookings) {
            try {
                bookingService.completeBooking(booking.getId());
                System.out.println("Auto-completed booking ID: " + booking.getId());
            } catch (Exception e) {
                System.err.println("Error auto-completing booking ID " + booking.getId() + ": " + e.getMessage());
            }
        }
    }

    private void createCancellationInvoice(Booking booking, BigDecimal feeAmount) {
        Invoice invoice = new Invoice();
        invoice.setBooking(booking);
        invoice.setAmount(feeAmount);
        invoice.setPaymentMethod("WALLET_DEDUCT");
        invoice.setTransactionCode("NOSHOW_FEE-" + System.currentTimeMillis());
        invoiceRepository.save(invoice);
    }
}
