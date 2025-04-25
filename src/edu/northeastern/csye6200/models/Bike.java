package edu.northeastern.csye6200.models;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Bike extends Vehicle {
    private static final double HOURLY_RATE = 2.0; // $2 per hour for bikes
    private final int requiredSlotSize = 1; // Bikes need smaller slots
    private double discountRate = 0.0; // Percentage discount (0-100)
    
    public Bike(String licensePlate, String brand, String model, String color) {
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
        return "Bike";
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