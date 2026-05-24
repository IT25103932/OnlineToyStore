package com.OnlineToyStore.Sllit.controller;

import com.OnlineToyStore.Sllit.service.*;
import com.OnlineToyStore.Sllit.util.SessionHelper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired private ToyService toyService;
    @Autowired private UserService userService;
    @Autowired private OrderService orderService;
    @Autowired private ReviewService reviewService;
    @Autowired private InventoryService inventoryService;
    @Autowired private MessageService messageService;

    // ── Public home page ──────────────────────────────
    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        model.addAttribute("featuredToys",
                toyService.getAllToys().stream()
                        .limit(6)
                        .collect(java.util.stream.Collectors.toList()));
        // Add real stats for home page
        model.addAttribute("totalToys",
                toyService.getAllToys().size());
        model.addAttribute("totalUsers",
                userService.getAllUsers().size());
        model.addAttribute("totalOrders",
                orderService.countTotal());
        model.addAttribute("totalReviews",
                reviewService.countTotal());
        return "index";
    }

    // ── Admin dashboard — ADMIN ONLY ──────────────────
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }

        populateDashboardModel(model);

        return "dashboard";
    }

    @GetMapping("/dashboard/report")
    public String dashboardReport(HttpSession session, Model model) {
        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }

        populateDashboardModel(model);
        return "dashboard-report";
    }

    private void populateDashboardModel(Model model) {
        long pendingOrders = orderService.countByStatus("PENDING");
        long shippedOrders = orderService.countByStatus("SHIPPED");
        long deliveredOrders = orderService.countByStatus("DELIVERED");
        long cancelledOrders = orderService.countByStatus("CANCELLED");
        long totalOrders = orderService.countTotal();

        int healthyStockCount = inventoryService.getHealthyStockToys().size();
        int lowStockCount = inventoryService.getLowStockCount();
        int outOfStockCount = inventoryService.getOutOfStockCount();
        int totalToys = inventoryService.getTotalToyCount();

        model.addAttribute("totalToys",
                totalToys);
        model.addAttribute("totalUsers",
                userService.getAllUsers().size());
        model.addAttribute("totalCustomers",
                userService.countByRole("CUSTOMER"));
        model.addAttribute("totalAdmins",
                userService.countByRole("ADMIN") + userService.countByRole("SUPER_ADMIN"));
        model.addAttribute("totalOrders",
                totalOrders);
        model.addAttribute("pendingOrders",
                pendingOrders);
        model.addAttribute("shippedOrders",
                shippedOrders);
        model.addAttribute("deliveredOrders",
                deliveredOrders);
        model.addAttribute("cancelledOrders",
                cancelledOrders);
        model.addAttribute("totalRevenue",
                orderService.getTotalRevenue());
        model.addAttribute("pendingRevenue",
                orderService.getPendingRevenue());
        model.addAttribute("totalOrderValue",
                orderService.getTotalOrderValue());
        model.addAttribute("totalReviews",
                reviewService.countTotal());
        model.addAttribute("pendingReviews",
                reviewService.countByStatus("PENDING"));
        model.addAttribute("openMessages",
                messageService.countOpenMessages());
        model.addAttribute("totalMessages",
                messageService.countTotalMessages());

        model.addAttribute("healthyStockCount", healthyStockCount);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("outOfStockCount", outOfStockCount);
        model.addAttribute("totalUnits", inventoryService.getTotalStockUnits());
        model.addAttribute("lowStockToys", inventoryService.getLowStockToys());

        model.addAttribute("pendingOrderPercent", percent(pendingOrders, totalOrders));
        model.addAttribute("shippedOrderPercent", percent(shippedOrders, totalOrders));
        model.addAttribute("deliveredOrderPercent", percent(deliveredOrders, totalOrders));
        model.addAttribute("cancelledOrderPercent", percent(cancelledOrders, totalOrders));
        model.addAttribute("healthyStockPercent", percent(healthyStockCount, totalToys));
        model.addAttribute("lowStockPercent", percent(lowStockCount, totalToys));
        model.addAttribute("outOfStockPercent", percent(outOfStockCount, totalToys));
        model.addAttribute("hasOrderChartData", totalOrders > 0);
        model.addAttribute("hasInventoryChartData", totalToys > 0);

        java.util.List<com.OnlineToyStore.Sllit.model.Order> allOrders =
                orderService.getAllOrders();
        int size = allOrders.size();
        model.addAttribute("recentOrders",
                allOrders.subList(Math.max(0, size - 5), size));
        model.addAttribute("allOrders", allOrders);
    }

    private int percent(long value, long total) {
        if (total <= 0) {
            return 0;
        }
        return (int) Math.round((value * 100.0) / total);
    }

    // ── Access denied page ────────────────────────────
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}
