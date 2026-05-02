package com.toystore.model;

import java.time.LocalDate;

// Abstract base class — demonstrates INHERITANCE
// Review.java will extend this class
public abstract class Feedback {

    private String userId;
    private String comment;
    private LocalDate date;

    // Default constructor
    public Feedback() {
        this.date = LocalDate.now();
    }

    // Parameterized constructor
    public Feedback(String userId , String comment) {
        this.userId = userId;
        this.comment = comment;
        this.date = LocalDate.now();
    }

    // Abstract method — subclasses MUST implement this
    // This is POLYMORPHISM — each subclass gives its own version
    public abstract String getSummary();

    // Getters and Setters — ENCAPSULATION
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}