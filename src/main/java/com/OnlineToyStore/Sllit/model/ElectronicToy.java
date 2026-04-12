package com.OnlineToyStore.Sllit.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ElectronicToy extends Toy {

    private String batteryType;       // e.g. "AA", "AAA", "USB"
    private boolean requiresAssembly;

    public ElectronicToy(String toyId, String name, String description,
                         double price, int stockQuantity,
                         String batteryType, boolean requiresAssembly) {
        super(toyId, name, "ELECTRONIC", description, price, stockQuantity);
        this.batteryType = batteryType;
        this.requiresAssembly = requiresAssembly;
    }

    // Polymorphism - overrides parent displayInfo()
    @Override
    public String displayInfo() {
        return super.displayInfo() +
                " | Battery: " + batteryType +
                " | Assembly Required: " + (requiresAssembly ? "Yes" : "No");
    }
}