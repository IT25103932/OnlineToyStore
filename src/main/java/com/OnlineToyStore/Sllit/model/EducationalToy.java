package com.OnlineToyStore.Sllit.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EducationalToy extends Toy {

    private String ageRange;   // e.g. "3-6 years"
    private String subject;    // e.g. "Mathematics", "Science"

    public EducationalToy(String toyId, String name, String description,
                          double price, int stockQuantity,
                          String ageRange, String subject) {
        super(toyId, name, "EDUCATIONAL", description, price, stockQuantity);
        this.ageRange = ageRange;
        this.subject = subject;
    }

    // Polymorphism - overrides parent displayInfo()
    @Override
    public String displayInfo() {
        return super.displayInfo() +
                " | Age Range: " + ageRange +
                " | Subject: " + subject;
    }
}