package com.OnlineToyStore.Sllit.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SoftToy extends Toy {

    private String material;  // e.g. "Cotton", "Polyester"
    private String size;      // e.g. "Small", "Medium", "Large"

    public SoftToy(String toyId, String name, String description,
                   double price, int stockQuantity,
                   String material, String size) {
        super(toyId, name, "SOFT", description, price, stockQuantity);
        this.material = material;
        this.size = size;
    }

    // Polymorphism - overrides parent displayInfo()
    @Override
    public String displayInfo() {
        return super.displayInfo() +
                " | Material: " + material +
                " | Size: " + size;
    }
}