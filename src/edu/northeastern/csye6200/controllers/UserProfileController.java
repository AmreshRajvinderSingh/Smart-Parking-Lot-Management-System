package edu.northeastern.csye6200.controllers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import edu.northeastern.csye6200.models.Membership;
import edu.northeastern.csye6200.models.ParkingTicket;
import edu.northeastern.csye6200.models.User;
import edu.northeastern.csye6200.models.Vehicle;
import edu.northeastern.csye6200.services.EnhancedParkingLotManager;
import edu.northeastern.csye6200.services.UserManager;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class UserProfileController implements Initializable {
    
    // User information
    @FXML private Label usernameLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    
    // Membership details
    @FXML private Label membershipIdLabel;
    @FXML private Label membershipTierLabel;
    @FXML private Label startDateLabel;
    @FXML private Label endDateLabel;
    @FXML private Label membershipStatusLabel;
    @FXML private Label discountRateLabel;
    
    // Parking statistics
    @FXML private Label totalVisitsLabel;
    @FXML private Label totalSpentLabel;
    @FXML private Label avgDurationLabel;
    @FXML private Label mostUsedVehicleLabel;
    
    // Charts
    @FXML private BarChart<String, Number> parkingUsageChart;
    
    // Parking history table
    @FXML private TableView<ParkingTicket> parkingHistoryTableView;
    @FXML private TableColumn<ParkingTicket, String> ticketIdColumn;
    @FXML private TableColumn<ParkingTicket, String> vehicleColumn;
    @FXML private TableColumn<ParkingTicket, String> entryTimeColumn;
    @FXML private TableColumn<ParkingTicket, String> exitTimeColumn;
    @FXML private TableColumn<ParkingTicket, Double> durationColumn;
    @FXML private TableColumn<ParkingTicket, Double> feeColumn;
    
    private UserManager userManager;
    private EnhancedParkingLotManager parkingManager;
    private User currentUser;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get manager instances
        userManager = UserManager.getInstance();
        parkingManager = EnhancedParkingLotManager.getInstance();
        
        // Get current user
        currentUser = userManager.getCurrentUser();
        
        if (currentUser == null) {
            // This shouldn't happen if login workflow is correct
            showAlert(AlertType.ERROR, "Error", "User not authenticated", 
                      "Please log in again.");
            handleBackToMain(null);
            return;
        }
        
        // Setup UI components
        setupUserInfo();
        setupMembershipInfo();
        setupParkingStatistics();
        setupParkingHistoryTable();
        setupCharts();
    }
    
    private void setupUserInfo() {
        usernameLabel.setText(currentUser.getUsername());
        fullNameLabel.setText(currentUser.getFullName());
        emailLabel.setText(currentUser.getEmail());
        roleLabel.setText(currentUser.getRole().getDisplayName());
    }
    
    private void setupMembershipInfo() {
        Membership membership = parkingManager.getMembershipByUserId(currentUser.getUserId());
        
        if (membership != null) {
            membershipIdLabel.setText(membership.getMembershipId());
            membershipTierLabel.setText(membership.getTier().getName());
            startDateLabel.setText(membership.getStartDate().toString());
            endDateLabel.setText(membership.getEndDate().toString());
            membershipStatusLabel.setText(membership.isValid() ? "Active" : "Inactive");
            discountRateLabel.setText(String.format("%.1f%%", membership.getDiscountRate()));
        } else {
            membershipIdLabel.setText("N/A");
            membershipTierLabel.setText("N/A");
            startDateLabel.setText("N/A");
            endDateLabel.setText("N/A");
            membershipStatusLabel.setText("N/A");
            discountRateLabel.setText("0.0%");
        }
    }
    
    private void setupParkingStatistics() {
        Map<String, Object> stats = parkingManager.getUserStatistics(currentUser.getUserId());
        
        totalVisitsLabel.setText(stats.get("totalVisits").toString());
        totalSpentLabel.setText(String.format("$%.2f", (double)stats.get("totalSpent")));
        avgDurationLabel.setText(String.format("%.1f hours", (double)stats.get("averageDuration")));
        mostUsedVehicleLabel.setText(stats.get("mostCommonVehicleType").toString());
    }
    
    private void setupParkingHistoryTable() {
        // Setup table columns
        ticketIdColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTicketId()));
        
        vehicleColumn.setCellValueFactory(data -> {
            Vehicle vehicle = data.getValue().getVehicle();
            return new SimpleStringProperty(vehicle.getLicensePlate() + " (" + vehicle.getVehicleType() + ")");
        });
        
        entryTimeColumn.setCellValueFactory(data -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return new SimpleStringProperty(data.getValue().getIssueTime().format(formatter));
        });
        
        exitTimeColumn.setCellValueFactory(data -> {
            ParkingTicket ticket = data.getValue();
            if (ticket.getExitTime() == null) {
                return new SimpleStringProperty("Active");
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                return new SimpleStringProperty(ticket.getExitTime().format(formatter));
            }
        });
        
        durationColumn.setCellValueFactory(data -> {
            ParkingTicket ticket = data.getValue();
            if (ticket.getExitTime() == null) {
            	return new SimpleDoubleProperty(0.0).asObject();
            } else {
                long minutes = java.time.temporal.ChronoUnit.MINUTES
                        .between(ticket.getIssueTime(), ticket.getExitTime());
                return new SimpleDoubleProperty(minutes / 60.0).asObject();
            }
        });
        
        // Format duration to show 1 decimal place
        durationColumn.setCellFactory(col -> new javafx.scene.control.TableCell<ParkingTicket, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f", item));
                }
            }
        });
        
        feeColumn.setCellValueFactory(data -> {
            ParkingTicket ticket = data.getValue();
            return new SimpleDoubleProperty(ticket.getFee()).asObject();
        });
        
        // Format fee to show currency
        feeColumn.setCellFactory(col -> new javafx.scene.control.TableCell<ParkingTicket, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });
        
        // Load data
        loadParkingHistory();
    }
    
    private void setupCharts() {
        // Create a series for the parking usage chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Parking visits per month");
        
        // Prepare data (this would normally come from actual history data)
        List<ParkingTicket> history = parkingManager.getUserParkingHistory(currentUser.getUserId());
        
        // Count visits by month
        Map<String, Integer> monthCountMap = new java.util.HashMap<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");
        
        for (ParkingTicket ticket : history) {
            String monthYear = ticket.getIssueTime().format(monthFormatter);
            monthCountMap.put(monthYear, monthCountMap.getOrDefault(monthYear, 0) + 1);
        }
        
        // Sort months chronologically
        List<String> months = new java.util.ArrayList<>(monthCountMap.keySet());
        java.util.Collections.sort(months, (a, b) -> {
            try {
                java.time.YearMonth ma = java.time.YearMonth.parse(a, java.time.format.DateTimeFormatter.ofPattern("MMM yyyy"));
                java.time.YearMonth mb = java.time.YearMonth.parse(b, java.time.format.DateTimeFormatter.ofPattern("MMM yyyy"));
                return ma.compareTo(mb);
            } catch (Exception e) {
                return a.compareTo(b);
            }
        });
        
        // Add data to series
        for (String month : months) {
            series.getData().add(new XYChart.Data<>(month, monthCountMap.get(month)));
        }
        
        // If no history, add placeholder data
        if (series.getData().isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (int i = 5; i >= 0; i--) {
                LocalDateTime month = now.minusMonths(i);
                series.getData().add(new XYChart.Data<>(month.format(monthFormatter), 0));
            }
        }
        
        // Add series to chart
        parkingUsageChart.getData().add(series);
    }
    
    private void loadParkingHistory() {
        List<ParkingTicket> history = parkingManager.getUserParkingHistory(currentUser.getUserId());
        ObservableList<ParkingTicket> historyList = FXCollections.observableArrayList(history);
        parkingHistoryTableView.setItems(historyList);
    }
    
    @FXML
    private void handleRefreshHistory(ActionEvent event) {
        loadParkingHistory();
        setupParkingStatistics();
    }
    
    @FXML
    private void handleExportHistory(ActionEvent event) {
        String csvContent = parkingManager.exportUserHistoryAsCSV(currentUser.getUserId());
        
        if (csvContent.equals("No parking history found.")) {
            showAlert(AlertType.INFORMATION, "Export History", "No Data", 
                      "No parking history data available to export.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Parking History");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("parking_history.csv");
        
        File file = fileChooser.showSaveDialog(parkingHistoryTableView.getScene().getWindow());
        
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(csvContent);
                showAlert(AlertType.INFORMATION, "Export History", "Export Successful", 
                          "Parking history exported successfully to " + file.getName());
            } catch (IOException e) {
                showAlert(AlertType.ERROR, "Export History", "Export Failed", 
                          "Failed to export parking history: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleBackToMain(ActionEvent event) {
        try {
            // Load the main view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/northeastern/csye6200/views/main.fxml"));
            Parent root = loader.load();
            
            // Get the current stage
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            
            // Set the new scene
            stage.setScene(new Scene(root));
            stage.setTitle("Smart Parking Lot System");
            stage.centerOnScreen();
            
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Navigation Error", "Error returning to main view", 
                      e.getMessage());
        }
    }
    
    private void showAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}