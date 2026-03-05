package poly.edu.duantotnghiep_nhom2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.duantotnghiep_nhom2.entity.Invoice;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByBookingId(Long bookingId);

    // Sửa ORDER BY createdAt -> ORDER BY paymentTime
    @Query("SELECT i FROM Invoice i " +
           "WHERE (:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(i.transactionCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.booking.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY i.paymentTime DESC")
    List<Invoice> searchInvoices(@Param("keyword") String keyword);

    // Tính tổng doanh thu theo tháng (Dựa trên ngày thanh toán)
    @Query("SELECT SUM(i.amount) " +
           "FROM Invoice i " +
           "WHERE function('MONTH', i.paymentTime) = :month AND function('YEAR', i.paymentTime) = :year")
    BigDecimal calculateTotalRevenueByMonth(@Param("month") int month, @Param("year") int year);
}
