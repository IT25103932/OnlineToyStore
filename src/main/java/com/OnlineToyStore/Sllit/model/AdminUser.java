package com.OnlineToyStore.Sllit.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
// Inheritance — AdminUser extends User
public class AdminUser extends User {
  public AdminUser(String userId, String username, String email,
                   String password, String address, String phone) {
    super(userId, username, email, password, address, phone, "ADMIN");
  }

  // Polymorphism — overrides getRole()
  @Override
  public String getRole() {
    return "ADMIN";
  }

  public boolean canDeleteUsers() {
    return true;
  }

  public boolean canManageToys() {
    return true;
  }
}
