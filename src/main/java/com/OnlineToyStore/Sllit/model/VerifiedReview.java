package com.OnlineToyStore.Sllit.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
// Inheritance — VerifiedReview extends Review
public class VerifiedReview extends Review {

    private String orderId; // proof of purchase

    public VerifiedReview(String reviewId, String toyId,
                          String toyName, String userId,
                          String username, int rating,
                          String comment, String date,
                          String orderId) {

        super(reviewId, toyId, toyName, userId,
                username, rating, comment, date, "APPROVED");
        this.orderId = orderId;
    }

    // Polymorphism — overrides parent getDisplayFormat()
    @Override
    public String getDisplayFormat() {
        return "✅ VERIFIED PURCHASE | " + getUsername() +
                " rated " + getRating() + "/5 : " + getComment();
    }
}