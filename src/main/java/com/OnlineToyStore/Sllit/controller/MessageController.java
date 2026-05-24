package com.OnlineToyStore.Sllit.controller;

import com.OnlineToyStore.Sllit.model.ContactMessage;
import com.OnlineToyStore.Sllit.model.User;
import com.OnlineToyStore.Sllit.service.EmailService;
import com.OnlineToyStore.Sllit.service.MessageService;
import com.OnlineToyStore.Sllit.util.SessionHelper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/contact")
    public String showContactForm(HttpSession session, Model model) {
        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/users/login";
        }
        ContactMessage message = new ContactMessage();
        User user = SessionHelper.getUser(session);
        message.setUsername(user.getUsername());
        message.setEmail(user.getEmail());
        model.addAttribute("message", message);
        return "message/contact";
    }

    @PostMapping("/contact")
    public String submitContactMessage(@ModelAttribute ContactMessage message,
                                       HttpSession session,
                                       Model model) {
        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/users/login";
        }

        User user = SessionHelper.getUser(session);
        message.setUserId(user.getUserId());
        message.setUsername(user.getUsername());
        message.setEmail(user.getEmail());

        String result = messageService.submitMessage(message);
        if (!"success".equals(result)) {
            model.addAttribute("message", message);
            model.addAttribute("error", result);
            return "message/contact";
        }
        emailService.notifyAdminContactMessage(
                message.getUsername(),
                message.getEmail(),
                message.getSubject(),
                message.getComment());
        return "redirect:/messages/mine?sent=true";
    }

    @GetMapping("/mine")
    public String myMessages(HttpSession session, Model model) {
        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/users/login";
        }
        User user = SessionHelper.getUser(session);
        model.addAttribute("messages", messageService.getMessagesByUser(user.getUserId()));
        return "message/mine";
    }

    @GetMapping("/admin")
    public String adminMessages(HttpSession session, Model model) {
        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        model.addAttribute("messages", messageService.getAllMessages());
        model.addAttribute("openCount", messageService.countOpenMessages());
        model.addAttribute("totalCount", messageService.countTotalMessages());
        return "message/admin";
    }

    @GetMapping("/reply/{messageId}")
    public String showReplyForm(@PathVariable String messageId,
                                HttpSession session,
                                Model model) {
        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        ContactMessage message = messageService.getMessageById(messageId);
        if (message == null) {
            return "redirect:/messages/admin";
        }
        model.addAttribute("message", message);
        return "message/reply";
    }

    @PostMapping("/reply/{messageId}")
    public String replyToMessage(@PathVariable String messageId,
                                 @RequestParam String reply,
                                 HttpSession session,
                                 Model model) {
        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }

        String result = messageService.replyToMessage(messageId, reply);
        if (!"success".equals(result)) {
            ContactMessage message = messageService.getMessageById(messageId);
            model.addAttribute("message", message);
            model.addAttribute("error", result);
            return "message/reply";
        }
        return "redirect:/messages/admin?replied=true";
    }
}
