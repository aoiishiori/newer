package Server.util;

import Server.model.Account;
import Server.model.Product;
import Server.model.Transaction;
import java.io.*;
import java.util.*;

/**
 * XMLWriter — writes Accounts, Products, Transactions to XML files.
 * All write methods are thread-safe via per-file lock objects.
 */
public class XMLWriter {

    private static final String DATA_DIR       = "data/";
    private static final String ACCOUNTS_FILE  = DATA_DIR + "Accounts.xml";
    private static final String PRODUCTS_FILE  = DATA_DIR + "Products.xml";
    private static final String TRANSACTIONS_FILE = DATA_DIR + "Transactions.xml";

    // Separate locks per file — avoids unnecessary blocking
    private static final Object accountsLock     = new Object();
    private static final Object productsLock     = new Object();
    private static final Object transactionsLock = new Object();

    // -------------------------------------------------------
    // Initialization
    // -------------------------------------------------------

    /**
     * Call once at server startup.
     * Creates the data/ directory and all XML files if they don't exist.
     */
    public static void initializeDataFiles() {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        createIfAbsent(ACCOUNTS_FILE,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Accounts>\n</Accounts>");

        createIfAbsent(PRODUCTS_FILE,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Products>\n</Products>");

        createIfAbsent(TRANSACTIONS_FILE,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Transactions>\n</Transactions>");

        // Also initialize the log file
        ServerLogger.initLogFile();
    }

    private static void createIfAbsent(String path, String defaultContent) {
        File file = new File(path);
        if (!file.exists()) {
            writeToFile(path, defaultContent);
        }
    }

    // -------------------------------------------------------
    // Write accounts
    // -------------------------------------------------------

    public static void writeAccounts(List<Account> accounts) {
        synchronized (accountsLock) {
            StringBuilder xml = new StringBuilder();
            xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            xml.append("<Accounts>\n");

            for (Account a : accounts) {
                xml.append("  <Account>\n");
                xml.append("    <accountId>").append(esc(a.getAccountId())).append("</accountId>\n");
                xml.append("    <username>").append(esc(a.getUsername())).append("</username>\n");
                xml.append("    <password>").append(esc(a.getPassword())).append("</password>\n");
                xml.append("    <role>").append(esc(a.getRole())).append("</role>\n");
                xml.append("    <status>").append(esc(a.getStatus())).append("</status>\n");
                xml.append("  </Account>\n");
            }

            xml.append("</Accounts>");
            writeToFile(ACCOUNTS_FILE, xml.toString());
        }
    }

    // -------------------------------------------------------
    // Write products
    // -------------------------------------------------------

    public static void writeProducts(List<Product> products) {
        synchronized (productsLock) {
            StringBuilder xml = new StringBuilder();
            xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            xml.append("<Products>\n");

            for (Product p : products) {
                xml.append("  <Product>\n");
                xml.append("    <productId>").append(esc(p.getProductId())).append("</productId>\n");
                xml.append("    <sellerUsername>").append(esc(p.getSellerUsername())).append("</sellerUsername>\n");
                xml.append("    <name>").append(esc(p.getName())).append("</name>\n");
                xml.append("    <category>").append(esc(p.getCategory())).append("</category>\n");
                xml.append("    <originalPrice>").append(p.getOriginalPrice()).append("</originalPrice>\n");
                xml.append("    <discountedPrice>").append(p.getDiscountedPrice()).append("</discountedPrice>\n");
                xml.append("    <availableQuantity>").append(p.getAvailableQuantity()).append("</availableQuantity>\n");
                xml.append("    <expiryDate>").append(esc(p.getExpiryDate())).append("</expiryDate>\n");
                xml.append("    <status>").append(esc(p.getStatus())).append("</status>\n");
                xml.append("  </Product>\n");
            }

            xml.append("</Products>");
            writeToFile(PRODUCTS_FILE, xml.toString());
        }
    }

    // -------------------------------------------------------
    // Write transactions
    // -------------------------------------------------------

    public static void writeTransactions(List<Transaction> transactions) {
        synchronized (transactionsLock) {
            StringBuilder xml = new StringBuilder();
            xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            xml.append("<Transactions>\n");

            for (Transaction t : transactions) {
                xml.append("  <Transaction>\n");
                xml.append("    <transactionId>").append(esc(t.getTransactionId())).append("</transactionId>\n");
                xml.append("    <productId>").append(esc(t.getProductId())).append("</productId>\n");
                xml.append("    <buyerUsername>").append(esc(t.getBuyerUsername())).append("</buyerUsername>\n");
                xml.append("    <sellerUsername>").append(esc(t.getSellerUsername())).append("</sellerUsername>\n");
                xml.append("    <quantity>").append(t.getQuantity()).append("</quantity>\n");
                xml.append("    <timestamp>").append(esc(t.getTimestamp())).append("</timestamp>\n");
                xml.append("  </Transaction>\n");
            }

            xml.append("</Transactions>");
            writeToFile(TRANSACTIONS_FILE, xml.toString());
        }
    }

    // -------------------------------------------------------
    // Append a single transaction (more efficient than rewriting all)
    // -------------------------------------------------------

    public static void appendTransaction(Transaction t) {
        synchronized (transactionsLock) {
            File file = new File(TRANSACTIONS_FILE);
            if (!file.exists()) {
                writeToFile(TRANSACTIONS_FILE,
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Transactions>\n</Transactions>");
            }

            try {
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }

                String entry = "  <Transaction>\n"
                        + "    <transactionId>" + esc(t.getTransactionId()) + "</transactionId>\n"
                        + "    <productId>" + esc(t.getProductId()) + "</productId>\n"
                        + "    <buyerUsername>" + esc(t.getBuyerUsername()) + "</buyerUsername>\n"
                        + "    <sellerUsername>" + esc(t.getSellerUsername()) + "</sellerUsername>\n"
                        + "    <quantity>" + t.getQuantity() + "</quantity>\n"
                        + "    <timestamp>" + esc(t.getTimestamp()) + "</timestamp>\n"
                        + "  </Transaction>\n";

                String updated = content.toString()
                        .replace("</Transactions>", entry + "</Transactions>");
                writeToFile(TRANSACTIONS_FILE, updated);

            } catch (IOException e) {
                System.err.println("[ERROR] appendTransaction: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------
    // Shared file write
    // -------------------------------------------------------

    private static void writeToFile(String filename, String content) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(content);
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to write to " + filename + ": " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // XML escape helper
    // -------------------------------------------------------

    private static String esc(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}