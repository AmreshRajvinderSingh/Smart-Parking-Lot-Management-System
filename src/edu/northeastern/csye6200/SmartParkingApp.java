package edu.northeastern.csye6200;

import java.io.File;
import java.net.URL;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SmartParkingApp extends Application {
    
	@Override
	public void start(Stage primaryStage) throws Exception {
	    System.out.println("Application starting...");
	    
	    try {
	        // Explicitly force window to be visible
	        Platform.setImplicitExit(false);
	        
	        // Load the login FXML file
	        URL url;
	        try {
	            
	            url = getClass().getResource("/edu/northeastern/csye6200/views/login.fxml");
	            if (url == null) {
	                
	                url = new File("src/edu/northeastern/csye6200/views/login.fxml").toURI().toURL();
	                System.out.println("Using direct file access: " + url);
	            }
	        } catch (Exception e) {
	            System.out.println("Error loading FXML resource: " + e.getMessage());
	            // Last resort - try another path
	            url = new File("src/main/resources/edu/northeastern/csye6200/views/login.fxml").toURI().toURL();
	        }
	        
	        System.out.println("Loading FXML from: " + url);
	        FXMLLoader loader = new FXMLLoader(url);
	        Parent root = loader.load();
	        
	        // Configure the stage with explicit dimensions
	        primaryStage.setTitle("Smart Parking Lot System - Login");
	        primaryStage.setScene(new Scene(root, 600, 400));
	        primaryStage.setX(200);
	        primaryStage.setY(200);
	        
	        // Force window to be visible
	        primaryStage.centerOnScreen();
	        primaryStage.setAlwaysOnTop(true);
	        primaryStage.show();
	        
	        // Force a repaint through iconification cycle
	        Platform.runLater(() -> {
	            primaryStage.setIconified(true);
	            Platform.runLater(() -> {
	                primaryStage.setIconified(false);
	                primaryStage.toFront();
	                primaryStage.setAlwaysOnTop(false);
	            });
	        });
	        
	        System.out.println("Window should be visible now");
	    } catch (Exception e) {
	        System.out.println("Error in start method: " + e.getMessage());
	        e.printStackTrace();
	    }
	}
}