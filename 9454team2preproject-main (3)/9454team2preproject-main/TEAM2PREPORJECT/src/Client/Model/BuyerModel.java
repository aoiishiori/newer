package Client.Model;

import Client.util.SocketClient;
import Client.util.CommonUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * BuyerModel — MVC Model for the Buyer dashboard.
 *
 * Each product is returned as a String array:
 *   [0] productId
 *   [1] sellerUsername
 *   [2] name
 *   [3] category
 *   [4] originalPrice
 *   [5] discountedPrice
 *   [6] availableQuantity
 *   [7] expiryDate
 *   [8] status
 */
public class BuyerModel {

    public List<String[]> fetchAllProducts(String username) {
        String request  = SocketClient.buildRequest("FETCH_ALL_PRODUCTS", username);
        String response = SocketClient.sendRequest(request);
        return parseProducts(response);
    }

    public List<String[]> searchProducts(String username, String keyword) {
        String data     = "    <keyword>" + CommonUtils.escapeXML(keyword) + "</keyword>";
        String request  = SocketClient.buildRequest("SEARCH_PRODUCTS", username, data);
        String response = SocketClient.sendRequest(request);
        return parseProducts(response);
    }

    public String[] buyProduct(String username, String productId, int quantity) {
        String data = "    <productId>" + productId + "</productId>\n"
                + "    <quantity>" + quantity + "</quantity>";
        String request  = SocketClient.buildRequest("BUY_PRODUCT", username, data);
        String response = SocketClient.sendRequest(request);
        return new String[]{
                SocketClient.getStatus(response),
                SocketClient.getMessage(response)
        };
    }

    public List<String[]> fetchMyPurchases(String username) {
        String request  = SocketClient.buildRequest("FETCH_MY_PURCHASES", username);
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
                    CommonUtils.extractTag(inner, "sellerUsername"),
                    CommonUtils.extractTag(inner, "quantity"),
                    CommonUtils.extractTag(inner, "timestamp")
            });
        }
        return txList;
    }
}