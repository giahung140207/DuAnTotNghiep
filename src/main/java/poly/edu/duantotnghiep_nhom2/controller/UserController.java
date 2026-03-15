package poly.edu.duantotnghiep_nhom2.controller;

import jakarta.servlet.http.HttpSession;
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
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Vui lòng kiểm tra email để lấy mã OTP.");
            return "redirect:/verify-otp?username=" + user.getUsername();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/verify-otp")
    public String showVerifyOtpPage(@RequestParam("username") String username, Model model) {
        model.addAttribute("username", username);
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String processVerifyOtp(@RequestParam("username") String username,
                                   @RequestParam("otpCode") String otpCode,
                                   RedirectAttributes redirectAttributes) {
        try {
            userService.verifyOtp(username, otpCode);
            redirectAttributes.addFlashAttribute("success", "Kích hoạt tài khoản thành công! Bạn có thể đăng nhập.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/verify-otp?username=" + username;
        }
    }

    @PostMapping("/resend-otp")
    public String processResendOtp(@RequestParam("username") String username,
                                   RedirectAttributes redirectAttributes) {
        try {
            userService.resendOtp(username);
            redirectAttributes.addFlashAttribute("success", "Đã gửi lại mã OTP mới vào email của bạn.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/verify-otp?username=" + username;
    }

    @GetMapping("/profile")
    public String userProfile(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String username = principal.getName();
        User user = userService.findByUsername(username).orElseThrow();

        List<Booking> history = bookingService.getHistoryByUser(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("bookings", history);

        return "profile";
    }

    @GetMapping("/profile/bookings-fragment")
    public String getBookingsFragment(Model model, Principal principal) {
        if (principal == null) return "";
        String username = principal.getName();
        User user = userService.findByUsername(username).orElseThrow();
        List<Booking> history = bookingService.getHistoryByUser(user.getId());
        model.addAttribute("bookings", history);
        return "profile :: bookingTable"; 
    }

    @GetMapping("/profile/wallet-fragment")
    public String getWalletFragment(Model model, Principal principal) {
        if (principal == null) return "";
        String username = principal.getName();
        User user = userService.findByUsername(username).orElseThrow();
        model.addAttribute("user", user);
        
        List<Booking> history = bookingService.getHistoryByUser(user.getId());
        model.addAttribute("bookings", history);
        
        return "profile :: walletInfo"; 
    }

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

    // YÊU CẦU ĐỔI MẬT KHẨU (Khách Hàng)
    @PostMapping("/user/change-password")
    public String requestChangePassword(@RequestParam String oldPassword, 
                                        @RequestParam String newPassword, 
                                        @RequestParam String confirmPassword,
                                        Principal principal, 
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "redirect:/profile";
        }

        try {
            User user = userService.findByUsername(principal.getName()).orElseThrow();
            
            // 1. Kiểm tra mật khẩu cũ trước
            userService.checkOldPassword(user.getId(), oldPassword, newPassword);
            
            // 2. Lưu mật khẩu mới tạm vào Session
            session.setAttribute("TEMP_NEW_PASSWORD", newPassword);
            
            // 3. Tạo và gửi OTP
            userService.generatePasswordOtp(user.getId());
            
            redirectAttributes.addFlashAttribute("success", "Mã xác nhận đã được gửi đến email của bạn.");
            return "redirect:/verify-password-otp";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/profile";
        }
    }

    // TRANG NHẬP OTP ĐỔI MẬT KHẨU
    @GetMapping("/verify-password-otp")
    public String showVerifyPasswordOtpPage(Principal principal) {
        if (principal == null) return "redirect:/login";
        return "verify-password-otp";
    }

    // XỬ LÝ NHẬP OTP ĐỔI MẬT KHẨU
    @PostMapping("/verify-password-otp")
    public String processVerifyPasswordOtp(@RequestParam("otpCode") String otpCode,
                                           Principal principal,
                                           HttpSession session,
                                           RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";

        try {
            User user = userService.findByUsername(principal.getName()).orElseThrow();
            
            // 1. Xác thực OTP
            if (userService.verifyPasswordOtp(user.getId(), otpCode)) {
                // 2. Lấy mật khẩu mới từ Session
                String newPassword = (String) session.getAttribute("TEMP_NEW_PASSWORD");
                if (newPassword == null) {
                    throw new RuntimeException("Phiên làm việc đã hết hạn. Vui lòng thử lại.");
                }
                
                // 3. Ép đổi mật khẩu
                userService.forceChangePassword(user.getId(), newPassword);
                
                // 4. Xóa session
                session.removeAttribute("TEMP_NEW_PASSWORD");
                
                redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
                return "redirect:/profile";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/verify-password-otp";
        }
        return "redirect:/profile";
    }
    
    // GỬI LẠI OTP ĐỔI MẬT KHẨU
    @PostMapping("/resend-password-otp")
    public String resendPasswordOtp(Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        try {
            User user = userService.findByUsername(principal.getName()).orElseThrow();
            userService.resendPasswordOtp(user.getId());
            redirectAttributes.addFlashAttribute("success", "Đã gửi lại mã OTP vào email của bạn.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/verify-password-otp";
    }
}
