package com.OnlineToyStore.Sllit.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
// Inheritance — CustomerUser extends User
public class CustomerUser extends User {
  private int loyaltyPoints;

  public CustomerUser(String userId, String username, String email,
                      String password, String address, String phone) {
    super(userId, username, email, password, address, phone, "CUSTOMER");
    this.loyaltyPoints = 0;
  }

  // Polymorphism — overrides getRole()
  @Override
  public String getRole() {
    return "CUSTOMER";
  }

  public void addLoyaltyPoints(int points) {
    this.loyaltyPoints += points;
  }
}
