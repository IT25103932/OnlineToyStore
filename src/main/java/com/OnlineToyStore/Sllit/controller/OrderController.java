package com.OnlineToyStore.Sllit.controller;

import com.OnlineToyStore.Sllit.model.Order;
import com.OnlineToyStore.Sllit.model.User;
import com.OnlineToyStore.Sllit.service.OrderService;
import com.OnlineToyStore.Sllit.service.ToyService;
import com.OnlineToyStore.Sllit.util.SessionHelper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired private OrderService orderService;
    @Autowired private ToyService toyService;

    // ── Admin: View ALL orders ────────────────────────
    @GetMapping
    public String listOrders(
            @RequestParam(required = false) String status,
            HttpSession session, Model model) {

        // GUARD — admin only
        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }

        if (status != null && !status.isEmpty()) {
            model.addAttribute("orders",
                    orderService.getOrdersByStatus(status));
            model.addAttribute("currentStatus", status);
        } else {
            model.addAttribute("orders",
                    orderService.getAllOrders());
            model.addAttribute("currentStatus", "ALL");
        }

        model.addAttribute("totalCount",
                orderService.countTotal());
        model.addAttribute("pendingCount",
                orderService.countByStatus("PENDING"));
        model.addAttribute("shippedCount",
                orderService.countByStatus("SHIPPED"));
        model.addAttribute("deliveredCount",
                orderService.countByStatus("DELIVERED"));
        model.addAttribute("cancelledCount",
                orderService.countByStatus("CANCELLED"));
        model.addAttribute("totalRevenue",
                orderService.getTotalRevenue());
        return "order/list";
    }

    // ── Checkout page — login required ────────────────
    @GetMapping("/checkout")
    public String showCheckout(
            @RequestParam(required = false) String toyId,
            HttpSession session, Model model) {

        // Must be logged in to checkout
        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/users/login";
        }

        model.addAttribute("order", new Order());
        model.addAttribute("toys", toyService.getAllToys());
        if (toyId != null) {
            model.addAttribute("selectedToy",
                    toyService.getToyById(toyId));
        }
        return "order/checkout";
    }

    // ── Place order — login required ──────────────────
    @PostMapping("/place")
    public String placeOrder(
            @ModelAttribute Order order,
            HttpSession session, Model model) {

        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/users/login";
        }

        // Get real user from session
        User loggedIn = SessionHelper.getUser(session);
        order.setUserId(loggedIn.getUserId());
        order.setUsername(loggedIn.getUsername());

        // Validate quantity
        if (order.getQuantity() <= 0) {
            model.addAttribute("error",
                    "Quantity must be at least 1.");
            model.addAttribute("toys", toyService.getAllToys());
            return "order/checkout";
        }

        String result = orderService.placeOrder(order);
        if (!result.equals("success")) {
            model.addAttribute("error", result);
            model.addAttribute("toys", toyService.getAllToys());
            model.addAttribute("order", order);
            return "order/checkout";
        }
        return "redirect:/orders/history?placed=true";
    }

    // ── My order history — login required ─────────────
    @GetMapping("/history")
    public String orderHistory(
            @RequestParam(required = false) String placed,
            HttpSession session, Model model) {

        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/users/login";
        }

        // Get real user's orders only
        User loggedIn = SessionHelper.getUser(session);
        model.addAttribute("orders",
                orderService.getOrdersByUser(
                        loggedIn.getUserId()));
        if (placed != null) {
            model.addAttribute("success",
                    "Order placed successfully! 🎉");
        }
        return "order/history";
    }

    // ── Order detail — login required ─────────────────
    @GetMapping("/detail/{orderId}")
    public String orderDetail(
            @PathVariable String orderId,
            HttpSession session, Model model) {

        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/users/login";
        }
        model.addAttribute("order",
                orderService.getOrderById(orderId));
        return "order/detail";
    }

    // ── Admin: Update status ──────────────────────────
    @PostMapping("/status")
    public String updateStatus(
            @RequestParam String orderId,
            @RequestParam String status,
            HttpSession session) {

        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        orderService.updateStatus(orderId, status);
        return "redirect:/orders";
    }

    // ── Customer: Cancel own order ────────────────────
    @GetMapping("/cancel/{orderId}")
    public String cancelOrder(
            @PathVariable String orderId,
            HttpSession session) {

        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/users/login";
        }
        orderService.cancelOrder(orderId);
        return "redirect:/orders/history";
    }

    // ── Admin: Delete order ───────────────────────────
    @GetMapping("/delete/{orderId}")
    public String deleteOrder(
            @PathVariable String orderId,
            HttpSession session) {

        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        orderService.deleteOrder(orderId);
        return "redirect:/orders";
    }
}