package Client.CONTROLLER;

import Client.Model.AuthModel;
import Client.Model.SessionData;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * LogInController — MVC Controller for LoginForm.fxml
 *
 * On success, stores the logged-in username and role in SessionData,
 * then opens the correct dashboard (Buyer, Seller, or Admin).
 */
public class LogInController {

    @FXML private TextField UserNameInput;
    @FXML private PasswordField PasswordInput;
    @FXML private Button LogInButton;
    @FXML private Hyperlink CreateAccount;

    private final AuthModel authModel = new AuthModel();

    // -------------------------------------------------------
    // Log In button
    // -------------------------------------------------------
    @FXML
    void handleLogIn(ActionEvent event) {
        String username = UserNameInput.getText().trim();
        String password = PasswordInput.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Please enter both username and password.");
            return;
        }

        AuthModel.LoginResult result = authModel.login(username, password);

        if (result.isSuccess()) {
            // Save session
            SessionData.setUsername(username);
            SessionData.setRole(result.getRole());
            openDashboard(result.getRole());
        } else if (result.isPending()) {
            showAlert(Alert.AlertType.INFORMATION, result.getMessage());
        } else if (result.isDenied()) {
            showAlert(Alert.AlertType.ERROR, result.getMessage());
        } else {
            // FAILED or ERROR
            String message = result.getMessage();
            showAlert(Alert.AlertType.ERROR,
                       (message == null || message.isEmpty()) ? "Login failed. Please try again." : message);
        }
    }

    // ------------------------------------------------------
    // "Create an Account" hyperlink
    // -------------------------------------------------------
    @FXML
    void handleSignUp(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    Objects.requireNonNull(getClass().getResource("/Client/VIEW/Create_Account.fxml")));
            Stage stage = (Stage) CreateAccount.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Create Account");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Cannot open registration: " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // Open the correct dashboard based on role
    // -------------------------------------------------------
    private void openDashboard(String role) {
        try {
            String fxml;
            String title;

            switch (role) {
                case "ADMIN":
                    fxml  = "/Client/VIEW/Admin_Dashboard.fxml";
                    title = "Admin Panel";
                    break;
                case "SELLER":
                    fxml  = "/Client/VIEW/Seller_Dashboard.fxml";
                    title = "Seller Dashboard";
                    break;
                default: // BUYER
                    fxml  = "/Client/VIEW/Buyer_Dashboard.fxml";
                    title = "Marketplace";
                    break;
            }

            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) LogInButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Cannot open dashboard: " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------
    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
