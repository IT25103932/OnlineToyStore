package com.OnlineToyStore.Sllit.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
// Inheritance — LocalSupplier extends Supplier
public class LocalSupplier extends Supplier {

    private String district;

    public LocalSupplier(String supplierId, String name,
                         String contactPerson, String email,
                         String phone, String address,
                         String  supplyCategory, String district) {
        super(supplierId, name, contactPerson,
                email, phone, address, supplyCategory);
        this.district = district;
    }

    // Polymorphism — overrides parent getRestockMethod()
    @Override
    public String getRestockMethod() {
        return "Local delivery from " + district + " — 1-2 days";
    }
}