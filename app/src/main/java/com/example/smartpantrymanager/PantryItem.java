package com.example.smartpantrymanager;

/** Model: one ingredient the user has at home (one row of the "pantry" table). */
public class PantryItem {
    private final int id;
    private final String name;
    private final double quantity;
    private final String unit;
    private final String expiryDate; // optional, format yyyy-MM-dd, "" when not set

    public PantryItem(int id, String name, double quantity, String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getExpiryDate() { return expiryDate; }
}
