package com.OnlineToyStore.Sllit.util;

import com.OnlineToyStore.Sllit.model.User;
import jakarta.servlet.http.HttpSession;

public class SessionHelper {

    private static final String SESSION_KEY = "loggedInUser";

    // Store user in session after login
    public static void setUser(HttpSession session, User user) {
        session.setAttribute(SESSION_KEY, user);
    }

    // Get currently logged-in user
    public static User getUser(HttpSession session) {
        return (User) session.getAttribute(SESSION_KEY);
    }

    // Check if anyone is logged in
    public static boolean isLoggedIn(HttpSession session) {
        return session.getAttribute(SESSION_KEY) != null;
    }

    // Check if logged-in user is ADMIN
    public static boolean isAdmin(HttpSession session) {
        User user = getUser(session);
        return user != null &&
                "ADMIN".equalsIgnoreCase(user.getRole());
    }

    // Check if logged-in user is CUSTOMER
    public static boolean isCustomer(HttpSession session) {
        User user = getUser(session);
        return user != null &&
                "CUSTOMER".equalsIgnoreCase(user.getRole());
    }

    // Clear session on logout
    public static void logout(HttpSession session) {
        session.invalidate();
    }

    // Get username for display
    public static String getUsername(HttpSession session) {
        User user = getUser(session);
        return user != null ? user.getUsername() : "Guest";
    }
}