package com.OnlineToyStore.Sllit.model;

import java.time.LocalDate;

/**
 * Abstract base class — demonstrates INHERITANCE.
 * Review.java extends this class.
 */
public abstract class Feedback {

    private String userId;
    private String comment;
    private String date; // Changed to String to match Review's current data type

    public Feedback() {
        this.date = LocalDate.now().toString();
    }

    public Feedback(String userId, String comment) {
        this.userId = userId;
        this.comment = comment;
        this.date = LocalDate.now().toString();
    }

    public Feedback(String userId, String comment, String date) {
        this.userId  = userId;
        this.comment = comment;
        this.date    = date;   // use the provided date, not LocalDate.now()
    }

    /**
     * Abstract method — subclasses MUST implement this.
     * This is POLYMORPHISM — each subclass gives its own version.
     */
    public abstract String getSummary();

    // Getters and Setters — ENCAPSULATION
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}