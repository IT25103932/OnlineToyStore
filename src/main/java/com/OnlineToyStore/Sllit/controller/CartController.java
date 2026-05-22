package com.OnlineToyStore.Sllit.controller;

import com.OnlineToyStore.Sllit.model.CartItem;
import com.OnlineToyStore.Sllit.model.User;
import com.OnlineToyStore.Sllit.service.CartService;
import com.OnlineToyStore.Sllit.service.ToyService;
import com.OnlineToyStore.Sllit.util.SessionHelper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private ToyService toyService;

    // ── View Cart — login required ─────────────────────
    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/users/login";
        }
        User user = SessionHelper.getUser(session);

        List<CartItem> items = cartService.getCartItems(user.getUserId());
        model.addAttribute("cartItems",  items);
        model.addAttribute("cartTotal",  cartService.getCartTotal(user.getUserId()));
        model.addAttribute("itemCount",  cartService.getCartItemCount(user.getUserId()));
        model.addAttribute("toys",       toyService.getAllToys());
        return "Cart/view";
    }

    // ── Add to Cart — login required ───────────────────
    @PostMapping("/add")
    public String addToCart(
            @RequestParam String toyId,
            @RequestParam(defaultValue = "1") int quantity,
            HttpSession session) {

        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/users/login";
        }
        User user = SessionHelper.getUser(session);
        cartService.addToCart(user.getUserId(), toyId, quantity);
        return "redirect:/cart";
    }

    // ── Update quantity ────────────────────────────────
    @PostMapping("/update")
    public String updateQuantity(
            @RequestParam String cartItemId,
            @RequestParam int quantity,
            HttpSession session) {

        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/users/login";
        }
        if (quantity <= 0) {
            cartService.removeItem(cartItemId);
        } else {
            cartService.updateQuantity(cartItemId, quantity);
        }
        return "redirect:/cart";
    }

    // ── Remove item ────────────────────────────────────
    @GetMapping("/remove/{cartItemId}")
    public String removeItem(
            @PathVariable String cartItemId,
            HttpSession session) {

        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/users/login";
        }
        cartService.removeItem(cartItemId);
        return "redirect:/cart";
    }

    // ── Clear entire cart ──────────────────────────────
    @GetMapping("/clear")
    public String clearCart(HttpSession session) {
        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/users/login";
        }
        User user = SessionHelper.getUser(session);
        cartService.clearCart(user.getUserId());
        return "redirect:/cart";
    }

    // ── Checkout page — login required ─────────────────
    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/users/login";
        }
        User user = SessionHelper.getUser(session);
        model.addAttribute("cartItems", cartService.getCartItems(user.getUserId()));
        model.addAttribute("cartTotal", cartService.getCartTotal(user.getUserId()));
        model.addAttribute("itemCount", cartService.getCartItemCount(user.getUserId()));
        return "Cart/checkout";
    }

    // ── Admin: View ALL users' carts — ADMIN ONLY ──────
    @GetMapping("/admin")
    public String adminView(HttpSession session, Model model) {
        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        model.addAttribute("allItems", cartService.getAllCartItems());
        return "Cart/admin";
    }
}