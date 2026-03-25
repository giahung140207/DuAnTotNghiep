package poly.edu.duantotnghiep_nhom2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import poly.edu.duantotnghiep_nhom2.entity.SupportMessage;
import poly.edu.duantotnghiep_nhom2.entity.User;
import poly.edu.duantotnghiep_nhom2.service.SupportMessageService;
import poly.edu.duantotnghiep_nhom2.service.UserService;

import java.security.Principal;
import java.util.List;

@Controller
public class SupportController {

    private final SupportMessageService supportMessageService;
    private final UserService userService;

    public SupportController(SupportMessageService supportMessageService, UserService userService) {
        this.supportMessageService = supportMessageService;
        this.userService = userService;
    }

    @GetMapping("/support")
    public String supportPage(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByUsername(principal.getName()).orElseThrow();
        List<SupportMessage> messages = supportMessageService.getMessagesByUser(user.getId());
        model.addAttribute("messages", messages);
        model.addAttribute("user", user); // THÊM USER VÀO MODEL
        return "support";
    }
    
    @GetMapping("/support/widget")
    public String supportWidget(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByUsername(principal.getName()).orElseThrow();
        List<SupportMessage> messages = supportMessageService.getMessagesByUser(user.getId());
        model.addAttribute("messages", messages);
        model.addAttribute("user", user); // THÊM USER VÀO MODEL (QUAN TRỌNG CHO WEBSOCKET)
        return "support-iframe";
    }

    // API trả về Fragment danh sách tin nhắn (cho AJAX load)
    @GetMapping("/support/messages")
    public String getMessagesFragment(Model model, Principal principal) {
        if (principal == null) return "";
        User user = userService.findByUsername(principal.getName()).orElseThrow();
        List<SupportMessage> messages = supportMessageService.getMessagesByUser(user.getId());
        model.addAttribute("messages", messages);
        model.addAttribute("user", user); // THÊM USER VÀO MODEL
        return "support-iframe :: messageList"; // Trả về fragment th:fragment="messageList"
    }

    // API gửi tin nhắn bằng AJAX
    @PostMapping("/support/send-ajax")
    @ResponseBody
    public String sendMessageAjax(@RequestParam String content, Principal principal) {
        if (principal == null) return "error";
        User user = userService.findByUsername(principal.getName()).orElseThrow();
        supportMessageService.sendMessage(user.getId(), content);
        return "success";
    }

    // Giữ lại method cũ cho form truyền thống (nếu cần)
    @PostMapping("/support/send")
    public String sendMessage(@RequestParam String content, Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByUsername(principal.getName()).orElseThrow();
        supportMessageService.sendMessage(user.getId(), content);
        return "redirect:/support/widget";
    }
}
