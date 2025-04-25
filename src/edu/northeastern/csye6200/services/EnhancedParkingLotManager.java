package edu.northeastern.csye6200.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import edu.northeastern.csye6200.models.*;

public class EnhancedParkingLotManager extends ParkingLotManager {
    // Singleton instance
    private static EnhancedParkingLotManager instance;
    
    // Additional data structures
    private Map<String, Membership> memberships; // membershipId -> Membership
    private Map<String, String> vehicleToUserMap; // licensePlate -> userId
    private ParkingHistory parkingHistory;
    private Map<String, List<ParkingSlot>> reservedSlots; // userId -> List of reserved slots
    
    // File paths for persistence
    private final String SLOTS_DATA_FILE = "slots.dat";
    private final String TICKETS_DATA_FILE = "tickets.dat";
    private final String MEMBERSHIPS_DATA_FILE = "memberships.dat";
    
    // Private constructor (singleton pattern)
    private EnhancedParkingLotManager() {
        super(); // Initialize base ParkingLotManager
        
        this.memberships = new HashMap<>();
        this.vehicleToUserMap = new HashMap<>();
        this.parkingHistory = new ParkingHistory();
        this.reservedSlots = new HashMap<>();
        
        // Load data from files
        loadPersistedData();
    }
    
    // Singleton instance getter
    public static synchronized EnhancedParkingLotManager getInstance() {
        if (instance == null) {
            instance = new EnhancedParkingLotManager();
        }
        return instance;
    }
    
    // Membership management
    public Membership createMembership(User user, Membership.MembershipTier tier, 
                                      LocalDate startDate, LocalDate endDate) {
        Membership membership = new Membership(user, tier, startDate, endDate);
        memberships.put(membership.getMembershipId(), membership);
        savePersistedData();
        return membership;
    }
    
    public Membership getMembershipByUserId(String userId) {
        for (Membership membership : memberships.values()) {
            if (membership.getUser().getUserId().equals(userId)) {
                return membership;
            }
        }
        return null;
    }
    
    public boolean updateMembership(String membershipId, Membership.MembershipTier tier, 
                                  LocalDate endDate, boolean active) {
        Membership membership = memberships.get(membershipId);
        
        if (membership != null) {
            membership.upgrade(tier);
            membership.setActive(active);
            if (endDate.isAfter(membership.getEndDate())) {
                int months = (int) (endDate.toEpochDay() - membership.getEndDate().toEpochDay()) / 30;
                membership.extend(months);
            }
            savePersistedData();
            return true;
        }
        
        return false;
    }
    
    @Override
    public ParkingTicket parkVehicle(Vehicle vehicle) {
        // Get current user for automatic association
        User currentUser = UserManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            return parkVehicle(vehicle, currentUser.getUserId());
        }
        
