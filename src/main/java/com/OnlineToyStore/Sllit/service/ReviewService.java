package com.OnlineToyStore.Sllit.service;

import com.OnlineToyStore.Sllit.model.Review;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Value("${data.file.path}")
    private String dataFilePath;

    private String getFilePath() {
        return dataFilePath + "reviews.txt";
    }

    // READ ALL reviews
    public List<Review> getAllReviews() {
        List<Review> reviews = new ArrayList<>();
        File file = new File(getFilePath());
        if (!file.exists()) return reviews;

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    reviews.add(Review.fromFileString(line));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    // GET reviews by toy ID
    public List<Review> getReviewsByToy(String toyId) {
        return getAllReviews().stream()
                .filter(r -> r.getToyId().equals(toyId)
                        && "APPROVED".equals(r.getStatus()))
                .collect(Collectors.toList());
    }

    // GET reviews by user ID
    public List<Review> getReviewsByUser(String userId) {
        return getAllReviews().stream()
                .filter(r -> r.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    // GET reviews by status (Admin)
    public List<Review> getReviewsByStatus(String status) {
        return getAllReviews().stream()
                .filter(r -> r.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    // GET one review by ID
    public Review getReviewById(String reviewId) {
        return getAllReviews().stream()
                .filter(r -> r.getReviewId().equals(reviewId))
                .findFirst()
                .orElse(null);
    }

    // CALCULATE average rating for a toy
    public double getAverageRating(String toyId) {
        List<Review> toyReviews = getReviewsByToy(toyId);
        if (toyReviews.isEmpty()) return 0.0;
        return toyReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    // CREATE — Submit new review
    public void submitReview(Review review) {
        review.setReviewId("REV-" +
                UUID.randomUUID().toString()
                        .substring(0, 8).toUpperCase());
        review.setDate(LocalDate.now().toString());
        review.setStatus("PENDING");

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(getFilePath(), true))) {
            writer.write(review.toFileString());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // UPDATE — Edit review
    public void updateReview(Review updated) {
        List<Review> all = getAllReviews();
        List<Review> saved = all.stream()
                .map(r -> r.getReviewId()
                        .equals(updated.getReviewId()) ? updated : r)
                .collect(Collectors.toList());
        saveAll(saved);
    }

    // UPDATE status (Admin approve/reject)
    public void updateStatus(String reviewId, String status) {
        List<Review> all = getAllReviews();
        for (Review r : all) {
            if (r.getReviewId().equals(reviewId)) {
                r.setStatus(status);
                break;
            }
        }
        saveAll(all);
    }

    // DELETE review
    public void deleteReview(String reviewId) {
        List<Review> remaining = getAllReviews().stream()
                .filter(r -> !r.getReviewId().equals(reviewId))
                .collect(Collectors.toList());
        saveAll(remaining);
    }

    // Count helpers for dashboard
    public long countByStatus(String status) {
        return getAllReviews().stream()
                .filter(r -> r.getStatus().equalsIgnoreCase(status))
                .count();
    }

    public long countTotal() {
        return getAllReviews().size();
    }

    // Star string helper
    public String getStarString(int rating) {
        return "★".repeat(Math.max(0, rating)) +
                "☆".repeat(Math.max(0, 5 - rating));
    }

    // Private helper — save all reviews to file
    private void saveAll(List<Review> reviews) {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(getFilePath(), false))) {
            for (Review r : reviews) {
                writer.write(r.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}