package edu.northeastern.csye6200.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class ParkingTicket {
    private String ticketId;
    private Vehicle vehicle;
    private ParkingSlot slot;
    private LocalDateTime issueTime;
    private LocalDateTime exitTime;
    private double fee;
    private boolean active;
    
    // Constructor for new ticket
    public ParkingTicket(Vehicle vehicle, ParkingSlot slot) {
        this.ticketId = generateTicketId();
        this.vehicle = vehicle;
        this.slot = slot;
        this.issueTime = LocalDateTime.now();
        this.vehicle.setEntryTime(this.issueTime);
        this.exitTime = null;
        this.fee = 0.0;
        this.active = true;
    }
    
    private String generateTicketId() {
        return "TKT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
    
    public void closeTicket() {
        if (active) {
            this.exitTime = LocalDateTime.now();
            this.fee = vehicle.calculateParkingFee(exitTime);
            this.active = false;
        }
    }
    
    // Getters
    public String getTicketId() {
        return ticketId;
    }
    
    public Vehicle getVehicle() {
        return vehicle;
    }
    
    public ParkingSlot getSlot() {
        return slot;
    }
    
    public LocalDateTime getIssueTime() {
        return issueTime;
    }
    
    public LocalDateTime getExitTime() {
        return exitTime;
    }
    
    public double getFee() {
        return fee;
    }
    
    public boolean isActive() {
        return active;
    }
    
    // Formatted time strings
    public String getFormattedIssueTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return issueTime.format(formatter);
    }
    
    public String getFormattedExitTime() {
        if (exitTime == null) {
            return "N/A";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return exitTime.format(formatter);
    }
    
    @Override
    public String toString() {
        return "Ticket " + ticketId + " for " + vehicle.getLicensePlate() + 
               " at Slot " + slot.getSlotId() + " - " + 
               (active ? "Active" : "Closed - Fee: $" + String.format("%.2f", fee));
    }
}