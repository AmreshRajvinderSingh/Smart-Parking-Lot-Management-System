package edu.northeastern.csye6200.models;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User class for authentication and access control
 */
public class User {
    private String userId;
    private String username;
    private String passwordHash; // In a real system, this would be properly hashed
    private String fullName;
    private String email;
    private UserRole role;
    private LocalDateTime lastLogin;
    private boolean active;
    
    public User(String username, String password, String fullName, String email, UserRole role) {
        this.userId = generateUserId();
        this.username = username;
        this.passwordHash = hashPassword(password); // Simple hash for demo
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.active = true;
    }
    
    private String generateUserId() {
        return "USR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    // Simple hash function for demonstration (not secure)
    private String hashPassword(String password) {
        // In a real system, use a proper hashing algorithm like BCrypt
        return Integer.toString(password.hashCode());
    }
    
    public boolean validatePassword(String password) {
    	System.out.println("Validating - Input: " + password);
    	System.out.println("Stored hash: " + this.passwordHash);
    	System.out.println("Input hash: " + hashPassword(password));
        return hashPassword(password).equals(this.passwordHash);
    }
    
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }
    
    // Getters
    public String getUserId() {
        return userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public boolean isActive() {
        return active;
    }
    
    // Setters
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public void changePassword(String oldPassword, String newPassword) {
        if (validatePassword(oldPassword)) {
            this.passwordHash = hashPassword(newPassword);
        } else {
            throw new IllegalArgumentException("Old password is incorrect");
        }
    }
    public String getPasswordHash() {
        return passwordHash;
    }


    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    @Override
    public String toString() {
        return fullName + " (" + username + ") - " + role;
    }
}