package poly.edu.duantotnghiep_nhom2.service;

import org.springframework.stereotype.Service;
import poly.edu.duantotnghiep_nhom2.entity.enums.BookingStatus;
import poly.edu.duantotnghiep_nhom2.entity.enums.PitchType;
import poly.edu.duantotnghiep_nhom2.repository.BookingRepository;
import poly.edu.duantotnghiep_nhom2.repository.InvoiceRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class DashboardService {

    private final BookingRepository bookingRepository;
    private final InvoiceRepository invoiceRepository;
    private static final List<BookingStatus> EXCLUDED_STATUSES = Arrays.asList(BookingStatus.CANCELLED,BookingStatus.ADMINCANCELLED);
    public DashboardService(BookingRepository bookingRepository, InvoiceRepository invoiceRepository) {
        this.bookingRepository = bookingRepository;
        this.invoiceRepository = invoiceRepository;
    }

    // Lấy doanh thu theo tháng (Dựa trên Hóa đơn - Bao gồm cả phí phạt)
    public BigDecimal getMonthlyRevenue(int month, int year) {
        BigDecimal revenue = invoiceRepository.calculateTotalRevenueByMonth(month, year);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    // Đếm số booking hôm nay
    public long countBookingsToday() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(23, 59, 59);
        return bookingRepository.countBookingsInDay(start, end, EXCLUDED_STATUSES);
    }

    // Lấy dữ liệu biểu đồ doanh thu 3 tháng gần nhất
    public List<BigDecimal> getRevenueLast6Months() {
        List<BigDecimal> data = new ArrayList<>();
        LocalDate now = LocalDate.now();
        
        for (int i = 2; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            BigDecimal revenue = getMonthlyRevenue(date.getMonthValue(), date.getYear());
            data.add(revenue);
        }
        return data;
    }
    
    // Lấy nhãn tháng cho biểu đồ
    public List<String> getMonthLabels() {
        List<String> labels = new ArrayList<>();
        LocalDate now = LocalDate.now();
        
        for (int i = 2; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            labels.add("Tháng " + date.getMonthValue());
        }
        return labels;
    }

    // Lấy tỷ lệ đặt sân theo loại (Sân 5 vs Sân 7)
    public List<Long> getPitchTypeStats() {
        List<Long> stats = new ArrayList<>();
        long count5 = bookingRepository.countByPitchType(PitchType.PITCH_5);
        long count7 = bookingRepository.countByPitchType(PitchType.PITCH_7);
        stats.add(count5);
        stats.add(count7);
        return stats;
    }
}
