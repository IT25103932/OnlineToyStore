package com.OnlineToyStore.Sllit.service;

import com.OnlineToyStore.Sllit.model.Order;
import com.OnlineToyStore.Sllit.model.Toy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Value("${data.file.path}")
    private String dataFilePath;

    @Autowired
    private ToyService toyService;

    private String getFilePath() {
        return dataFilePath + "orders.txt";
    }

    // ── READ ALL orders ───────────────────────────────
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        File file = new File(getFilePath());
        if (!file.exists()) return orders;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    orders.add(Order.fromFileString(line));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return orders;
    }

    // ── GET one order by ID ───────────────────────────
    public Order getOrderById(String orderId) {
        return getAllOrders().stream()
                .filter(o -> o.getOrderId().equals(orderId))
                .findFirst()
                .orElse(null);
    }

    // ── GET orders by user ID ─────────────────────────
    public List<Order> getOrdersByUser(String userId) {
        return getAllOrders().stream()
                .filter(o -> o.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    // ── GET orders by status ──────────────────────────
    public List<Order> getOrdersByStatus(String status) {
        return getAllOrders().stream()
                .filter(o -> o.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    // ── PLACE new order ───────────────────────────────
    public String placeOrder(Order order) {
        Toy toy = toyService.getToyById(order.getToyId());
        if (toy == null) return "Toy not found";

        if (toy.getStockQuantity() < order.getQuantity()) {
            return "Not enough stock. Available: " + toy.getStockQuantity();
        }

        order.setOrderId("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setToyName(toy.getName());
        order.setTotalAmount(toy.getPrice() * order.getQuantity());
        order.setOrderDate(LocalDate.now().toString());
        order.setStatus("PENDING");

        if ("ONLINE".equals(order.getPaymentMethod())) {
            order.setPaymentStatus("PAID");
        } else {
            order.setPaymentStatus("UNPAID");
        }

        toy.setStockQuantity(toy.getStockQuantity() - order.getQuantity());
        toyService.updateToy(toy);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(getFilePath(), true))) {
            writer.write(order.toFileString());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "Error saving order";
        }
        return "success";
    }

    // ── UPDATE order status ───────────────────────────
    public void updateStatus(String orderId, String status) {
        List<Order> all = getAllOrders();
        for (Order o : all) {
            if (o.getOrderId().equals(orderId)) {
                o.setStatus(status);
                if ("DELIVERED".equals(status) && "COD".equals(o.getPaymentMethod())) {
                    o.setPaymentStatus("PAID");
                }
                if ("CANCELLED".equals(status) && "ONLINE".equals(o.getPaymentMethod())) {
                    o.setPaymentStatus("REFUNDED");
                    Toy toy = toyService.getToyById(o.getToyId());
                    if (toy != null) {
                        toy.setStockQuantity(toy.getStockQuantity() + o.getQuantity());
                        toyService.updateToy(toy);
                    }
                }
                break;
            }
        }
        saveAll(all);
    }

    // ── CANCEL order ──────────────────────────────────
    public void cancelOrder(String orderId) {
        updateStatus(orderId, "CANCELLED");
    }

    // ── DELETE order (Admin) ──────────────────────────
    public void deleteOrder(String orderId) {
        List<Order> remaining = getAllOrders().stream()
                .filter(o -> !o.getOrderId().equals(orderId))
                .collect(Collectors.toList());
        saveAll(remaining);
    }

    // ── SUMMARY counts ────────────────────────────────
    public long countByStatus(String status) {
        return getAllOrders().stream()
                .filter(o -> o.getStatus().equalsIgnoreCase(status))
                .count();
    }

    public double getTotalRevenue() {
        return getAllOrders().stream()
                .filter(o -> "PAID".equals(o.getPaymentStatus()))
                .mapToDouble(Order::getTotalAmount)
                .sum();
    }

    public long countTotal() {
        return getAllOrders().size();
    }

    // ── Private helper ────────────────────────────────
    private void saveAll(List<Order> orders) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(getFilePath(), false))) {
            for (Order o : orders) {
                writer.write(o.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}