package edu.northeastern.csye6200.controllers;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

import edu.northeastern.csye6200.models.Membership;
import edu.northeastern.csye6200.models.User;
import edu.northeastern.csye6200.services.EnhancedParkingLotManager;
import edu.northeastern.csye6200.services.ParkingLotManager;
import edu.northeastern.csye6200.services.UserManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MemberDashboardController {
    @FXML
    private Label userNameLabel;
    
    @FXML
    private Label membershipTierLabel;
    
    @FXML
    private Label membershipValidLabel;
    
    @FXML
    private Label userActiveVehiclesLabel;
    
    @FXML
    private Label userTotalVisitsLabel;
    
    @FXML
    private Label userTotalSpentLabel;
    
    private ParkingLotManager parkingManager;
    private User currentUser;
    private NumberFormat currencyFormatter;
    
    @FXML
    public void initialize() {
        System.out.println("MemberDashboardController initialize() called");
        
        try {
            // Initialize formatters
            currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
            
            // Get the parking manager instance
            parkingManager = ParkingLotManager.getInstance();
            
            // Set default values
            userNameLabel.setText("Not logged in");
            membershipTierLabel.setText("N/A");
            membershipValidLabel.setText("N/A");
            userActiveVehiclesLabel.setText("0");
            userTotalVisitsLabel.setText("0");
            userTotalSpentLabel.setText(currencyFormatter.format(0));
            
            // Try to get current user from UserManager
            currentUser = UserManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                System.out.println("Found current user: " + currentUser.getUsername());
                updateDashboard(currentUser.getUsername());
            } else {
                System.out.println("No current user found during initialization");
            }
        } catch (Exception e) {
            System.err.println("Error in MemberDashboardController initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Updates the dashboard with the current user's information
     * @param username The current user's username
     */
    public void updateDashboard(String username) {
        System.out.println("Updating member dashboard for user: " + username);
        
        if (username == null || username.isEmpty()) {
            System.out.println("Invalid username provided to updateDashboard");
            return;
        }
        
        try {
            // Try to get user from UserManager first
            currentUser = UserManager.getInstance().getCurrentUser();
            
            // If that failed, try to get by username
            if (currentUser == null || !currentUser.getUsername().equals(username)) {
                System.out.println("Getting user by username: " + username);
                currentUser = UserManager.getInstance().getUserByUsername(username);
            }
            
            if (currentUser != null) {
                // Update user information section
                userNameLabel.setText(currentUser.getUsername());
                
                // Check if we have enhanced manager for membership functions
                if (parkingManager instanceof EnhancedParkingLotManager) {
                    EnhancedParkingLotManager enhancedManager = (EnhancedParkingLotManager) parkingManager;
                    
                    // Get membership info
                    Membership membership = enhancedManager.getMembershipByUserId(currentUser.getUserId());
                    if (membership != null) {
                        membershipTierLabel.setText(membership.getTier().getName());
                        membershipValidLabel.setText(membership.isValid() ? "Active" : "Inactive");
                    } else {
                        // Default values if no membership found
                        membershipTierLabel.setText("Bronze");
                        membershipValidLabel.setText("Active");
                    }
                    
                    // Update statistics using enhancedManager
                    Map<String, Object> userStats = enhancedManager.getUserParkingStatistics(currentUser.getUserId());
                    
                    if (userStats != null) {
                        userActiveVehiclesLabel.setText(userStats.get("userParkedVehicles").toString());
                        userTotalVisitsLabel.setText(userStats.get("userTotalVisits").toString());
                        userTotalSpentLabel.setText(currencyFormatter.format((double)userStats.get("userTotalSpent")));
                    } else {
                        System.out.println("No user stats available from EnhancedParkingLotManager");
                        // Set default values
                        userActiveVehiclesLabel.setText("0");
                        userTotalVisitsLabel.setText("0");
                        userTotalSpentLabel.setText(currencyFormatter.format(0));
                    }
                } else {
                    // Fallback to default values if no enhanced manager
                    System.out.println("ParkingManager is not an instance of EnhancedParkingLotManager");
                    membershipTierLabel.setText("Bronze");
                    membershipValidLabel.setText("Active");
                    userActiveVehiclesLabel.setText("0");
                    userTotalVisitsLabel.setText("0");
                    userTotalSpentLabel.setText(currencyFormatter.format(0));
                }
                
                System.out.println("Dashboard updated successfully for user: " + username);
            } else {
                System.err.println("Could not find user: " + username);
            }
        } catch (Exception e) {
            System.err.println("Error updating member dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Refreshes all data on the dashboard
     */
    public void refreshDashboard() {
        System.out.println("Refreshing dashboard");
        if (currentUser != null) {
            updateDashboard(currentUser.getUsername());
        } else {
            System.out.println("Cannot refresh dashboard - no current user");
            // Try to get the current user again
            currentUser = UserManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                updateDashboard(currentUser.getUsername());
            }
        }
    }
}