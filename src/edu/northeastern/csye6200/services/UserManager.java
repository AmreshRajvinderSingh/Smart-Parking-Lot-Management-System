package edu.northeastern.csye6200.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.northeastern.csye6200.models.User;
import edu.northeastern.csye6200.models.UserRole;

/**
 * Service class for managing users with file-based persistence
 */
public class UserManager {
    private static UserManager instance;
    
    private Map<String, User> users; // username -> user
    private User currentUser;
    private final String USER_DATA_FILE = "users.txt";
    
    // Private constructor (singleton pattern)
    private UserManager() {
        this.users = new HashMap<>();
        loadUsers();
        
        // Create default admin if no users exist
        if (users.isEmpty()) {
            User admin = new User("admin", "password", "Administrator", "admin@parking.com", UserRole.ADMIN);
            users.put(admin.getUsername(), admin);
            saveUsers();
        }
    }
    
    // Singleton instance getter
    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }
    
    // User authentication
    public boolean login(String username, String password) {
        User user = users.get(username);
        
        if (user != null && user.isActive() && user.validatePassword(password)) {
            currentUser = user;
            user.updateLastLogin();
            saveUsers();
            return true;
        }
        
        return false;
    }
    
    public void logout() {
        currentUser = null;
    }
    
    // User management
    public User registerUser(String username, String password, String fullName, String email, UserRole role) {
        // Check if username already exists
        if (users.containsKey(username)) {
            return null;
        }
        
        User newUser = new User(username, password, fullName, email, role);
        users.put(username, newUser);
        saveUsers();
        
        return newUser;
    }
    
    public boolean updateUser(String userId, String fullName, String email, UserRole role, boolean active) {
        User user = getUserById(userId);
        
        if (user != null) {
            user.setFullName(fullName);
            user.setEmail(email);
            user.setRole(role);
            user.setActive(active);
            saveUsers();
            return true;
        }
        
        return false;
    }
    
    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);
        
        if (user != null) {
            try {
                user.changePassword(oldPassword, newPassword);
                saveUsers();
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        
        return false;
    }
    
    public boolean deleteUser(String userId) {
        User user = getUserById(userId);
        
        if (user != null) {
            users.remove(user.getUsername());
            saveUsers();
            return true;
        }

        return false;
    }


    
    // Data access
    public User getCurrentUser() {
        return currentUser;
    }
    
    public User getUserById(String userId) {
        for (User user : users.values()) {
            if (user.getUserId().equals(userId)) {
                return user;
            }
        }
        return null;
    }
    
    public User getUserByUsername(String username) {
        return users.get(username);
    }
    
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
    
    // Persistence
    private void loadUsers() {
        File file = new File(USER_DATA_FILE);
        
        if (!file.exists()) {
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                
                // Simple parsing for demonstration
                // In a real system, use a proper serialization format like JSON or XML
                String userId = parts[0];
                String username = parts[1];
                String passwordHash = parts[2];
                String fullName = parts[3];
                String email = parts[4];
                UserRole role = UserRole.valueOf(parts[5]);
                boolean active = Boolean.parseBoolean(parts[6]);
                
                // Create user with empty password
                User user = new User(username, "", fullName, email, role);
                
                // If we're loading the admin user, set its password hash to the correct value
                if (username.equals("admin")) {
                    // For admin, use the hash of the default password "password"
                    user.setPasswordHash(Integer.toString("password".hashCode()));
                } else if (!passwordHash.equals("password_hash")) {
                    // For other users, use the stored hash if it's not the placeholder
                    user.setPasswordHash(passwordHash);
                }
                
                // Store the user in the map
                users.put(username, user);
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }
    
    private void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_DATA_FILE))) {
            for (User user : users.values()) {
                // Simple serialization for demonstration
                // In a real system, we will use a proper serialization format like JSON or XML
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%b\n",
                        user.getUserId(),
                        user.getUsername(),
                        user.getPasswordHash(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getRole().name(),
                        user.isActive()));
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }
}