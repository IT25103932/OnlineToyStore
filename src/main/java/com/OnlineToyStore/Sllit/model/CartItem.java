package com.OnlineToyStore.Sllit.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    // Encapsulation — all fields are private
    // Only accessible through getters and setters (Lombok @Data)
    private String cartItemId;
    private String userId;
    private String toyId;
    private String toyName;
    private String toyImageUrl;
    private double unitPrice;
    private int quantity;

    // Encapsulation — total price calculation hidden inside the class
    public double getTotalPrice() {
        return unitPrice * quantity;
    }

    // Polymorphism — regular user gets no discount
    public double getDiscountedPrice() {
        return getTotalPrice();
    }

    // Save to cart.txt
    // Format: cartItemId|userId|toyId|toyName|toyImageUrl|unitPrice|quantity
    public String toFileString() {
        return cartItemId + "|" +
                userId + "|" +
                toyId + "|" +
                toyName + "|" +
                (toyImageUrl != null ? toyImageUrl : "") + "|" +
                unitPrice + "|" +
                quantity;
    }

    // Read from cart.txt
    public static CartItem fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        CartItem item = new CartItem();
        item.setCartItemId(p[0]);
        item.setUserId(p[1]);
        item.setToyId(p[2]);
        item.setToyName(p[3]);
        item.setToyImageUrl(p.length > 4 ? p[4] : "");
        item.setUnitPrice(Double.parseDouble(p[5]));
        item.setQuantity(Integer.parseInt(p[6]));
        return item;
    }
}