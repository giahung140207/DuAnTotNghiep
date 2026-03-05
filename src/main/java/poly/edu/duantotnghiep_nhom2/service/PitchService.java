package poly.edu.duantotnghiep_nhom2.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.duantotnghiep_nhom2.dto.PitchStatusDTO;
import poly.edu.duantotnghiep_nhom2.entity.Booking;
import poly.edu.duantotnghiep_nhom2.entity.Pitch;
import poly.edu.duantotnghiep_nhom2.entity.enums.BookingStatus;
import poly.edu.duantotnghiep_nhom2.entity.enums.PitchStatus;
import poly.edu.duantotnghiep_nhom2.entity.enums.PitchType;
import poly.edu.duantotnghiep_nhom2.repository.BookingRepository;
import poly.edu.duantotnghiep_nhom2.repository.PitchRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PitchService {

    private final PitchRepository pitchRepository;
    private final BookingRepository bookingRepository;

    // THÊM SWAPPED VÀO DANH SÁCH LOẠI TRỪ
    private static final List<BookingStatus> EXCLUDED_STATUSES = Arrays.asList(BookingStatus.CANCELLED, BookingStatus.REFUNDED, BookingStatus.SWAPPED);

    public PitchService(PitchRepository pitchRepository, BookingRepository bookingRepository) {
        this.pitchRepository = pitchRepository;
        this.bookingRepository = bookingRepository;
    }

    public Pitch getPitchById(Long id) {
        return pitchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sân ID: " + id));
    }

    public List<Pitch> getAllPitches() {
        return pitchRepository.findAll();
    }

    public List<Pitch> findAvailablePitches(Long facilityId, PitchType type, LocalDateTime start, LocalDateTime end) {
        // SỬA LẠI: Bỏ tham số facilityId trong query
        List<Pitch> candidates = pitchRepository.findByTypeAndStatus(type, PitchStatus.ACTIVE);
        return candidates.stream()
                .filter(pitch -> !bookingRepository.existsByPitchIdAndOverlapTime(pitch.getId(), start, end, EXCLUDED_STATUSES))
                .collect(Collectors.toList());
    }

    public List<Booking> getBookedSlots(Long pitchId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        List<Booking> bookings = bookingRepository.findBookingsByPitchAndDate(pitchId, startOfDay, endOfDay, EXCLUDED_STATUSES);

        if (date.isEqual(LocalDate.now())) {
            LocalDateTime now = LocalDateTime.now();
            return bookings.stream()
                    .filter(b -> b.getEndTime().isAfter(now))
                    .collect(Collectors.toList());
        }
        return bookings;
    }

    @Transactional
    public void savePitch(Pitch pitch) {
        if (pitch.getId() != null) {
            boolean hasActiveBooking = bookingRepository.existsActiveBookingForPitch(pitch.getId(), LocalDateTime.now());
            if (hasActiveBooking) {
                throw new RuntimeException("Không thể sửa thông tin sân này vì đang có lịch đặt chưa hoàn thành!");
            }
        }
        
        // Set default images if null
        if (pitch.getImageUrl() == null || pitch.getImageUrl().isEmpty()) {
            pitch.setImageUrl("https://images.unsplash.com/photo-1575361204480-aadea25e6e68?q=80&w=2071&auto=format&fit=crop");
        }
        if (pitch.getSurfaceImageUrl() == null || pitch.getSurfaceImageUrl().isEmpty()) {
            pitch.setSurfaceImageUrl("https://images.unsplash.com/photo-1529900748604-07564a03e7a6?q=80&w=2070&auto=format&fit=crop");
        }
        if (pitch.getBenchImageUrl() == null || pitch.getBenchImageUrl().isEmpty()) {
            pitch.setBenchImageUrl("https://images.unsplash.com/photo-1518605348416-72580200d3f8?q=80&w=2070&auto=format&fit=crop");
        }

        pitchRepository.save(pitch);
    }

    @Transactional
    public void deletePitch(Long id) {
        boolean hasActiveBooking = bookingRepository.existsActiveBookingForPitch(id, LocalDateTime.now());
        if (hasActiveBooking) {
            throw new RuntimeException("Không thể xóa sân này vì đang có lịch đặt chưa hoàn thành!");
        }
        pitchRepository.deleteById(id);
    }

    public List<PitchStatusDTO> getLiveStatus() {
        List<Pitch> allPitches = pitchRepository.findAll();
        List<PitchStatusDTO> statusList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Pitch pitch : allPitches) {
            if (pitch.getStatus() == PitchStatus.MAINTENANCE) {
                statusList.add(new PitchStatusDTO(pitch, "MAINTENANCE", null, 0));
                continue;
            }

            List<Booking> activeBookings = bookingRepository.findBookingsByPitchAndDate(pitch.getId(), now.minusHours(3), now.plusHours(3), EXCLUDED_STATUSES);
            
            Booking current = activeBookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.CONFIRMED && 
                                 b.getStartTime().isBefore(now) && 
                                 b.getEndTime().isAfter(now))
                    .findFirst().orElse(null);

            if (current != null) {
                long minutesLeft = Duration.between(now, current.getEndTime()).toMinutes();
                statusList.add(new PitchStatusDTO(pitch, "OCCUPIED", current, minutesLeft));
            } else {
                statusList.add(new PitchStatusDTO(pitch, "FREE", null, 0));
            }
        }
        return statusList;
    }
}
