package com.OnlineToyStore.Sllit.controller;

import com.OnlineToyStore.Sllit.model.Review;
import com.OnlineToyStore.Sllit.model.User;
import com.OnlineToyStore.Sllit.service.ReviewService;
import com.OnlineToyStore.Sllit.service.ToyService;
import com.OnlineToyStore.Sllit.util.SessionHelper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ToyService toyService;

    // Admin — view all reviews with filter
    // REPLACE listReviews in ReviewController.java
    @GetMapping
    public String listReviews(
            @RequestParam(required = false) String status,
            HttpSession session,
            Model model) {

        // Admin sees all reviews + moderation tools
        // Customers can only see APPROVED reviews
        if (SessionHelper.isAdmin(session)) {
            if (status != null && !status.isEmpty()) {
                model.addAttribute("reviews",
                        reviewService.getReviewsByStatus(status));
                model.addAttribute("currentStatus", status);
            } else {
                model.addAttribute("reviews",
                        reviewService.getAllReviews());
                model.addAttribute("currentStatus", "ALL");
            }
        } else {
            // Non-admins only see APPROVED reviews
            model.addAttribute("reviews",
                    reviewService.getReviewsByStatus("APPROVED"));
            model.addAttribute("currentStatus", "APPROVED");
        }

        model.addAttribute("isAdmin",
                SessionHelper.isAdmin(session));
        model.addAttribute("totalCount",
                reviewService.countTotal());
        model.addAttribute("pendingCount",
                reviewService.countByStatus("PENDING"));
        model.addAttribute("approvedCount",
                reviewService.countByStatus("APPROVED"));
        model.addAttribute("rejectedCount",
                reviewService.countByStatus("REJECTED"));

        return "review/list";
    }

    // CRUD 1 — Show submit review form (Login Protected)
    @GetMapping("/submit")
    public String showSubmitForm(
            @RequestParam(required = false) String toyId,
            HttpSession session,
            Model model) {

        // Must be logged in to submit a review
        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/users/login";
        }

        Review review = new Review();
        if (toyId != null) {
            review.setToyId(toyId);
            var toy = toyService.getToyById(toyId);
            if (toy != null) {
                review.setToyName(toy.getName());
            }
        }
        model.addAttribute("review", review);
        model.addAttribute("toys", toyService.getAllToys());
        return "review/submit";
    }

    // CRUD 1 — Handle submit review (Session Aware)
    @PostMapping("/submit")
    public String submitReview(
            @ModelAttribute Review review,
            HttpSession session) {

        if (!SessionHelper.isLoggedIn(session)) {
            return "redirect:/users/login";
        }

        // Use REAL session user data
        User loggedIn = SessionHelper.getUser(session);
        review.setUserId(loggedIn.getUserId());
        review.setUsername(loggedIn.getUsername());

        // Ensure Toy Name is populated if not in form
        if (review.getToyId() != null && !review.getToyId().isEmpty()) {
            var toy = toyService.getToyById(review.getToyId());
            if (toy != null) {
                review.setToyName(toy.getName());
            }
        }

        // Validate rating range
        if (review.getRating() < 1 || review.getRating() > 5) {
            review.setRating(3); // default fallback
        }

        reviewService.submitReview(review);
        return "redirect:/reviews?submitted=true";
    }

    // CRUD 2 — View reviews for one toy
    @GetMapping("/toy/{toyId}")
    public String viewToyReviews(
            @PathVariable String toyId, Model model) {

        var toy = toyService.getToyById(toyId);
        model.addAttribute("toy", toy);
        model.addAttribute("reviews", reviewService.getReviewsByToy(toyId));
        model.addAttribute("avgRating", reviewService.getAverageRating(toyId));
        return "review/toy-reviews";
    }

    // CRUD 3 — Show edit form
    @GetMapping("/edit/{reviewId}")
    public String showEditForm(
            @PathVariable String reviewId, Model model) {

        model.addAttribute("review", reviewService.getReviewById(reviewId));
        model.addAttribute("toys", toyService.getAllToys());
        return "review/edit";
    }

    // CRUD 3 — Handle edit review
    @PostMapping("/edit/{reviewId}")
    public String editReview(
            @PathVariable String reviewId,
            @ModelAttribute Review review) {

        review.setReviewId(reviewId);
        reviewService.updateReview(review);
        return "redirect:/reviews";
    }

    // Admin — Approve a review
    @GetMapping("/approve/{reviewId}")
    public String approveReview(
            @PathVariable String reviewId,
            HttpSession session) {
        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        reviewService.updateStatus(reviewId, "APPROVED");
        return "redirect:/reviews";
    }

    // Admin — Reject a review
    @GetMapping("/reject/{reviewId}")
    public String rejectReview(
            @PathVariable String reviewId,
            HttpSession session) {
        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        reviewService.updateStatus(reviewId, "REJECTED");
        return "redirect:/reviews";
    }

    // CRUD 4 — Delete a review (Admin Protected)
    @GetMapping("/delete/{reviewId}")
    public String deleteReview(
            @PathVariable String reviewId,
            HttpSession session) {
        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        reviewService.deleteReview(reviewId);
        return "redirect:/reviews";
    }
}