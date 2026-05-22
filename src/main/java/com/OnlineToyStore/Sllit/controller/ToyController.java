package com.OnlineToyStore.Sllit.controller;

import com.OnlineToyStore.Sllit.model.Toy;
import com.OnlineToyStore.Sllit.service.ToyService;
import com.OnlineToyStore.Sllit.util.SessionHelper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/toys")
public class ToyController {

    private static final Path TOY_UPLOAD_DIR = Paths.get("src/main/resources/static/uploads/toys");
    private static final String TOY_UPLOAD_PATH = "/uploads/toys/";

    @Autowired
    private ToyService toyService;

    // ── List all toys — PUBLIC ────────────────────────
    @GetMapping
    public String listToys(
            @RequestParam(required = false) String category,
            Model model,
            HttpSession session) {

        List<Toy> allToys = toyService.getAllToys();

        // Category filter
        List<Toy> displayed = (category != null && !category.isEmpty())
                ? allToys.stream()
                .filter(t -> category.equalsIgnoreCase(t.getCategory()))
                .collect(Collectors.toList())
                : allToys;

        model.addAttribute("toys", displayed);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("totalCount", allToys.size());
        model.addAttribute("educationalCount",
                allToys.stream().filter(t -> "EDUCATIONAL".equalsIgnoreCase(t.getCategory())).count());
        model.addAttribute("electronicCount",
                allToys.stream().filter(t -> "ELECTRONIC".equalsIgnoreCase(t.getCategory())).count());
        model.addAttribute("softCount",
                allToys.stream().filter(t -> "SOFT".equalsIgnoreCase(t.getCategory())).count());
        model.addAttribute("generalCount",
                allToys.stream().filter(t -> "GENERAL".equalsIgnoreCase(t.getCategory())).count());
        return "toy/list";
    }

    // ── Show Add Toy form — ADMIN ONLY ───────────────
    @GetMapping("/add")
    public String showAddForm(HttpSession session, Model model) {
        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        model.addAttribute("toy", new Toy());
        return "toy/add";
    }

    // ── Handle Add Toy — ADMIN ONLY ──────────────────
    @PostMapping("/add")
    public String addToy(@ModelAttribute Toy toy,
                         @RequestParam("imageFile") MultipartFile imageFile,
                         HttpSession session,
                         Model model) {
        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        // Validate
        if (toy.getName() == null || toy.getName().trim().isEmpty()) {
            model.addAttribute("error", "Toy name is required.");
            return "toy/add";
        }
        if (toy.getPrice() <= 0) {
            model.addAttribute("error", "Price must be greater than 0.");
            return "toy/add";
        }
        if (imageFile == null || imageFile.isEmpty()) {
            model.addAttribute("error", "Toy photo is required.");
            return "toy/add";
        }
        try {
            toy.setImageUrl(saveToyImage(imageFile));
        } catch (IllegalArgumentException | IOException e) {
            model.addAttribute("error", e.getMessage());
            return "toy/add";
        }
        toyService.addToy(toy);
        return "redirect:/toys?added=true";
    }

    // ── Show Edit form — ADMIN ONLY ───────────────────
    @GetMapping("/edit/{toyId}")
    public String showEditForm(@PathVariable String toyId,
                               HttpSession session,
                               Model model) {
        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        model.addAttribute("toy", toyService.getToyById(toyId));
        return "toy/edit";
    }

    // ── Handle Edit — ADMIN ONLY ──────────────────────
    @PostMapping("/edit/{toyId}")
    public String updateToy(@PathVariable String toyId,
                            @ModelAttribute Toy toy,
                            @RequestParam("imageFile") MultipartFile imageFile,
                            HttpSession session,
                            Model model) {
        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        Toy existingToy = toyService.getToyById(toyId);
        if (existingToy == null) {
            return "redirect:/toys";
        }
        toy.setToyId(toyId);
        toy.setImageUrl(existingToy.getImageUrl());

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String oldImageUrl = existingToy.getImageUrl();
                toy.setImageUrl(saveToyImage(imageFile));
                deleteLocalToyImage(oldImageUrl);
            } catch (IllegalArgumentException | IOException e) {
                toy.setImageUrl(existingToy.getImageUrl());
                model.addAttribute("toy", toy);
                model.addAttribute("error", e.getMessage());
                return "toy/edit";
            }
        }

        toyService.updateToy(toy);
        return "redirect:/toys";
    }

    // ── Delete — ADMIN ONLY ───────────────────────────
    @GetMapping("/delete/{toyId}")
    public String deleteToy(@PathVariable String toyId,
                            HttpSession session) {
        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        toyService.deleteToy(toyId);
        return "redirect:/toys";
    }


    // ── Search — PUBLIC ───────────────────────────────
    @GetMapping("/search")
    public String searchToys(
            @RequestParam(required = false) String keyword,
            Model model) {
        if (keyword != null && !keyword.isEmpty()) {
            model.addAttribute("toys", toyService.searchToys(keyword));
            model.addAttribute("keyword", keyword);
        } else {
            model.addAttribute("toys", toyService.getAllToys());
        }
        return "toy/search";
    }

    private String saveToyImage(MultipartFile imageFile) throws IOException {
        String originalName = imageFile.getOriginalFilename();
        String extension = getAllowedImageExtension(originalName);
        Files.createDirectories(TOY_UPLOAD_DIR);

        String fileName = UUID.randomUUID().toString() + "." + extension;
        Path destination = TOY_UPLOAD_DIR.resolve(fileName).normalize();
        imageFile.transferTo(destination);
        return TOY_UPLOAD_PATH + fileName;
    }

    private String getAllowedImageExtension(String originalName) {
        if (originalName == null || !originalName.contains(".")) {
            throw new IllegalArgumentException("Please upload a JPG, PNG, GIF or WebP image.");
        }

        String extension = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!List.of("jpg", "jpeg", "png", "gif", "webp").contains(extension)) {
            throw new IllegalArgumentException("Only JPG, PNG, GIF and WebP images are allowed.");
        }
        return extension;
    }

    private void deleteLocalToyImage(String imageUrl) throws IOException {
        if (imageUrl == null || !imageUrl.startsWith(TOY_UPLOAD_PATH)) {
            return;
        }

        String fileName = imageUrl.substring(TOY_UPLOAD_PATH.length());
        Path imagePath = TOY_UPLOAD_DIR.resolve(fileName).normalize();
        if (imagePath.startsWith(TOY_UPLOAD_DIR)) {
            Files.deleteIfExists(imagePath);
        }
    }
}
