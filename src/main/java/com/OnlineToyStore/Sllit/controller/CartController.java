package com.OnlineToyStore.Sllit.controller;

import com.OnlineToyStore.Sllit.model.CartItem;
import com.OnlineToyStore.Sllit.model.Order;
import com.OnlineToyStore.Sllit.model.Toy;
import com.OnlineToyStore.Sllit.model.User;
import com.OnlineToyStore.Sllit.service.CartService;
import com.OnlineToyStore.Sllit.service.OrderService;
import com.OnlineToyStore.Sllit.service.ToyService;
import com.OnlineToyStore.Sllit.util.SessionHelper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private ToyService toyService;

    @Autowired
    private OrderService orderService;

    // ── View Cart — login required ─────────────────────
    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/users/login";
        }
        if (!SessionHelper.isCustomer(session)) {
            return "redirect:/access-denied";
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
        if (!SessionHelper.isCustomer(session)) {
            return "redirect:/access-denied";
        }
        User user = SessionHelper.getUser(session);
        String result = cartService.addToCartChecked(user.getUserId(), toyId, quantity);
        if (!"success".equals(result)) {
            return "redirect:/cart?error=true";
        }
        return "redirect:/cart?added=true";
    }

    @PostMapping("/add-multiple")
    public String addMultipleToCart(
            @RequestParam(required = false) List<String> toyIds,
            @RequestParam Map<String, String> requestParams,
            HttpSession session) {

        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/users/login";
        }
        if (!SessionHelper.isCustomer(session)) {
            return "redirect:/access-denied";
        }

        Map<String, Integer> quantities = new HashMap<>();
        if (toyIds != null) {
            for (String toyId : toyIds) {
                try {
                    quantities.put(toyId,
                            Integer.parseInt(requestParams.getOrDefault("quantity_" + toyId, "1")));
                } catch (NumberFormatException ex) {
                    quantities.put(toyId, 1);
                }
            }
        }

        User user = SessionHelper.getUser(session);
        cartService.addMultipleToCart(user.getUserId(), toyIds, quantities);
        return "redirect:/cart?added=true";
    }

    @GetMapping("/api")
    @ResponseBody
    public Map<String, Object> getCartApi(HttpSession session) {
        if (!SessionHelper.isCustomer(session)) {
            return Map.of("success", false, "message", "Please login as a customer.");
        }

        User user = SessionHelper.getUser(session);
        return buildCartPayload(user.getUserId(), true, "");
    }

    @PostMapping("/api/add")
    @ResponseBody
    public Map<String, Object> addToCartApi(
            @RequestParam String toyId,
            @RequestParam(defaultValue = "1") int quantity,
            HttpSession session) {

        if (!SessionHelper.isLoggedIn(session)) {
            return Map.of("success", false, "message", "Please login to add toys to cart.");
        }
        if (!SessionHelper.isCustomer(session)) {
            return Map.of("success", false, "message", "Only customers can use the cart.");
        }

        User user = SessionHelper.getUser(session);
        String result = cartService.addToCartChecked(user.getUserId(), toyId, quantity);
        return buildCartPayload(user.getUserId(), "success".equals(result), result);
    }

    @PostMapping("/api/update")
    @ResponseBody
    public Map<String, Object> updateCartApi(
            @RequestParam String cartItemId,
            @RequestParam int quantity,
            HttpSession session) {

        if (!SessionHelper.isCustomer(session)) {
            return Map.of("success", false, "message", "Please login as a customer.");
        }

        User user = SessionHelper.getUser(session);
        String result = cartService.updateQuantityChecked(user.getUserId(), cartItemId, quantity);
        return buildCartPayload(user.getUserId(), "success".equals(result), result);
    }

    @PostMapping("/api/remove")
    @ResponseBody
    public Map<String, Object> removeCartApi(
            @RequestParam String cartItemId,
            HttpSession session) {

        if (!SessionHelper.isCustomer(session)) {
            return Map.of("success", false, "message", "Please login as a customer.");
        }

        User user = SessionHelper.getUser(session);
        boolean removed = cartService.removeItemForUser(user.getUserId(), cartItemId);
        return buildCartPayload(user.getUserId(), removed, removed ? "success" : "Cart item not found.");
    }

    private Map<String, Object> buildCartPayload(String userId, boolean success, String message) {
        List<CartItem> items = cartService.getCartItems(userId);
        List<Map<String, Object>> itemPayload = items.stream()
                .map(item -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("cartItemId", item.getCartItemId());
                    row.put("toyId", item.getToyId());
                    row.put("toyName", item.getToyName());
                    row.put("toyImageUrl", item.getToyImageUrl());
                    row.put("unitPrice", item.getUnitPrice());
                    row.put("quantity", item.getQuantity());
                    row.put("totalPrice", item.getTotalPrice());
                    return row;
                })
                .toList();

        Map<String, Object> payload = new HashMap<>();
        payload.put("success", success);
        payload.put("message", message);
        payload.put("items", itemPayload);
        payload.put("subtotal", cartService.getCartTotal(userId));
        payload.put("count", cartService.getCartItemCount(userId));
        return payload;
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
        if (!SessionHelper.isCustomer(session)) {
            return "redirect:/access-denied";
        }
        User user = SessionHelper.getUser(session);
        if (quantity <= 0) {
            cartService.removeItemForUser(user.getUserId(), cartItemId);
        } else {
            cartService.updateQuantityChecked(user.getUserId(), cartItemId, quantity);
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
        if (!SessionHelper.isCustomer(session)) {
            return "redirect:/access-denied";
        }
        User user = SessionHelper.getUser(session);
        cartService.removeItemForUser(user.getUserId(), cartItemId);
        return "redirect:/cart";
    }

    // ── Clear entire cart ──────────────────────────────
    @GetMapping("/clear")
    public String clearCart(HttpSession session) {
        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/users/login";
        }
        if (!SessionHelper.isCustomer(session)) {
            return "redirect:/access-denied";
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
        if (!SessionHelper.isCustomer(session)) {
            return "redirect:/access-denied";
        }
        User user = SessionHelper.getUser(session);
        populateCheckoutModel(user.getUserId(), model);
        return "Cart/checkout";
    }

    // ── Admin: View ALL users' carts — ADMIN ONLY ──────
    @PostMapping("/checkout/place")
    public String placeCartOrder(
            @RequestParam(defaultValue = "COD") String paymentMethod,
            HttpSession session,
            Model model) {
        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/users/login";
        }
        if (!SessionHelper.isCustomer(session)) {
            return "redirect:/access-denied";
        }

        User user = SessionHelper.getUser(session);
        List<CartItem> cartItems = cartService.getCartItems(user.getUserId());
        if (cartItems.isEmpty()) {
            return "redirect:/cart/checkout";
        }

        for (CartItem item : cartItems) {
            Toy toy = toyService.getToyById(item.getToyId());
            if (toy == null) {
                model.addAttribute("error", item.getToyName() + " is no longer available.");
                populateCheckoutModel(user.getUserId(), model);
                return "Cart/checkout";
            }
            if (item.getQuantity() > toy.getStockQuantity()) {
                model.addAttribute("error",
                        item.getToyName() + " has only " + toy.getStockQuantity() + " unit(s) available.");
                populateCheckoutModel(user.getUserId(), model);
                return "Cart/checkout";
            }
        }

        List<String> orderIds = new ArrayList<>();
        for (CartItem item : cartItems) {
            Order order = new Order();
            order.setUserId(user.getUserId());
            order.setUsername(user.getUsername());
            order.setToyId(item.getToyId());
            order.setQuantity(item.getQuantity());
            order.setPaymentMethod("ONLINE".equals(paymentMethod) ? "ONLINE" : "COD");

            String result = orderService.placeOrder(order);
            if (!"success".equals(result)) {
                model.addAttribute("error", result);
                populateCheckoutModel(user.getUserId(), model);
                return "Cart/checkout";
            }
            orderIds.add(order.getOrderId());
        }

        cartService.clearCart(user.getUserId());
        session.setAttribute("lastCartOrderIds", orderIds);
        return "redirect:/cart/payment-success";
    }

    @GetMapping("/payment-success")
    public String cartPaymentSuccess(HttpSession session, Model model) {
        if (!SessionHelper.isCustomer(session)) {
            return "redirect:/users/login";
        }

        List<String> orderIds = getLastCartOrderIds(session);
        if (orderIds.isEmpty()) {
            return "redirect:/orders/history";
        }

        model.addAttribute("orderCount", orderIds.size());
        model.addAttribute("billUrl", "/cart/bill");
        return "Cart/payment-success";
    }

    @GetMapping("/bill")
    public String cartBill(HttpSession session, Model model) {
        if (!SessionHelper.isCustomer(session)) {
            return "redirect:/users/login";
        }

        User user = SessionHelper.getUser(session);
        List<Order> orders = getLastCartOrderIds(session).stream()
                .map(orderService::getOrderById)
                .filter(order -> order != null && user.getUserId().equals(order.getUserId()))
                .toList();

        if (orders.isEmpty()) {
            return "redirect:/orders/history";
        }

        double billTotal = orders.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();
        model.addAttribute("orders", orders);
        model.addAttribute("billTotal", billTotal);
        model.addAttribute("customerName", user.getUsername());
        return "Cart/bill";
    }

    private void populateCheckoutModel(String userId, Model model) {
        model.addAttribute("cartItems", cartService.getCartItems(userId));
        model.addAttribute("cartTotal", cartService.getCartTotal(userId));
        model.addAttribute("itemCount", cartService.getCartItemCount(userId));
    }

    @SuppressWarnings("unchecked")
    private List<String> getLastCartOrderIds(HttpSession session) {
        Object orderIds = session.getAttribute("lastCartOrderIds");
        if (orderIds instanceof List<?>) {
            return ((List<?>) orderIds).stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }
        return List.of();
    }

    @GetMapping("/admin")
    public String adminView(HttpSession session, Model model) {
        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        model.addAttribute("allItems", cartService.getAllCartItems());
        return "Cart/admin";
    }
}
