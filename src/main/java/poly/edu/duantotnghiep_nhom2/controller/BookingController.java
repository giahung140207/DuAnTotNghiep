package poly.edu.duantotnghiep_nhom2.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.duantotnghiep_nhom2.entity.Booking;
import poly.edu.duantotnghiep_nhom2.entity.Pitch;
import poly.edu.duantotnghiep_nhom2.entity.User;
import poly.edu.duantotnghiep_nhom2.service.BookingService;
import poly.edu.duantotnghiep_nhom2.service.PitchService;
import poly.edu.duantotnghiep_nhom2.service.UserService;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;
    private final PitchService pitchService;

    public BookingController(BookingService bookingService, UserService userService, PitchService pitchService) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.pitchService = pitchService;
    }

    @PostMapping("/create")
    public String createBooking(
            @RequestParam Long pitchId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
            @RequestParam Integer duration,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/login";
        }

        try {
            String username = principal.getName();
            User user = userService.findByUsername(username).orElseThrow();

            LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
            
            // LOGIC MỚI: XÁC ĐỊNH GIỜ KẾT THÚC CỦA KHUNG GIỜ (SLOT END TIME)
            // Các khung: 7-9, 10-12, 14-16, 18-20
            LocalDateTime slotEndTime;
            int hour = startTime.getHour();
            if (hour == 7) slotEndTime = LocalDateTime.of(date, LocalTime.of(9, 0));
            else if (hour == 10) slotEndTime = LocalDateTime.of(date, LocalTime.of(12, 0));
            else if (hour == 14) slotEndTime = LocalDateTime.of(date, LocalTime.of(16, 0));
            else if (hour == 18) slotEndTime = LocalDateTime.of(date, LocalTime.of(20, 0));
            else {
                // Nếu chọn giờ lạ (không trong khung), mặc định cộng duration
                slotEndTime = startDateTime.plusMinutes(duration);
            }

            // Gọi Service (Service sẽ tự động xử lý nếu startDateTime < now, ví dụ dời startDateTime về now)
            // LƯU Ý: Phải cho phép Service cắt luôn thời gian kết thúc (endDateTime) nếu nó vượt quá slotEndTime
            
            // Tính giờ kết thúc dự kiến ban đầu (Có thể bị lố)
            LocalDateTime expectedEndDateTime = startDateTime.plusMinutes(duration);

            // Chặn không cho endDateTime vượt quá giới hạn của ca đá
            LocalDateTime finalEndDateTime = expectedEndDateTime.isAfter(slotEndTime) ? slotEndTime : expectedEndDateTime;

            bookingService.createBooking(user.getId(), pitchId, startDateTime, finalEndDateTime);

            redirectAttributes.addFlashAttribute("success", "Đặt sân thành công! Vui lòng chờ xác nhận.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pitches/" + pitchId;
        }

        return "redirect:/profile";
    }

    @GetMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        try {
            User user = userService.findByUsername(principal.getName()).orElseThrow();
            bookingService.cancelBooking(id, user.getId());
            redirectAttributes.addFlashAttribute("success", "Đã hủy đặt sân thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/extend")
    public String extendBooking(@RequestParam Long bookingId, @RequestParam int extraMinutes, RedirectAttributes redirectAttributes) {
        try {
            bookingService.extendBooking(bookingId, extraMinutes);
            redirectAttributes.addFlashAttribute("success", "Gia hạn thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }

    // Trang chọn sân để đổi (Khách hàng)
    @GetMapping("/swap-options/{id}")
    public String swapOptions(@PathVariable Long id, Model model, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        try {
            Booking currentBooking = bookingService.getBookingById(id);
            
            // Tìm các sân trống cùng khung giờ (Bỏ qua facilityId)
            List<Pitch> availablePitches = pitchService.findAvailablePitches(
                    null, // facilityId = null
                    currentBooking.getPitch().getType(),
                    currentBooking.getStartTime(),
                    currentBooking.getEndTime()
            );
            // Loại bỏ sân hiện tại
            availablePitches.removeIf(p -> p.getId().equals(currentBooking.getPitch().getId()));
            
            model.addAttribute("currentBooking", currentBooking);
            model.addAttribute("availablePitches", availablePitches);
            return "swap-options"; // Tạo file template mới cho khách
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/profile";
        }
    }

    // Xử lý yêu cầu đổi sân (Khách hàng)
    @PostMapping("/swap")
    public String requestSwap(@RequestParam Long bookingId, @RequestParam Long newPitchId, RedirectAttributes redirectAttributes) {
        try {
            bookingService.requestSwapPitch(bookingId, newPitchId);
            redirectAttributes.addFlashAttribute("success", "Đã gửi yêu cầu đổi sân. Vui lòng chờ Admin duyệt.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/profile";
    }
}
