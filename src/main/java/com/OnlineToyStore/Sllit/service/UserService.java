package com.OnlineToyStore.Sllit.service;

import com.OnlineToyStore.Sllit.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

  @Value("${data.file.path}")
  private String dataFilePath;

  private String getFilePath() {
    return dataFilePath + "users.txt";
  }

  // ── READ ALL users ────────────────────────────────
  public List<User> getAllUsers() {
    List<User> users = new ArrayList<>();
    File file = new File(getFilePath());
    if (!file.exists())
      return users;
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (!line.trim().isEmpty()) {
          users.add(User.fromFileString(line));
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return users;
  }

  // ── READ ONE user by ID ───────────────────────────
  public User getUserById(String userId) {
    return getAllUsers().stream()
        .filter(u -> u.getUserId().equals(userId))
        .findFirst()
        .orElse(null);
  }

  // ── SEARCH by username or email ───────────────────
  public List<User> searchUsers(String keyword) {
    String lower = keyword.toLowerCase();
    return getAllUsers().stream()
        .filter(u -> u.getUsername().toLowerCase().contains(lower)
            || u.getEmail().toLowerCase().contains(lower))
        .collect(Collectors.toList());
  }

  // ── LOGIN — check username + password ─────────────
  public User login(String username, String password) {
    return getAllUsers().stream()
        .filter(u -> u.getUsername().equals(username)
            && u.getPassword().equals(password))
        .findFirst()
        .orElse(null);
  }

  // ── CHECK if username already exists ──────────────
  public boolean usernameExists(String username) {
    return getAllUsers().stream()
        .anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
  }

  // ── CHECK if email already exists ─────────────────
  public boolean emailExists(String email) {
    return getAllUsers().stream()
        .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
  }

  // ── CREATE — Register new user ────────────────────
  public String registerUser(User user) {
    if (usernameExists(user.getUsername())) {
      return "Username already taken";
    }
    if (emailExists(user.getEmail())) {
      return "Email already registered";
    }
    user.setUserId("USR-" +
        UUID.randomUUID().toString()
            .substring(0, 8).toUpperCase());
    if (user.getRole() == null || user.getRole().isEmpty()) {
      user.setRole("CUSTOMER");
    }
    try (BufferedWriter writer = new BufferedWriter(
        new FileWriter(getFilePath(), true))) {
      writer.write(user.toFileString());
      writer.newLine();
    } catch (IOException e) {
      e.printStackTrace();
      return "Error saving user";
    }
    return "success";
  }

  // ── UPDATE — Edit profile ─────────────────────────
  public void updateUser(User updatedUser) {
    List<User> users = getAllUsers();
    List<User> updated = users.stream()
        .map(u -> u.getUserId()
            .equals(updatedUser.getUserId()) ? updatedUser : u)
        .collect(Collectors.toList());
    saveAll(updated);
  }

  // ── DELETE ────────────────────────────────────────
  public void deleteUser(String userId) {
    List<User> remaining = getAllUsers().stream()
        .filter(u -> !u.getUserId().equals(userId))
        .collect(Collectors.toList());
    saveAll(remaining);
  }

  // ── Count by role (for dashboard) ─────────────────
  public long countByRole(String role) {
    return getAllUsers().stream()
        .filter(u -> role.equalsIgnoreCase(u.getRole()))
        .count();
  }

  // ── Private helper: rewrite whole file ────────────
  private void saveAll(List<User> users) {
    try (BufferedWriter writer = new BufferedWriter(
        new FileWriter(getFilePath(), false))) {
      for (User u : users) {
        writer.write(u.toFileString());
        writer.newLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  // ── CREATE default admin if none exists ───────────
  public void createDefaultAdminIfNotExists() {
    boolean adminExists = getAllUsers().stream()
            .anyMatch(u -> "ADMIN".equalsIgnoreCase(u.getRole()));

    if (!adminExists) {
      User admin = new User();
      admin.setUserId("USR-ADMIN-0001");
      admin.setUsername("admin");
      admin.setEmail("admin@toystore.lk");
      admin.setPassword("admin123");
      admin.setAddress("ToyStore HQ");
      admin.setPhone("0112345678");
      admin.setRole("ADMIN");

      try (BufferedWriter writer = new BufferedWriter(
              new FileWriter(getFilePath(), true))) {
        writer.write(admin.toFileString());
        writer.newLine();
      } catch (IOException e) {
        e.printStackTrace();
      }
      System.out.println(
              "✅ Default admin created — username: admin | password: admin123");
    }
  }
}
