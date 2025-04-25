package edu.northeastern.csye6200.models;

import java.time.LocalDateTime;

public abstract class Vehicle {
    private String licensePlate;
    private String brand;
    private String model;
    private String color;
    private LocalDateTime entryTime;
    private String userId;
    
    // Constructor
    public Vehicle(String licensePlate, String brand, String model, String color) {
        this.licensePlate = licensePlate;
        this.brand = brand;
        this.model = model;
        this.color = color;
        this.entryTime = null; // Will be set when vehicle enters the parking lot
    }
    
    // Abstract method for calculating parking fee - polymorphism will be used here
    public abstract double calculateParkingFee(LocalDateTime exitTime);
    
    // Abstract method to get vehicle type
    public abstract String getVehicleType();
    
    // Abstract method to get required slot size
    public abstract int getRequiredSlotSize();
    
    // Getters and setters
    public String getLicensePlate() {
        return licensePlate;
    }
    
    public String getBrand() {
        return brand;
    }
    
    public String getModel() {
        return model;
    }
    
    public String getColor() {
        return color;
    }
    
    public LocalDateTime getEntryTime() {
        return entryTime;
    }
    
    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    @Override
    public String toString() {
        return getVehicleType() + " [" + licensePlate + "] - " + brand + " " + model + " (" + color + ")";
    }
}