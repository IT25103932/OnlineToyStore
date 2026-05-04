package com.OnlineToyStore.Sllit.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
// Encapsulation — all fields are private, accessed via getters/setters (Lombok)
public class Supplier {

    private String supplierId;
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private String supplyCategory; // EDUCATIONAL, ELECTRONIC, SOFT, GENERAL, ALL

    // Abstraction — restock method hidden inside class
    // Polymorphism — overridden  in subclasses
    public String getRestockMethod() {
        return "Standard restock order";
    }

    // Save to suppliers.txt
    // Format: supplierId|name|contactPerson|email|phone|address|supplyCategory
    public String toFileString() {
        return supplierId + "|" +
                name + "|" +
                (contactPerson != null ? contactPerson : "") + "|" +
                (email != null ? email : "") + "|" +
                (phone != null ? phone : "") + "|" +
                (address != null ? address : "") + "|" +
                (supplyCategory != null ? supplyCategory : "ALL");
    }

    // Read one line from suppliers.txt → Supplier object
    public static Supplier fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        Supplier s = new Supplier();
        s.setSupplierId(p[0]);
        s.setName(p[1]);
        s.setContactPerson(p.length > 2 ? p[2] : "");
        s.setEmail(p.length > 3 ? p[3] : "");
        s.setPhone(p.length > 4 ? p[4] : "");
        s.setAddress(p.length > 5 ? p[5] : "");
        s.setSupplyCategory(p.length > 6 ? p[6] : "ALL");
        return s;
    }
}