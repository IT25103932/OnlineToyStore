package com.OnlineToyStore.Sllit.model;

// Inheritance — PremiumCartItem extends CartItem
// Polymorphism — premium users get 10% discount
public class PremiumCartItem extends CartItem {

    private static final double DISCOUNT_RATE = 0.10;

    public PremiumCartItem() {
        super();
    }

    @Override
    public double getDiscountedPrice() {
        return getTotalPrice() * (1 - DISCOUNT_RATE);
    }
}