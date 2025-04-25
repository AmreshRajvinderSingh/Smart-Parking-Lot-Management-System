package edu.northeastern.csye6200.models;

import java.util.UUID;

public class ParkingSlot {
    private String slotId;
    private int slotSize; // 1 for small (bikes), 2 for medium (cars), 3 for large (trucks)
    private boolean occupied;
    private Vehicle parkedVehicle;
    
    public ParkingSlot(int slotSize) {
        this.slotId = generateSlotId();
        this.slotSize = slotSize;
        this.occupied = false;
        this.parkedVehicle = null;
    }
    
    private String generateSlotId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
    public boolean canFit(Vehicle vehicle) {
        return vehicle.getRequiredSlotSize() <= this.slotSize;
    }
    
    public boolean parkVehicle(Vehicle vehicle) {
        if (!occupied && canFit(vehicle)) {
            this.parkedVehicle = vehicle;
            this.occupied = true;
            return true;
        }
        return false;
    }
    
    public Vehicle removeVehicle() {
        if (occupied) {
            Vehicle vehicle = this.parkedVehicle;
            this.parkedVehicle = null;
            this.occupied = false;
            return vehicle;
        }
        return null;
    }
    
    // Getters
    public String getSlotId() {
        return slotId;
    }
    
    public int getSlotSize() {
        return slotSize;
    }
    
    public boolean isOccupied() {
        return occupied;
    }
    
    public Vehicle getParkedVehicle() {
        return parkedVehicle;
    }
    
    public String getSlotSizeLabel() {
        switch (slotSize) {
            case 1: return "Small";
            case 2: return "Medium";
            case 3: return "Large";
            default: return "Unknown";
        }
    }
    
    @Override
    public String toString() {
        return "Slot " + slotId + " (" + getSlotSizeLabel() + ") - " + 
               (occupied ? "Occupied by " + parkedVehicle.getLicensePlate() : "Available");
    }
}