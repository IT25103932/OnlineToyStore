package com.OnlineToyStore.Sllit.controller;

import com.OnlineToyStore.Sllit.model.Review;
import com.OnlineToyStore.Sllit.service.ReviewService;
import com.OnlineToyStore.Sllit.service.ToyService;
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

    private static final String DEMO_USER_ID = "USER-001";
    private static final String DEMO_USERNAME = "DemoUser";

    // Admin — view all reviews with filter
    @GetMapping
    public String listReviews(
            @RequestParam(required = false) String status,
            Model model) {

        if (status != null && !status.isEmpty()) {
            model.addAttribute("reviews",
                    reviewService.getReviewsByStatus(status));
            model.addAttribute("currentStatus", status);
        } else {
            model.addAttribute("reviews",
                    reviewService.getAllReviews());
            model.addAttribute("currentStatus", "ALL");
        }

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

    // CRUD 1 — Show submit review form
    @GetMapping("/submit")
    public String showSubmitForm(
            @RequestParam(required = false) String toyId,
            Model model) {

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

    // CRUD 1 — Handle submit review
    @PostMapping("/submit")
    public String submitReview(@ModelAttribute Review review) {
        review.setUserId(DEMO_USER_ID);
        review.setUsername(DEMO_USERNAME);

        if (review.getToyId() != null
                && !review.getToyId().isEmpty()) {
            var toy = toyService.getToyById(review.getToyId());
            if (toy != null) {
                review.setToyName(toy.getName());
            }
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
        model.addAttribute("reviews",
                reviewService.getReviewsByToy(toyId));
        model.addAttribute("avgRating",
                reviewService.getAverageRating(toyId));
        return "review/toy-reviews";
    }

    // CRUD 3 — Show edit form
    @GetMapping("/edit/{reviewId}")
    public String showEditForm(
            @PathVariable String reviewId, Model model) {

        model.addAttribute("review",
                reviewService.getReviewById(reviewId));
        model.addAttribute("toys",
                toyService.getAllToys());
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
    public String approveReview(@PathVariable String reviewId) {
        reviewService.updateStatus(reviewId, "APPROVED");
        return "redirect:/reviews";
    }

    // Admin — Reject a review
    @GetMapping("/reject/{reviewId}")
    public String rejectReview(@PathVariable String reviewId) {
        reviewService.updateStatus(reviewId, "REJECTED");
        return "redirect:/reviews";
    }

    // CRUD 4 — Delete a review
    @GetMapping("/delete/{reviewId}")
    public String deleteReview(@PathVariable String reviewId) {
        reviewService.deleteReview(reviewId);
        return "redirect:/reviews";
    }
}