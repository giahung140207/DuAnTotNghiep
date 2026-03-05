package poly.edu.duantotnghiep_nhom2.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.duantotnghiep_nhom2.entity.Booking;
import poly.edu.duantotnghiep_nhom2.entity.Invoice;
import poly.edu.duantotnghiep_nhom2.entity.enums.BookingStatus;
import poly.edu.duantotnghiep_nhom2.repository.BookingRepository;
import poly.edu.duantotnghiep_nhom2.repository.InvoiceRepository;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserService userService;

    public PaymentService(BookingRepository bookingRepository, InvoiceRepository invoiceRepository, UserService userService) {
        this.bookingRepository = bookingRepository;
        this.invoiceRepository = invoiceRepository;
        this.userService = userService;
    }

    // 1. Thanh toán bằng Ví (Wallet)
    @Transactional
    public void payByWallet(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));

        if ("PAID".equals(booking.getPaymentStatus())) return;

        // Trừ tiền
        userService.deductBalance(userId, booking.getTotalPrice());

        // Chốt đơn
        finalizePayment(booking, "WALLET", "WALLET_" + System.currentTimeMillis());
    }

    // 2. Xử lý Callback từ VNPay
    @Transactional
    public void processVnPaySuccess(Long bookingId, String transactionCode) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));
        if ("PAID".equals(booking.getPaymentStatus())) return;

        finalizePayment(booking, "VNPAY", transactionCode);
    }

    private void finalizePayment(Booking booking, String method, String transactionCode) {
        booking.setPaymentStatus("PAID");
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

       // Tạo hóa đơn
        Invoice invoice = new Invoice();
        invoice.setBooking(booking);
        invoice.setAmount(booking.getTotalPrice());
        invoice.setPaymentMethod(method);
        invoice.setTransactionCode(transactionCode);
        invoice.setPaymentTime(LocalDateTime.now());

        invoiceRepository.save(invoice);
    }
}
