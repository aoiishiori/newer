package Client.CONTROLLER;

import Client.Model.CommonModel.ProductRow;
import Client.Model.SellerModel;
import Client.Model.SessionData;
import Client.util.CommonUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class SellerController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private TableView<ProductRow> mainTable;
    @FXML private VBox settingsDropdown;
    @FXML private Circle userAvatar;
    @FXML private Label welcomeLabel;

    private final SellerModel sellerModel = new SellerModel();
    private final String      username    = SessionData.getUsername();
    private ObservableList<ProductRow> productRows = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        welcomeLabel.setText("Welcome, " + username);
        setupTableColumns();
        mainTable.setItems(productRows);
        showDashboard(null);
        closeDropdown();
    }

    @SuppressWarnings("unchecked")
    private void setupTableColumns() {
        mainTable.getColumns().clear();
        mainTable.getColumns().addAll(
                makeColumn("Product ID", "productId", 110),
                makeColumn("Name",       "name",      140),
                makeColumn("Category",   "category",  100),
                makeColumn("Price (₱)",  "price",     90),
                makeColumn("Qty",        "quantity",  55),
                makeColumn("Expires",    "expiryDate",110),
                makeColumn("Status",     "status",    90)
        );
    }

    private TableColumn<ProductRow, String> makeColumn(String title, String prop, int width) {
        TableColumn<ProductRow, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(prop));
        col.setPrefWidth(width);
        return col;
    }

    private void loadMyProducts() {
        productRows.clear();
        for (String[] p : sellerModel.fetchMyProducts(username)) {
            // p = [productId, sellerUsername, name, category,
            //      originalPrice, discountedPrice, qty, expiryDate, status]
            productRows.add(new ProductRow(p[0], p[2], p[3], p[5], p[6], p[7], p[8]));
        }
    }

    // -------------------------------------------------------
    // Sidebar navigation
    // -------------------------------------------------------
    @FXML
    void showDashboard(ActionEvent event) {
        contentArea.getChildren().clear();
        Label lbl = new Label("Use the sidebar to manage products or view orders.");
        lbl.setStyle("-fx-font-size: 15; -fx-text-fill: #7f8c8d;");
        contentArea.getChildren().add(lbl);
    }

    @FXML
    void showProducts(ActionEvent event) {
        contentArea.getChildren().clear();
        HBox toolbar = new HBox(10);
        Button add = new Button("➕ Add");
        Button edit = new Button("✏️ Edit");
        Button del = new Button("🗑️ Delete");
        add.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        edit.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");
        del.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");
        add.setOnAction(e -> showAddDialog());
        edit.setOnAction(e -> showEditDialog());
        del.setOnAction(e -> handleDelete());
        toolbar.getChildren().addAll(add, edit, del);

        VBox view = new VBox(10);
        view.setStyle("-fx-padding: 20;");
        view.getChildren().addAll(toolbar, mainTable);
        VBox.setVgrow(mainTable, Priority.ALWAYS);
        contentArea.getChildren().add(view);
        loadMyProducts();
    }

    @FXML
    void showOrders(ActionEvent event) {
        contentArea.getChildren().clear();
        TableView<String[]> table = new TableView<>();
        TableColumn<String[], String> c1 = new TableColumn<>("Tx ID");
        TableColumn<String[], String> c2 = new TableColumn<>("Product ID");
        TableColumn<String[], String> c3 = new TableColumn<>("Buyer");
        TableColumn<String[], String> c4 = new TableColumn<>("Qty");
        TableColumn<String[], String> c5 = new TableColumn<>("Date");
        c1.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0]));
        c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[1]));
        c3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[2]));
        c4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[3]));
        c5.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[4]));
        table.getColumns().addAll(c1, c2, c3, c4, c5);
        table.setItems(FXCollections.observableArrayList(sellerModel.fetchMySales(username)));

        VBox view = new VBox(10);
        view.setStyle("-fx-padding: 20;");
        view.getChildren().addAll(new Label("My Sales"), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        contentArea.getChildren().add(view);
    }

    // -------------------------------------------------------
    // Product dialogs
    // -------------------------------------------------------
    private void showAddDialog() {
        Dialog<Boolean> dlg = new Dialog<>();
        dlg.setTitle("Add Product");
        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(10);
        TextField nm = new TextField(), cat = new TextField(),
                op = new TextField(), dp = new TextField(),
                qty = new TextField(), exp = new TextField("YYYY-MM-DD");
        int r = 0;
        g.add(new Label("Name:"),             0, r); g.add(nm,  1, r++);
        g.add(new Label("Category:"),         0, r); g.add(cat, 1, r++);
        g.add(new Label("Original Price:"),   0, r); g.add(op,  1, r++);
        g.add(new Label("Discounted Price:"), 0, r); g.add(dp,  1, r++);
        g.add(new Label("Quantity:"),         0, r); g.add(qty, 1, r++);
        g.add(new Label("Expiry Date:"),      0, r); g.add(exp, 1, r++);
        dlg.getDialogPane().setContent(g);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, evt -> {
            try {
                String expiry = CommonUtils.normalizeExpiryDate(exp.getText());
                if (expiry == null) {
                    new Alert(Alert.AlertType.WARNING,
                            "Invalid expiry date. Use yyyy-MM-dd (example: 2026-02-18).",
                            ButtonType.OK).showAndWait();
                    evt.consume();
                    return;
                }
                double originalPrice = Double.parseDouble(op.getText().trim());
                double discountedPrice = Double.parseDouble(dp.getText().trim());
                int quantity = Integer.parseInt(qty.getText().trim());

                String[] res = sellerModel.addProduct(username,
                        nm.getText().trim(), cat.getText().trim(),
                        originalPrice, discountedPrice, quantity, expiry);

                new Alert("SUCCESS".equals(res[0])
                        ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                        res[1], ButtonType.OK).showAndWait();

                if ("SUCCESS".equals(res[0])) {
                    loadMyProducts();
                } else {
                    evt.consume();
                }
            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.WARNING,
                        "Enter valid numbers for price and quantity.",
                        ButtonType.OK).showAndWait();
                evt.consume();
            }
        });

        dlg.showAndWait();
    }

    private void showEditDialog() {
        ProductRow sel = mainTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Select a product first.", ButtonType.OK).showAndWait();
            return;
        }
        Dialog<Boolean> dlg = new Dialog<>();
        dlg.setTitle("Edit: " + sel.getName());
        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(10);
        TextField nm  = new TextField(sel.getName());
        TextField cat = new TextField(sel.getCategory());
        TextField dp  = new TextField(sel.getPrice());
        TextField qty = new TextField(sel.getQuantity());
        TextField exp = new TextField(sel.getExpiryDate());
        ComboBox<String> st = new ComboBox<>(FXCollections.observableArrayList("AVAILABLE", "UNAVAILABLE"));
        st.setValue(sel.getStatus());
        int r = 0;
        g.add(new Label("Name:"),             0, r); g.add(nm,  1, r++);
        g.add(new Label("Category:"),         0, r); g.add(cat, 1, r++);
        g.add(new Label("Discounted Price:"), 0, r); g.add(dp,  1, r++);
        g.add(new Label("Quantity:"),         0, r); g.add(qty, 1, r++);
        g.add(new Label("Expiry Date:"),      0, r); g.add(exp, 1, r++);
        g.add(new Label("Status:"),           0, r); g.add(st,  1, r++);
        dlg.getDialogPane().setContent(g);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, evt -> {
            try {
                String expiry = CommonUtils.normalizeExpiryDate(exp.getText());
                if (expiry == null) {
                    new Alert(Alert.AlertType.WARNING,
                            "Invalid expiry date. Use yyyy-MM-dd (example: 2026-02-18).",
                            ButtonType.OK).showAndWait();
                    evt.consume();
                    return;
                }

                double discountedPrice = Double.parseDouble(dp.getText().trim());
                int quantity = Integer.parseInt(qty.getText().trim());

                String[] res = sellerModel.updateProduct(username, sel.getProductId(),
                        nm.getText().trim(), cat.getText().trim(), 0,
                        discountedPrice, quantity, expiry, st.getValue());

                new Alert("SUCCESS".equals(res[0])
                        ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                        res[1], ButtonType.OK).showAndWait();

                if ("SUCCESS".equals(res[0])) {
                    loadMyProducts();
                } else {
                    evt.consume();
                }
            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.WARNING, "Invalid numbers.", ButtonType.OK).showAndWait();
                evt.consume();
            }
        });

        dlg.showAndWait();
    }

    private void handleDelete() {
        ProductRow sel = mainTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Select a product first.", ButtonType.OK).showAndWait();
            return;
        }
        new Alert(Alert.AlertType.CONFIRMATION, "Delete: " + sel.getName() + "?",
                ButtonType.YES, ButtonType.NO)
                .showAndWait().ifPresent(b -> {
                    if (b == ButtonType.YES) {
                        String[] res = sellerModel.deleteProduct(username, sel.getProductId());
                        new Alert("SUCCESS".equals(res[0])
                                ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                                res[1], ButtonType.OK).showAndWait();
                        loadMyProducts();
                    }
                });
    }

    // -------------------------------------------------------
    // Settings dropdown
    // -------------------------------------------------------
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

    @FXML void handleUpdateProfile(ActionEvent event) {
        new Alert(Alert.AlertType.INFORMATION, "Coming soon.", ButtonType.OK).showAndWait();
        closeDropdown();
    }
    @FXML void handleShowNotifications(ActionEvent event) { closeDropdown(); showOrders(null); }
    @FXML void handleChangePassword(ActionEvent event) {
        closeDropdown();
        new Alert(Alert.AlertType.INFORMATION,
                "Log in as Buyer to change password.", ButtonType.OK).showAndWait();
    }

    @FXML
    void switchToBuyer(ActionEvent event) {
        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(
                    getClass().getResource("/Client/VIEW/Buyer_Dashboard.fxml"))));
            stage.setTitle("Marketplace");
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    // =====================================================
    // LOGOUT — required by Seller_Dashboard.fxml
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
}