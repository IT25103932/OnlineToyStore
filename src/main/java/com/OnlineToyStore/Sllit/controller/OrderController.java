package com.OnlineToyStore.Sllit.controller;
import com.OnlineToyStore.Sllit.model.Order;
import com.OnlineToyStore.Sllit.service.OrderService;
import com.OnlineToyStore.Sllit.service.ToyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@Controller
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private ToyService toyService;
    // Demo user — replace with session user later
    private static final String DEMO_USER_ID = "USER-001";
    private static final String DEMO_USERNAME = "DemoUser";
    // ── All orders (Admin view) ───────────────────────
    @GetMapping
    public String listOrders(
            @RequestParam(required = false) String status,
            Model model) {
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
    // ── Checkout page ─────────────────────────────────
    @GetMapping("/checkout")
    public String showCheckout(
            @RequestParam(required = false) String toyId,
            Model model) {
        model.addAttribute("order", new Order());
        model.addAttribute("toys", toyService.getAllToys());
        if (toyId != null) {
            model.addAttribute("selectedToy",
                    toyService.getToyById(toyId));
        }
        return "order/checkout";
    }
    // ── Handle place order ────────────────────────────
    @PostMapping("/place")
    public String placeOrder(
            @ModelAttribute Order order, Model model) {
        order.setUserId(DEMO_USER_ID);
        order.setUsername(DEMO_USERNAME);
        String result = orderService.placeOrder(order);
        if (!result.equals("success")) {
            model.addAttribute("error", result);
            model.addAttribute("toys", toyService.getAllToys());
            model.addAttribute("order", order);
            return "order/checkout";
        }
        return "redirect:/orders/history?placed=true";
    }
    // ── Order history (user view) ─────────────────────
    @GetMapping("/history")
    public String orderHistory(
            @RequestParam(required = false) String placed,
            Model model) {
        model.addAttribute("orders",
                orderService.getOrdersByUser(DEMO_USER_ID));
        if (placed != null) {
            model.addAttribute("success",
                    "Order placed successfully!");
        }
        return "order/history";
    }
    // ── Order detail page ─────────────────────────────
    @GetMapping("/detail/{orderId}")
    public String orderDetail(
            @PathVariable String orderId, Model model) {
        model.addAttribute("order",
                orderService.getOrderById(orderId));
        return "order/detail";
    }
    // ── Admin: Update status ──────────────────────────
    @PostMapping("/status")
    public String updateStatus(
            @RequestParam String orderId,
            @RequestParam String status) {
        orderService.updateStatus(orderId, status);
        return "redirect:/orders";
    }
    // ── Cancel order ──────────────────────────────────
    @GetMapping("/cancel/{orderId}")
    public String cancelOrder(@PathVariable String orderId) {
        orderService.cancelOrder(orderId);
        return "redirect:/orders/history";
    }
    // ── Admin: Delete order ───────────────────────────
    @GetMapping("/delete/{orderId}")
    public String deleteOrder(@PathVariable String orderId) {
        orderService.deleteOrder(orderId);
        return "redirect:/orders";
    }
}