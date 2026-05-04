package com.OnlineToyStore.Sllit.model;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
// Encapsulation — all fields private, accessed via Lombok getters/setters
public class Order {
    private String orderId;
    private String userId;
    private String username;
    private String toyId;
    private String toyName;
    private int quantity;
    private double totalAmount;
    private String orderDate;
    private String status; // PENDING, SHIPPED, DELIVERED, CANCELLED
    private String paymentMethod; // ONLINE, COD
    private String paymentStatus; // PAID, UNPAID, REFUNDED
    // Save to orders.txt
// Format: orderId|userId|username|toyId|toyName|
// quantity|totalAmount|orderDate|status|
// paymentMethod|paymentStatus
    public String toFileString() {
        return orderId + "|" +
                userId + "|" +
                (username != null ? username : "") + "|" +
                (toyId != null ? toyId : "") + "|" +
                (toyName != null ? toyName : "") + "|" +
                quantity + "|" +
                totalAmount + "|" +
                (orderDate != null ? orderDate : "") + "|" +
                (status != null ? status : "PENDING") + "|" +
                (paymentMethod != null ? paymentMethod : "COD") + "|" +
                (paymentStatus != null ? paymentStatus : "UNPAID");
    }
    // Read one line from orders.txt → Order object
    public static Order fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        Order o = new Order();
        o.setOrderId(p[0]);
        o.setUserId(p.length > 1 ? p[1] : "");
        o.setUsername(p.length > 2 ? p[2] : "");
        o.setToyId(p.length > 3 ? p[3] : "");
        o.setToyName(p.length > 4 ? p[4] : "");
        o.setQuantity(p.length > 5
                ? Integer.parseInt(p[5]) : 0);
        o.setTotalAmount(p.length > 6
                ? Double.parseDouble(p[6]) : 0.0);
        o.setOrderDate(p.length > 7 ? p[7] : "");
        o.setStatus(p.length > 8 ? p[8] : "PENDING");
        o.setPaymentMethod(p.length > 9 ? p[9] : "COD");
        o.setPaymentStatus(p.length > 10 ? p[10] : "UNPAID");
        return o;
    }
}