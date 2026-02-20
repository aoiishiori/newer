package Client.Model;

import Client.util.SocketClient;
import Client.util.CommonUtils;
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
        String request = SocketClient.buildRequest("FETCH_SELLER_PRODUCTS", username);
        String response = SocketClient.sendRequest(request);
        return parseProducts(response);
    }

    public String[] addProduct(String username, String name, String category,
                               double originalPrice, double discountedPrice,
                               int quantity, String expiryDate) {
        String data = "    <name>"              + CommonUtils.escapeXML(name)       + "</name>\n"
                + "    <category>"          + CommonUtils.escapeXML(category)   + "</category>\n"
                + "    <originalPrice>"     + originalPrice      + "</originalPrice>\n"
                + "    <discountedPrice>"   + discountedPrice    + "</discountedPrice>\n"
                + "    <availableQuantity>" + quantity           + "</availableQuantity>\n"
                + "    <expiryDate>"        + CommonUtils.escapeXML(expiryDate) + "</expiryDate>";
        String request  = SocketClient.buildRequest("ADD_PRODUCT", username, data);
        String response = SocketClient.sendRequest(request);
        return new String[]{SocketClient.getStatus(response), SocketClient.getMessage(response)};
    }

    public String[] updateProduct(String username, String productId,
                                  String name, String category,
                                  double originalPrice, double discountedPrice,
                                  int quantity, String expiryDate, String status) {
        String data = "    <productId>"         + CommonUtils.escapeXML(productId)  + "</productId>\n"
                + "    <name>"              + CommonUtils.escapeXML(name)       + "</name>\n"
                + "    <category>"          + CommonUtils.escapeXML(category)   + "</category>\n"
                + "    <originalPrice>"     + originalPrice      + "</originalPrice>\n"
                + "    <discountedPrice>"   + discountedPrice    + "</discountedPrice>\n"
                + "    <availableQuantity>" + quantity           + "</availableQuantity>\n"
                + "    <expiryDate>"        + CommonUtils.escapeXML(expiryDate) + "</expiryDate>\n"
                + "    <status>"            + CommonUtils.escapeXML(status)     + "</status>";
        String request  = SocketClient.buildRequest("UPDATE_PRODUCT", username, data);
        String response = SocketClient.sendRequest(request);
        return new String[]{SocketClient.getStatus(response), SocketClient.getMessage(response)};
    }

    public String[] deleteProduct(String username, String productId) {
        String data     = "  <productId>" + CommonUtils.escapeXML(productId) + "</productId>";
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
                    CommonUtils.extractTag(inner, "productId"),
                    CommonUtils.extractTag(inner, "sellerUsername"),
                    CommonUtils.extractTag(inner, "name"),
                    CommonUtils.extractTag(inner, "category"),
                    CommonUtils.extractTag(inner, "originalPrice"),
                    CommonUtils.extractTag(inner, "discountedPrice"),
                    CommonUtils.extractTag(inner, "availableQuantity"),
                    CommonUtils.extractTag(inner, "expiryDate"),
                    CommonUtils.extractTag(inner, "status")
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
                    CommonUtils.extractTag(inner, "transactionId"),
                    CommonUtils.extractTag(inner, "productId"),
                    CommonUtils.extractTag(inner, "buyerUsername"),
                    CommonUtils.extractTag(inner, "quantity"),
                    CommonUtils.extractTag(inner, "timestamp")
            });
        }
        return txList;
    }
}