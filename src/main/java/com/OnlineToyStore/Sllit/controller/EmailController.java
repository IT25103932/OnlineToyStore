package com.OnlineToyStore.Sllit.controller;

import com.OnlineToyStore.Sllit.model.User;
import com.OnlineToyStore.Sllit.service.EmailService;
import com.OnlineToyStore.Sllit.service.UserService;
import com.OnlineToyStore.Sllit.util.SessionHelper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @GetMapping("/promotions")
    public String showPromotionForm(HttpSession session, Model model) {
        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        model.addAttribute("customerCount", getCustomerRecipients().size());
        return "admin/promotions";
    }

    @PostMapping("/promotions")
    public String sendPromotion(@RequestParam String subject,
                                @RequestParam String message,
                                HttpSession session,
                                Model model) {
        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        if (subject == null || subject.trim().isEmpty()
                || message == null || message.trim().isEmpty()) {
            model.addAttribute("error", "Subject and message are required.");
            model.addAttribute("customerCount", getCustomerRecipients().size());
            return "admin/promotions";
        }
        List<User> recipients = getCustomerRecipients();
        int sentCount = emailService.sendPromotionalEmail(
                recipients,
                "ToyMart Promotion - " + subject.trim(),
                message.trim());
        model.addAttribute("success", "Promotion email queued for " + sentCount + " customer(s).");
        model.addAttribute("customerCount", recipients.size());
        return "admin/promotions";
    }

    private List<User> getCustomerRecipients() {
        return userService.getAllUsers().stream()
                .filter(user -> "CUSTOMER".equalsIgnoreCase(user.getRole()))
                .collect(Collectors.toList());
    }
}
