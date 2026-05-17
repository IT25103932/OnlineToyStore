package com.OnlineToyStore.Sllit.service;

import com.OnlineToyStore.Sllit.model.Toy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ToyService {

    @Value("${data.file.path}")
    private String dataFilePath;

    private String getFilePath() {
        return dataFilePath + "toys.txt";
    }

    // ─── READ ALL ───────────────────────────────────────────
    public List<Toy> getAllToys() {
        List<Toy> toys = new ArrayList<>();
        File file = new File(getFilePath());
        if (!file.exists()) return toys;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    toys.add(Toy.fromFileString(line));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return toys;
    }

    // ─── CREATE ─────────────────────────────────────────────
    public void addToy(Toy toy) {
        // Auto-generate ID
        toy.setToyId("TOY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(getFilePath(), true))) {
            writer.write(toy.toFileString());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ─── READ ONE (by ID) ────────────────────────────────────
    public Toy getToyById(String toyId) {
        return getAllToys().stream()
                .filter(t -> t.getToyId().equals(toyId))
                .findFirst()
                .orElse(null);
    }

    // ─── SEARCH (by name or category) ───────────────────────
    public List<Toy> searchToys(String keyword) {
        String lower = keyword.toLowerCase();
        return getAllToys().stream()
                .filter(t -> t.getName().toLowerCase().contains(lower)
                        || t.getCategory().toLowerCase().contains(lower)
                        || t.getDescription().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    // ─── UPDATE ─────────────────────────────────────────────
    public void updateToy(Toy updatedToy) {
        List<Toy> toys = getAllToys();
        List<Toy> updated = toys.stream()
                .map(t -> t.getToyId().equals(updatedToy.getToyId()) ? updatedToy : t)
                .collect(Collectors.toList());
        saveAllToys(updated);
    }

    // ─── DELETE ─────────────────────────────────────────────
    public void deleteToy(String toyId) {
        List<Toy> toys = getAllToys();
        List<Toy> remaining = toys.stream()
                .filter(t -> !t.getToyId().equals(toyId))
                .collect(Collectors.toList());
        saveAllToys(remaining);
    }

    // ─── HELPER: Save entire list back to file ───────────────
    private void saveAllToys(List<Toy> toys) {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(getFilePath(), false))) {
            for (Toy toy : toys) {
                writer.write(toy.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}