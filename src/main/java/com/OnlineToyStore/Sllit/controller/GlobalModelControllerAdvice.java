package com.OnlineToyStore.Sllit.controller;

import com.OnlineToyStore.Sllit.model.User;
import com.OnlineToyStore.Sllit.service.CartService;
import com.OnlineToyStore.Sllit.util.SessionHelper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelControllerAdvice {

    @Autowired
    private CartService cartService;

    @ModelAttribute
    public void addSessionAttributes(HttpSession session, Model model) {
        User user = SessionHelper.getUser(session);
        boolean isLoggedIn = user != null;
        boolean isAdmin = SessionHelper.isAdmin(session);
        boolean isSuperAdmin = SessionHelper.isSuperAdmin(session);
        boolean isCustomer = SessionHelper.isCustomer(session);

        model.addAttribute("loggedInUser", user);
        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isSuperAdmin", isSuperAdmin);
        model.addAttribute("isCustomer", isCustomer);
        model.addAttribute("cartItemCount",
                isCustomer ? cartService.getCartItemCount(user.getUserId()) : 0);
    }
}
