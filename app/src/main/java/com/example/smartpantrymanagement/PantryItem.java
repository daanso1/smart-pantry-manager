package com.example.smartpantrymanagement;

/**
 * Model class representing one ingredient the user currently has at home.
 * Maps to one row in the "pantry" table.
 */
public class PantryItem {
    private long id;
    private String name;
    private double quantity;
    private String unit;        // e.g. "g", "kg", "ml", "l", "pcs"
    private String expiryDate;  // optional, format yyyy-MM-dd (may be empty)

    public PantryItem(long id, String name, double quantity, String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getExpiryDate() { return expiryDate; }

    public void setName(String name) { this.name = name; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
}
