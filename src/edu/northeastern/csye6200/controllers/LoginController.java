package edu.northeastern.csye6200.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import edu.northeastern.csye6200.models.User;
import edu.northeastern.csye6200.models.UserRole;
import edu.northeastern.csye6200.services.UserManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController implements Initializable {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label errorMessageLabel;
    
    private UserManager userManager;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get the user manager instance
        userManager = UserManager.getInstance();
        
        // Clear any previous error messages
        errorMessageLabel.setText("");
        
        // Set focus to username field
        usernameField.requestFocus();
        
        System.out.println("Login view loaded successfully");
        System.out.println("Login window should now be visible");
    }
    
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            errorMessageLabel.setText("Please enter both username and password");
            return;
        }
        
        System.out.println("Validating - Input: " + password);
        
        // Attempt login
        boolean loginSuccess = userManager.login(username, password);
        
        if (loginSuccess) {
            try {
                System.out.println("Login successful, loading main view...");
                
                // Load the main view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/northeastern/csye6200/views/main.fxml"));
                Parent root = loader.load();
                
                // Get the current stage
                Stage stage = (Stage) loginButton.getScene().getWindow();
                
                // Create and set a new scene
                Scene scene = new Scene(root);
                stage.setScene(scene);
                
                // Get the current user after login
                User currentUser = userManager.getCurrentUser();
                
                // Configure the stage
                stage.setTitle("Smart Parking Lot System - " + 
                        (currentUser.getRole() == UserRole.ADMIN ? "Admin" : "Member") + 
                        " Panel");
                stage.setWidth(1000);
                stage.setHeight(700);
                stage.centerOnScreen();
                
                // Show the window
                stage.show();
                
                System.out.println("Main window should now be visible");
                
            } catch (IOException e) {
                errorMessageLabel.setText("Error loading application: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            errorMessageLabel.setText("Invalid username or password");
            passwordField.clear();
        }
    }
    
    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            System.out.println("Register button clicked in LoginController");
            
            // Load the registration view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/northeastern/csye6200/views/register.fxml"));
            Parent root = loader.load();
            
            // Get the current stage
            Stage stage = (Stage) registerButton.getScene().getWindow();
            
            // Create a new scene
            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            
            // Configure the stage with explicit sizing and visibility
            stage.setTitle("Register - Smart Parking Lot System");
            stage.setWidth(600);
            stage.setHeight(500);
            stage.centerOnScreen();
            
            // Show the window
            stage.show();
            
            System.out.println("Registration window should now be visible");
            
        } catch (IOException e) {
            errorMessageLabel.setText("Error loading registration page: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        // If we have the login button, set stage owner
        if (loginButton != null && loginButton.getScene() != null) {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            alert.initOwner(stage);
        }
        
        alert.showAndWait();
    }
}