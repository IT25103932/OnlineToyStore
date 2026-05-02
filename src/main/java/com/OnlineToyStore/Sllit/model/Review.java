package com.OnlineToyStore.Sllit.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    private String reviewId;
    private String toyId;
    private String toyName;
    private String userId;
    private String username;
    private int rating;
    private String comment;
    private String date;
    private String status; // PENDING, APPROVED, REJECTED

    // Polymorphism — overridden in subclasses
    public String getDisplayFormat() {
        return "[" + status + "] " + username +
                " rated " + rating + "/5 : " + comment;
    }

    // Review object → one line in reviews.txt
    public String toFileString() {
        return reviewId + "|" +
                toyId + "|" +
                (toyName != null ? toyName : "") + "|" +
                (userId != null ? userId : "") + "|" +
                (username != null ? username : "") + "|" +
                rating + "|" +
                (comment != null ? comment.replace("|", "-") : "") + "|" +
                (date != null ? date : "") + "|" +
                (status != null ? status : "PENDING");
    }

    // One line from reviews.txt → Review object
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