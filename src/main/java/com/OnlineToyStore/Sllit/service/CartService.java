package com.OnlineToyStore.Sllit.service;

import com.OnlineToyStore.Sllit.model.CartItem;
import com.OnlineToyStore.Sllit.model.Toy;
import com.OnlineToyStore.Sllit.util.FileStorageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Value("${data.file.path}")
    private String dataFilePath;

    @Autowired
    private ToyService toyService;

    private String getFilePath() {
        return FileStorageUtil.ensureDataFilePath(dataFilePath, "cart.txt");
    }

    //  READ ALL items for a user ─────────────────────
    public List<CartItem> getCartItems(String userId) {
        return readAll().stream()
                .filter(item -> item.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    //  READ ALL items (admin view) ───────────────────
    public List<CartItem> getAllCartItems() {
        return readAll();
    }

    // ADD item to cart ──────────────────────────────
    public void addToCart(String userId, String toyId, int quantity) {
        if (quantity < 1) {
            return;
        }

        Toy toy = toyService.getToyById(toyId);
        if (toy == null || toy.getStockQuantity() <= 0) {
            return;
        }

        List<CartItem> all = readAll();

        // Check if this toy is already in this user's cart
        for (CartItem item : all) {
            if (item.getUserId().equals(userId) &&
                    item.getToyId().equals(toyId)) {
                // Just increase quantity instead of adding duplicate
                item.setQuantity(Math.min(item.getQuantity() + quantity, toy.getStockQuantity()));
                saveAll(all);
                return;
            }
        }

        // New cart item — get toy details
        CartItem newItem = new CartItem();
        newItem.setCartItemId("CART-" + UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase());
        newItem.setUserId(userId);
        newItem.setToyId(toyId);
        newItem.setToyName(toy.getName());
        newItem.setToyImageUrl(toy.getImageUrl());
        newItem.setUnitPrice(toy.getPrice());
        newItem.setQuantity(Math.min(quantity, toy.getStockQuantity()));

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(getFilePath(), true))) {
            writer.write(newItem.toFileString());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String addToCartChecked(String userId, String toyId, int quantity) {
        if (quantity < 1) {
            return "Quantity must be at least 1.";
        }

        Toy toy = toyService.getToyById(toyId);
        if (toy == null) {
            return "Toy not found.";
        }
        if (toy.getStockQuantity() <= 0) {
            return "This toy is sold out.";
        }

        int existingQuantity = getCartItems(userId).stream()
                .filter(item -> item.getToyId().equals(toyId))
                .mapToInt(CartItem::getQuantity)
                .sum();
        if (existingQuantity + quantity > toy.getStockQuantity()) {
            return "Only " + toy.getStockQuantity() + " units are available.";
        }

        addToCart(userId, toyId, quantity);
        return "success";
    }

    public void addMultipleToCart(String userId, List<String> toyIds, Map<String, Integer> quantities) {
        if (toyIds == null || toyIds.isEmpty()) {
            return;
        }

        for (String toyId : toyIds) {
            if (toyId == null || toyId.isBlank()) {
                continue;
            }

            int quantity = quantities.getOrDefault(toyId, 1);
            if (quantity < 1) {
                continue;
            }

            addToCartChecked(userId, toyId, quantity);
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

    public String updateQuantityChecked(String userId, String cartItemId, int newQuantity) {
        if (newQuantity < 1) {
            return "Quantity must be at least 1.";
        }

        CartItem cartItem = getCartItems(userId).stream()
                .filter(item -> item.getCartItemId().equals(cartItemId))
                .findFirst()
                .orElse(null);
        if (cartItem == null) {
            return "Cart item not found.";
        }

        Toy toy = toyService.getToyById(cartItem.getToyId());
        if (toy == null) {
            return "Toy not found.";
        }
        if (newQuantity > toy.getStockQuantity()) {
            return "Only " + toy.getStockQuantity() + " units are available.";
        }

        updateQuantity(cartItemId, newQuantity);
        return "success";
    }

    // ── DELETE one item ───────────────────────────────
    public void removeItem(String cartItemId) {
        List<CartItem> remaining = readAll().stream()
                .filter(item -> !item.getCartItemId().equals(cartItemId))
                .collect(Collectors.toList());
        saveAll(remaining);
    }

    public boolean removeItemForUser(String userId, String cartItemId) {
        boolean belongsToUser = getCartItems(userId).stream()
                .anyMatch(item -> item.getCartItemId().equals(cartItemId));
        if (!belongsToUser) {
            return false;
        }
        removeItem(cartItemId);
        return true;
    }

    // ── CLEAR entire cart for a user
    public void clearCart(String userId) {
        List<CartItem> remaining = readAll().stream()
                .filter(item -> !item.getUserId().equals(userId))
                .collect(Collectors.toList());
        saveAll(remaining);
    }

    //  CALCULATE cart total
    public double getCartTotal(String userId) {
        return getCartItems(userId).stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
    }

    //  CALCULATE item count ──────────────────────────
    public int getCartItemCount(String userId) {
        return getCartItems(userId).stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    //  Private helpers ───────────────────────────────
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
