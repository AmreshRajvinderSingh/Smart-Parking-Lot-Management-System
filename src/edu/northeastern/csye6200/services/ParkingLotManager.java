package edu.northeastern.csye6200.services;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import edu.northeastern.csye6200.models.*;

public class ParkingLotManager {
    // Singleton instance
    private static ParkingLotManager instance;
    
    // Data structures
    private Map<String, ParkingSlot> allSlots; // Map for O(1) access by ID
    private Map<String, Queue<ParkingSlot>> availableSlotsBySize; // Queue for FIFO slot assignment
    private Map<String, ParkingTicket> activeTickets; // Map of active tickets
    private Stack<ParkingTicket> completedTickets; // Stack for most recent tickets first
    private Set<String> registeredLicensePlates; // Set for quick lookup
    
    // Constructor is private (singleton pattern)
 // Protected constructor (singleton pattern)
    protected ParkingLotManager() {
        this.allSlots = new HashMap<>();
        this.availableSlotsBySize = new HashMap<>();
        this.activeTickets = new HashMap<>();
        this.completedTickets = new Stack<>();
        this.registeredLicensePlates = new HashSet<>();
        
        // Initialize available slots queues
        availableSlotsBySize.put("small", new LinkedList<>());
        availableSlotsBySize.put("medium", new LinkedList<>());
        availableSlotsBySize.put("large", new LinkedList<>());
        
        // Initialize the parking lot with slots
        initializeParkingLot(10, 10, 5); // 10 small, 10 medium, 5 large slots
    }
    
    // Singleton instance getter
    public static synchronized ParkingLotManager getInstance() {
        if (instance == null) {
            instance = new ParkingLotManager();
        }
        return instance;
    }
    
    private void initializeParkingLot(int smallSlots, int mediumSlots, int largeSlots) {
        // Create small slots (size 1)
        for (int i = 0; i < smallSlots; i++) {
            ParkingSlot slot = new ParkingSlot(1);
            allSlots.put(slot.getSlotId(), slot);
            availableSlotsBySize.get("small").add(slot);
        }
        
        // Create medium slots (size 2)
        for (int i = 0; i < mediumSlots; i++) {
            ParkingSlot slot = new ParkingSlot(2);
            allSlots.put(slot.getSlotId(), slot);
            availableSlotsBySize.get("medium").add(slot);
        }
        
        // Create large slots (size 3)
        for (int i = 0; i < largeSlots; i++) {
            ParkingSlot slot = new ParkingSlot(3);
            allSlots.put(slot.getSlotId(), slot);
            availableSlotsBySize.get("large").add(slot);
        }
    }
    
    public ParkingTicket parkVehicle(Vehicle vehicle) {
        // Check if vehicle is already parked
        if (registeredLicensePlates.contains(vehicle.getLicensePlate())) {
            return null;
        }
        
        // Find appropriate slot based on vehicle size
        ParkingSlot slot = findAvailableSlot(vehicle);
        
        if (slot == null) {
            return null; // No available slot
        }
        
        // Park the vehicle in the slot
        slot.parkVehicle(vehicle);
        
        // Create and register the ticket
        ParkingTicket ticket = new ParkingTicket(vehicle, slot);
        activeTickets.put(ticket.getTicketId(), ticket);
        registeredLicensePlates.add(vehicle.getLicensePlate());
        
        return ticket;
    }
    
    private ParkingSlot findAvailableSlot(Vehicle vehicle) {
        int requiredSize = vehicle.getRequiredSlotSize();
        
        // Try to find a slot that matches the exact size first (optimal allocation)
        Queue<ParkingSlot> exactSizeQueue = null;
        
        switch (requiredSize) {
            case 1:
                exactSizeQueue = availableSlotsBySize.get("small");
                break;
            case 2:
                exactSizeQueue = availableSlotsBySize.get("medium");
                break;
            case 3:
                exactSizeQueue = availableSlotsBySize.get("large");
                break;
        }
        
        if (exactSizeQueue != null && !exactSizeQueue.isEmpty()) {
            ParkingSlot slot = exactSizeQueue.poll();
            return slot;
        }
        
        // If no exact match, try larger slots
        if (requiredSize == 1) {
            if (!availableSlotsBySize.get("medium").isEmpty()) {
                return availableSlotsBySize.get("medium").poll();
            }
            if (!availableSlotsBySize.get("large").isEmpty()) {
                return availableSlotsBySize.get("large").poll();
            }
        } else if (requiredSize == 2) {
            if (!availableSlotsBySize.get("large").isEmpty()) {
                return availableSlotsBySize.get("large").poll();
            }
        }
        
        return null; // No slot found
    }
    
