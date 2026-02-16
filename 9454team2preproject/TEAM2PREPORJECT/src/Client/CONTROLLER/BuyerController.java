package Client.CONTROLLER;

import Client.Buyer.Model.BuyerModel;
import Client.Model.AuthModel;
import Client.util.SocketClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class BuyerController implements Initializable {

    @FXML private FlowPane productContainer;
    @FXML private TextField searchBar;
    @FXML private VBox settingsDropdown;
    @FXML private Circle userAvatar;
    @FXML private Label welcomeLabel;

    private final BuyerModel buyerModel = new BuyerModel();
    private final String     username   = SessionData.getUsername();
    private List<String[]>   currentList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        welcomeLabel.setText("Welcome back, " + username + "!");

        searchBar.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                renderProducts(currentList);
            } else {
                renderProducts(buyerModel.searchProducts(username, newVal.trim()));
            }
        });

        loadAllProducts();
        closeDropdown();
    }

    private void loadAllProducts() {
        currentList = buyerModel.fetchAllProducts(username);
        renderProducts(currentList);
    }

    private void renderProducts(List<String[]> products) {
        productContainer.getChildren().clear();
        if (products == null || products.isEmpty()) {
            Label empty = new Label("No products available.");
            empty.setStyle("-fx-font-size: 16; -fx-text-fill: #95a5a6;");
            productContainer.getChildren().add(empty);
            return;
        }
        for (String[] p : products) {
            productContainer.getChildren().add(buildProductCard(p));
        }
    }

    private VBox buildProductCard(String[] p) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; "
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 15; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        card.setPrefWidth(200);

        Label name = new Label(p[2]);
        name.setFont(new Font("System Bold", 14));
        name.setWrapText(true);

        Label price = new Label("₱" + p[5]);
        price.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 16;");
        Label origPrice = new Label("Was: ₱" + p[4]);
        origPrice.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11;");
        Label qty    = new Label("Stock: " + p[6]);
        Label expiry = new Label("Expires: " + p[7]);
        expiry.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11;");
        Label seller = new Label("By: " + p[1]);
        seller.setStyle("-fx-font-size: 11;");

        Button buyBtn = new Button("BUY NOW");
        buyBtn.setMaxWidth(Double.MAX_VALUE);
        buyBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-background-radius: 5;");
        buyBtn.setOnAction(e -> handleBuy(p[0], p[2]));

        card.getChildren().addAll(name, new Label("📦 " + p[3]), seller,
                price, origPrice, qty, expiry, buyBtn);
        return card;
    }

    private void handleBuy(String productId, String productName) {
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Buy Product");
        dialog.setHeaderText("Buying: " + productName);
        dialog.setContentText("Quantity:");
        dialog.showAndWait().ifPresent(input -> {
            try {
                int qty = Integer.parseInt(input.trim());
                if (qty <= 0) throw new NumberFormatException();
                String[] result = buyerModel.buyProduct(username, productId, qty);
                new Alert("SUCCESS".equals(result[0])
                        ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                        result[1], ButtonType.OK).showAndWait();
                if ("SUCCESS".equals(result[0])) loadAllProducts();
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.WARNING,
                        "Enter a valid quantity.", ButtonType.OK).showAndWait();
            }
        });
    }

    @FXML
    void toggleSettings(MouseEvent event) {
        boolean v = settingsDropdown.isVisible();
        settingsDropdown.setVisible(!v);
        settingsDropdown.setManaged(!v);
    }

    private void closeDropdown() {
        settingsDropdown.setVisible(false);
        settingsDropdown.setManaged(false);
    }

    @FXML
    void handleSortLowToHigh(ActionEvent event) {
        if (currentList == null) return;
        currentList.sort(Comparator.comparingDouble(p -> parseDouble(p[5])));
        renderProducts(currentList);
    }

    @FXML
    void handleSortHighToLow(ActionEvent event) {
        if (currentList == null) return;
        currentList.sort((a, b) -> Double.compare(parseDouble(b[5]), parseDouble(a[5])));
        renderProducts(currentList);
    }

    @FXML
    void handleUpdateProfile(ActionEvent event) {
        new Alert(Alert.AlertType.INFORMATION,
                "Profile update coming soon.", ButtonType.OK).showAndWait();
        closeDropdown();
    }

    @FXML
    void handleShowNotifications(ActionEvent event) {
        List<String[]> purchases = buyerModel.fetchMyPurchases(username);
        StringBuilder sb = new StringBuilder("Your Recent Purchases:\n\n");
        if (purchases.isEmpty()) {
            sb.append("No purchases yet.");
        } else {
            for (String[] t : purchases) {
                sb.append("📦 ").append(t[0])
                        .append("\n   Product: ").append(t[1])
                        .append("\n   Seller: ").append(t[2])
                        .append("\n   Qty: ").append(t[3])
                        .append("\n   Date: ").append(t[4]).append("\n\n");
            }
        }
        TextArea area = new TextArea(sb.toString());
        area.setEditable(false);
        area.setPrefSize(420, 300);
        Stage s = new Stage();
        s.setTitle("My Purchases");
        s.setScene(new Scene(area));
        s.show();
        closeDropdown();
    }

    @FXML
    void handleChangePassword(ActionEvent event) {
        closeDropdown();
        Dialog<String[]> dlg = new Dialog<>();
        dlg.setTitle("Change Password");
        GridPane g = new GridPane();
        g.setHgap(10); g.setVgap(10);
        PasswordField op = new PasswordField(), np = new PasswordField(), cp = new PasswordField();
        g.add(new Label("Old Password:"), 0, 0); g.add(op, 1, 0);
        g.add(new Label("New Password:"), 0, 1); g.add(np, 1, 1);
        g.add(new Label("Confirm:"),      0, 2); g.add(cp, 1, 2);
        dlg.getDialogPane().setContent(g);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.setResultConverter(b -> b == ButtonType.OK ? new String[]{op.getText(), np.getText(), cp.getText()} : null);
        dlg.showAndWait().ifPresent(f -> {
            if (!f[1].equals(f[2])) {
                new Alert(Alert.AlertType.WARNING, "Passwords don't match.", ButtonType.OK).showAndWait();
                return;
            }
            String resp = new AuthModel().changePassword(username, f[0], f[1]);
            new Alert(SocketClient.isSuccess(resp)
                    ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                    SocketClient.getMessage(resp), ButtonType.OK).showAndWait();
        });
    }

    @FXML
    void handleBecomeSeller(ActionEvent event) {
        closeDropdown();
        try {
            Stage s = new Stage();
            s.setTitle("Seller Registration");
            s.setScene(new Scene(FXMLLoader.load(
                    getClass().getResource("/Client/VIEW/RegistrationForm.fxml"))));
            s.show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR,
                    "Cannot open form: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    // =====================================================
    // LOGOUT — required by Buyer_Dashboard.fxml
    // =====================================================
    @FXML
    public void handleLogout() {
        try {
            SessionData.clear();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(
                    getClass().getResource("/Client/VIEW/LoginForm.fxml"))));
            stage.setTitle("Login");
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR,
                    "Logout failed: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    private double parseDouble(String v) {
        try { return Double.parseDouble(v); } catch (NumberFormatException e) { return 0; }
    }
}