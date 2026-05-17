package com.OnlineToyStore.Sllit.controller;

import com.OnlineToyStore.Sllit.model.Toy;
import com.OnlineToyStore.Sllit.service.ToyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/toys")
public class ToyController {

    @Autowired
    private ToyService toyService;

    // ─── List all toys ───────────────────────────────────────
    @GetMapping
    public String listToys(Model model) {
        List<Toy> toys = toyService.getAllToys();
        model.addAttribute("toys", toys);
        return "toy/list";
    }

    // ─── Show Add Toy form ───────────────────────────────────
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("toy", new Toy());
        return "toy/add";
    }

    // ─── Handle Add Toy form submit ──────────────────────────
    @PostMapping("/add")
    public String addToy(@ModelAttribute Toy toy) {
        toyService.addToy(toy);
        return "redirect:/toys";
    }

    // ─── Show Edit form ──────────────────────────────────────
    @GetMapping("/edit/{toyId}")
    public String showEditForm(@PathVariable String toyId, Model model) {
        Toy toy = toyService.getToyById(toyId);
        model.addAttribute("toy", toy);
        return "toy/edit";
    }

    // ─── Handle Edit form submit ─────────────────────────────
    @PostMapping("/edit/{toyId}")
    public String updateToy(@PathVariable String toyId,
                            @ModelAttribute Toy toy) {
        toy.setToyId(toyId);
        toyService.updateToy(toy);
        return "redirect:/toys";
    }

    // ─── Delete toy ──────────────────────────────────────────
    @GetMapping("/delete/{toyId}")
    public String deleteToy(@PathVariable String toyId) {
        toyService.deleteToy(toyId);
        return "redirect:/toys";
    }

    // ─── Search toys ─────────────────────────────────────────
    @GetMapping("/search")
    public String searchToys(@RequestParam(required = false) String keyword,
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