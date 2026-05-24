package com.OnlineToyStore.Sllit.service;

import com.OnlineToyStore.Sllit.model.Supplier;
import com.OnlineToyStore.Sllit.util.FileStorageUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
// ENCAPSULATION — all file reading/writing logic is hidden inside this service class
public class SupplierService {

    @Value("${data.file.path}")
    private String dataFilePath;

    // Builds the full path to suppliers.txt
    private String getFilePath() {
        return FileStorageUtil.ensureDataFilePath(dataFilePath, "suppliers.txt");
    }

    // ── READ ALL — gets every supplier from suppliers.txt ─────────────────────
    public List<Supplier> getAllSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        File file = new File(getFilePath());

        if (!file.exists()) return suppliers; // return empty list if file not created yet

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    suppliers.add(Supplier.fromFileString(line)); // uses fromFileString() we wrote
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return suppliers;
    }

    // ── READ ONE — find a single supplier by their ID ─────────────────────────
    public Supplier getSupplierById(String supplierId) {
        return getAllSuppliers().stream()
                .filter(s -> s.getSupplierId().equals(supplierId))
                .findFirst()
                .orElse(null);
    }

    // ── SEARCH — find suppliers by name, category, or email ──────────────────
    public List<Supplier> searchSuppliers(String keyword) {
        String lower = keyword.toLowerCase();
        return getAllSuppliers().stream()
                .filter(s -> s.getName().toLowerCase().contains(lower)
                        || s.getSupplyCategory().toLowerCase().contains(lower)
                        || s.getEmail().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    // ── FILTER BY CATEGORY — get suppliers that supply a specific category ────
    public List<Supplier> getSuppliersByCategory(String category) {
        return getAllSuppliers().stream()
                .filter(s -> s.getSupplyCategory().equalsIgnoreCase(category)
                        || s.getSupplyCategory().equalsIgnoreCase("ALL"))
                .collect(Collectors.toList());
    }

    // ── CREATE — add a new supplier and save to suppliers.txt ─────────────────
    public void addSupplier(Supplier supplier) {
        // Auto-generate a unique ID like SUP-A1B2C3D4
        supplier.setSupplierId("SUP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        // Append the new supplier as one line at the end of the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(getFilePath(), true))) {
            writer.write(supplier.toFileString()); // uses toFileString() we wrote
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── UPDATE — replace an existing supplier's data ──────────────────────────
    public void updateSupplier(Supplier updated) {
        List<Supplier> all = getAllSuppliers();

        // Go through the list — when we find the matching ID, swap it with the updated one
        List<Supplier> saved = all.stream()
                .map(s -> s.getSupplierId().equals(updated.getSupplierId()) ? updated : s)
                .collect(Collectors.toList());

        saveAll(saved); // rewrite the whole file with updated data
    }

    // ── DELETE — remove a supplier by ID ─────────────────────────────────────
    public void deleteSupplier(String supplierId) {
        List<Supplier> remaining = getAllSuppliers().stream()
                .filter(s -> !s.getSupplierId().equals(supplierId))
                .collect(Collectors.toList());

        saveAll(remaining); // rewrite the file without the deleted supplier
    }

    // ── COUNT BY CATEGORY — how many suppliers per category ──────────────────
    public long countByCategory(String category) {
        return getAllSuppliers().stream()
                .filter(s -> s.getSupplyCategory().equalsIgnoreCase(category))
                .count();
    }

    // ── PRIVATE HELPER — rewrites the entire suppliers.txt file ──────────────
    // false in FileWriter means overwrite (not append)
    private void saveAll(List<Supplier> suppliers) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(getFilePath(), false))) {
            for (Supplier s : suppliers) {
                writer.write(s.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
