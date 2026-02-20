package Client.CONTROLLER;

import Client.Model.AdminModel;
import Client.Model.CommonModel.LogRow;
import Client.Model.CommonModel.UserRow;
import Client.Model.SessionData;
import Client.util.CommonUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    @FXML private StackPane adminContentArea;
    @FXML private TextField adminSearchField;
    @FXML private TableView<UserRow> userTable;
    @FXML private TableColumn<UserRow, String> colId;
    @FXML private TableColumn<UserRow, String> colUsername;
    @FXML private TableColumn<UserRow, String> colRole;
    @FXML private TableColumn<UserRow, String> colStatus;
    @FXML private ComboBox<String> roleFilter;
    @FXML private VBox serverControlView;
    @FXML private VBox userMgmtView;
    @FXML private Label serverStatusLabel;
    @FXML private Label viewTitle;

    // ✅ ADDED: Log viewer components
    @FXML private VBox logViewerView;
    @FXML private TableView<LogRow> logTable;
    @FXML private TableColumn<LogRow, String> colTimestamp;
    @FXML private TableColumn<LogRow, String> colUser;
    @FXML private TableColumn<LogRow, String> colAction;
    @FXML private TableColumn<LogRow, String> colData;
    @FXML private TableColumn<LogRow, String> colResult;

    private final AdminModel adminModel  = new AdminModel();
    private final String adminUsername   = SessionData.getUsername();
    private ObservableList<UserRow> allUsers = FXCollections.observableArrayList();
    private ObservableList<LogRow> allLogs = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // User table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("accountId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Log table columns
        if (logTable != null) {
            colTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
            colUser.setCellValueFactory(new PropertyValueFactory<>("user"));
            colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
            colData.setCellValueFactory(new PropertyValueFactory<>("dataAffected"));
            colResult.setCellValueFactory(new PropertyValueFactory<>("result"));
        }

        roleFilter.setItems(FXCollections.observableArrayList("All", "ADMIN", "BUYER", "SELLER"));
        roleFilter.setValue("All");
        roleFilter.setOnAction(e -> applyFilter());
        adminSearchField.textProperty().addListener((obs, o, n) -> applyFilter());

        loadUsers();
        showUserManagement(null);
    }

    private void loadUsers() {
        allUsers.clear();
        for (String[] u : adminModel.fetchAllUsers(adminUsername)) {
            allUsers.add(new UserRow(u[0], u[1], u[2], u[3]));
        }
        userTable.setItems(allUsers);
    }

    private void applyFilter() {
        String kw   = adminSearchField.getText().toLowerCase();
        String role = roleFilter.getValue();
        ObservableList<UserRow> filtered = FXCollections.observableArrayList();
        for (UserRow r : allUsers) {
            boolean matchKw   = r.getUsername().toLowerCase().contains(kw)
                    || r.getAccountId().toLowerCase().contains(kw);
            boolean matchRole = "All".equals(role) || role.equals(r.getRole());
            if (matchKw && matchRole) filtered.add(r);
        }
        userTable.setItems(filtered);
    }

    // -------------------------------------------------------
    // Sidebar
    // -------------------------------------------------------
    @FXML
    void showUserManagement(ActionEvent event) {
        viewTitle.setText("User Management");
        userMgmtView.setVisible(true);
        userMgmtView.setManaged(true);
        serverControlView.setVisible(false);
        serverControlView.setManaged(false);
        if (logViewerView != null) {
            logViewerView.setVisible(false);
            logViewerView.setManaged(false);
        }
        loadUsers();
    }

    @FXML
    void showSellerRequests(ActionEvent event) {
        viewTitle.setText("Seller Requests — Pending Accounts");
        roleFilter.setValue("SELLER");
        applyFilter();
        userMgmtView.setVisible(true);
        userMgmtView.setManaged(true);
        serverControlView.setVisible(false);
        serverControlView.setManaged(false);
        if (logViewerView != null) {
            logViewerView.setVisible(false);
            logViewerView.setManaged(false);
        }
    }

    @FXML
    void showServerControl(ActionEvent event) {
        viewTitle.setText("Server Control");
        userMgmtView.setVisible(false);
        userMgmtView.setManaged(false);
        serverControlView.setVisible(true);
        serverControlView.setManaged(true);
        if (logViewerView != null) {
            logViewerView.setVisible(false);
            logViewerView.setManaged(false);
        }
    }

    @FXML
    public void handleViewLogs() {
        viewTitle.setText("Server Activity Logs");
        userMgmtView.setVisible(false);
        userMgmtView.setManaged(false);
        serverControlView.setVisible(false);
        serverControlView.setManaged(false);

        if (logViewerView != null) {
            logViewerView.setVisible(true);
            logViewerView.setManaged(true);
        }

        loadLogs();
    }

    private void loadLogs() {
        allLogs.clear();

        String rawXML = adminModel.fetchLogs(adminUsername);

        try {
            String[] entries = rawXML.split("</LogEntry>");

            for (String entry : entries) {
                if (!entry.contains("<LogEntry>")) continue;

                String timestamp = CommonUtils.extractTag(entry, "timestamp");
                String user = CommonUtils.extractTag(entry, "user");
                String action = CommonUtils.extractTag(entry, "action");
                String dataAffected = CommonUtils.extractTag(entry, "dataAffected");
                String result = CommonUtils.extractTag(entry, "result");

                allLogs.add(new LogRow(timestamp, user, action, dataAffected, result));
            }

            if (logTable != null) {
                logTable.setItems(allLogs);
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to parse logs: " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // Accept Seller
    // -------------------------------------------------------
    @FXML
    void handleAcceptSeller(ActionEvent event) {
        UserRow sel = userTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.WARNING, "Please select a seller first.");
            return;
        }

        if (!"SELLER".equals(sel.getRole())) {
            showAlert(Alert.AlertType.WARNING, "Selected user is not a seller.");
            return;
        }

        boolean ok = adminModel.updateUserStatus(
                adminUsername, sel.getUsername(), "APPROVED");
        showAlert(ok ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                ok ? sel.getUsername() + " has been approved as a seller."
                        : "Failed to approve seller.");
        if (ok) loadUsers();
    }

    @FXML
    void handleResetPassword(ActionEvent event) {
        UserRow sel = userTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.WARNING, "Please select a user first.");
            return;
        }

        Alert choice = new Alert(Alert.AlertType.CONFIRMATION);
        choice.setTitle("Update Account Status");
        choice.setHeaderText("User: " + sel.getUsername()
                + "  |  Role: " + sel.getRole()
                + "  |  Current Status: " + sel.getStatus());
        choice.setContentText("What action do you want to perform?");

        ButtonType btnApprove = new ButtonType("✅ APPROVE");
        ButtonType btnDeny    = new ButtonType("❌ DENY");
        ButtonType btnCancel  = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        choice.getButtonTypes().setAll(btnApprove, btnDeny, btnCancel);

        choice.showAndWait().ifPresent(btn -> {
            if (btn == btnApprove || btn == btnDeny) {
                String newStatus = (btn == btnApprove) ? "APPROVED" : "DENIED";
                boolean ok = adminModel.updateUserStatus(
                        adminUsername, sel.getUsername(), newStatus);
                showAlert(ok ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                        ok ? sel.getUsername() + " is now " + newStatus + "."
                                : "Failed to update status.");
                if (ok) loadUsers();
            }
        });
    }

    // -------------------------------------------------------
    // Delete account
    // -------------------------------------------------------
    @FXML
    void handleDeleteAccount(ActionEvent event) {
        UserRow sel = userTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.WARNING, "Select a user to delete.");
            return;
        }
        new Alert(Alert.AlertType.CONFIRMATION,
                "Delete user: " + sel.getUsername() + "?",
                ButtonType.YES, ButtonType.NO)
                .showAndWait().ifPresent(b -> {
                    if (b == ButtonType.YES) {
                        boolean ok = adminModel.deleteUser(adminUsername, sel.getUsername());
                        showAlert(ok ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                                ok ? "User deleted." : "Failed.");
                        if (ok) loadUsers();
                    }
                });
    }

    // -------------------------------------------------------
    // Server control
    // -------------------------------------------------------
    @FXML
    void handleRestartServer(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION,
                "Go to the Server console and type 'stop', then relaunch Server.java.");
    }

    // -------------------------------------------------------
    // Logout
    // -------------------------------------------------------
    @FXML
    public void handleLogout() {
        try {
            SessionData.clear();
            Stage stage = (Stage) viewTitle.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(
                    getClass().getResource("/Client/VIEW/LoginForm.fxml"))));
            stage.setTitle("Login");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Logout failed: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String msg) {
        new Alert(type, msg, ButtonType.OK).showAndWait();
    }
}