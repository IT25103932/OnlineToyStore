package com.OnlineToyStore.Sllit.model;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
// Inheritance — OnlinePayment extends Payment
public class OnlinePayment extends Payment {
    private String cardLastFour;
    private String cardType; // VISA, MASTERCARD, AMEX
    public OnlinePayment(String paymentId, String orderId,
                         String userId, double amount,
                         String paymentDate,
                         String cardLastFour, String cardType) {
        super(paymentId, orderId, userId,
                amount, paymentDate, "ONLINE", "PAID");
        this.cardLastFour = cardLastFour;
        this.cardType = cardType;
    }
    // Polymorphism — overrides parent processPayment()
    @Override
    public String processPayment() {
        return "Online payment of Rs." + getAmount() +
                " processed via " + cardType +
                " ending in " + cardLastFour;
    }
}