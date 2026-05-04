package com.OnlineToyStore.Sllit.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
// Inheritance — PublicReview extends Review
public class PublicReview extends Review {

    private boolean isAnonymous;

    public PublicReview(String reviewId, String toyId,
                        String toyName, String userId,
                        String username, int rating,
                        String comment, String date,
                        boolean isAnonymous) {

        super(reviewId, toyId, toyName, userId,
                username, rating, comment, date, "APPROVED");
        this.isAnonymous = isAnonymous;
    }

    // Polymorphism — overrides parent getDisplayFormat()
    // Anonymous නම් username වෙනුවට "Anonymous" පෙන්වනවා
    @Override
    public String getDisplayFormat() {
        String displayName = isAnonymous ? "Anonymous" : getUsername();
        return "🌐 PUBLIC | " + displayName +
                " rated " + getRating() + "/5 : " + getComment();
    }
}