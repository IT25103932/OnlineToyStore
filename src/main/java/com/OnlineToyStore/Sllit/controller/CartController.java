package com.OnlineToyStore.Sllit.controller;

import com.OnlineToyStore.Sllit.model.CartItem;
import com.OnlineToyStore.Sllit.service.CartService;
import com.OnlineToyStore.Sllit.service.ToyService;
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

    // For now we use a fixed demo userId
    // (when User Management is done this will come from the session)
    private static final String DEMO_USER_ID = "USER-001";

    // ── View Cart ─────────────────────────────────────
    @GetMapping
    public String viewCart(Model model) {
        List<CartItem> items = cartService.getCartItems(DEMO_USER_ID);
        double total = cartService.getCartTotal(DEMO_USER_ID);
        int itemCount = cartService.getCartItemCount(DEMO_USER_ID);

        model.addAttribute("cartItems", items);
        model.addAttribute("cartTotal", total);
        model.addAttribute("itemCount", itemCount);
        model.addAttribute("toys", toyService.getAllToys());
        return "cart/view";
    }

    // ── Add to Cart ───────────────────────────────────
    @PostMapping("/add")
    public String addToCart(@RequestParam String toyId,
                            @RequestParam(defaultValue = "1") int quantity) {
        cartService.addToCart(DEMO_USER_ID, toyId, quantity);
        return "redirect:/cart";
    }

    // ── Update quantity ───────────────────────────────
    @PostMapping("/update")
    public String updateQuantity(@RequestParam String cartItemId,
                                 @RequestParam int quantity) {
        if (quantity <= 0) {
            cartService.removeItem(cartItemId);
        } else {
            cartService.updateQuantity(cartItemId, quantity);
        }
        return "redirect:/cart";
    }

    // ── Remove item ───────────────────────────────────
    @GetMapping("/remove/{cartItemId}")
    public String removeItem(@PathVariable String cartItemId) {
        cartService.removeItem(cartItemId);
        return "redirect:/cart";
    }

    // ── Clear entire cart ─────────────────────────────
    @GetMapping("/clear")
    public String clearCart() {
        cartService.clearCart(DEMO_USER_ID);
        return "redirect:/cart";
    }

    // ── Checkout page ─────────────────────────────────
    @GetMapping("/checkout")
    public String checkout(Model model) {
        List<CartItem> items = cartService.getCartItems(DEMO_USER_ID);
        double total = cartService.getCartTotal(DEMO_USER_ID);
        int itemCount = cartService.getCartItemCount(DEMO_USER_ID);

        model.addAttribute("cartItems", items);
        model.addAttribute("cartTotal", total);
        model.addAttribute("itemCount", itemCount);
        return "cart/checkout";
    }

    // ── Admin: view all users' carts ──────────────────
    @GetMapping("/admin")
    public String adminView(Model model) {
        List<CartItem> allItems = cartService.getAllCartItems();
        model.addAttribute("allItems", allItems);
        return "cart/admin";
    }
}