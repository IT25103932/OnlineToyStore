package com.OnlineToyStore.Sllit.model;
/// /////////
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Toy {
    private String toyId;
    private String name;
    private String category; //Educational, Electronic, Soft, General
    private String description;
    private double price;
    private int stockQuantity;
    private String imageUrl;

    public Toy(String toyId, String name, String category, String description,
               double price, int stockQuantity) {
        this.toyId = toyId;
        this.name = name;
        this.category = category;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    // This method will be overridden in subclasses (Polymorphism)
    public String displayInfo(){
        return "Toy: " + name +
                " | Category: " + category +
                " | Price: Rs." + price +
                " | Stock: " + stockQuantity;
    }

    // Convert toy object to a line in toys.txt
    public String toFileString() {
        return toyId + "|" + name + "|" + category + "|" +
                description + "|" + price + "|" + stockQuantity + "|" +
                (imageUrl != null ? imageUrl : "");
    }

    // Convert a line from toys.txt back to a Toy object
    public static Toy fromFileString(String line) {
        String[] parts = line.split("\\|", -1);
        Toy toy = new Toy();
        toy.setToyId(parts[0]);
        toy.setName(parts[1]);
        toy.setCategory(parts[2]);
        toy.setDescription(parts[3]);
        toy.setPrice(Double.parseDouble(parts[4]));
        toy.setStockQuantity(Integer.parseInt(parts[5]));
        toy.setImageUrl(parts.length > 6 ? parts[6] : "");
        return toy;
    }
}
