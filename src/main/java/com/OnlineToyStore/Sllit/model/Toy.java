package com.OnlineToyStore.Sllit.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Toy {
    private String toyId;
    private String name;
    private String category;
    private String description;
    private double price;
    private int stockQuantity;
}