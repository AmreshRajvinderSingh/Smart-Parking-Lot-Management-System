package edu.northeastern.csye6200.models;

/**
 * Enumeration for user roles with different permission levels
 */
public enum UserRole {
    ADMIN("Administrator", "Full system access"),
    OPERATOR("Operator", "Parking management access"),
    MEMBER("Member", "View own parking history");
    
    private final String displayName;
    private final String description;
    
    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}