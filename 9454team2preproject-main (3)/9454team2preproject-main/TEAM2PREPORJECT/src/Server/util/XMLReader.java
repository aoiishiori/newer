package Server.util;

import Server.model.Account;
import Server.model.Product;
import Server.model.Transaction;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

/**
 * XMLReader — reads Accounts, Products, Transactions, and ServerLog from XML files.
 * All read methods are synchronized to prevent read/write conflicts.
 */
public class XMLReader {

    private static final String DATA_DIR         = "data/";
    private static final String ACCOUNTS_FILE    = DATA_DIR + "Accounts.xml";
    private static final String PRODUCTS_FILE    = DATA_DIR + "Products.xml";
    private static final String TRANSACTIONS_FILE= DATA_DIR + "Transactions.xml";
    private static final String LOG_FILE         = DATA_DIR + "server_log.xml";

    // -------------------------------------------------------
    // Read Accounts
    // -------------------------------------------------------

    // Generic XML Parsing Method
    private static <T> List<T> readEntities(String filePath, String tagName, EntityMapper<T> mapper) {
        List<T> entities = new ArrayList<>();
        try {
            File file = new File(filePath);
            if (!file.exists()) return entities;

            NodeList nodeList = getParsedList(file, tagName);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    entities.add(mapper.map((Element) node));
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] readEntities: " + e.getMessage());
        }
        return entities;
    }

    @FunctionalInterface
    private interface EntityMapper<T> {
        T map(Element element);
    }

    public synchronized static List<Account> readAccounts() {
        return readEntities(ACCOUNTS_FILE, "Account", e -> {
            Account a = new Account();
            a.setAccountId(getText(e, "accountId"));
            a.setUsername(getText(e, "username"));
            a.setPassword(getText(e, "password"));
            a.setRole(getText(e, "role"));
            a.setStatus(getText(e, "status"));
            return a;
        });
    }

    // -------------------------------------------------------
    // Read Products
    // -------------------------------------------------------

    public synchronized static List<Product> readProducts() {
        List<Product> products = new ArrayList<>();
        try {
            File file = new File(PRODUCTS_FILE);
            if (!file.exists()) return products;

            NodeList nodeList = getParsedList(file, "Product");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) node;
                    Product p = new Product();
                    p.setProductId(getText(e, "productId"));
                    p.setSellerUsername(getText(e, "sellerUsername"));
                    p.setName(getText(e, "name"));
                    p.setCategory(getText(e, "category"));
                    p.setOriginalPrice(toDouble(getText(e, "originalPrice")));
                    p.setDiscountedPrice(toDouble(getText(e, "discountedPrice")));
                    p.setAvailableQuantity(toInt(getText(e, "availableQuantity")));
                    p.setExpiryDate(getText(e, "expiryDate"));
                    p.setStatus(getText(e, "status"));
                    products.add(p);
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] readProducts: " + e.getMessage());
        }
        return products;
    }

    // -------------------------------------------------------
    // Read Transactions
    // -------------------------------------------------------

    public synchronized static List<Transaction> readTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        try {
            File file = new File(TRANSACTIONS_FILE);
            if (!file.exists()) return transactions;

            NodeList nodeList = getParsedList(file, "Transaction");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) node;
                    Transaction t = new Transaction();
                    t.setTransactionId(getText(e, "transactionId"));
                    t.setProductId(getText(e, "productId"));
                    t.setBuyerUsername(getText(e, "buyerUsername"));
                    t.setSellerUsername(getText(e, "sellerUsername"));
                    t.setQuantity(toInt(getText(e, "quantity")));
                    t.setTimestamp(getText(e, "timestamp"));
                    transactions.add(t);
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] readTransactions: " + e.getMessage());
        }
        return transactions;
    }

    // -------------------------------------------------------
    // Read Server Log — returns raw XML string for the admin
    // -------------------------------------------------------

    public synchronized static String readLogsAsXML() {
        File file = new File(LOG_FILE);
        if (!file.exists()) return "<ServerLog></ServerLog>";

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println("[ERROR] readLogs: " + e.getMessage());
        }
        return sb.toString();
    }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------

    private static NodeList getParsedList(File file, String tagName) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);
        doc.getDocumentElement().normalize();
        return doc.getElementsByTagName(tagName);
    }

    private static String getText(Element parent, String tagName) {
        NodeList nl = parent.getElementsByTagName(tagName);
        if (nl.getLength() > 0 && nl.item(0) != null) {
            return Optional.ofNullable(nl.item(0).getTextContent()).orElse("");
        }
        return "";
    }

    private static double toDouble(String val) {
        return Optional.ofNullable(val).map(Double::parseDouble).orElse(0.0);
    }

    private static int toInt(String val) {
        return Optional.ofNullable(val).map(Integer::parseInt).orElse(0);
    }
}