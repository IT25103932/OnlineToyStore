package com.OnlineToyStore.Sllit.controller;

import com.OnlineToyStore.Sllit.model.User;
import com.OnlineToyStore.Sllit.service.UserService;
import com.OnlineToyStore.Sllit.util.SessionHelper;
import jakarta.servlet.http.HttpSession;
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

  // ── User list — ADMIN ONLY ────────────────────────
  @GetMapping
  public String listUsers(HttpSession session, Model model) {
    if (!SessionHelper.isAdmin(session)) {
      return "redirect:/access-denied";
    }
    List<User> users = userService.getAllUsers();
    model.addAttribute("users", users);
    model.addAttribute("totalCount", users.size());
    model.addAttribute("customerCount", userService.countByRole("CUSTOMER"));
    model.addAttribute("adminCount", userService.countByRole("ADMIN"));
    return "user/list";
  }

  // ── Show Registration form ────────────────────────
  @GetMapping("/register")
  public String showRegisterForm(HttpSession session, Model model) {
    // Already logged in? Redirect home
    if (SessionHelper.isLoggedIn(session)) {
      return "redirect:/";
    }
    model.addAttribute("user", new User());
    return "user/register";
  }

  // ── Handle Registration ───────────────────────────
  @PostMapping("/register")
  public String registerUser(@ModelAttribute User user,
                             Model model) {
    // Validate required fields
    if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
      model.addAttribute("error", "Username is required.");
      return "user/register";
    }
    if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
      model.addAttribute("error", "Email is required.");
      return "user/register";
    }
    if (user.getPassword() == null || user.getPassword().length() < 6) {
      model.addAttribute("error", "Password must be at least 6 characters.");
      model.addAttribute("user", user);
      return "user/register";
    }

    // Customers can only register as CUSTOMER
    user.setRole("CUSTOMER");

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
  public String showLoginForm(HttpSession session,
                              @RequestParam(required = false) String registered,
                              @RequestParam(required = false) String logout,
                              Model model) {

    if (SessionHelper.isLoggedIn(session)) {
      return "redirect:/";
    }
    if (registered != null) {
      model.addAttribute("success", "Registration successful! Please log in.");
    }
    if (logout != null) {
      model.addAttribute("success", "You have been logged out.");
    }
    return "user/login";
  }

  // ── Handle Login ──────────────────────────────────
  @PostMapping("/login")
  public String login(@RequestParam String username,
                      @RequestParam String password,
                      HttpSession session,
                      Model model) {

    // Validate inputs
    if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
      model.addAttribute("error", "Please enter username and password.");
      return "user/login";
    }

    User user = userService.login(username.trim(), password.trim());
    if (user == null) {
      model.addAttribute("error", "Invalid username or password.");
      return "user/login";
    }

    // Store in session
    SessionHelper.setUser(session, user);

    // Redirect based on role
    if ("ADMIN".equalsIgnoreCase(user.getRole())) {
      return "redirect:/dashboard";
    }
    return "redirect:/toys";
  }

  // ── Logout ────────────────────────────────────────
  @GetMapping("/logout")
  public String logout(HttpSession session) {
    SessionHelper.logout(session);
    return "redirect:/users/login?logout=true";
  }

  // ── Show Edit Profile form ────────────────────────
  @GetMapping("/edit/{userId}")
  public String showEditForm(@PathVariable String userId,
                             HttpSession session,
                             Model model) {
    if (!SessionHelper.isLoggedIn(session)) {
      return "redirect:/users/login";
    }
    User loggedIn = SessionHelper.getUser(session);
    // User can only edit their own profile (unless admin)
    if (!SessionHelper.isAdmin(session) &&
            !loggedIn.getUserId().equals(userId)) {
      return "redirect:/access-denied";
    }
    model.addAttribute("user", userService.getUserById(userId));
    return "user/edit";
  }

  // ── Handle Profile Update ─────────────────────────
  @PostMapping("/edit/{userId}")
  public String updateUser(@PathVariable String userId,
                           @ModelAttribute User user,
                           HttpSession session) {
    if (!SessionHelper.isLoggedIn(session)) {
      return "redirect:/users/login";
    }
    user.setUserId(userId);

    // Validate password length
    if (user.getPassword() != null &&
            !user.getPassword().isEmpty() &&
            user.getPassword().length() < 6) {
      return "redirect:/users/edit/" + userId + "?error=Password+must+be+at+least+6+characters";
    }

    userService.updateUser(user);

    // Update session if user edited their own profile
    User loggedIn = SessionHelper.getUser(session);
    if (loggedIn.getUserId().equals(userId)) {
      SessionHelper.setUser(session, user);
    }

    if (SessionHelper.isAdmin(session)) {
      return "redirect:/users";
    }
    return "redirect:/";
  }

  // ── Delete user — ADMIN ONLY ──────────────────────
  @GetMapping("/delete/{userId}")
  public String deleteUser(@PathVariable String userId,
                           HttpSession session) {
    if (!SessionHelper.isAdmin(session)) {
      return "redirect:/access-denied";
    }
    // Prevent admin from deleting themselves
    User loggedIn = SessionHelper.getUser(session);
    if (loggedIn.getUserId().equals(userId)) {
      return "redirect:/users?error=Cannot+delete+your+own+account";
    }
    userService.deleteUser(userId);
    return "redirect:/users";
  }

  // ── Search users — ADMIN ONLY ─────────────────────
  @GetMapping("/search")
  public String searchUsers(@RequestParam(required = false) String keyword,
                            HttpSession session,
                            Model model) {
    if (!SessionHelper.isAdmin(session)) {
      return "redirect:/access-denied";
    }
    if (keyword != null && !keyword.isEmpty()) {
      model.addAttribute("users", userService.searchUsers(keyword));
      model.addAttribute("keyword", keyword);
    } else {
      model.addAttribute("users", userService.getAllUsers());
    }
    return "user/search";
  }

  // ── My Profile page ───────────────────────────────
  @GetMapping("/profile")
  public String myProfile(HttpSession session, Model model) {
    if (!SessionHelper.isLoggedIn(session)) {
      return "redirect:/users/login";
    }
    User user = SessionHelper.getUser(session);
    model.addAttribute("user", user);
    return "user/profile";
  }
}