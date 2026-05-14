package com.OnlineToyStore.Sllit.service;

import com.OnlineToyStore.Sllit.model.CartItem;
import com.OnlineToyStore.Sllit.model.Toy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Value("${data.file.path}")
    private String dataFilePath;

    @Autowired
    private ToyService toyService;

    private String getFilePath() {
        return dataFilePath + "cart.txt";
    }

    // ── READ ALL items for a user ─────────────────────
    public List<CartItem> getCartItems(String userId) {
        return readAll().stream()
                .filter(item -> item.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    // ── READ ALL items (admin view) ───────────────────
    public List<CartItem> getAllCartItems() {
        return readAll();
    }

    // ── ADD item to cart ──────────────────────────────
    public void addToCart(String userId, String toyId, int quantity) {
        List<CartItem> all = readAll();

        // Check if this toy is already in this user's cart
        for (CartItem item : all) {
            if (item.getUserId().equals(userId) &&
                    item.getToyId().equals(toyId)) {
                // Just increase quantity instead of adding duplicate
                item.setQuantity(item.getQuantity() + quantity);
                saveAll(all);
                return;
            }
        }

        // New cart item — get toy details
        Toy toy = toyService.getToyById(toyId);
        if (toy == null) return;

        CartItem newItem = new CartItem();
        newItem.setCartItemId("CART-" + UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase());
        newItem.setUserId(userId);
        newItem.setToyId(toyId);
        newItem.setToyName(toy.getName());
        //newItem.setToyImageUrl(toy.getImageUrl());
        newItem.setUnitPrice(toy.getPrice());
        newItem.setQuantity(quantity);

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(getFilePath(), true))) {
            writer.write(newItem.toFileString());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── UPDATE quantity ───────────────────────────────
    public void updateQuantity(String cartItemId, int newQuantity) {
        List<CartItem> all = readAll();
        for (CartItem item : all) {
            if (item.getCartItemId().equals(cartItemId)) {
                item.setQuantity(newQuantity);
                break;
            }
        }
        saveAll(all);
    }

    // ── DELETE one item ───────────────────────────────
    public void removeItem(String cartItemId) {
        List<CartItem> remaining = readAll().stream()
                .filter(item -> !item.getCartItemId().equals(cartItemId))
                .collect(Collectors.toList());
        saveAll(remaining);
    }

    // ── CLEAR entire cart for a user ──────────────────
    public void clearCart(String userId) {
        List<CartItem> remaining = readAll().stream()
                .filter(item -> !item.getUserId().equals(userId))
                .collect(Collectors.toList());
        saveAll(remaining);
    }

    // ── CALCULATE cart total ──────────────────────────
    public double getCartTotal(String userId) {
        return getCartItems(userId).stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
    }

    // ── CALCULATE item count ──────────────────────────
    public int getCartItemCount(String userId) {
        return getCartItems(userId).stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    // ── Private helpers ───────────────────────────────
    private List<CartItem> readAll() {
        List<CartItem> items = new ArrayList<>();
        File file = new File(getFilePath());
        if (!file.exists()) return items;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    items.add(CartItem.fromFileString(line));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return items;
    }

    private void saveAll(List<CartItem> items) {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(getFilePath(), false))) {
            for (CartItem item : items) {
                writer.write(item.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}