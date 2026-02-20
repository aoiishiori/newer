package Client.CONTROLLER;

import Client.Model.AuthModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * CreateAccController — MVC Controller for Create_Account.fxml
 */
public class CreateAccController implements Initializable { // Added Initializable

    @FXML private TextField regUser;
    @FXML private PasswordField regPass;
    @FXML private PasswordField regRePass;
    @FXML private CheckBox showPass;
    @FXML private Button createButton;

    // These now have @FXML so they link to the IDs in your FXML
    @FXML private TextField visiblePass;
    @FXML private TextField visibleRePass;

    private final AuthModel authModel = new AuthModel();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // or public void initialize(URL url) {
        // This links the fields "live". If user types in regPass,
        // visiblePass updates automatically and vice versa.
        visiblePass.textProperty().bindBidirectional(regPass.textProperty());
        visibleRePass.textProperty().bindBidirectional(regRePass.textProperty());

        // Ensure they start hidden (handled in FXML, but good for safety)
        visiblePass.setManaged(false);
        visiblePass.setVisible(false);
        visibleRePass.setManaged(false);
        visibleRePass.setVisible(false);
    }

    @FXML
    void createHandler(ActionEvent event) {
        String username = regUser.getText().trim();

        // Because of Bidirectional Binding, regPass ALWAYS has the current text
        // regardless of which field the user was typing in.
        String password = regPass.getText();
        String rePassword = regRePass.getText();

        if (username.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Please fill in all fields.");
            return;
        }

        if (username.length() < 5) {
            showAlert(Alert.AlertType.WARNING, "Username must be at least 5 characters.");
            return;
        }
        if (password.length() < 8) {
            showAlert(Alert.AlertType.WARNING, "Password must be at least 8 characters.");
            return;
        }

        if (!password.equals(rePassword)) {
            showAlert(Alert.AlertType.WARNING, "Passwords do not match.");
            return;
        }

        AuthModel.RegisterResult result = authModel.register(username, password, "BUYER");

        if (result.isSuccess()) {
            showAlert(Alert.AlertType.INFORMATION, "Account created! You can now log in.");
            goToLogin();
        } else {
            String message = result.getMessage();
            showAlert(Alert.AlertType.ERROR,
                    (message == null || message.isEmpty()) ? "Registration failed." : message);
        }
    }

    @FXML
    void showPassHandler(ActionEvent event) {
        boolean show = showPass.isSelected();

        // No more manual setText() needed thanks to binding!
        toggleFields(regPass, visiblePass, show);
        toggleFields(regRePass, visibleRePass, show);
    }

    private void toggleFields(PasswordField pField, TextField tField, boolean showText) {
        pField.setVisible(!showText);
        pField.setManaged(!showText);
        tField.setVisible(showText);
        tField.setManaged(showText);

        // Maintain focus: if user is typing and clicks show, keep cursor in the box
        if (showText && pField.isFocused()) tField.requestFocus();
        else if (!showText && tField.isFocused()) pField.requestFocus();
    }

    private void goToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Client/VIEW/LoginForm.fxml"));
            Stage stage = (Stage) createButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Cannot open login: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type, msg, ButtonType.OK);
        alert.showAndWait();
    }
}