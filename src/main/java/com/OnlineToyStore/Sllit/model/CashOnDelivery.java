package com.OnlineToyStore.Sllit.model;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
// Inheritance — CashOnDelivery extends Payment
public class CashOnDelivery extends Payment {
    private String deliveryAddress;
    public CashOnDelivery(String paymentId, String orderId,
                          String userId, double amount,
                          String paymentDate,
                          String deliveryAddress) {
        super(paymentId, orderId, userId,
                amount, paymentDate, "COD", "UNPAID");
        this.deliveryAddress = deliveryAddress;
    }
    // Polymorphism — overrides parent processPayment()
    @Override
    public String processPayment() {
        return "Cash on Delivery of Rs." + getAmount() +
                " — collect at: " + deliveryAddress;
    }
}