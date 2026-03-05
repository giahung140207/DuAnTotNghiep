package poly.edu.duantotnghiep_nhom2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.duantotnghiep_nhom2.entity.Booking;
import poly.edu.duantotnghiep_nhom2.entity.User;
import poly.edu.duantotnghiep_nhom2.service.BookingService;
import poly.edu.duantotnghiep_nhom2.service.UserService;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
public class UserController {

    private final UserService userService;
    private final BookingService bookingService;

    public UserController(UserService userService, BookingService bookingService) {
        this.userService = userService;
        this.bookingService = bookingService;
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }
    @PostMapping("/register")
    public String processRegister(@ModelAttribute("user") User user,
                                  @RequestParam("confirmPassword") String confirmPassword,
                                  RedirectAttributes redirectAttributes) {
        try {
            if (!user.getPassword().equals(confirmPassword)) {
                throw new Exception("Mật khẩu xác nhận không khớp!");
            }
            userService.register(user);
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/profile")
    public String userProfile(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String username = principal.getName();
        User user = userService.findByUsername(username).orElseThrow();

        // Lấy lịch sử đặt sân
        List<Booking> history = bookingService.getHistoryByUser(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("bookings", history);

        return "profile";
    }

    // API trả về Fragment Bảng lịch sử (để AJAX load lại)
    @GetMapping("/profile/bookings-fragment")
    public String getBookingsFragment(Model model, Principal principal) {
        if (principal == null) return "";
        String username = principal.getName();
        User user = userService.findByUsername(username).orElseThrow();
        List<Booking> history = bookingService.getHistoryByUser(user.getId());
        model.addAttribute("bookings", history);
        return "profile :: bookingTable"; // Trả về fragment có th:fragment="bookingTable"
    }

    // API trả về Fragment Thông tin ví & Điểm (để AJAX load lại)
    @GetMapping("/profile/wallet-fragment")
    public String getWalletFragment(Model model, Principal principal) {
        if (principal == null) return "";
        String username = principal.getName();
        User user = userService.findByUsername(username).orElseThrow();
        model.addAttribute("user", user);
        
        // Cần lấy thêm booking mới nhất để hiện mã vé nếu có
        List<Booking> history = bookingService.getHistoryByUser(user.getId());
        model.addAttribute("bookings", history);
        
        return "profile :: walletInfo"; // Trả về fragment có th:fragment="walletInfo"
    }

    // Khách hàng tự nạp tiền
    @PostMapping("/user/topup")
    public String userTopUp(@RequestParam BigDecimal amount, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        
        try {
            String username = principal.getName();
            User user = userService.findByUsername(username).orElseThrow();
            
            userService.topUpBalance(user.getId(), amount);
            
            redirectAttributes.addFlashAttribute("success", "Nạp thành công " + amount + " vào ví.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi nạp tiền: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    // Cập nhật thông tin cá nhân
    @PostMapping("/user/update")
    public String updateProfile(@ModelAttribute User updatedUser, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";

        try {
            String username = principal.getName();
            User currentUser = userService.findByUsername(username).orElseThrow();
            currentUser.setFullName(updatedUser.getFullName());
            currentUser.setEmail(updatedUser.getEmail());
            currentUser.setPhone(updatedUser.getPhone());
            if (updatedUser.getAvatarUrl() != null && !updatedUser.getAvatarUrl().isEmpty()) {
                currentUser.setAvatarUrl(updatedUser.getAvatarUrl());
            }

            userService.updateUser(currentUser);

            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi cập nhật: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    // Đổi mật khẩu
    @PostMapping("/user/change-password")
    public String changePassword(@RequestParam String oldPassword, 
                                 @RequestParam String newPassword, 
                                 @RequestParam String confirmPassword,
                                 Principal principal, 
                                 RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "redirect:/profile";
        }

        try {
            String username = principal.getName();
            User user = userService.findByUsername(username).orElseThrow();
            
            userService.changePassword(user.getId(), oldPassword, newPassword);
            
            redirectAttributes.addFlashAttribute("success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }
}
