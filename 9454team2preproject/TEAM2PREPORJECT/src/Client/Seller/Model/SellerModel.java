package Client.SELLER.MODEL;

import Client.util.SocketClient;
import java.util.ArrayList;
import java.util.List;

/**
 * SellerModel — MVC Model for the Seller dashboard.
 *
 * Each product is a String array:
 *   [0] productId  [1] sellerUsername  [2] name  [3] category
 *   [4] originalPrice  [5] discountedPrice  [6] availableQuantity
 *   [7] expiryDate  [8] status
 */
public class SellerModel {

    public List<String[]> fetchMyProducts(String username) {
        String request  = SocketClient.buildRequest("FETCH_SELLER_PRODUCTS", username);
        String response = SocketClient.sendRequest(request);
        return parseProducts(response);
    }

    public String[] addProduct(String username, String name, String category,
                               double originalPrice, double discountedPrice,
                               int quantity, String expiryDate) {
        String data = "    <name>"              + escXML(name)       + "</name>\n"
                + "    <category>"          + escXML(category)   + "</category>\n"
                + "    <originalPrice>"     + originalPrice      + "</originalPrice>\n"
                + "    <discountedPrice>"   + discountedPrice    + "</discountedPrice>\n"
                + "    <availableQuantity>" + quantity           + "</availableQuantity>\n"
                + "    <expiryDate>"        + escXML(expiryDate) + "</expiryDate>";

        String request  = SocketClient.buildRequest("ADD_PRODUCT", username, data);
        String response = SocketClient.sendRequest(request);
        return new String[]{SocketClient.getStatus(response), SocketClient.getMessage(response)};
    }

    public String[] updateProduct(String username, String productId,
                                  String name, String category,
                                  double originalPrice, double discountedPrice,
                                  int quantity, String expiryDate, String status) {
        String data = "    <productId>"         + escXML(productId)  + "</productId>\n"
                + "    <name>"              + escXML(name)       + "</name>\n"
                + "    <category>"          + escXML(category)   + "</category>\n"
                + "    <originalPrice>"     + originalPrice      + "</originalPrice>\n"
                + "    <discountedPrice>"   + discountedPrice    + "</discountedPrice>\n"
                + "    <availableQuantity>" + quantity           + "</availableQuantity>\n"
                + "    <expiryDate>"        + escXML(expiryDate) + "</expiryDate>\n"
                + "    <status>"            + escXML(status)     + "</status>";

        String request  = SocketClient.buildRequest("UPDATE_PRODUCT", username, data);
        String response = SocketClient.sendRequest(request);
        return new String[]{SocketClient.getStatus(response), SocketClient.getMessage(response)};
    }

    public String[] deleteProduct(String username, String productId) {
        String data     = "    <productId>" + escXML(productId) + "</productId>";
        String request  = SocketClient.buildRequest("DELETE_PRODUCT", username, data);
        String response = SocketClient.sendRequest(request);
        return new String[]{SocketClient.getStatus(response), SocketClient.getMessage(response)};
    }

    public List<String[]> fetchMySales(String username) {
        String request  = SocketClient.buildRequest("FETCH_MY_SALES", username);
        String response = SocketClient.sendRequest(request);
        return parseTransactions(response);
    }

    // -------------------------------------------------------
    // Parsers
    // -------------------------------------------------------
    private List<String[]> parseProducts(String response) {
        List<String[]> products = new ArrayList<>();
        if (!SocketClient.isSuccess(response)) return products;

        String dataBlock = SocketClient.getDataBlock(response);
        String[] blocks  = dataBlock.split("</product>");

        for (String block : blocks) {
            if (!block.contains("<product>")) continue;
            String inner = block.substring(block.indexOf("<product>") + 9);

            products.add(new String[]{
                    extractTag(inner, "productId"),
                    extractTag(inner, "sellerUsername"),
                    extractTag(inner, "name"),          // ← FIXED: was "n"
                    extractTag(inner, "category"),
                    extractTag(inner, "originalPrice"),
                    extractTag(inner, "discountedPrice"),
                    extractTag(inner, "availableQuantity"),
                    extractTag(inner, "expiryDate"),
                    extractTag(inner, "status")
            });
        }
        return products;
    }

    private List<String[]> parseTransactions(String response) {
        List<String[]> txList = new ArrayList<>();
        if (!SocketClient.isSuccess(response)) return txList;

        String dataBlock = SocketClient.getDataBlock(response);
        String[] blocks  = dataBlock.split("</transaction>");

        for (String block : blocks) {
            if (!block.contains("<transaction>")) continue;
            String inner = block.substring(block.indexOf("<transaction>") + 13);

            txList.add(new String[]{
                    extractTag(inner, "transactionId"),
                    extractTag(inner, "productId"),
                    extractTag(inner, "buyerUsername"),
                    extractTag(inner, "quantity"),
                    extractTag(inner, "timestamp")
            });
        }
        return txList;
    }

    private String extractTag(String xml, String tagName) {
        String open  = "<"  + tagName + ">";
        String close = "</" + tagName + ">";
        int start = xml.indexOf(open);
        int end   = xml.indexOf(close);
        if (start == -1 || end == -1) return "";
        return xml.substring(start + open.length(), end).trim();
    }

    private String escXML(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}