package poly.edu.duantotnghiep_nhom2.repository;

import poly.edu.duantotnghiep_nhom2.entity.Booking;
import poly.edu.duantotnghiep_nhom2.entity.enums.BookingStatus;
import poly.edu.duantotnghiep_nhom2.entity.enums.PitchType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByTicketCode(String ticketCode);

    List<Booking> findByUserIdOrderByStartTimeDesc(Long userId);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END " +
           "FROM Booking b " +
           "WHERE b.pitch.id = :pitchId " +
           "AND b.status NOT IN :excludedStatuses " +
           "AND (b.startTime < :endTime AND b.endTime > :startTime)")
    boolean existsByPitchIdAndOverlapTime(
            @Param("pitchId") Long pitchId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludedStatuses") Collection<BookingStatus> excludedStatuses
    );

    @Query("SELECT b FROM Booking b WHERE b.startTime BETWEEN :from AND :to ORDER BY b.createdAt DESC")
    List<Booking> findBookingsByDateRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT SUM(b.totalPrice) " +
           "FROM Booking b " +
           "WHERE b.status IN :statuses " +
           "AND function('MONTH', b.startTime) = :month AND function('YEAR', b.startTime) = :year")
    BigDecimal calculateRevenueByMonth(@Param("month") int month, @Param("year") int year, @Param("statuses") Collection<BookingStatus> statuses);

    @Query("SELECT b FROM Booking b " +
           "WHERE b.pitch.id = :pitchId " +
           "AND b.startTime >= :currentTime " +
           "AND b.status NOT IN :excludedStatuses " +
           "ORDER BY b.startTime ASC")
    List<Booking> findNextBookings(@Param("pitchId") Long pitchId, 
                                   @Param("currentTime") LocalDateTime currentTime,
                                   @Param("excludedStatuses") Collection<BookingStatus> excludedStatuses);

    List<Booking> findByStatusOrderByCreatedAtDesc(BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.startTime BETWEEN :start AND :end AND b.status != :excludedStatus")
    long countBookingsInDay(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("excludedStatus") BookingStatus excludedStatus);

    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.startTime < :threshold AND b.isCheckedIn = false")
    List<Booking> findExpiredConfirmedBookings(@Param("threshold") LocalDateTime threshold, @Param("status") BookingStatus status);

    // SỬA LẠI: Tìm đơn PENDING mà endTime < now (Đã hết giờ đá mà chưa duyệt)
    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.endTime < :now")
    List<Booking> findExpiredPendingBookings(@Param("now") LocalDateTime now, @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.endTime < :now")
    List<Booking> findFinishedBookings(@Param("now") LocalDateTime now, @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b " +
           "WHERE b.pitch.id = :pitchId " +
           "AND b.startTime BETWEEN :startOfDay AND :endOfDay " +
           "AND b.status NOT IN :excludedStatuses " +
           "ORDER BY b.startTime ASC")
    List<Booking> findBookingsByPitchAndDate(@Param("pitchId") Long pitchId, 
                                             @Param("startOfDay") LocalDateTime startOfDay, 
                                             @Param("endOfDay") LocalDateTime endOfDay,
                                             @Param("excludedStatuses") Collection<BookingStatus> excludedStatuses);

    boolean existsByUserIdAndStatusIn(Long userId, Collection<BookingStatus> statuses);

    long countByStatus(BookingStatus status);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END " +
           "FROM Booking b " +
           "WHERE b.pitch.id = :pitchId " +
           "AND b.status IN (poly.edu.duantotnghiep_nhom2.entity.enums.BookingStatus.PENDING, poly.edu.duantotnghiep_nhom2.entity.enums.BookingStatus.CONFIRMED) " +
           "AND b.endTime > :now")
    boolean existsActiveBookingForPitch(@Param("pitchId") Long pitchId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.pitch.type = :type")
    long countByPitchType(@Param("type") PitchType type);
}
