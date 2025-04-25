package edu.northeastern.csye6200.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for managing parking history records and generating reports
 */
public class ParkingHistory {
    private Map<String, List<ParkingTicket>> userParkingHistory; // userId -> List of tickets
    
    public ParkingHistory() {
        this.userParkingHistory = new HashMap<>();
    }
    
    public void addTicket(String userId, ParkingTicket ticket) {
        userParkingHistory.computeIfAbsent(userId, k -> new ArrayList<>()).add(ticket);
    }
    
    public List<ParkingTicket> getUserParkingHistory(String userId) {
        List<ParkingTicket> history = userParkingHistory.getOrDefault(userId, new ArrayList<>());
        Collections.sort(history, Comparator.comparing(ParkingTicket::getIssueTime).reversed());
        return history;
    }
    
    public Map<String, Object> getUserStatistics(String userId) {
        Map<String, Object> stats = new HashMap<>();
        List<ParkingTicket> tickets = userParkingHistory.getOrDefault(userId, new ArrayList<>());
        
        if (tickets.isEmpty()) {
            stats.put("totalVisits", 0);
            stats.put("totalSpent", 0.0);
            stats.put("averageDuration", 0.0);
            stats.put("mostCommonVehicleType", "N/A");
            return stats;
        }
        
        // Total visits
        stats.put("totalVisits", tickets.size());
        
        // Total spent
        double totalSpent = tickets.stream()
                .filter(t -> !t.isActive()) // Only completed tickets
                .mapToDouble(ParkingTicket::getFee)
                .sum();
        stats.put("totalSpent", totalSpent);
        
        // Average duration in hours
        double totalHours = tickets.stream()
                .filter(t -> !t.isActive()) // Only completed tickets
                .mapToDouble(ticket -> {
                    if (ticket.getExitTime() != null) {
                        long minutes = java.time.temporal.ChronoUnit.MINUTES
                                .between(ticket.getIssueTime(), ticket.getExitTime());
                        return minutes / 60.0;
                    }
                    return 0;
                })
                .sum();
        
        double avgDuration = tickets.stream().filter(t -> !t.isActive()).count() > 0 
                ? totalHours / tickets.stream().filter(t -> !t.isActive()).count() 
                : 0;
        stats.put("averageDuration", avgDuration);
        
        // Most common vehicle type
        Map<String, Integer> vehicleTypeCounts = new HashMap<>();
        for (ParkingTicket ticket : tickets) {
            String type = ticket.getVehicle().getVehicleType();
            vehicleTypeCounts.put(type, vehicleTypeCounts.getOrDefault(type, 0) + 1);
        }
        
        String mostCommonType = "N/A";
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : vehicleTypeCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostCommonType = entry.getKey();
            }
        }
        stats.put("mostCommonVehicleType", mostCommonType);
        
        return stats;
    }
    
    public List<Map<String, Object>> generateMonthlyReport(int year, int month) {
        List<Map<String, Object>> report = new ArrayList<>();
        
        // For each user
        for (String userId : userParkingHistory.keySet()) {
            List<ParkingTicket> tickets = userParkingHistory.get(userId);
            
            // Filter tickets for the specified month and year
            List<ParkingTicket> monthlyTickets = tickets.stream()
                    .filter(ticket -> {
                        LocalDateTime time = ticket.getIssueTime();
                        return time.getYear() == year && time.getMonthValue() == month;
                    })
                    .toList();
                    
            if (!monthlyTickets.isEmpty()) {
                Map<String, Object> userReport = new HashMap<>();
                userReport.put("userId", userId);
                userReport.put("totalVisits", monthlyTickets.size());
                
                double totalSpent = monthlyTickets.stream()
                        .filter(t -> !t.isActive())
                        .mapToDouble(ParkingTicket::getFee)
                        .sum();
                userReport.put("totalSpent", totalSpent);
                
                report.add(userReport);
            }
        }
        
        return report;
    }
    
    public String exportUserHistoryAsCSV(String userId) {
        List<ParkingTicket> tickets = getUserParkingHistory(userId);
        
        if (tickets.isEmpty()) {
            return "No parking history found.";
        }
        
        StringBuilder csv = new StringBuilder();
        csv.append("Ticket ID,Vehicle,Entry Time,Exit Time,Duration (hours),Fee\n");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (ParkingTicket ticket : tickets) {
            StringBuilder line = new StringBuilder();
            line.append(ticket.getTicketId()).append(",");
            line.append(ticket.getVehicle().getLicensePlate()).append(" (")
                .append(ticket.getVehicle().getVehicleType()).append("),");
            line.append(ticket.getIssueTime().format(formatter)).append(",");
            
            if (ticket.getExitTime() != null) {
                line.append(ticket.getExitTime().format(formatter)).append(",");
                long minutes = java.time.temporal.ChronoUnit.MINUTES
                        .between(ticket.getIssueTime(), ticket.getExitTime());
                double hours = minutes / 60.0;
                line.append(String.format("%.2f,", hours));
                line.append(String.format("$%.2f", ticket.getFee()));
            } else {
                line.append("Active,N/A,N/A");
            }
            
            csv.append(line).append("\n");
        }
        
        return csv.toString();
    }
}