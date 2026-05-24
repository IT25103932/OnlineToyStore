package com.OnlineToyStore.Sllit.service;

import com.OnlineToyStore.Sllit.model.User;
import com.OnlineToyStore.Sllit.util.FileStorageUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

  @Value("${data.file.path}")
  private String dataFilePath;

  private static final String HASH_PREFIX = "{sha256}";

  private String getFilePath() {
    return FileStorageUtil.ensureDataFilePath(dataFilePath, "users.txt");
  }

  public List<User> getAllUsers() {
    List<User> users = new ArrayList<>();
    File file = new File(getFilePath());
    if (!file.exists()) {
      return users;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (!line.trim().isEmpty()) {
          try {
            users.add(User.fromFileString(line));
          } catch (RuntimeException ex) {
            System.err.println("Skipping invalid user row: " + line);
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return users;
  }

  public User getUserById(String userId) {
    return getAllUsers().stream()
            .filter(u -> u.getUserId().equals(userId))
            .findFirst()
            .orElse(null);
  }

  public User getUserByEmail(String email) {
    if (email == null) {
      return null;
    }
    return getAllUsers().stream()
            .filter(u -> email.equalsIgnoreCase(u.getEmail()))
            .findFirst()
            .orElse(null);
  }

  public void updatePassword(String userId, String newPassword) {
    User user = getUserById(userId);
    if (user == null) {
      return;
    }
    user.setPassword(newPassword);
    updateUser(user);
  }

  public List<User> searchUsers(String keyword) {
    String lower = keyword.toLowerCase();
    return getAllUsers().stream()
            .filter(u -> u.getUsername().toLowerCase().contains(lower)
                    || u.getEmail().toLowerCase().contains(lower))
            .collect(Collectors.toList());
  }

  public User login(String username, String password) {
    User user = getAllUsers().stream()
            .filter(u -> u.getUsername().equals(username))
            .findFirst()
            .orElse(null);

    if (user == null || !passwordMatches(password, user.getPassword())) {
      return null;
    }

    if (!isHashedPassword(user.getPassword())) {
      user.setPassword(hashPassword(password));
      updateUser(user);
    }
    return user;
  }

  public boolean usernameExists(String username) {
    return getAllUsers().stream()
            .anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
  }

  public boolean emailExists(String email) {
    return getAllUsers().stream()
            .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
  }

  public String registerUser(User user) {
    if (usernameExists(user.getUsername())) {
      return "Username already taken";
    }
    if (emailExists(user.getEmail())) {
      return "Email already registered";
    }

    user.setUserId("USR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    if (user.getRole() == null || user.getRole().isEmpty()) {
      user.setRole("CUSTOMER");
    }
    user.setPassword(hashPassword(user.getPassword()));

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(getFilePath(), true))) {
      writer.write(user.toFileString());
      writer.newLine();
    } catch (IOException e) {
      e.printStackTrace();
      return "Error saving user";
    }
    return "success";
  }

  public void updateUser(User updatedUser) {
    if (updatedUser.getPassword() != null && !isHashedPassword(updatedUser.getPassword())) {
      updatedUser.setPassword(hashPassword(updatedUser.getPassword()));
    }

    List<User> updated = getAllUsers().stream()
            .map(u -> u.getUserId().equals(updatedUser.getUserId()) ? updatedUser : u)
            .collect(Collectors.toList());
    saveAll(updated);
  }

  public void deleteUser(String userId) {
    List<User> remaining = getAllUsers().stream()
            .filter(u -> !u.getUserId().equals(userId))
            .collect(Collectors.toList());
    saveAll(remaining);
  }

  public long countByRole(String role) {
    return getAllUsers().stream()
            .filter(u -> role.equalsIgnoreCase(u.getRole()))
            .count();
  }

  public boolean isSuperAdmin(User user) {
    return user != null && "SUPER_ADMIN".equalsIgnoreCase(user.getRole());
  }

  public boolean isProtectedSuperAdmin(String userId) {
    return isSuperAdmin(getUserById(userId));
  }

  public boolean isAllowedAssignableRole(String role) {
    return "ADMIN".equalsIgnoreCase(role) || "CUSTOMER".equalsIgnoreCase(role);
  }

  public String normalizeRole(String role) {
    if ("ADMIN".equalsIgnoreCase(role)) {
      return "ADMIN";
    }
    if ("SUPER_ADMIN".equalsIgnoreCase(role)) {
      return "SUPER_ADMIN";
    }
    return "CUSTOMER";
  }

  public void createDefaultAdminIfNotExists() {
    List<User> users = getAllUsers();
    boolean superAdminExists = users.stream()
            .anyMatch(u -> "SUPER_ADMIN".equalsIgnoreCase(u.getRole()));

    if (superAdminExists) {
      return;
    }

    User existingDefaultAdmin = users.stream()
            .filter(u -> "USR-ADMIN-0001".equals(u.getUserId()))
            .findFirst()
            .orElse(null);

    if (existingDefaultAdmin != null) {
      existingDefaultAdmin.setRole("SUPER_ADMIN");
      saveAll(users);
      System.out.println("Default admin upgraded to SUPER_ADMIN.");
      return;
    }

    User superAdmin = new User();
    superAdmin.setUserId("USR-ADMIN-0001");
    superAdmin.setUsername("admin");
    superAdmin.setEmail("admin@toystore.lk");
    superAdmin.setPassword(hashPassword("admin123"));
    superAdmin.setAddress("ToyStore HQ");
    superAdmin.setPhone("0112345678");
    superAdmin.setRole("SUPER_ADMIN");

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(getFilePath(), true))) {
      writer.write(superAdmin.toFileString());
      writer.newLine();
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println("Default SUPER_ADMIN created. Please change the default password before hosting.");
  }

  private void saveAll(List<User> users) {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(getFilePath(), false))) {
      for (User u : users) {
        writer.write(u.toFileString());
        writer.newLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private boolean passwordMatches(String rawPassword, String storedPassword) {
    if (storedPassword == null) {
      return false;
    }
    if (isHashedPassword(storedPassword)) {
      return storedPassword.equals(hashPassword(rawPassword));
    }
    return storedPassword.equals(rawPassword);
  }

  private boolean isHashedPassword(String password) {
    return password != null && password.startsWith(HASH_PREFIX);
  }

  private String hashPassword(String password) {
    if (password == null) {
      return "";
    }
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashed = digest.digest(password.getBytes(StandardCharsets.UTF_8));
      return HASH_PREFIX + Base64.getEncoder().encodeToString(hashed);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is not available", e);
    }
  }
}