    public ParkingTicket exitVehicle(String ticketId) {
        ParkingTicket ticket = activeTickets.get(ticketId);
        
        if (ticket == null) {
            return null; // Ticket not found
        }
        
        // Close the ticket and calculate fee
        ticket.closeTicket();
        
        // Free up the parking slot
        ParkingSlot slot = ticket.getSlot();
        Vehicle vehicle = slot.removeVehicle();
        
        // Update data structures
        activeTickets.remove(ticketId);
        completedTickets.push(ticket);
        registeredLicensePlates.remove(vehicle.getLicensePlate());
        
        // Add slot back to available queue
        switch (slot.getSlotSize()) {
            case 1:
                availableSlotsBySize.get("small").add(slot);
                break;
            case 2:
                availableSlotsBySize.get("medium").add(slot);
                break;
            case 3:
                availableSlotsBySize.get("large").add(slot);
                break;
        }
        
        return ticket;
    }
    // Returns all slots (both available and occupied)
    public List<ParkingSlot> getAllSlots() {
        return new ArrayList<>(allSlots.values());
    }
    
    // Returns only available slots
    public List<ParkingSlot> getAvailableSlots() {
        List<ParkingSlot> available = new ArrayList<>();
        availableSlotsBySize.forEach((size, queue) -> {
            available.addAll(queue);
        });
        return available;
    }
    
    // Returns only occupied slots
    public List<ParkingSlot> getOccupiedSlots() {
        return allSlots.values().stream()
                .filter(ParkingSlot::isOccupied)
                .collect(Collectors.toList());
    }
    
    // Returns all active tickets
    public List<ParkingTicket> getActiveTickets() {
        return new ArrayList<>(activeTickets.values());
    }
    
    // Returns completed tickets (most recent first)
    public List<ParkingTicket> getCompletedTickets() {
        return new ArrayList<>(completedTickets);
    }
    
    // Get ticket by ID
    public ParkingTicket getTicketById(String ticketId) {
        // Check active tickets first
        if (activeTickets.containsKey(ticketId)) {
            return activeTickets.get(ticketId);
        }
        
        // Otherwise, search in completed tickets (less efficient)
        for (ParkingTicket ticket : completedTickets) {
            if (ticket.getTicketId().equals(ticketId)) {
                return ticket;
            }
        }
        
        return null;
    }
    
    // Search for vehicle by license plate (demonstrating search algorithm)
    public Vehicle findVehicleByLicensePlate(String licensePlate) {
        // First check if vehicle is currently parked
        if (!registeredLicensePlates.contains(licensePlate)) {
            return null;
        }
        
        // Find the vehicle in active tickets
        for (ParkingTicket ticket : activeTickets.values()) {
            if (ticket.getVehicle().getLicensePlate().equals(licensePlate)) {
                return ticket.getVehicle();
            }
        }
        
        return null;
    }
    
    // Get statistics of parking lot usage
    public Map<String, Object> getParkingStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Total slots
        stats.put("totalSlots", allSlots.size());
        
        // Available and occupied slots
        stats.put("availableSlots", getAvailableSlots().size());
        stats.put("occupiedSlots", getOccupiedSlots().size());
        
        // Occupancy rate
        double occupancyRate = (double) getOccupiedSlots().size() / allSlots.size() * 100;
        stats.put("occupancyRate", occupancyRate);
        
        // Vehicles by type
        Map<String, Integer> vehicleTypes = new HashMap<>();
        for (ParkingTicket ticket : activeTickets.values()) {
            String type = ticket.getVehicle().getVehicleType();
            vehicleTypes.put(type, vehicleTypes.getOrDefault(type, 0) + 1);
        }
        stats.put("vehicleTypes", vehicleTypes);
        
        // Revenue calculation
        double totalRevenue = 0.0;
        for (ParkingTicket ticket : completedTickets) {
            totalRevenue += ticket.getFee();
        }
        stats.put("totalRevenue", totalRevenue);
        
        return stats;
    }
}