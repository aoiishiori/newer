package Client.CONTROLLER;

import Client.Model.AuthModel;
import Client.Model.BuyerModel;
import Client.Model.CommonModel;
import Client.Model.CommonModel.CartEntry;
import Client.Model.SessionData;
import Client.util.CommonUtils;
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
import java.util.ArrayList;
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
    private final AuthModel  authModel  = new AuthModel();
    private final String     username   = SessionData.getUsername();
    private List<String[]>   currentList;

    // Simple in-memory cart for the current buyer session
    private final List<CartEntry> cart = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        welcomeLabel.setText("Welcome back, " + username + "!");
        loadAllProducts();
        closeDropdown();
    }

    private void loadAllProducts() {
        currentList = buyerModel.fetchAllProducts(username);
        renderProducts(currentList);
    }

    @FXML
    void handleSearchButton(ActionEvent event) {
        String keyword = searchBar.getText().trim();
        if (keyword.isEmpty()) {
            renderProducts(currentList);
        } else {
            renderProducts(buyerModel.searchProducts(username, keyword));
        }
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
                + "-fx-effect: drop-shadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        card.setPrefWidth(200);

        // Product Name - prominently displayed
        String productName = (p.length > 2 && p[2] != null && !p[2].isEmpty()) ? p[2] : "Unknown Product";
        Label name = new Label(productName);
        name.setFont(new Font("System Bold", 14));
        name.setWrapText(true);
        name.setStyle("-fx-text-fill: #2c3e50;");

        Label category = new Label("📦 " + (p.length > 3 && p[3] != null ? p[3] : ""));
        category.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");

        Label price = new Label("₱" + (p.length > 5 && p[5] != null ? p[5] : "0"));
        price.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 16;");

        Label origPrice = new Label("Was: ₱" + (p.length > 4 && p[4] != null ? p[4] : "0"));
        origPrice.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11; -fx-strikethrough: true;");

        // Product Quantity - clearly displayed
        String quantity = (p.length > 6 && p[6] != null && !p[6].isEmpty()) ? p[6] : "0";
        Label qty = new Label("Quantity Available: " + quantity);
        qty.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #2980b9;");

        Label expiry = new Label("Expires: " + (p.length > 7 && p[7] != null ? p[7] : "N/A"));
        expiry.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11;");

        Label seller = new Label("By: " + (p.length > 1 && p[1] != null ? p[1] : "Unknown"));
        seller.setStyle("-fx-font-size: 11; -fx-font-style: italic;");

        Button buyBtn = new Button("BUY NOW");
        buyBtn.setMaxWidth(Double.MAX_VALUE);
        buyBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-background-radius: 5;");
        buyBtn.setOnAction(e -> handleBuy(p[0], productName));

        Button addToCartBtn = new Button("ADD TO CART");
        addToCartBtn.setMaxWidth(Double.MAX_VALUE);
        addToCartBtn.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: #2c3e50; "
                + "-fx-font-weight: bold; -fx-background-radius: 5;");
        addToCartBtn.setOnAction(e -> handleAddToCart(p));

        card.getChildren().addAll(name, category, seller, price, origPrice, qty, expiry, buyBtn, addToCartBtn);
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

    // Add selected product to in-memory cart
    private void handleAddToCart(String[] p) {
        String productId   = p[0];
        String productName = (p.length > 2 && p[2] != null && !p[2].isEmpty()) ? p[2] : "Unknown Product";
        String seller      = (p.length > 1 && p[1] != null ? p[1] : "Unknown");
        double unitPrice   = parseDouble(p.length > 5 && p[5] != null ? p[5] : "0");

        int available = 0;
        try {
            if (p.length > 6 && p[6] != null && !p[6].isEmpty()) {
                available = Integer.parseInt(p[6]);
            }
        } catch (NumberFormatException ignored) {
            available = 0;
        }

        if (available <= 0) {
            new Alert(Alert.AlertType.WARNING,
                    "This product is currently out of stock.", ButtonType.OK).showAndWait();
            return;
        }

        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Add to Cart");
        dialog.setHeaderText("Adding: " + productName);
        dialog.setContentText("Quantity (max " + available + "):");

        final int maxAvailable = available;

        dialog.showAndWait().ifPresent(input -> {
            try {
                int qty = Integer.parseInt(input.trim());
                if (qty <= 0) throw new NumberFormatException();

                int alreadyInCart = 0;
                for (CartEntry c : cart) {
                    if (c.getProductId().equals(productId)) {
                        alreadyInCart += c.getQuantity();
                    }
                }

                if (qty + alreadyInCart > maxAvailable) {
                    int remaining = maxAvailable - alreadyInCart;
                    new Alert(Alert.AlertType.WARNING,
                            "Cannot add that many. Remaining available: " + Math.max(remaining, 0),
                            ButtonType.OK).showAndWait();
                    return;
                }

                CartEntry existing = null;
                for (CartEntry c : cart) {
                    if (c.getProductId().equals(productId)) {
                        existing = c;
                        break;
                    }
                }

                if (existing != null) {
                    existing.addQuantity(qty);
                } else {
                    cart.add(new CartEntry(productId, productName, seller, unitPrice, qty, maxAvailable));
                }

                new Alert(Alert.AlertType.INFORMATION,
                        "Added to cart: " + productName + " (x" + qty + ")", ButtonType.OK).showAndWait();
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.WARNING,
                        "Enter a valid quantity.", ButtonType.OK).showAndWait();
            }
        });
    }

    // Shows current cart and lets user confirm checkout
    @FXML
    void handleViewCart(ActionEvent event) {
        if (cart.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION,
                    "Your cart is empty.", ButtonType.OK).showAndWait();
            return;
        }

        StringBuilder sb = new StringBuilder("Items in your cart:\n\n");
        double total = 0.0;

        for (CartEntry c : cart) {
            double lineTotal = c.getLineTotal();
            total += lineTotal;
            sb.append("• ").append(c.getProductName())
                    .append(" (x").append(c.getQuantity()).append(")\n")
                    .append("   Seller: ").append(c.getSeller()).append("\n")
                    .append("   Price: ₱").append(c.getUnitPrice())
                    .append(" each, Subtotal: ₱")
                    .append(String.format("%.2f", lineTotal)).append("\n\n");
        }

        sb.append("Total: ₱").append(String.format("%.2f", total)).append("\n\n")
                .append("Proceed to checkout?");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, sb.toString(),
                ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("My Cart");
        alert.setHeaderText("Cart Overview");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                checkoutCart();
            }
        });
    }

    // Performs server-side purchases for all items in cart
    private void checkoutCart() {
        if (cart.isEmpty()) {
            return;
        }

        StringBuilder summary = new StringBuilder();
        boolean anySuccess = false;
        boolean anyFailed  = false;

        for (CartEntry c : cart) {
            String[] result = buyerModel.buyProduct(username, c.getProductId(), c.getQuantity());
            summary.append(c.getProductName())
                    .append(" (x").append(c.getQuantity()).append("): ")
                    .append(result[1]).append("\n\n");

            if ("SUCCESS".equals(result[0])) {
                anySuccess = true;
            } else {
                anyFailed = true;
            }
        }

        if (anySuccess) {
            cart.clear();
            loadAllProducts();
        }

        Alert.AlertType type;
        if (anySuccess && anyFailed) {
            type = Alert.AlertType.WARNING;
        } else if (anySuccess) {
            type = Alert.AlertType.INFORMATION;
        } else {
            type = Alert.AlertType.ERROR;
        }

        new Alert(type, summary.toString(), ButtonType.OK).showAndWait();
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
        g.add(new Label("Old Password:"), 0, 0);
        g.add(op, 1, 0);
        g.add(new Label("New Password:"), 0, 1);
        g.add(np, 1, 1);
        g.add(new Label("Confirm:"), 0, 2);
        g.add(cp, 1, 2);
        dlg.getDialogPane().setContent(g);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.setResultConverter(b -> b == ButtonType.OK ? new String[]{op.getText(), np.getText(), cp.getText()} : null);
        dlg.showAndWait().ifPresent(f -> {
            if (!f[1].equals(f[2])) {
                new Alert(Alert.AlertType.WARNING, "Passwords don't match.", ButtonType.OK).showAndWait();
                return;
            }

            AuthModel.ChangePasswordResult result = authModel.changePassword(username, f[0], f[1]);
            new Alert(result.isSuccess() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                    result.getMessage(), ButtonType.OK).showAndWait();
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
        try {
            return Double.parseDouble(v);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}