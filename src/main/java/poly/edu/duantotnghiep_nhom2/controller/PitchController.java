
package poly.edu.duantotnghiep_nhom2.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import poly.edu.duantotnghiep_nhom2.entity.Booking;
import poly.edu.duantotnghiep_nhom2.entity.Pitch;
import poly.edu.duantotnghiep_nhom2.entity.Review;
import poly.edu.duantotnghiep_nhom2.entity.User;
import poly.edu.duantotnghiep_nhom2.entity.enums.PitchType;
import poly.edu.duantotnghiep_nhom2.service.BookingService;
import poly.edu.duantotnghiep_nhom2.service.PitchService;
import poly.edu.duantotnghiep_nhom2.service.ReviewService;
import poly.edu.duantotnghiep_nhom2.service.UserService;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/pitches") // Đổi từ /facilities sang /pitches
public class PitchController {

    private final PitchService pitchService;
    private final BookingService bookingService;
    private final UserService userService;
    private final ReviewService reviewService;

    public PitchController(PitchService pitchService, BookingService bookingService, UserService userService, ReviewService reviewService) {
        this.pitchService = pitchService;
        this.bookingService = bookingService;
        this.userService = userService;
        this.reviewService = reviewService;
    }

    // 1. Trang danh sách sân
    @GetMapping
    public String listPitches(Model model, Principal principal) {
        model.addAttribute("pitches", pitchService.getAllPitches());

        if (principal != null) {
            User user = userService.findByUsername(principal.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("hasActiveBooking", bookingService.hasActiveBooking(user.getId()));
            }
        }

        return "pitch-list";
    }

    // 2. Tìm kiếm sân
    @GetMapping("/search")
    public String searchPitches(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String date,
            Model model, Principal principal) {

        List<Pitch> results = pitchService.getAllPitches();
        if (keyword != null && !keyword.isEmpty()) {
            results = results.stream()
                    .filter(p -> p.getName().toLowerCase().contains(keyword.toLowerCase()))
                    .toList();
        }
        if (type != null && !type.isEmpty()) {
            PitchType pType = type.equals("5") ? PitchType.PITCH_5 : PitchType.PITCH_7;
            results = results.stream().filter(p -> p.getType() == pType).toList();
        }

        model.addAttribute("pitches", results);

        if (principal != null) {
            User user = userService.findByUsername(principal.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("hasActiveBooking", bookingService.hasActiveBooking(user.getId()));
            }
        }

        return "pitch-list";
    }

    // 3. Xem chi tiết sân
    @GetMapping("/{id}") // Đổi từ /pitch/{id} sang /{id} cho gọn (/pitches/1)
    public String viewPitchDetail(@PathVariable Long id, Model model, Principal principal) {
        try {
            Pitch pitch = pitchService.getPitchById(id);
            model.addAttribute("pitch", pitch);

            List<Booking> bookedSlots = pitchService.getBookedSlots(id, LocalDate.now());
            model.addAttribute("bookedSlots", bookedSlots);

            // Lấy danh sách đánh giá
            List<Review> reviews = reviewService.getReviewsByPitch(id);
            model.addAttribute("reviews", reviews);

            if (principal != null) {
                User user = userService.findByUsername(principal.getName()).orElse(null);
                if (user != null) {
                    model.addAttribute("hasActiveBooking", bookingService.hasActiveBooking(user.getId()));
                }
            }

            return "pitch-detail";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/pitches";
        }
    }
}
