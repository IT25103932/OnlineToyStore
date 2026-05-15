package com.OnlineToyStore.Sllit.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String userId;
    private String username;
    private String email;
    private String password;
    private String address;
    private String phone;
    private String role;

    // ADMIN or CUSTOMER
    // Polymorphism — overridden in subclasses
    public String getRole() {
        return "CUSTOMER";
    }

    // Save to users.txt
    // Format: userId|username|email|password|address|phone|role
    public String toFileString() {
        return userId + "|" +
                username + "|" +
                email + "|" +
                password + "|" +
                (address != null ? address : "") + "|" +
                (phone != null ? phone : "") + "|" +
                (role != null ? role : "CUSTOMER");
    }

    // Read one line from users.txt → User object
    public static User fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        User u = new User();
        u.setUserId(p[0]);
        u.setUsername(p[1]);
        u.setEmail(p[2]);
        u.setPassword(p[3]);
        u.setAddress(p.length > 4 ? p[4] : "");
        u.setPhone(p.length > 5 ? p[5] : "");
        u.setRole(p.length > 6 ? p[6] : "CUSTOMER");
        return u;
    }
}
