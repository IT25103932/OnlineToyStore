package com.OnlineToyStore.Sllit.controller;

import com.OnlineToyStore.Sllit.model.User;
import com.OnlineToyStore.Sllit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {
  @Autowired
  private UserService userService;

  // ── User list (Admin view) ────────────────────────
  @GetMapping
  public String listUsers(Model model) {
    List<User> users = userService.getAllUsers();
    model.addAttribute("users", users);
    model.addAttribute("totalCount", users.size());
    model.addAttribute("customerCount",
        userService.countByRole("CUSTOMER"));
    model.addAttribute("adminCount",
        userService.countByRole("ADMIN"));
    return "user/list";
  }

  // ── Show Registration form ────────────────────────
  @GetMapping("/register")
  public String showRegisterForm(Model model) {
    model.addAttribute("user", new User());
    return "user/register";
  }

  // ── Handle Registration ───────────────────────────
  @PostMapping("/register")
  public String registerUser(@ModelAttribute User user, Model model) {
    String result = userService.registerUser(user);
    if (!result.equals("success")) {
      model.addAttribute("error", result);
      model.addAttribute("user", user);
      return "user/register";
    }
    return "redirect:/users/login?registered=true";
  }

  // ── Show Login form ───────────────────────────────
  @GetMapping("/login")
  public String showLoginForm(
      @RequestParam(required = false) String registered,
      Model model) {
    if (registered != null) {
      model.addAttribute("success",
          "Registration successful! Please log in.");
    }
    return "user/login";
  }

  // ── Handle Login ──────────────────────────────────
  @PostMapping("/login")
  public String login(@RequestParam String username,
      @RequestParam String password,
      Model model) {
    User user = userService.login(username, password);
    if (user == null) {
      model.addAttribute("error",
          "Invalid username or password.");
      return "user/login";
    }
    // For now redirect to dashboard
    // (when session management is added, store user in session here)
    return "redirect:/";
  }

  // ── Show Profile / Edit form ──────────────────────
  @GetMapping("/edit/{userId}")
  public String showEditForm(@PathVariable String userId,
      Model model) {
    User user = userService.getUserById(userId);
    model.addAttribute("user", user);
    return "user/edit";
  }

  // ── Handle Profile Update ─────────────────────────
  @PostMapping("/edit/{userId}")
  public String updateUser(@PathVariable String userId,
      @ModelAttribute User user) {
    user.setUserId(userId);
    userService.updateUser(user);
    return "redirect:/users";
  }

  // ── Delete user (Admin only) ──────────────────────
  @GetMapping("/delete/{userId}")
  public String deleteUser(@PathVariable String userId) {
    userService.deleteUser(userId);
    return "redirect:/users";
  }

  // ── Search users ──────────────────────────────────
  @GetMapping("/search")
  public String searchUsers(
      @RequestParam(required = false) String keyword,
      Model model) {
    if (keyword != null && !keyword.isEmpty()) {
      model.addAttribute("users",
          userService.searchUsers(keyword));
      model.addAttribute("keyword", keyword);
    } else {
      model.addAttribute("users", userService.getAllUsers());
    }
    return "user/search";
  }
}
