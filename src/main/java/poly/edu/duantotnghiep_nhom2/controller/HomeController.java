package poly.edu.duantotnghiep_nhom2.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.duantotnghiep_nhom2.repository.PitchRepository;
import poly.edu.duantotnghiep_nhom2.service.ReviewService;
import poly.edu.duantotnghiep_nhom2.service.UserService;

@Controller
public class HomeController {

    private final PitchRepository pitchRepository;
    private final ReviewService reviewService;
    private final UserService userService;

    public HomeController(PitchRepository pitchRepository, ReviewService reviewService, UserService userService) {
        this.pitchRepository = pitchRepository;
        this.reviewService = reviewService;
        this.userService = userService;
    }

    // Map cả "/" và "/home" về cùng một trang
    @GetMapping({"/", "/home"})
    public String home(Model model, Authentication authentication) {
        // KIỂM TRA QUYỀN ADMIN ĐỂ REDIRECT
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("ROLE_ADMIN"));
            if (isAdmin) {
                return "redirect:/admin/dashboard";
            }
        }

        try {
            // Lấy 6 sân được đặt nhiều nhất
            model.addAttribute("featuredPitches", pitchRepository.findTopPitchesByBookings(PageRequest.of(0, 6)));
            
            // Lấy đánh giá mới nhất
            model.addAttribute("latestReviews", reviewService.getLatestReviews());
        } catch (Exception e) {
            e.printStackTrace(); 
        }
        
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // --- QUÊN MẬT KHẨU ---

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes, Model model) {
        try {
            // Gọi hàm tạo OTP và gửi qua Email
            userService.generateForgotPasswordOtp(email);
            model.addAttribute("email", email);
            model.addAttribute("success", "Mã xác nhận đã được gửi đến email của bạn.");
            return "reset-password-otp"; // Trả về view nhập OTP thay vì redirect để giữ được email
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/forgot-password";
        }
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("email") String email,
                                       @RequestParam("otpCode") String otpCode,
                                       @RequestParam("newPassword") String newPassword,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
            model.addAttribute("email", email);
            return "reset-password-otp";
        }

        try {
            // Xác thực OTP và đặt lại mật khẩu
            userService.verifyAndResetPassword(email, otpCode, newPassword);
            redirectAttributes.addFlashAttribute("success", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("email", email);
            return "reset-password-otp";
        }
    }
}
