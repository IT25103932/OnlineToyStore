package com.OnlineToyStore.Sllit.model;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
// Encapsulation — Payment class with private fields
public class Payment {
    private String paymentId;
    private String orderId;
    private String userId;
    private double amount;
    private String paymentDate;
    private String method; // ONLINE, COD
    private String status; // PAID, UNPAID, REFUNDED
    // Polymorphism — overridden in subclasses
    public String processPayment() {
        return "Processing payment of Rs." + amount;
    }
    // Save to a string (used inside OrderService — not a separate file)
    public String toFileString() {
        return paymentId + "|" +
                orderId + "|" +
                userId + "|" +
                amount + "|" +
                (paymentDate != null ? paymentDate : "") + "|" +
                (method != null ? method : "COD") + "|" +
                (status != null ? status : "UNPAID");
    }
    public static Payment fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        Payment pay = new Payment();
        pay.setPaymentId(p[0]);
        pay.setOrderId(p.length > 1 ? p[1] : "");
        pay.setUserId(p.length > 2 ? p[2] : "");
        pay.setAmount(p.length > 3
                ? Double.parseDouble(p[3]) : 0.0);
        pay.setPaymentDate(p.length > 4 ? p[4] : "");
        pay.setMethod(p.length > 5 ? p[5] : "COD");
        pay.setStatus(p.length > 6 ? p[6] : "UNPAID");
        return pay;
    }
}