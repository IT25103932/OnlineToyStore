package com.OnlineToyStore.Sllit.controller;

import com.OnlineToyStore.Sllit.model.Toy;
import com.OnlineToyStore.Sllit.service.ToyService;
import com.OnlineToyStore.Sllit.util.SessionHelper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/toys")
public class ToyController {

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
                            HttpSession session) {
        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        toy.setToyId(toyId);
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
}