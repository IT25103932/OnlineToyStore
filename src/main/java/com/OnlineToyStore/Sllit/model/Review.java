package com.OnlineToyStore.Sllit.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true) // Important for Lombok with inheritance
public class Review extends Feedback {

    private String reviewId;
    private String toyId;
    private String toyName;
    private String username;
    private int rating;
    private String status; // PENDING, APPROVED, REJECTED


    public Review(String reviewId, String toyId, String toyName, String userId,
                  String username, int rating, String comment, String date, String status) {
        // Pass Feedback fields to the Feedback constructor (assuming Feedback has this)
        super(userId, comment, date);
        this.reviewId = reviewId;
        this.toyId = toyId;
        this.toyName = toyName;
        this.username = username;
        this.rating = rating;
        this.status = status;
    }

    /**
     * Implementation of the abstract method from Feedback.
     * Demonstration of POLYMORPHISM.
     */
    @Override
    public String getSummary() {
        return username + " gave " + rating + "/5 stars for " + toyName;
    }

    /**
     * Overridden method for specialized formatting.
     */
    public String getDisplayFormat() {
        return "[" + status + "] " + username +
                " rated " + rating + "/5 : " + getComment();
    }

    /**
     * Review object → one line in reviews.txt
     */
    public String toFileString() {
        return reviewId + "|" +
                toyId + "|" +
                (toyName != null ? toyName : "") + "|" +
                (getUserId() != null ? getUserId() : "") + "|" +
                (username != null ? username : "") + "|" +
                rating + "|" +
                (getComment() != null ? getComment().replace("|", "-") : "") + "|" +
                (getDate() != null ? getDate() : "") + "|" +
                (status != null ? status : "PENDING");
    }

    /**
     * One line from reviews.txt → Review object
     */
    public static Review fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        Review r = new Review();
        r.setReviewId(p[0]);
        r.setToyId(p[1]);
        r.setToyName(p.length > 2 ? p[2] : "");
        r.setUserId(p.length > 3 ? p[3] : "");
        r.setUsername(p.length > 4 ? p[4] : "");
        r.setRating(p.length > 5 ? Integer.parseInt(p[5]) : 0);
        r.setComment(p.length > 6 ? p[6] : "");
        r.setDate(p.length > 7 ? p[7] : "");
        r.setStatus(p.length > 8 ? p[8] : "PENDING");
        return r;
    }
}