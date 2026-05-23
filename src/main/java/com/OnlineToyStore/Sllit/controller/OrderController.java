package com.OnlineToyStore.Sllit.controller;

import com.OnlineToyStore.Sllit.model.Order;
import com.OnlineToyStore.Sllit.model.Toy;
import com.OnlineToyStore.Sllit.model.User;
import com.OnlineToyStore.Sllit.service.EmailService;
import com.OnlineToyStore.Sllit.service.OrderService;
import com.OnlineToyStore.Sllit.service.ToyService;
import com.OnlineToyStore.Sllit.service.UserService;
import com.OnlineToyStore.Sllit.util.SessionHelper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired private OrderService orderService;
    @Autowired private ToyService toyService;
    @Autowired private UserService userService;
    @Autowired private EmailService emailService;

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
    public String checkout(@RequestParam String toyId,
                           @RequestParam(defaultValue = "1") int quantity,
                           HttpSession session,
                           Model model) {

        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/users/login";
        }

        if (!SessionHelper.isCustomer(session)) {
            return "redirect:/access-denied";
        }

        Toy selectedToy = toyService.getToyById(toyId);

        if (selectedToy == null) {
            return "redirect:/toys";
        }

        model.addAttribute("toys", toyService.getAllToys());
        model.addAttribute("selectedToy", selectedToy);
        model.addAttribute("selectedToyId", toyId);
        model.addAttribute("quantity", quantity);
        model.addAttribute("paymentMethod", "ONLINE");
        model.addAttribute("totalAmount", selectedToy.getPrice() * quantity);

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
        if (!SessionHelper.isCustomer(session)) {
            return "redirect:/access-denied";
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
        Order savedOrder = orderService.getOrderById(order.getOrderId());
        if (savedOrder != null) {
            emailService.sendOrderConfirmation(loggedIn, savedOrder);
            emailService.notifyAdminNewOrder(savedOrder);
            Toy toy = toyService.getToyById(savedOrder.getToyId());
            if (toy != null && toy.getStockQuantity() > 0 && toy.getStockQuantity() <= 5) {
                emailService.notifyAdminLowStock(toy.getName(), toy.getStockQuantity());
            }
        }
        return "redirect:/orders/payment-success/" + order.getOrderId();
    }

    @GetMapping("/payment-success/{orderId}")
    public String paymentSuccess(
            @PathVariable String orderId,
            HttpSession session,
            Model model) {

        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/users/login";
        }

        Order order = orderService.getOrderById(orderId);
        if (!canAccessOrder(session, order)) {
            return "redirect:/access-denied";
        }

        model.addAttribute("order", order);
        return "order/payment-success";
    }

    // ── My order history — login required ─────────────
    @GetMapping("/history")
    public String orderHistory(
            @RequestParam(required = false) String placed,
            @RequestParam(required = false) String status,
            HttpSession session, Model model) {

        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/users/login";
        }

        // Get real user's orders only
        User loggedIn = SessionHelper.getUser(session);
        List<Order> allUserOrders = orderService.getOrdersByUser(loggedIn.getUserId());
        List<Order> displayedOrders = (status != null && !status.isEmpty() && !"ALL".equalsIgnoreCase(status))
                ? allUserOrders.stream()
                .filter(order -> status.equalsIgnoreCase(order.getStatus()))
                .collect(Collectors.toList())
                : allUserOrders;

        Map<String, String> orderImages = new HashMap<>();
        for (Order order : displayedOrders) {
            Toy toy = toyService.getToyById(order.getToyId());
            orderImages.put(order.getOrderId(), toy != null ? toy.getImageUrl() : "");
        }

        model.addAttribute("orders", displayedOrders);
        model.addAttribute("orderImages", orderImages);
        model.addAttribute("currentStatus", status == null || status.isEmpty() ? "ALL" : status.toUpperCase());
        model.addAttribute("allCount", allUserOrders.size());
        model.addAttribute("pendingCount", countUserOrdersByStatus(allUserOrders, "PENDING"));
        model.addAttribute("shippedCount", countUserOrdersByStatus(allUserOrders, "SHIPPED"));
        model.addAttribute("deliveredCount", countUserOrdersByStatus(allUserOrders, "DELIVERED"));
        model.addAttribute("cancelledCount", countUserOrdersByStatus(allUserOrders, "CANCELLED"));
        if (placed != null) {
            model.addAttribute("success",
                    "Order placed successfully! 🎉");
        }
        return "order/history";
    }

    // ── Order detail — login required ─────────────────
    private long countUserOrdersByStatus(List<Order> orders, String status) {
        return orders.stream()
                .filter(order -> status.equalsIgnoreCase(order.getStatus()))
                .count();
    }

    @GetMapping("/detail/{orderId}")
    public String orderDetail(
            @PathVariable String orderId,
            HttpSession session, Model model) {

        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/users/login";
        }
        if (!SessionHelper.isCustomer(session)) {
            return "redirect:/access-denied";
        }
        Order order = orderService.getOrderById(orderId);
        if (!canAccessOrder(session, order)) {
            return "redirect:/access-denied";
        }
        model.addAttribute("order", order);
        return "order/detail";
    }

    @GetMapping("/bill/{orderId}")
    public String orderBill(
            @PathVariable String orderId,
            @RequestParam(required = false) String placed,
            @RequestParam(required = false) String emailed,
            HttpSession session,
            Model model) {

        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/users/login";
        }

        Order order = orderService.getOrderById(orderId);
        if (!canAccessOrder(session, order)) {
            return "redirect:/access-denied";
        }

        model.addAttribute("order", order);
        model.addAttribute("unitPrice",
                order.getQuantity() > 0 ? order.getTotalAmount() / order.getQuantity() : 0);
        if (placed != null) {
            model.addAttribute("success", "Order placed successfully. You can save this bill as PDF.");
        }
        if (emailed != null) {
            model.addAttribute("success", "Receipt email sent to your registered email address.");
        }
        return "order/bill";
    }

    // ── Admin: Update status ──────────────────────────
    @GetMapping("/bill/{orderId}/email")
    public String emailBill(
            @PathVariable String orderId,
            HttpSession session) {

        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/users/login";
        }

        Order order = orderService.getOrderById(orderId);
        if (!canAccessOrder(session, order)) {
            return "redirect:/access-denied";
        }

        User customer = userService.getUserById(order.getUserId());
        if (customer != null) {
            emailService.sendBillReceipt(customer, order);
        }
        return "redirect:/orders/bill/" + orderId + "?emailed=true";
    }

    @PostMapping("/status")
    public String updateStatus(
            @RequestParam String orderId,
            @RequestParam String status,
            HttpSession session) {

        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        orderService.updateStatus(orderId, status);
        Order order = orderService.getOrderById(orderId);
        if (order != null) {
            User customer = userService.getUserById(order.getUserId());
            if (customer != null) {
                emailService.sendOrderStatusUpdate(customer, order);
            }
        }
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
        Order order = orderService.getOrderById(orderId);
        if (!canAccessOrder(session, order)) {
            return "redirect:/access-denied";
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

    private boolean canAccessOrder(HttpSession session, Order order) {
        if (order == null) {
            return false;
        }
        if (SessionHelper.isAdmin(session)) {
            return true;
        }
        User loggedIn = SessionHelper.getUser(session);
        return loggedIn != null && loggedIn.getUserId().equals(order.getUserId());
    }
}
