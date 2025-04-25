package edu.northeastern.csye6200.controllers;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import edu.northeastern.csye6200.models.*;
import edu.northeastern.csye6200.services.EnhancedParkingLotManager;
import edu.northeastern.csye6200.services.ParkingLotManager;
import edu.northeastern.csye6200.services.UserManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class MainController implements Initializable {
    
    private ParkingLotManager parkingManager;
    private User currentUser;
    private boolean isAdmin = false;
    
    // UI Components - Main interface tabs
    @FXML private TabPane mainTabPane;
    @FXML private Tab parkingTab;
    @FXML private Tab ticketsTab;
    @FXML private Tab statsTab;
    @FXML private Tab memberDashboardTab;
    
    // Dashboard & Stats
    @FXML private Label totalSlotsLabel;
    @FXML private Label availableSlotsLabel;
    @FXML private Label occupiedSlotsLabel;
    @FXML private Label occupancyRateLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label currentUserRoleLabel;
    @FXML private Label currentUserNameLabel;
    @FXML private PieChart occupancyChart;
    @FXML private PieChart vehicleTypesChart;
    
 // Parking Tab Dashboard
    @FXML private Label dashboardTotalSlotsLabel;
    @FXML private Label dashboardAvailableSlotsLabel;
    @FXML private Label dashboardOccupiedSlotsLabel;
    @FXML private Label dashboardOccupancyRateLabel;
    @FXML private Label dashboardTotalRevenueLabel;
    
    // Member Dashboard
    @FXML private Label userNameLabel;
    @FXML private Label membershipTierLabel;
    @FXML private Label membershipValidLabel;
    @FXML private Label userActiveVehiclesLabel;
    @FXML private Label userTotalVisitsLabel;
    @FXML private Label userTotalSpentLabel;
    
    // Parking slot view
    @FXML private TableView<ParkingSlot> slotTableView;
    @FXML private TableColumn<ParkingSlot, String> slotIdColumn;
    @FXML private TableColumn<ParkingSlot, String> slotSizeColumn;
    @FXML private TableColumn<ParkingSlot, Boolean> slotStatusColumn;
    @FXML private TableColumn<ParkingSlot, String> slotVehicleColumn;
    
    // Add vehicle form controls
    @FXML private ComboBox<String> vehicleTypeComboBox;
    @FXML private TextField licensePlateField;
    @FXML private TextField brandField;
    @FXML private TextField modelField;
    @FXML private TextField colorField;
    @FXML private Button parkVehicleButton;
    
    // Tickets view
    @FXML private TableView<ParkingTicket> activeTicketsTableView;
    @FXML private TableColumn<ParkingTicket, String> ticketIdColumn;
    @FXML private TableColumn<ParkingTicket, String> vehicleLicenseColumn;
    @FXML private TableColumn<ParkingTicket, String> slotIdTicketColumn;
    @FXML private TableColumn<ParkingTicket, String> entryTimeColumn;
    
    // Exit vehicle form
    @FXML private TextField ticketIdField;
    @FXML private Button exitVehicleButton;
    @FXML private Button generateTestDataButton;
    
    // Completed tickets view
    @FXML private TableView<ParkingTicket> completedTicketsTableView;
    @FXML private TableColumn<ParkingTicket, String> completedTicketIdColumn;
    @FXML private TableColumn<ParkingTicket, String> completedVehicleLicenseColumn;
    @FXML private TableColumn<ParkingTicket, String> completedEntryTimeColumn;
    @FXML private TableColumn<ParkingTicket, String> completedExitTimeColumn;
    @FXML private TableColumn<ParkingTicket, Double> completedFeeColumn;
    
    @FXML private Button logoutButton;
    
    @FXML private Tab userManagementTab;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, UserRole> roleColumn;
    @FXML private TableColumn<User, Boolean> activeColumn;
    @FXML private TableColumn<User, Double> revenueColumn;

    
    // Store for the member dashboard loader and controller
    private FXMLLoader memberDashboardLoader;
    private MemberDashboardController memberDashboardController;
    
    // Method to get the member dashboard controller
    public MemberDashboardController getMemberDashboardController() {
        if (memberDashboardController != null) {
            return memberDashboardController;
        }
        
        if (memberDashboardTab != null) {
            try {
                // If we already have a loader, use it
                if (memberDashboardLoader != null) {
                    return memberDashboardLoader.getController();
                }
                
                // Otherwise try to find it in the userData
                Node content = memberDashboardTab.getContent();
                if (content != null && content.getUserData() instanceof FXMLLoader) {
                    FXMLLoader loader = (FXMLLoader) content.getUserData();
                    memberDashboardController = loader.getController();
                    return memberDashboardController;
                }
            } catch (Exception e) {
                System.err.println("Error getting member dashboard controller: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }
    
    private void setupMemberDashboardTab() {
        try {
            // Create the FXMLLoader for member dashboard
            memberDashboardLoader = new FXMLLoader(getClass().getResource("/edu/northeastern/csye6200/views/member_dashboard.fxml"));
            
            // Load the content
            Parent dashboardContent = memberDashboardLoader.load();
            
            // Store the loader in the user data of the content for later retrieval
            dashboardContent.setUserData(memberDashboardLoader);
            
            // Set the content on the tab
            memberDashboardTab.setContent(dashboardContent);
            
            // Get and store the controller
            memberDashboardController = memberDashboardLoader.getController();
            
            // Update it with current user
            if (memberDashboardController != null && currentUser != null) {
                memberDashboardController.updateDashboard(currentUser.getUsername());
            }
        } catch (IOException e) {
            System.err.println("Error loading member dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Initialize the controller
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initializing MainController");
        
        // Get the current user
        currentUser = UserManager.getInstance().getCurrentUser();
        isAdmin = (currentUser != null && currentUser.getRole() == UserRole.ADMIN);
        
        // Get the singleton instance of parking manager
        parkingManager = EnhancedParkingLotManager.getInstance();

        
        // Initialize UI components
        setupVehicleTypeComboBox();
        setupSlotsTableView();
        setupActiveTicketsTableView();
        setupCompletedTicketsTableView();
        setupUserInfo();
        
        // Apply role-based access control
        setupRoleBasedAccess();
        
        // Load initial data
        refreshData();
        
        // Setup auto-refresh timer (every 30 seconds)
        setupAutoRefresh();
        
        // Add tab change listener
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == statsTab) {
                // Force refresh when switching to Statistics tab
                refreshStatistics();
            } else if (newTab == memberDashboardTab) {
                // Refresh member dashboard
                updateMemberStatistics();
            }
        });
    }

    private void setupUserInfo() {
        if (currentUser != null) {
            currentUserNameLabel.setText("User: " + currentUser.getUsername());
            currentUserRoleLabel.setText("Role: " + currentUser.getRole().getDisplayName());
        }
    }
    
    private void setupVehicleTypeComboBox() {
        ObservableList<String> vehicleTypes = FXCollections.observableArrayList(
                "Car", "Bike", "Truck"
        );
        vehicleTypeComboBox.setItems(vehicleTypes);
        vehicleTypeComboBox.getSelectionModel().selectFirst();
    }
    
    private void setupRoleBasedAccess() {
        if (!isAdmin) {
            // Hide admin-specific features
            if (generateTestDataButton != null) {
                generateTestDataButton.setVisible(false);
            }

            // For regular members, hide the statistics and user management tabs
            if (statsTab != null) {
                mainTabPane.getTabs().removeAll(statsTab, userManagementTab);
            }

            // Create and show the member dashboard
            if (memberDashboardTab != null) {
                setupMemberDashboardTab();
            }

        } else {
            // For admins, hide the member dashboard tab
            if (memberDashboardTab != null) {
                memberDashboardTab.setDisable(true);
            }

            //  Setup admin-only user management tab
            setupUserManagementTab();
        }
    }

    private void updateMemberStatistics() {
        // First try to get the controller
        MemberDashboardController dashboardController = getMemberDashboardController();
        
        // If we have a controller and a current user, update the dashboard
        if (dashboardController != null && currentUser != null) {
            dashboardController.updateDashboard(currentUser.getUsername());
        }
        // Otherwise, we'll use the UI components directly if available
        else if (currentUser != null && userActiveVehiclesLabel != null && userTotalVisitsLabel != null && userTotalSpentLabel != null) {
            // Get user-specific statistics
            EnhancedParkingLotManager enhancedManager = (EnhancedParkingLotManager) parkingManager;
            Map<String, Object> userStats = enhancedManager.getUserParkingStatistics(currentUser.getUserId());
            
            // Update the UI
            userActiveVehiclesLabel.setText(userStats.get("userParkedVehicles").toString());
            userTotalVisitsLabel.setText(userStats.get("userTotalVisits").toString());
            userTotalSpentLabel.setText(String.format("$%.2f", (double)userStats.get("userTotalSpent")));
        }
    }

    private void refreshData() {
        // If admin, show all data
        if (isAdmin) {
            refreshSlotTableView();
            refreshTicketTableViews();
            updateStatistics();
        } else {
            // If member, only show their own data
            refreshMemberSlotTableView();
            refreshMemberTicketTableViews();
            updateMemberStatistics();
        }
    }

    private void refreshMemberSlotTableView() {
        if (currentUser != null) {
            EnhancedParkingLotManager enhancedManager = (EnhancedParkingLotManager) parkingManager;
            List<ParkingSlot> userSlots = enhancedManager.getUserOccupiedSlots(currentUser.getUserId());
            
            // Add all available slots too
            List<ParkingSlot> availableSlots = parkingManager.getAvailableSlots();
            
            // Combine lists
            List<ParkingSlot> allRelevantSlots = new ArrayList<>();
            allRelevantSlots.addAll(userSlots);
            allRelevantSlots.addAll(availableSlots);
            
            // Update table
            ObservableList<ParkingSlot> slotList = FXCollections.observableArrayList(allRelevantSlots);
            slotTableView.setItems(slotList);
            
            // Force refresh
            slotTableView.refresh();
        }
    }

    private void refreshMemberTicketTableViews() {
        if (currentUser != null) {
            EnhancedParkingLotManager enhancedManager = (EnhancedParkingLotManager) parkingManager;
            
            // Get user's active tickets
            List<ParkingTicket> userActiveTickets = enhancedManager.getUserActiveTickets(currentUser.getUserId());
            ObservableList<ParkingTicket> activeTicketList = FXCollections.observableArrayList(userActiveTickets);
            activeTicketsTableView.setItems(activeTicketList);
            
            // Get user's completed tickets
            List<ParkingTicket> userCompletedTickets = enhancedManager.getUserCompletedTickets(currentUser.getUserId());
            ObservableList<ParkingTicket> completedTicketList = FXCollections.observableArrayList(userCompletedTickets);
            completedTicketsTableView.setItems(completedTicketList);
        }
    }
    
    private void setupSlotsTableView() {
        slotIdColumn.setCellValueFactory(new PropertyValueFactory<>("slotId"));
        slotSizeColumn.setCellValueFactory(new PropertyValueFactory<>("slotSizeLabel"));
        slotStatusColumn.setCellValueFactory(new PropertyValueFactory<>("occupied"));
        
        // Custom cell factory for displaying vehicle info
        slotVehicleColumn.setCellValueFactory(cellData -> {
            ParkingSlot slot = cellData.getValue();
            if (slot.isOccupied() && slot.getParkedVehicle() != null) {
                return javafx.beans.binding.Bindings.createStringBinding(
                        () -> slot.getParkedVehicle().getLicensePlate() + " - " + 
                              slot.getParkedVehicle().getBrand() + " " + 
                              slot.getParkedVehicle().getModel()
                );
            } else {
                return javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
            }
        });
        
        // Custom cell factory for status column to show Yes/No instead of true/false
        slotStatusColumn.setCellFactory(col -> new TableCell<ParkingSlot, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Occupied" : "Available");
                    setStyle(item ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
                }
            }
        });
    }
    
    private void setupActiveTicketsTableView() {
        ticketIdColumn.setCellValueFactory(new PropertyValueFactory<>("ticketId"));
        slotIdTicketColumn.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getSlot().getSlotId()
            )
        );
        vehicleLicenseColumn.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getVehicle().getLicensePlate()
            )
        );
        entryTimeColumn.setCellValueFactory(new PropertyValueFactory<>("formattedIssueTime"));
    }
    
    private void setupCompletedTicketsTableView() {
        completedTicketIdColumn.setCellValueFactory(new PropertyValueFactory<>("ticketId"));
        completedVehicleLicenseColumn.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getVehicle().getLicensePlate()
            )
        );
        completedEntryTimeColumn.setCellValueFactory(new PropertyValueFactory<>("formattedIssueTime"));
        completedExitTimeColumn.setCellValueFactory(new PropertyValueFactory<>("formattedExitTime"));
        completedFeeColumn.setCellValueFactory(new PropertyValueFactory<>("fee"));
        
        // Format fee column to show currency
        completedFeeColumn.setCellFactory(col -> new TableCell<ParkingTicket, Double>() {
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
    }
    
    private void setupAutoRefresh() {
        Thread refreshThread = new Thread(() -> {
            while(true) {
                try {
                    Thread.sleep(30000); // 30 seconds
                    Platform.runLater(() -> {
                        refreshData();
                    });
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        refreshThread.setDaemon(true); // Allow JVM to exit
        refreshThread.start();
    }

    private void setupUserManagementTab() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        revenueColumn.setCellValueFactory(user -> 
            new SimpleDoubleProperty(calculateRevenue(user.getValue())).asObject());

        List<User> users = UserManager.getInstance().getAllUsers();
        userTable.setItems(FXCollections.observableArrayList(users));
    }
    private double calculateRevenue(User user) {
        EnhancedParkingLotManager enhancedManager = (EnhancedParkingLotManager) parkingManager;
        List<ParkingTicket> tickets = enhancedManager.getUserParkingHistory(user.getUserId());
        return tickets.stream()
                      .mapToDouble(ParkingTicket::getFee)
                      .sum();
    }
    @FXML
    private void handleEditUser(ActionEvent event) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "No User Selected", "Please select a user to edit.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/northeastern/csye6200/views/EditUserDialog.fxml"));
            Parent root = loader.load();

            EditUserDialogController controller = loader.getController();
            controller.setUser(selectedUser);

            // ✅ Hook up the refresh callback
            controller.setOnUserUpdated((v) -> {
                userTable.setItems(FXCollections.observableArrayList(UserManager.getInstance().getAllUsers()));
                userTable.refresh();
            });

            Stage stage = new Stage();
            stage.setTitle("Edit User - " + selectedUser.getUsername());
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Dialog Error", "Could not load the edit user dialog.");
        }
    }






    /**
     * Handles the action when the Park Vehicle button is clicked
     * @param event The action event from the button click
     */
    @FXML
    private void handleParkVehicle(ActionEvent event) {
        // Validate input fields
        if (licensePlateField.getText().isEmpty() || 
            brandField.getText().isEmpty() || 
            modelField.getText().isEmpty() || 
            colorField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Missing Information", 
                      "Please fill in all fields.");
            return;
        }
        
        // Create vehicle based on selected type
        String vehicleType = vehicleTypeComboBox.getValue();
        String licensePlate = licensePlateField.getText();
        String brand = brandField.getText();
        String model = modelField.getText();
        String color = colorField.getText();
        
        Vehicle vehicle = null;
        switch (vehicleType) {
            case "Car":
                vehicle = new Car(licensePlate, brand, model, color);
                break;
            case "Bike":
                vehicle = new Bike(licensePlate, brand, model, color);
                break;
            case "Truck":
                vehicle = new Truck(licensePlate, brand, model, color);
                break;
            default:
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid Vehicle Type", 
                          "Please select a valid vehicle type.");
                return;
        }
        
        // Check if vehicle is already parked
        Vehicle existingVehicle = parkingManager.findVehicleByLicensePlate(licensePlate);
        if (existingVehicle != null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Vehicle Already Parked", 
                      "A vehicle with this license plate is already in the parking lot.");
            return;
        }
        
        // Try to park the vehicle with current user association
        ParkingTicket ticket;
        if (currentUser != null) {
            // Use enhanced manager for user association
            EnhancedParkingLotManager enhancedManager = (EnhancedParkingLotManager) parkingManager;
            ticket = enhancedManager.parkVehicle(vehicle, currentUser.getUserId());
        } else {
            // Fallback to standard parking
            ticket = parkingManager.parkVehicle(vehicle);
        }
        
        if (ticket == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Parking Failed", 
                      "No suitable parking slot available for this vehicle.");
        } else {
            // Create the alert dialog
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Vehicle Parked");
            alert.setContentText("Vehicle parked successfully.\nTicket ID: " + ticket.getTicketId() + 
                      "\nSlot: " + ticket.getSlot().getSlotId());
            
            // Add owner window to prevent focus loss
            Stage stage = (Stage) parkVehicleButton.getScene().getWindow();
            alert.initOwner(stage);
            
            // Show the alert and ensure the main window stays focused
            alert.showAndWait().ifPresent(response -> {
                Platform.runLater(() -> {
                    stage.requestFocus();
                    stage.toFront();
                });
            });
            
            // Clear form fields
            clearVehicleForm();
            
            // Refresh views
            refreshData();
        }
    }

    @FXML
    private void handleExitVehicle(ActionEvent event) {
        String ticketId = ticketIdField.getText();
        
        if (ticketId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Missing Information", 
                      "Please enter a ticket ID.");
            return;
        }
        
        // Try to exit the vehicle with user association
        ParkingTicket ticket;
        if (currentUser != null && !isAdmin) {
            // Use enhanced manager for user-specific exit
            EnhancedParkingLotManager enhancedManager = (EnhancedParkingLotManager) parkingManager;
            ticket = enhancedManager.exitVehicle(ticketId, currentUser.getUserId());
        } else {
            // Admin or no user, use standard exit
            ticket = parkingManager.exitVehicle(ticketId);
        }
        
        if (ticket == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Exit Failed", 
                      "Invalid ticket ID or ticket already processed.");
        } else {
            // Create the alert dialog
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Vehicle Exited");
            alert.setContentText("Vehicle exited successfully.\nFee: $" + 
                      String.format("%.2f", ticket.getFee()));
            
            // Add owner window to prevent focus loss
            Stage stage = (Stage) exitVehicleButton.getScene().getWindow();
            alert.initOwner(stage);
            
            // Show the alert and ensure the main window stays focused
            alert.showAndWait().ifPresent(response -> {
                Platform.runLater(() -> {
                    stage.requestFocus();
                    stage.toFront();
                });
            });
            
            // Clear form field
            ticketIdField.clear();
            
            // Refresh views
            refreshData();
        }
    }

    private void refreshSlotTableView() {
        List<ParkingSlot> slots = parkingManager.getAllSlots();
        ObservableList<ParkingSlot> slotList = FXCollections.observableArrayList(slots);
        slotTableView.setItems(slotList);
        
        // Force refresh
        slotTableView.refresh();
        
        // Alternative approach if the above doesn't work
        slotTableView.getColumns().forEach(column -> {
            column.setVisible(false);
            column.setVisible(true);
        });
    }

    private void refreshTicketTableViews() {
        // Refresh active tickets
        List<ParkingTicket> activeTickets = parkingManager.getActiveTickets();
        ObservableList<ParkingTicket> activeTicketList = FXCollections.observableArrayList(activeTickets);
        activeTicketsTableView.setItems(activeTicketList);
        
        // Refresh completed tickets
        List<ParkingTicket> completedTickets = parkingManager.getCompletedTickets();
        ObservableList<ParkingTicket> completedTicketList = FXCollections.observableArrayList(completedTickets);
        completedTicketsTableView.setItems(completedTicketList);
    }

    @SuppressWarnings("unchecked")
    private void updateStatistics() {
        Map<String, Object> stats = parkingManager.getParkingStatistics();

        // Get actual values
        int totalSlots = (int) stats.get("totalSlots");
        int availableSlots = (int) stats.get("availableSlots");
        int occupiedSlots = (int) stats.get("occupiedSlots");
        double occupancyRate = (double) stats.get("occupancyRate");
        double totalRevenue = (double) stats.get("totalRevenue");

        // Update Statistics Tab labels
        totalSlotsLabel.setText(String.valueOf(totalSlots));
        availableSlotsLabel.setText(String.valueOf(availableSlots));
        occupiedSlotsLabel.setText(String.valueOf(occupiedSlots));
        occupancyRateLabel.setText(String.format("%.1f%%", occupancyRate));
        totalRevenueLabel.setText(String.format("$%.2f", totalRevenue));

        // ✅ Update Parking Management Dashboard labels (if declared)
        if (dashboardTotalSlotsLabel != null) dashboardTotalSlotsLabel.setText(String.valueOf(totalSlots));
        if (dashboardAvailableSlotsLabel != null) dashboardAvailableSlotsLabel.setText(String.valueOf(availableSlots));
        if (dashboardOccupiedSlotsLabel != null) dashboardOccupiedSlotsLabel.setText(String.valueOf(occupiedSlots));
        if (dashboardOccupancyRateLabel != null) dashboardOccupancyRateLabel.setText(String.format("%.1f%%", occupancyRate));
        if (dashboardTotalRevenueLabel != null) dashboardTotalRevenueLabel.setText(String.format("$%.2f", totalRevenue));

        // Force UI refresh (charts + tables)
        Platform.runLater(() -> {
            slotTableView.refresh();
            updateOccupancyChart(occupiedSlots, availableSlots);
            updateVehicleTypesChart((Map<String, Integer>) stats.get("vehicleTypes"));
        });
    }


    // Method specifically for refreshing the Statistics tab
    private void refreshStatistics() {
        Map<String, Object> stats = parkingManager.getParkingStatistics();
        
        Platform.runLater(() -> {
            // Update charts again specifically for the Statistics tab
            updateOccupancyChart((int)stats.get("occupiedSlots"), (int)stats.get("availableSlots"));
            updateVehicleTypesChart((Map<String, Integer>)stats.get("vehicleTypes"));
        });
    }

    // Separate methods for chart updates to improve performance
    private void updateOccupancyChart(int occupiedSlots, int availableSlots) {
        ObservableList<PieChart.Data> occupancyData = FXCollections.observableArrayList(
            new PieChart.Data("Occupied", occupiedSlots),
            new PieChart.Data("Available", availableSlots)
        );
        occupancyChart.setData(occupancyData);
    }

    private void updateVehicleTypesChart(Map<String, Integer> vehicleTypes) {
        ObservableList<PieChart.Data> vehicleData = FXCollections.observableArrayList();
        
        if (vehicleTypes != null && !vehicleTypes.isEmpty()) {
            vehicleTypes.forEach((type, count) -> {
                vehicleData.add(new PieChart.Data(type, count));
            });
        } else {
            // Handle case when no vehicles are parked
            vehicleData.add(new PieChart.Data("No Vehicles", 1));
        }
        
        vehicleTypesChart.setData(vehicleData);
    }

    private void clearVehicleForm() {
        licensePlateField.clear();
        brandField.clear();
        modelField.clear();
        colorField.clear();
        vehicleTypeComboBox.getSelectionModel().selectFirst();
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        // Add owner window to prevent focus loss
        if (parkVehicleButton != null && parkVehicleButton.getScene() != null) {
            Stage stage = (Stage) parkVehicleButton.getScene().getWindow();
            alert.initOwner(stage);
        }
        
        alert.showAndWait().ifPresent(response -> {
            if (parkVehicleButton != null && parkVehicleButton.getScene() != null) {
                Platform.runLater(() -> {
                    Stage stage = (Stage) parkVehicleButton.getScene().getWindow();
                    stage.requestFocus();
                    stage.toFront();
                });
            }
        });
    }

    // Helper method to search for vehicles
    @FXML
    private void handleSearchVehicle(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Search Vehicle");
        dialog.setHeaderText("Search for a vehicle by license plate");
        dialog.setContentText("License Plate:");
        
        // Add owner window to prevent focus loss
        if (parkVehicleButton.getScene() != null) {
            Stage stage = (Stage) parkVehicleButton.getScene().getWindow();
            dialog.initOwner(stage);
        }
        
        dialog.showAndWait().ifPresent(licensePlate -> {
            Vehicle vehicle = parkingManager.findVehicleByLicensePlate(licensePlate);
            
            if (vehicle == null) {
                showAlert(Alert.AlertType.INFORMATION, "Search Result", "Vehicle Not Found", 
                          "No vehicle with license plate " + licensePlate + " is currently parked.");
            } else {
                // For non-admin users, only show their own vehicles
                if (!isAdmin && currentUser != null && 
                    !currentUser.getUserId().equals(vehicle.getUserId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Search Result", "Vehicle Not Found", 
                              "No vehicle with license plate " + licensePlate + " is currently parked.");
                    return;
                }
                
                ParkingSlot slot = null;
                
                // Find the slot for this vehicle
                for (ParkingSlot s : parkingManager.getAllSlots()) {
                    if (s.isOccupied() && s.getParkedVehicle().getLicensePlate().equals(licensePlate)) {
                        slot = s;
                        break;
                    }
                }
                
                // Show vehicle details
                StringBuilder details = new StringBuilder();
                details.append("License Plate: ").append(vehicle.getLicensePlate()).append("\n");
                details.append("Type: ").append(vehicle.getVehicleType()).append("\n");
                details.append("Brand/Model: ").append(vehicle.getBrand()).append(" ").append(vehicle.getModel()).append("\n");
                details.append("Color: ").append(vehicle.getColor()).append("\n");
                details.append("Entry Time: ").append(vehicle.getEntryTime().toString()).append("\n");
                
                if (slot != null) {
                    details.append("Slot: ").append(slot.getSlotId()).append(" (").append(slot.getSlotSizeLabel()).append(")");
                }
                
                showAlert(Alert.AlertType.INFORMATION, "Search Result", "Vehicle Found", details.toString());
            }
        });
    }

    @FXML
    private void handleRefreshAction(ActionEvent event) {
        refreshData();
    }

    @FXML
    private void handleGenerateTestData(ActionEvent event) {
        // Create some test vehicles and park them
        Vehicle[] testVehicles = {
            new Car("CAR-001", "Toyota", "Corolla", "Blue"),
            new Car("CAR-002", "Honda", "Civic", "Black"),
            new Bike("BIKE-001", "Yamaha", "FZ", "Red"),
            new Truck("TRUCK-001", "Ford", "F-150", "White")
        };
        
        int parkedCount = 0;
        
        for (Vehicle vehicle : testVehicles) {
            ParkingTicket ticket = parkingManager.parkVehicle(vehicle);
            if (ticket != null) {
                parkedCount++;
            }
        }
        
        // Create the alert dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Test Data");
        alert.setHeaderText("Test Data Generated");
        alert.setContentText("Successfully parked " + parkedCount + " test vehicles.");
        
        // Add owner window to prevent focus loss
        if (parkVehicleButton.getScene() != null) {
            Stage stage = (Stage) parkVehicleButton.getScene().getWindow();
            alert.initOwner(stage);
        }
        
        // Show the alert and ensure the main window stays focused
        alert.showAndWait().ifPresent(response -> {
            if (parkVehicleButton.getScene() != null) {
                Platform.runLater(() -> {
                    Stage stage = (Stage) parkVehicleButton.getScene().getWindow();
                    stage.requestFocus();
                    stage.toFront();
                });
            }
        });
        
        // Refresh views
        refreshData();
    }
    @FXML
    private void handleLogout(ActionEvent event) {
        // Log out the user
        UserManager.getInstance().logout();
        
        try {
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/northeastern/csye6200/views/login.fxml"));
            Parent root = loader.load();
            
            // Get the current stage
            Stage stage = (Stage) mainTabPane.getScene().getWindow();
            
            // Set the new scene
            Scene scene = new Scene(root, 600, 400);
            stage.setScene(scene);
            stage.setTitle("Login - Smart Parking Lot System");
            stage.centerOnScreen();
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Logout Failed", 
                      "Error returning to login page: " + e.getMessage());
        }
    }
}