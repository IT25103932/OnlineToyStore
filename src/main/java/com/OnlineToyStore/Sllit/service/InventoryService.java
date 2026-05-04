package com.OnlineToyStore.Sllit.service;

import com.OnlineToyStore.Sllit.model.Toy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
// ABSTRACTION — complex stock-checking logic is hidden behind simple method names
// The controller just calls getLowStockToys() without knowing HOW it works internally
public class InventoryService {

    private static final int LOW_STOCK_THRESHOLD = 5;   // 1-5 units = low stock
    private static final int OUT_OF_STOCK_THRESHOLD = 0; // 0 units = out of stock

    @Autowired
    private ToyService toyService; // uses the existing ToyService written by your teammate

    // ── READ ALL — returns the full inventory list ────────────────────────────
    public List<Toy> getAllInventory() {
        return toyService.getAllToys();
    }

    // ── ABSTRACTION: low stock toys — quantity between 1 and 5 ───────────────
    public List<Toy> getLowStockToys() {
        return toyService.getAllToys().stream()
                .filter(t -> t.getStockQuantity() > OUT_OF_STOCK_THRESHOLD
                        && t.getStockQuantity() <= LOW_STOCK_THRESHOLD)
                .collect(Collectors.toList());
    }

    // ── ABSTRACTION: out of stock toys — quantity is exactly 0 ───────────────
    public List<Toy> getOutOfStockToys() {
        return toyService.getAllToys().stream()
                .filter(t -> t.getStockQuantity() == OUT_OF_STOCK_THRESHOLD)
                .collect(Collectors.toList());
    }

    // ── ABSTRACTION: healthy stock toys — quantity above 5 ───────────────────
    public List<Toy> getHealthyStockToys() {
        return toyService.getAllToys().stream()
                .filter(t -> t.getStockQuantity() > LOW_STOCK_THRESHOLD)
                .collect(Collectors.toList());
    }

    // ── UPDATE — restock a toy by adding quantity to current stock ────────────
    public void restockToy(String toyId, int addQuantity) {
        Toy toy = toyService.getToyById(toyId);
        if (toy != null) {
            toy.setStockQuantity(toy.getStockQuantity() + addQuantity); // add to existing stock
            toyService.updateToy(toy); // save the updated toy back to toys.txt
        }
    }

    // ── SUMMARY COUNTS — used to display stats on the inventory page ──────────
    public int getTotalToyCount() {
        return toyService.getAllToys().size();
    }

    public int getLowStockCount() {
        return getLowStockToys().size();
    }

    public int getOutOfStockCount() {
        return getOutOfStockToys().size();
    }

    public int getTotalStockUnits() {
        return toyService.getAllToys().stream()
                .mapToInt(Toy::getStockQuantity)
                .sum(); // adds up all stockQuantity values across every toy
    }
}