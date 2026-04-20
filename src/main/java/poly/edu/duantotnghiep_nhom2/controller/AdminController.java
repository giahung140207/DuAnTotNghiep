package poly.edu.duantotnghiep_nhom2.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.duantotnghiep_nhom2.config.VnPayConfig;
import poly.edu.duantotnghiep_nhom2.dto.AdminNotificationDTO;
import poly.edu.duantotnghiep_nhom2.dto.CalendarEventDTO;
import poly.edu.duantotnghiep_nhom2.entity.Booking;
import poly.edu.duantotnghiep_nhom2.entity.Pitch;
import poly.edu.duantotnghiep_nhom2.entity.User;
import poly.edu.duantotnghiep_nhom2.entity.enums.BookingStatus;
import poly.edu.duantotnghiep_nhom2.repository.BookingRepository;
import poly.edu.duantotnghiep_nhom2.repository.InvoiceRepository;
import poly.edu.duantotnghiep_nhom2.repository.SupportMessageRepository;
import poly.edu.duantotnghiep_nhom2.service.*;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final DashboardService dashboardService;
    private final PitchService pitchService;
    private final UserService userService;
    private final BookingService bookingService;
    private final SupportMessageService supportMessageService;
    private final SupportMessageRepository supportMessageRepository;
    private final BookingRepository bookingRepository;
    private final InvoiceRepository invoiceRepository;
    private final VnPayConfig vnPayConfig;

    public AdminController(DashboardService dashboardService, PitchService pitchService, UserService userService, BookingService bookingService, SupportMessageService supportMessageService, SupportMessageRepository supportMessageRepository, BookingRepository bookingRepository, InvoiceRepository invoiceRepository, VnPayConfig vnPayConfig) {
        this.dashboardService = dashboardService;
        this.pitchService = pitchService;
        this.userService = userService;
        this.bookingService = bookingService;
        this.supportMessageService = supportMessageService;
        this.supportMessageRepository = supportMessageRepository;
        this.bookingRepository = bookingRepository;
        this.invoiceRepository = invoiceRepository;
        this.vnPayConfig = vnPayConfig;
    }

    @ModelAttribute("unreadCount")
    public long getUnreadCount() {
        return supportMessageRepository.countUnreadMessages();
    }

    @ModelAttribute("pendingBookingCount")
    public long getPendingBookingCount() {
        return bookingRepository.countByStatus(BookingStatus.PENDING);
    }

    @GetMapping("/api/notifications")
    @ResponseBody
    public AdminNotificationDTO getNotifications() {
        long unread = supportMessageRepository.countUnreadMessages();
        long pending = bookingRepository.countByStatus(BookingStatus.PENDING);
        return new AdminNotificationDTO(unread, pending);
    }

    // API for Calendar
    @GetMapping("/api/bookings")
    @ResponseBody
    public List<CalendarEventDTO> getBookingsForCalendar(@RequestParam("start") String start, @RequestParam("end") String end) {
        String cleanStart = start.split("\\+")[0]; 
        String cleanEnd = end.split("\\+")[0];
        LocalDateTime startDate = LocalDateTime.parse(cleanStart, DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime endDate = LocalDateTime.parse(cleanEnd, DateTimeFormatter.ISO_DATE_TIME);
        
        List<Booking> bookings = bookingService.findBookingsByDateRange(startDate, endDate);
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);

        return bookings.stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED && b.getStatus() != BookingStatus.ADMINCANCELLED)
                .map(booking -> {
                    String title = booking.getPitch().getName() + " - " + booking.getUser().getFullName();
                    String color;
                    String textColor = "#0a192f"; 
                    switch (booking.getStatus()) {
                        case PENDING: color = "#f39c12"; break;
                        case CONFIRMED: color = "#64ffda"; break;
                        case COMPLETED: color = "#95a5a6"; break;
                        default: color = "#e74c3c"; break;
                    }
                    String description = "Khách: " + booking.getUser().getFullName() + "<br>SĐT: " + booking.getUser().getPhone() + "<br>Tổng tiền: " + booking.getTotalPrice().toPlainString() + " đ";
                    return new CalendarEventDTO(title, booking.getStartTime().toString(), booking.getEndTime().toString(), color, color, textColor, description);
                }).collect(Collectors.toList());
    }

    // Dashboard
    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model) {
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();
        model.addAttribute("monthlyRevenue", dashboardService.getMonthlyRevenue(currentMonth, currentYear));
        model.addAttribute("totalPitches", pitchService.getAllPitches().size());
        model.addAttribute("bookingsToday", dashboardService.countBookingsToday());
        model.addAttribute("chartData", dashboardService.getRevenueLast6Months());
        model.addAttribute("chartLabels", dashboardService.getMonthLabels());
        
        // Thêm dữ liệu biểu đồ tròn
        model.addAttribute("pitchTypeStats", dashboardService.getPitchTypeStats());
        
        return "admin/dashboard";
    }

    // Pitch Management
    @GetMapping("/pitches")
    public String managePitches(Model model) {
        model.addAttribute("pitches", pitchService.getAllPitches());
        model.addAttribute("newPitch", new Pitch());
        model.addAttribute("liveStatus", pitchService.getLiveStatus());
        model.addAttribute("customers", userService.getAllCustomers());
        return "admin/pitches";
    }

    @PostMapping("/pitches/save")
    public String savePitch(@ModelAttribute Pitch pitch, RedirectAttributes redirectAttributes) {
        try {
            pitchService.savePitch(pitch);
            redirectAttributes.addFlashAttribute("success", "Đã lưu thông tin sân thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/pitches";
    }

    @GetMapping("/pitches/delete/{id}")
    public String deletePitch(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            pitchService.deletePitch(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa sân thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa sân này (có thể đang có booking).");
        }
        return "redirect:/admin/pitches";
    }

    @PostMapping("/bookings/create")
    public String adminCreateBooking(
            @RequestParam Long userId,
            @RequestParam Long pitchId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
            @RequestParam Integer duration,
            RedirectAttributes redirectAttributes) {
        try {
            LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
            LocalDateTime endDateTime = startDateTime.plusMinutes(duration);
            Booking booking = bookingService.createBooking(userId, pitchId, startDateTime, endDateTime);
            // Không tự động approve nữa, để PENDING
            // bookingService.approveBooking(booking.getId()); 
            redirectAttributes.addFlashAttribute("success", "Đã đặt sân thành công cho khách hàng (Chờ duyệt).");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi đặt sân: " + e.getMessage());
        }
        return "redirect:/admin/pitches";
    }

    // Booking Management
    @GetMapping("/bookings")
    public String manageBookings(Model model) {
        model.addAttribute("pendingBookings", bookingService.getPendingBookings());
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        model.addAttribute("activeBookings", bookingService.findBookingsByDateRange(startOfDay, endOfDay));
        model.addAttribute("now", LocalDateTime.now()); // Truyền thời gian hiện tại vào model
        return "admin/bookings";
    }

    @GetMapping("/bookings/approve/{id}")
    public String approveBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.approveBooking(id);
            redirectAttributes.addFlashAttribute("success", "Đã duyệt đơn #" + id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/bookings";
    }

    @GetMapping("/bookings/reject/{id}")
    public String rejectBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.rejectBooking(id);
            redirectAttributes.addFlashAttribute("success", "Đã từ chối đơn #" + id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/bookings";
    }

    @PostMapping("/bookings/extend")
    public String adminExtendBooking(@RequestParam Long bookingId, @RequestParam int extraMinutes, RedirectAttributes redirectAttributes) {
        try {
            bookingService.extendBooking(bookingId, extraMinutes);
            redirectAttributes.addFlashAttribute("success", "Đã gia hạn thêm " + extraMinutes + " phút.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi gia hạn: " + e.getMessage());
        }
        return "redirect:/admin/pitches";
    }

    @GetMapping("/bookings/swap-options/{id}")
    public String adminSwapOptions(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Booking currentBooking = bookingService.getBookingById(id);
            // SỬA LẠI: Bỏ tham số facilityId (truyền null hoặc bỏ hẳn)
            List<Pitch> availablePitches = pitchService.findAvailablePitches(
                    null, // facilityId = null
                    currentBooking.getPitch().getType(),
                    currentBooking.getStartTime(),
                    currentBooking.getEndTime()
            );
            availablePitches.removeIf(p -> p.getId().equals(currentBooking.getPitch().getId()));
            model.addAttribute("currentBooking", currentBooking);
            model.addAttribute("availablePitches", availablePitches);
            return "admin/swap-options"; 
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/pitches";
        }
    }

    @PostMapping("/bookings/swap")
    public String adminSwapPitch(@RequestParam Long bookingId, @RequestParam Long newPitchId, RedirectAttributes redirectAttributes) {
        try {
            bookingService.swapPitch(bookingId, newPitchId);
            redirectAttributes.addFlashAttribute("success", "Đã đổi sân thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi đổi sân: " + e.getMessage());
        }
        return "redirect:/admin/pitches";
    }

    // Finish Booking Early
    @GetMapping("/bookings/finish/{id}")
    public String finishBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.completeBooking(id);
            redirectAttributes.addFlashAttribute("success", "Đã kết thúc trận đấu và tạo hóa đơn.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/pitches";
    }

    // Check-in Ticket
    @PostMapping("/bookings/checkin")
    public String checkInTicket(@RequestParam String ticketCode, RedirectAttributes redirectAttributes) {
        try {
            Booking booking = bookingService.checkInTicket(ticketCode.trim().toUpperCase());
            redirectAttributes.addFlashAttribute("success", "Check-in thành công! Khách: " + booking.getUser().getFullName() + " - Sân: " + booking.getPitch().getName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi Check-in: " + e.getMessage());
        }
        return "redirect:/admin/bookings";
    }

    // User Management
    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userService.getAllCustomers());
        return "admin/users";
    }

    // THÊM USER
    @PostMapping("/users/create")
    public String createUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            userService.register(user); // Dùng hàm register để mã hóa pass và set role
            redirectAttributes.addFlashAttribute("success", "Thêm khách hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // SỬA USER (KHÔNG CÒN CHO PHÉP ĐỔI MẬT KHẨU TỪ ĐÂY NỮA)
    @PostMapping("/users/update")
    public String updateUser(@RequestParam Long id, 
                             @RequestParam String fullName, 
                             @RequestParam String email, 
                             @RequestParam String phone,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(id);
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPhone(phone);
            // Đã xóa phần xử lý đổi mật khẩu ở đây
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // XÓA USER (Thực chất nên là khóa, nhưng ở đây xóa tạm)
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Cần thêm hàm delete trong UserService
            // userService.deleteUser(id); 
            redirectAttributes.addFlashAttribute("error", "Chức năng xóa đang phát triển (Cần xử lý ràng buộc khóa ngoại).");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/topup")
    public String topUpUser(@RequestParam Long userId, 
                            @RequestParam BigDecimal amount, 
                            @RequestParam String paymentMethod,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {
        try {
            if ("CASH".equals(paymentMethod)) {
                userService.topUpBalance(userId, amount);
                redirectAttributes.addFlashAttribute("success", "Đã nạp " + amount + " (Tiền mặt) cho khách hàng.");
                return "redirect:/admin/users";
            } else {
                // Create VNPAY URL
                String vnp_Version = "2.1.0";
                String vnp_Command = "pay";
                String vnp_OrderInfo = "Admin nap tien cho user " + userId + ": " + amount;
                String orderType = "other";
                String vnp_TxnRef = VnPayConfig.getRandomNumber(8);
                String vnp_IpAddr = VnPayConfig.getIpAddress(request);
                String vnp_TmnCode = vnPayConfig.getVnp_TmnCode();

                long amountVnp = amount.longValue() * 100;
                
                Map<String, String> vnp_Params = new HashMap<>();
                vnp_Params.put("vnp_Version", vnp_Version);
                vnp_Params.put("vnp_Command", vnp_Command);
                vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
                vnp_Params.put("vnp_Amount", String.valueOf(amountVnp));
                vnp_Params.put("vnp_CurrCode", "VND");
                vnp_Params.put("vnp_BankCode", "NCB");
                vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
                vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
                vnp_Params.put("vnp_OrderType", orderType);
                vnp_Params.put("vnp_Locale", "vn");
                // SỬA RETURN URL ĐỂ QUAY VỀ TRANG ADMIN/USERS
                vnp_Params.put("vnp_ReturnUrl", "http://localhost:8080/admin/vnpay-return"); 
                vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

                Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                String vnp_CreateDate = formatter.format(cld.getTime());
                vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
                
                cld.add(Calendar.MINUTE, 15);
                String vnp_ExpireDate = formatter.format(cld.getTime());
                vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
                
                List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
                Collections.sort(fieldNames);
                StringBuilder hashData = new StringBuilder();
                StringBuilder query = new StringBuilder();
                Iterator<String> itr = fieldNames.iterator();
                while (itr.hasNext()) {
                    String fieldName = itr.next();
                    String fieldValue = vnp_Params.get(fieldName);
                    if ((fieldValue != null) && (fieldValue.length() > 0)) {
                        hashData.append(fieldName);
                        hashData.append('=');
                        hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                        query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                        query.append('=');
                        query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                        if (itr.hasNext()) {
                            query.append('&');
                            hashData.append('&');
                        }
                    }
                }
                String queryUrl = query.toString();
                String vnp_SecureHash = VnPayConfig.hmacSHA512(vnPayConfig.getVnp_HashSecret(), hashData.toString());
                queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
                String paymentUrl = vnPayConfig.getVnp_Url() + "?" + queryUrl;
                
                return "redirect:" + paymentUrl;
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    // Xử lý VNPAY Return cho Admin
    @GetMapping("/vnpay-return")
    public String adminVnpayReturn(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = URLEncoder.encode(params.nextElement(), StandardCharsets.US_ASCII);
            String fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if (fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }
        
        String signValue = VnPayConfig.hmacSHA512(vnPayConfig.getVnp_HashSecret(), buildHashData(fields));
        
        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(request.getParameter("vnp_ResponseCode"))) {
                // Lấy userId từ vnp_OrderInfo (Admin nap tien cho user 123: ...)
                String orderInfo = request.getParameter("vnp_OrderInfo");
                try {
                    // Parse userId từ chuỗi orderInfo
                    String[] parts = orderInfo.split(" ");
                    Long userId = Long.parseLong(parts[5].replace(":", "")); // Giả sử format đúng như trên
                    
                    long amount = Long.parseLong(request.getParameter("vnp_Amount")) / 100;
                    userService.topUpBalance(userId, BigDecimal.valueOf(amount));
                    
                    redirectAttributes.addFlashAttribute("success", "Nạp thành công " + amount + " VNĐ qua VNPAY.");
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("error", "Lỗi xử lý kết quả thanh toán: " + e.getMessage());
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Giao dịch thất bại hoặc bị hủy.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Chữ ký không hợp lệ!");
        }
        return "redirect:/admin/users";
    }

    private String buildHashData(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                sb.append(fieldName);
                sb.append('=');
                sb.append(fieldValue);
            }
            if (itr.hasNext()) {
                sb.append('&');
            }
        }
        return sb.toString();
    }

    // Invoice Management (CẬP NHẬT TÌM KIẾM)
    @GetMapping("/invoices")
    public String manageInvoices(Model model, @RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            model.addAttribute("invoices", invoiceRepository.searchInvoices(keyword));
            model.addAttribute("keyword", keyword);
        } else {
            model.addAttribute("invoices", invoiceRepository.searchInvoices(null)); // Lấy tất cả, sắp xếp mới nhất
        }
        return "admin/invoices";
    }

    // Customer Support (Chat)
    @GetMapping("/support")
    public String supportCenter(Model model, @RequestParam(required = false) Long userId) {
        model.addAttribute("users", supportMessageService.getUsersWithSupportRequest());
        if (userId != null) {
            supportMessageService.markAsRead(userId);
            model.addAttribute("selectedUser", userService.getUserById(userId));
            // THÊM DÒNG NÀY ĐỂ LẤY TIN NHẮN
            model.addAttribute("messages", supportMessageService.getMessagesByUser(userId));
        }
        return "admin/support";
    }

    @GetMapping("/chat-widget")
    public String adminChatWidget(Model model, @RequestParam Long userId) {
        supportMessageService.markAsRead(userId);
        model.addAttribute("messages", supportMessageService.getMessagesByUser(userId));
        model.addAttribute("targetUserId", userId);
        return "admin/admin-chat-iframe";
    }

    // API TRẢ VỀ FRAGMENT TIN NHẮN CHO ADMIN SUPPORT CHÍNH
    @GetMapping("/chat-messages")
    public String getAdminMessagesFragment(Model model, @RequestParam Long userId) {
        supportMessageService.markAsRead(userId);
        model.addAttribute("messages", supportMessageService.getMessagesByUser(userId));
        return "admin/support :: messageList";
    }

    // API TRẢ VỀ FRAGMENT TIN NHẮN CHO IFRAME NHỎ (Ở CÁC TRANG KHÁC)
    @GetMapping("/chat-messages-iframe")
    public String getAdminMessagesFragmentIframe(Model model, @RequestParam Long userId) {
        supportMessageService.markAsRead(userId);
        model.addAttribute("messages", supportMessageService.getMessagesByUser(userId));
        return "admin/admin-chat-iframe :: messageList";
    }


    @PostMapping("/chat-reply-ajax")
    @ResponseBody
    public String replyMessageAjax(@RequestParam Long userId, @RequestParam String content, Principal principal) {
        User admin = userService.findByUsername(principal.getName()).orElseThrow();
        supportMessageService.replyMessage(userId, admin.getId(), content);
        return "success";
    }

    @GetMapping("/support/end/{userId}")
    public String endSupport(@PathVariable Long userId) {
        supportMessageService.endSupport(userId);
        return "redirect:/admin/support";
    }
}