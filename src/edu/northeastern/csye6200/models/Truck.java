package edu.northeastern.csye6200.models;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Truck extends Vehicle {
    private static final double HOURLY_RATE = 8.0; // $8 per hour for trucks
    private final int requiredSlotSize = 3; // Trucks need larger slots
    private double discountRate = 0.0; // Percentage discount (0-100)
    
    public Truck(String licensePlate, String brand, String model, String color) {
        super(licensePlate, brand, model, color);
    }
    
    @Override
    public double calculateParkingFee(LocalDateTime exitTime) {
        if (getEntryTime() == null || exitTime == null) {
            return 0;
        }

        // Calculate total minutes parked
        long minutesParked = ChronoUnit.MINUTES.between(getEntryTime(), exitTime);

        // Always round up to at least 1 hour
        double hoursParked = Math.max(1, Math.ceil(minutesParked / 60.0));

        // Calculate base fee
        double baseFee = hoursParked * HOURLY_RATE;

        // Apply membership discount
        double discountAmount = baseFee * (discountRate / 100.0);

        return baseFee - discountAmount;
    }
    
    @Override
    public String getVehicleType() {
        return "Truck";
    }
    
    @Override
    public int getRequiredSlotSize() {
        return requiredSlotSize;
    }
    
    public void setDiscountRate(double discountRate) {
        this.discountRate = discountRate;
    }
    
    public double getDiscountRate() {
        return discountRate;
    }
}