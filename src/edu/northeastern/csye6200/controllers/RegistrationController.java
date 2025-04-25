package edu.northeastern.csye6200.controllers;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import edu.northeastern.csye6200.models.Membership;
import edu.northeastern.csye6200.models.User;
import edu.northeastern.csye6200.models.UserRole;
import edu.northeastern.csye6200.services.EnhancedParkingLotManager;
import edu.northeastern.csye6200.services.UserManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegistrationController implements Initializable {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<Membership.MembershipTier> membershipTypeComboBox;
    @FXML private Button registerButton;
    @FXML private Button backToLoginButton;
    @FXML private Label errorMessageLabel;
    
    private UserManager userManager;
    private EnhancedParkingLotManager parkingManager;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get the manager instances
        userManager = UserManager.getInstance();
        parkingManager = EnhancedParkingLotManager.getInstance();
        
        // Initialize membership type combo box
        membershipTypeComboBox.setItems(FXCollections.observableArrayList(
                Membership.MembershipTier.values()));
        
        // Set default selection
        membershipTypeComboBox.getSelectionModel().selectFirst();
        
        // Clear any previous error messages
        errorMessageLabel.setText("");
    }
    
    @FXML
    private void handleRegister(ActionEvent event) {
        System.out.println("Register button clicked");
        
        // Get input values
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        Membership.MembershipTier membershipTier = membershipTypeComboBox.getValue();
        
        // Validate input
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || 
            fullName.isEmpty() || email.isEmpty()) {
            errorMessageLabel.setText("Please fill in all fields");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            errorMessageLabel.setText("Passwords do not match");
            return;
        }
        
        // Check if username already exists
        if (userManager.getUserByUsername(username) != null) {
            errorMessageLabel.setText("Username already exists");
            return;
        }
        
        System.out.println("Creating new user: " + username);
        
        // Create the user
        User newUser = userManager.registerUser(username, password, fullName, email, UserRole.MEMBER);
        
        if (newUser != null) {
            // Create membership
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusYears(1); // Default to 1 year membership
            
            Membership membership = parkingManager.createMembership(newUser, membershipTier, startDate, endDate);
            
            if (membership != null) {
                try {
                    System.out.println("Registration successful, returning to login...");
                    
                    // Registration successful, go back to login
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/northeastern/csye6200/views/login.fxml"));
                    Parent root = loader.load();
                    System.out.println("Login view loaded successfully");
                    
                    // Get the current stage
                    Stage stage = (Stage) registerButton.getScene().getWindow();
                    
                    // Create and set a new scene
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    
                    // Configure the stage
                    stage.setTitle("Login - Smart Parking Lot System");
                    stage.setWidth(600);
                    stage.setHeight(400);
                    stage.centerOnScreen();
                    stage.setAlwaysOnTop(true);
                    
                    // Show the window
                    stage.show();
                    
                    // Force window to be visible after a short delay
                    javafx.application.Platform.runLater(() -> {
                        stage.setIconified(true);
                        javafx.application.Platform.runLater(() -> {
                            stage.setIconified(false);
                            stage.toFront();
                            stage.requestFocus();
                            stage.setAlwaysOnTop(false);
                        });
                    });
                    
                    System.out.println("Login window should now be visible");
                    
                } catch (IOException e) {
                    errorMessageLabel.setText("Error returning to login: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                errorMessageLabel.setText("Error creating membership");
            }
        } else {
            errorMessageLabel.setText("Error registering user");
        }
    }
    
    @FXML
    private void handleBackToLogin(ActionEvent event) {
        try {
            System.out.println("Back to login button clicked");
            
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/northeastern/csye6200/views/login.fxml"));
            Parent root = loader.load();
            System.out.println("Login view loaded successfully");
            
            // Get the current stage
            Stage stage = (Stage) backToLoginButton.getScene().getWindow();
            
            // Create and set a new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            
            // Configure the stage
            stage.setTitle("Login - Smart Parking Lot System");
            stage.setWidth(600);
            stage.setHeight(400);
            stage.centerOnScreen();
            stage.setAlwaysOnTop(true);
            
            // Show the window
            stage.show();
            
            // Force window to be visible after a short delay
            javafx.application.Platform.runLater(() -> {
                stage.setIconified(true);
                javafx.application.Platform.runLater(() -> {
                    stage.setIconified(false);
                    stage.toFront();
                    stage.requestFocus();
                    stage.setAlwaysOnTop(false);
                });
            });
            
            System.out.println("Login window should now be visible");
            
        } catch (IOException e) {
            errorMessageLabel.setText("Error returning to login: " + e.getMessage());
            e.printStackTrace();
        }
    }
}