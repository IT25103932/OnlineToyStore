package com.OnlineToyStore.Sllit.controller;

import com.OnlineToyStore.Sllit.model.User;
import com.OnlineToyStore.Sllit.service.EmailService;
import com.OnlineToyStore.Sllit.service.UserService;
import com.OnlineToyStore.Sllit.util.SessionHelper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Random;

@Controller
@RequestMapping("/users")
public class UserController {

  @Autowired
  private UserService userService;

  @Autowired
  private EmailService emailService;

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
    model.addAttribute("adminCount",
            userService.countByRole("ADMIN") + userService.countByRole("SUPER_ADMIN"));
    model.addAttribute("superAdminCount", userService.countByRole("SUPER_ADMIN"));
    return "user/list";
  }

  // ── Show Registration form ────────────────────────
  @GetMapping("/register")
  public String showRegisterForm(HttpSession session, Model model) {
    if (SessionHelper.isLoggedIn(session) && !SessionHelper.isAdmin(session)) {
      return "redirect:/";
    }
    model.addAttribute("user", new User());
    return "user/register";
  }

  // ── Handle Registration ───────────────────────────
  @PostMapping("/register")
  public String registerUser(@ModelAttribute User user,
                             HttpSession session,
                             Model model) {
    boolean adminCreatingUser = SessionHelper.isAdmin(session);
    if (SessionHelper.isLoggedIn(session) && !adminCreatingUser) {
      return "redirect:/access-denied";
    }

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
    if (adminCreatingUser) {
      emailService.sendWelcomeEmail(user, null);
      return "redirect:/users";
    }
    String verificationCode = generateOtp();
    session.setAttribute("pendingVerificationEmail", user.getEmail());
    session.setAttribute("pendingVerificationCode", verificationCode);
    emailService.sendWelcomeEmail(user, verificationCode);
    return "redirect:/users/verify-email";
  }

  @GetMapping("/verify-email")
  public String showVerifyEmail(HttpSession session, Model model) {
    Object email = session.getAttribute("pendingVerificationEmail");
    if (email == null) {
      return "redirect:/users/login";
    }
    model.addAttribute("email", email.toString());
    return "user/verify-email";
  }

  @PostMapping("/verify-email")
  public String verifyEmail(@RequestParam String code,
                            HttpSession session,
                            Model model) {
    Object expected = session.getAttribute("pendingVerificationCode");
    Object email = session.getAttribute("pendingVerificationEmail");
    if (expected == null || email == null) {
      return "redirect:/users/login";
    }
    if (!expected.toString().equals(code.trim())) {
      model.addAttribute("email", email.toString());
      model.addAttribute("error", "Invalid verification code.");
      return "user/verify-email";
    }
    session.removeAttribute("pendingVerificationCode");
    session.removeAttribute("pendingVerificationEmail");
    return "redirect:/users/login?verified=true";
  }

  // ── Show Login form ───────────────────────────────
  @GetMapping("/login")
  public String showLoginForm(HttpSession session,
                              @RequestParam(required = false) String registered,
                              @RequestParam(required = false) String verified,
                              @RequestParam(required = false) String reset,
                              @RequestParam(required = false) String logout,
                              Model model) {

    if (SessionHelper.isLoggedIn(session)) {
      return "redirect:/";
    }
    if (registered != null) {
      model.addAttribute("success", "Registration successful! Please log in.");
    }
    if (verified != null) {
      model.addAttribute("success", "Email verified successfully. Please log in.");
    }
    if (reset != null) {
      model.addAttribute("success", "Password reset successfully. Please log in.");
    }
    if (logout != null) {
      model.addAttribute("success", "You have been logged out.");
    }
    return "user/login";
  }

  @GetMapping("/forgot-password")
  public String showForgotPassword(HttpSession session) {
    if (SessionHelper.isLoggedIn(session)) {
      return "redirect:/";
    }
    return "user/forgot-password";
  }

  @PostMapping("/forgot-password")
  public String sendResetOtp(@RequestParam String email,
                             HttpSession session,
                             Model model) {
    User user = userService.getUserByEmail(email.trim());
    if (user == null) {
      model.addAttribute("error", "No account found for that email address.");
      return "user/forgot-password";
    }
    String otp = generateOtp();
    session.setAttribute("resetEmail", user.getEmail());
    session.setAttribute("resetUserId", user.getUserId());
    session.setAttribute("resetOtp", otp);
    emailService.sendPasswordResetOtp(user, otp);
    return "redirect:/users/reset-password";
  }

  @GetMapping("/reset-password")
  public String showResetPassword(HttpSession session, Model model) {
    Object email = session.getAttribute("resetEmail");
    if (email == null) {
      return "redirect:/users/forgot-password";
    }
    model.addAttribute("email", email.toString());
    return "user/reset-password";
  }

  @PostMapping("/reset-password")
  public String resetPassword(@RequestParam String otp,
                              @RequestParam String password,
                              @RequestParam String confirmPassword,
                              HttpSession session,
                              Model model) {
    Object expectedOtp = session.getAttribute("resetOtp");
    Object userId = session.getAttribute("resetUserId");
    Object email = session.getAttribute("resetEmail");
    if (expectedOtp == null || userId == null || email == null) {
      return "redirect:/users/forgot-password";
    }
    if (!expectedOtp.toString().equals(otp.trim())) {
      model.addAttribute("email", email.toString());
      model.addAttribute("error", "Invalid OTP.");
      return "user/reset-password";
    }
    if (password == null || password.length() < 6) {
      model.addAttribute("email", email.toString());
      model.addAttribute("error", "Password must be at least 6 characters.");
      return "user/reset-password";
    }
    if (!password.equals(confirmPassword)) {
      model.addAttribute("email", email.toString());
      model.addAttribute("error", "Passwords do not match.");
      return "user/reset-password";
    }
    userService.updatePassword(userId.toString(), password);
    session.removeAttribute("resetOtp");
    session.removeAttribute("resetUserId");
    session.removeAttribute("resetEmail");
    return "redirect:/users/login?reset=true";
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
    if ("ADMIN".equalsIgnoreCase(user.getRole()) ||
            "SUPER_ADMIN".equalsIgnoreCase(user.getRole())) {
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
    User targetUser = userService.getUserById(userId);
    if (targetUser == null) {
      return "redirect:/users";
    }
    if (userService.isSuperAdmin(targetUser) &&
            !SessionHelper.isSuperAdmin(session)) {
      return "redirect:/users?error=Super+admin+account+is+protected";
    }
    model.addAttribute("user", targetUser);
    model.addAttribute("canManageRoles",
            SessionHelper.isSuperAdmin(session) &&
                    !userService.isSuperAdmin(targetUser));
    model.addAttribute("canDeleteUser",
            canDeleteTargetUser(session, loggedIn, targetUser));
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

    User loggedIn = SessionHelper.getUser(session);
    User existingUser = userService.getUserById(userId);
    if (existingUser == null) {
      return "redirect:/users";
    }

    if (!SessionHelper.isAdmin(session) &&
            !loggedIn.getUserId().equals(userId)) {
      return "redirect:/access-denied";
    }

    if (userService.isSuperAdmin(existingUser) &&
            !SessionHelper.isSuperAdmin(session)) {
      return "redirect:/users?error=Super+admin+account+is+protected";
    }

    // Validate password length
    if (user.getPassword() != null &&
            !user.getPassword().isEmpty() &&
            user.getPassword().length() < 6) {
      return "redirect:/users/edit/" + userId + "?error=Password+must+be+at+least+6+characters";
    }

    user.setUserId(userId);
    if (user.getPassword() == null || user.getPassword().isEmpty()) {
      user.setPassword(existingUser.getPassword());
    }

    if (userService.isSuperAdmin(existingUser)) {
      user.setRole("SUPER_ADMIN");
    } else if (SessionHelper.isSuperAdmin(session) &&
            userService.isAllowedAssignableRole(user.getRole())) {
      user.setRole(userService.normalizeRole(user.getRole()));
    } else {
      user.setRole(existingUser.getRole());
    }

    userService.updateUser(user);

    // Update session if user edited their own profile
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
    if (userService.isProtectedSuperAdmin(userId)) {
      return "redirect:/users?error=Super+admin+account+cannot+be+deleted";
    }
    User targetUser = userService.getUserById(userId);
    if (!canDeleteTargetUser(session, loggedIn, targetUser)) {
      return "redirect:/users?error=Only+super+admin+can+delete+admin+accounts";
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

  private boolean canDeleteTargetUser(HttpSession session, User loggedIn, User targetUser) {
    if (loggedIn == null || targetUser == null) {
      return false;
    }
    if (loggedIn.getUserId().equals(targetUser.getUserId())) {
      return false;
    }
    if (userService.isSuperAdmin(targetUser)) {
      return false;
    }
    if (SessionHelper.isSuperAdmin(session)) {
      return true;
    }
    return SessionHelper.isAdmin(session) &&
            "CUSTOMER".equalsIgnoreCase(targetUser.getRole());
  }

  private String generateOtp() {
    return String.valueOf(100000 + new Random().nextInt(900000));
  }
}
