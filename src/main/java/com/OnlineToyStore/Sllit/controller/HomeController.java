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

        // Live stats for dashboard
        model.addAttribute("totalToys",
                toyService.getAllToys().size());
        model.addAttribute("totalUsers",
                userService.getAllUsers().size());
        model.addAttribute("totalCustomers",
                userService.countByRole("CUSTOMER"));
        model.addAttribute("totalAdmins",
                userService.countByRole("ADMIN"));
        model.addAttribute("totalOrders",
                orderService.countTotal());
        model.addAttribute("pendingOrders",
                orderService.countByStatus("PENDING"));
        model.addAttribute("deliveredOrders",
                orderService.countByStatus("DELIVERED"));
        model.addAttribute("totalRevenue",
                orderService.getTotalRevenue());
        model.addAttribute("totalReviews",
                reviewService.countTotal());
        model.addAttribute("pendingReviews",
                reviewService.countByStatus("PENDING"));

        // Low stock toys (qty < 5)
        model.addAttribute("lowStockToys",
                toyService.getAllToys().stream()
                        .filter(t -> t.getStockQuantity() < 5)
                        .collect(java.util.stream.Collectors.toList()));

        // Recent orders (last 5)
        java.util.List<com.OnlineToyStore.Sllit.model.Order> allOrders =
                orderService.getAllOrders();
        int size = allOrders.size();
        model.addAttribute("recentOrders",
                allOrders.subList(Math.max(0, size - 5), size));

        return "dashboard";
    }

    // ── Access denied page ────────────────────────────
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}