package poly.edu.duantotnghiep_nhom2.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.duantotnghiep_nhom2.entity.Pitch;
import poly.edu.duantotnghiep_nhom2.entity.enums.PitchStatus;
import poly.edu.duantotnghiep_nhom2.entity.enums.PitchType;

import java.util.List;

@Repository
public interface PitchRepository extends JpaRepository<Pitch, Long> {

    // ĐÃ XÓA findByFacilityId

    // SỬA LẠI: Bỏ facilityId
    @Query("SELECT p FROM Pitch p WHERE p.type = :type AND p.status = :status")
    List<Pitch> findByTypeAndStatus(@Param("type") PitchType type, 
                                    @Param("status") PitchStatus status);

    // Tìm top sân được đặt nhiều nhất
    @Query("SELECT p FROM Pitch p " +
           "LEFT JOIN Booking b ON p.id = b.pitch.id " +
           "GROUP BY p " +
           "ORDER BY COUNT(b) DESC")
    List<Pitch> findTopPitchesByBookings(Pageable pageable);
}
