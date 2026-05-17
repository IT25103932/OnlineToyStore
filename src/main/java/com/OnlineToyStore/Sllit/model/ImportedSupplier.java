package com.OnlineToyStore.Sllit.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
// Inheritance — ImportedSupplier extends Supplier
public class ImportedSupplier extends Supplier {

    private String country;

    public ImportedSupplier(String supplierId, String name,
                            String contactPerson, String email,
                            String phone, String address,
                            String supplyCategory, String country) {
        super(supplierId, name, contactPerson,
                email, phone, address, supplyCategory);
        this.country = country;
    }

    // Polymorphism — overrides parent getRestockMethod()
    @Override
    public String getRestockMethod() {
        return "International shipment from " + country + " — 7-14 days";
    }
}