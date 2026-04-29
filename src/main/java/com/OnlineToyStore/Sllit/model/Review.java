package com.OnlineToyStore.Sllit.model;

import java.time.LocalDate;

// Base class — demonstrates ENCAPSULATION
// VerifiedReview and PublicReview will extend this
public abstract class Review {

    // Private fields — ENCAPSULATION
    // No outside class can access these directly
    private String reviewId;
    private String toyId;
    private String userId;
    private String username;
    private int rating;        // 1 to 5 stars
    private String comment;
    private LocalDate date;

    // Default constructor
    public Review() {
        this.date = LocalDate.now();
    }

    // Parameterized constructor
    public Review(String reviewId, String toyId, String userId,
                  String username, int rating, String comment) {
        this.reviewId = reviewId;
        this.toyId    = toyId;
        this.userId   = userId;
        this.username = username;
        this.rating   = rating;
        this.comment  = comment;
        this.date     = LocalDate.now();
    }

    // Abstract method — POLYMORPHISM
    // VerifiedReview and PublicReview each give their OWN version of this
    // Admin sees one format, regular users see another
    public abstract String getDisplayFormat();

    // Save object as one line in reviews.txt
    public String toFileString() {
        return reviewId + "|" + toyId + "|" + userId + "|"
                + username + "|" + rating + "|" + comment + "|" + date;
    }

    // ─── Getters and Setters — ENCAPSULATION ───

    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }

    public String getToyId() { return toyId; }
    public void setToyId(String toyId) { this.toyId = toyId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}