        // No user association, use parent implementation
        return super.parkVehicle(vehicle);
    }
    
    // Enhanced version with user association
    public ParkingTicket parkVehicle(Vehicle vehicle, String userId) {
        // Check if user exists
        User user = UserManager.getInstance().getUserById(userId);
        if (user == null) {
            return null;
        }
        
        // Set the userId on the vehicle
        vehicle.setUserId(userId);
        
        // Standard parking logic from parent class
        ParkingTicket ticket = super.parkVehicle(vehicle);
        
        if (ticket != null) {
            // Associate vehicle with user
            vehicleToUserMap.put(vehicle.getLicensePlate(), userId);
            
            // Add to parking history
            parkingHistory.addTicket(userId, ticket);
            
            savePersistedData();
        }
        
        return ticket;
    }
    
    // Add these methods for user-specific data access
    
    // Get slots occupied by a specific user
    public List<ParkingSlot> getUserOccupiedSlots(String userId) {
        return getAllSlots().stream()
                .filter(slot -> slot.isOccupied() && 
                       slot.getParkedVehicle() != null && 
                       userId.equals(slot.getParkedVehicle().getUserId()))
                .collect(Collectors.toList());
    }
    
    // Get active tickets for a specific user
    public List<ParkingTicket> getUserActiveTickets(String userId) {
        return getActiveTickets().stream()
                .filter(ticket -> userId.equals(ticket.getVehicle().getUserId()))
                .collect(Collectors.toList());
    }
    
    // Get completed tickets for a specific user
    public List<ParkingTicket> getUserCompletedTickets(String userId) {
        return getCompletedTickets().stream()
                .filter(ticket -> userId.equals(ticket.getVehicle().getUserId()))
                .collect(Collectors.toList());
    }
    
    // Get statistics for a specific user
    public Map<String, Object> getUserParkingStatistics(String userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // Count user's vehicles currently parked
        long userVehicles = getActiveTickets().stream()
                .filter(ticket -> userId.equals(ticket.getVehicle().getUserId()))
                .count();
        stats.put("userParkedVehicles", userVehicles);
        
        // Calculate user's total spending
        double totalSpent = getCompletedTickets().stream()
                .filter(ticket -> userId.equals(ticket.getVehicle().getUserId()))
                .mapToDouble(ParkingTicket::getFee)
                .sum();
        stats.put("userTotalSpent", totalSpent);
        
        // Count total visits
        long totalVisits = getCompletedTickets().stream()
                .filter(ticket -> userId.equals(ticket.getVehicle().getUserId()))
                .count() + userVehicles;
        stats.put("userTotalVisits", totalVisits);
        
        return stats;
    }
    
    // Enhanced exit with membership discount
    public ParkingTicket exitVehicle(String ticketId, String userId) {
        ParkingTicket ticket = super.getTicketById(ticketId);
        
        if (ticket == null || !ticket.isActive()) {
            return null;
        }
        
        // Apply membership discount if applicable
        if (userId != null) {
            Membership membership = getMembershipByUserId(userId);
            if (membership != null && membership.isValid()) {
                // Apply discount to the vehicle
                Vehicle vehicle = ticket.getVehicle();
                double discountRate = membership.getDiscountRate();
                
                // Customize the vehicle's fee calculation
                if (vehicle instanceof Car) {
                    ((Car) vehicle).setDiscountRate(discountRate);
                } else if (vehicle instanceof Bike) {
                    ((Bike) vehicle).setDiscountRate(discountRate);
                } else if (vehicle instanceof Truck) {
                    ((Truck) vehicle).setDiscountRate(discountRate);
                }
            }
        }
        
        // Standard exit process from parent class
        ParkingTicket completedTicket = super.exitVehicle(ticketId);
        
        if (completedTicket != null) {
            // Remove vehicle-user association
            vehicleToUserMap.remove(completedTicket.getVehicle().getLicensePlate());
            
            savePersistedData();
        }
        
        return completedTicket;
    }
    
    // Override the original exitVehicle method for backward compatibility
    @Override
    public ParkingTicket exitVehicle(String ticketId) {
        String userId = null;
        ParkingTicket ticket = super.getTicketById(ticketId);
        
        if (ticket != null) {
            String licensePlate = ticket.getVehicle().getLicensePlate();
            userId = vehicleToUserMap.get(licensePlate);
        }
        
        return exitVehicle(ticketId, userId);
    }
    
    // Slot reservation system
    public boolean reserveSlot(String userId, int slotSize, LocalDateTime startTime, LocalDateTime endTime) {
        // Find an available slot of the requested size
        List<ParkingSlot> availableSlots = super.getAvailableSlots().stream()
                .filter(slot -> slot.getSlotSize() == slotSize)
                .collect(Collectors.toList());
        
        if (availableSlots.isEmpty()) {
            return false;
        }
        
        // Reserve the first available slot
        ParkingSlot slot = availableSlots.get(0);
        reservedSlots.computeIfAbsent(userId, k -> new ArrayList<>()).add(slot);
        
        // Remove from available slots (we'd need to modify the parent class to fully implement this)
        // For now, just mark it as reserved in our local data structure
        
        savePersistedData();
        return true;
    }
    
    public List<ParkingSlot> getUserReservedSlots(String userId) {
        return reservedSlots.getOrDefault(userId, new ArrayList<>());
    }
    
    public boolean cancelReservation(String userId, String slotId) {
        List<ParkingSlot> userSlots = reservedSlots.get(userId);
        
        if (userSlots != null) {
            for (Iterator<ParkingSlot> it = userSlots.iterator(); it.hasNext();) {
                ParkingSlot slot = it.next();
                if (slot.getSlotId().equals(slotId)) {
                    it.remove();
                    savePersistedData();
                    return true;
                }
            }
        }
        
        return false;
    }
    
    // History and reporting
    public ParkingHistory getParkingHistory() {
        return parkingHistory;
    }
    
    public List<ParkingTicket> getUserParkingHistory(String userId) {
        return parkingHistory.getUserParkingHistory(userId);
    }
    
    public Map<String, Object> getUserStatistics(String userId) {
        return parkingHistory.getUserStatistics(userId);
    }
    
    public String exportUserHistoryAsCSV(String userId) {
        return parkingHistory.exportUserHistoryAsCSV(userId);
    }
    
    // Enhanced statistics
    @Override
    public Map<String, Object> getParkingStatistics() {
        Map<String, Object> stats = super.getParkingStatistics();
        
        // Add enhanced statistics
        int memberCount = memberships.size();
        stats.put("memberCount", memberCount);
        
        // Active members with valid memberships
        long activeMembers = memberships.values().stream()
                .filter(Membership::isValid)
                .count();
        stats.put("activeMembers", activeMembers);
        
        // Most popular vehicle type
        Map<String, Integer> vehicleTypes = (Map<String, Integer>) stats.get("vehicleTypes");
        String mostPopularType = "";
        int maxCount = 0;
        
        if (vehicleTypes != null) {
            for (Map.Entry<String, Integer> entry : vehicleTypes.entrySet()) {
                if (entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    mostPopularType = entry.getKey();
                }
            }
        }
        stats.put("mostPopularVehicleType", mostPopularType);
        
        // Average occupancy time
        List<ParkingTicket> completedTickets = super.getCompletedTickets();
        if (!completedTickets.isEmpty()) {
            double totalMinutes = completedTickets.stream()
                    .mapToDouble(ticket -> {
                        if (ticket.getExitTime() != null && ticket.getIssueTime() != null) {
                            return java.time.temporal.ChronoUnit.MINUTES
                                    .between(ticket.getIssueTime(), ticket.getExitTime());
                        }
                        return 0;
                    })
                    .sum();
                    
            double avgHours = (totalMinutes / 60.0) / completedTickets.size();
            stats.put("averageOccupancyHours", avgHours);
        } else {
            stats.put("averageOccupancyHours", 0.0);
        }
        
        // Peak hours calculation
        Map<Integer, Integer> hourlyOccupancy = new HashMap<>();
        for (int hour = 0; hour < 24; hour++) {
            hourlyOccupancy.put(hour, 0);
        }
        
        // Count entries by hour
        for (ParkingTicket ticket : completedTickets) {
            int hour = ticket.getIssueTime().getHour();
            hourlyOccupancy.put(hour, hourlyOccupancy.get(hour) + 1);
        }
        
        int peakHour = 0;
        int maxEntries = 0;
        for (Map.Entry<Integer, Integer> entry : hourlyOccupancy.entrySet()) {
            if (entry.getValue() > maxEntries) {
                maxEntries = entry.getValue();
                peakHour = entry.getKey();
            }
        }
        
        stats.put("peakHour", peakHour);
        stats.put("hourlyOccupancy", hourlyOccupancy);
        
        return stats;
    }
    
    // Data persistence methods
    private void loadPersistedData() {
        // Load data from files - this would be implemented with proper
        // serialization in a production system
        try {
            loadMemberships();
        } catch (IOException e) {
            System.err.println("Error loading membership data: " + e.getMessage());
        }
    }
    
    private void savePersistedData() {
        // Save data to files - this would be implemented with proper
        // serialization in a production system
        try {
            saveMemberships();
        } catch (IOException e) {
            System.err.println("Error saving membership data: " + e.getMessage());
        }
    }
    
    private void loadMemberships() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(MEMBERSHIPS_DATA_FILE))) {
            String line;
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                
                // Simple parsing for demonstration
                String membershipId = parts[0];
                String userId = parts[1];
                Membership.MembershipTier tier = Membership.MembershipTier.valueOf(parts[2]);
                LocalDate startDate = LocalDate.parse(parts[3], dateFormatter);
                LocalDate endDate = LocalDate.parse(parts[4], dateFormatter);
                boolean active = Boolean.parseBoolean(parts[5]);
                
                // Get user from UserManager
                User user = UserManager.getInstance().getUserById(userId);
                if (user != null) {
                    Membership membership = new Membership(user, tier, startDate, endDate);
                    membership.setActive(active);
                    memberships.put(membershipId, membership);
                }
            }
        } catch (IOException e) {
            // If file doesn't exist yet, that's okay
            if (!new java.io.File(MEMBERSHIPS_DATA_FILE).exists()) {
                return;
            }
            throw e;
        }
    }
    private void saveMemberships() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MEMBERSHIPS_DATA_FILE))) {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            for (Membership membership : memberships.values()) {
                writer.write(String.format("%s,%s,%s,%s,%s,%b\n",
                        membership.getMembershipId(),
                        membership.getUser().getUserId(),
                        membership.getTier().name(),
                        membership.getStartDate().format(dateFormatter),
                        membership.getEndDate().format(dateFormatter),
                        membership.isActive()));
            }
        }
    }
}