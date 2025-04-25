package edu.northeastern.csye6200.controllers;

import edu.northeastern.csye6200.models.User;
import edu.northeastern.csye6200.models.UserRole;
import edu.northeastern.csye6200.services.UserManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.function.Consumer;

public class EditUserDialogController {
	
	private Consumer<Void> onUserUpdated;
	
	public void setOnUserUpdated(Consumer<Void> callback) {
	    this.onUserUpdated = callback;
	}


    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<UserRole> roleComboBox;
    @FXML private CheckBox activeCheckBox;
    @FXML private TextField fullNameField;


    private User user;

    public void setUser(User user) {
        this.user = user;
        usernameField.setText(user.getUsername());
        fullNameField.setText(user.getFullName());
        emailField.setText(user.getEmail());
        roleComboBox.setItems(FXCollections.observableArrayList(UserRole.values()));
        roleComboBox.setValue(user.getRole());
        activeCheckBox.setSelected(user.isActive());
    }


    @FXML
    private void handleSave() {
        // Get updated values from form
        String updatedFullName = fullNameField.getText();
        String updatedEmail = emailField.getText();
        UserRole updatedRole = roleComboBox.getValue();
        boolean updatedActive = activeCheckBox.isSelected();

        // Use existing UserManager method to persist changes
        boolean success = UserManager.getInstance().updateUser(
            user.getUserId(),
            updatedFullName,
            updatedEmail,
            updatedRole,
            updatedActive
        );

        if (success) {
            // Reflect changes in UI (important for table refresh)
            user.setFullName(updatedFullName);
            user.setEmail(updatedEmail);
            user.setRole(updatedRole);
            user.setActive(updatedActive);

            // Trigger table refresh callback
            if (onUserUpdated != null) {
                onUserUpdated.accept(null);
            }

            // Show success alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Update Successful");
            alert.setHeaderText(null);
            alert.setContentText("User details updated successfully!");
            alert.showAndWait();

            closeDialog();
        } else {
            // Optional: Show error alert if update failed
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Update Failed");
            alert.setHeaderText(null);
            alert.setContentText("Unable to update user details.");
            alert.showAndWait();
        }
    }



    @FXML
    private void handleCancel() {
        closeDialog();
    }
    @FXML
    private void handleDelete() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete User");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This will permanently delete the user: " + user.getUsername());

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                boolean deleted = UserManager.getInstance().deleteUser(user.getUserId());
                if (deleted) {
                    // ✅ Refresh the main table
                    if (onUserUpdated != null) {
                        onUserUpdated.accept(null);
                    }

                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("User Deleted");
                    success.setHeaderText(null);
                    success.setContentText("User deleted successfully.");
                    success.showAndWait();

                    closeDialog();
                } else {
                    Alert fail = new Alert(Alert.AlertType.ERROR);
                    fail.setTitle("Delete Failed");
                    fail.setHeaderText(null);
                    fail.setContentText("Unable to delete user.");
                    fail.showAndWait();
                }
            }
        });
    }


    private void closeDialog() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }
}

