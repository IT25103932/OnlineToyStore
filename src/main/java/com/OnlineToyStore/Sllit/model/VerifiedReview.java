package com.OnlineToyStore.Sllit.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VerifiedReview extends Review {

    private String orderId;

    public VerifiedReview(String reviewId, String toyId,
                          String toyName, String userId,
                          String username, int rating,
                          String comment, String date,
                          String orderId) {

        // 1. Call the Review constructor (6 arguments matching Review's fields)
        super(reviewId, toyId, toyName, username, rating, "APPROVED");

        // 2. Set the fields inherited from Feedback
        this.setUserId(userId);
        this.setComment(comment);
        this.setDate(date);

        // 3. Set the field unique to VerifiedReview
        this.orderId = orderId;
    }

    @Override
    public String getDisplayFormat() {
        return "✅ VERIFIED PURCHASE | " + getUsername() +
                " rated " + getRating() + "/5 : " + getComment();
    }

    @Override
    public String getSummary() {
        return "[Verified] " + getUsername() + " for Order: " + orderId;
    }
}