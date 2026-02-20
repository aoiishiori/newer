package Client.Model;

/**
 * CommonModel --- Consolidated data model classes used across multiple controllers.
 */
public class CommonModel {

    // =====================================================
    // UserRow - Used by AdminController
    // Represents a user account in the admin user management table
    // =====================================================
    public static class UserRow {
        private final String accountId;
        private final String username;
        private final String role;
        private final String status;

        /**
         * Creates a new UserRow.
         */
        public UserRow(String accountId, String username, String role, String status) {
            this.accountId = accountId;
            this.username = username;
            this.role = role;
            this.status = status;
        }

        public String getAccountId() { return accountId; }
        public String getUsername() { return username; }
        public String getRole() { return role; }
        public String getStatus() { return status; }

        @Override
        public String toString() {
            return String.format("UserRow{accountId='%s', username='%s', role='%s', status='%s'}",
                    accountId, username, role, status);
        }
    }

    // =====================================================
    // LogRow - Used by AdminController
    // Represents a server activity log entry
    // =====================================================
    public static class LogRow {
        private final String timestamp;
        private final String user;
        private final String action;
        private final String dataAffected;
        private final String result;

        /**
         * Creates a new LogRow.
         */
        public LogRow(String timestamp, String user, String action,
                      String dataAffected, String result) {
            this.timestamp = timestamp;
            this.user = user;
            this.action = action;
            this.dataAffected = dataAffected;
            this.result = result;
        }

        public String getTimestamp() { return timestamp; }
        public String getUser() { return user; }
        public String getAction() { return action; }
        public String getDataAffected() { return dataAffected; }
        public String getResult() { return result; }

        @Override
        public String toString() {
            return String.format("LogRow{timestamp='%s', user='%s', action='%s', data='%s', result='%s'}",
                    timestamp, user, action, dataAffected, result);
        }
    }

    // =====================================================
    // CartEntry - Used by BuyerController
    // Represents an item in the buyer's shopping cart
    // =====================================================
    public static class CartEntry {
        private final String productId;
        private final String productName;
        private final String seller;
        private final double unitPrice;
        private int quantity;
        private final int maxAvailable;

        /**
         * Creates a new CartEntry.
         */
        public CartEntry(String productId, String productName, String seller,
                         double unitPrice, int quantity, int maxAvailable) {
            this.productId = productId;
            this.productName = productName;
            this.seller = seller;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
            this.maxAvailable = maxAvailable;
        }

        public String getProductId() { return productId; }
        public String getProductName() { return productName; }
        public String getSeller() { return seller; }
        public double getUnitPrice() { return unitPrice; }
        public int getQuantity() { return quantity; }
        public int getMaxAvailable() { return maxAvailable; }

        /**
         * Sets the quantity to purchase.
         * Used when adding more of the same product to cart.
         */
        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        /**
         * Increments the quantity by the specified amount.
         */
        public void addQuantity(int amount) {
            this.quantity += amount;
        }

        /**
         * Calculates the total price for this cart entry.
         */
        public double getLineTotal() {
            return unitPrice * quantity;
        }

        @Override
        public String toString() {
            return String.format("CartEntry{product='%s', seller='%s', price=%.2f, qty=%d}",
                    productName, seller, unitPrice, quantity);
        }
    }

    // =====================================================
    // ProductRow - Used by SellerController
    // Represents a product in the seller's product management table
    // =====================================================
    public static class ProductRow {
        private final String productId;
        private final String name;
        private final String category;
        private final String price;
        private final String quantity;
        private final String expiryDate;
        private final String status;

        /**
         * Creates a new ProductRow.
         */
        public ProductRow(String productId, String name, String category,
                          String price, String quantity, String expiryDate, String status) {
            this.productId = productId;
            this.name = name;
            this.category = category;
            this.price = price;
            this.quantity = quantity;
            this.expiryDate = expiryDate;
            this.status = status;
        }

        public String getProductId() { return productId; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public String getPrice() { return price; }
        public String getQuantity() { return quantity; }
        public String getExpiryDate() { return expiryDate; }
        public String getStatus() { return status; }

        /**
         * Parses the price as a double value.
         */
        public double getPriceAsDouble() {
            try {
                return Double.parseDouble(price);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }

        /**
         * Parses the quantity as an integer value.
         */
        public int getQuantityAsInt() {
            try {
                return Integer.parseInt(quantity);
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        @Override
        public String toString() {
            return String.format("ProductRow{id='%s', name='%s', category='%s', price='%s', qty='%s', status='%s'}",
                    productId, name, category, price, quantity, status);
        }
    }
}