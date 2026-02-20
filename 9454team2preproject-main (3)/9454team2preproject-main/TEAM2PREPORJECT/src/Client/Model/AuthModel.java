package Client.Model;

import Client.util.SocketClient;
import Client.util.CommonUtils;

/**
 * AuthModel — MVC Model for authentication.
 * Handles LOGIN, REGISTER, and password change requests to the server.
 */
public class AuthModel {

    /**
     * Attempts to log in with the given credentials.
     * Returns a LoginResult object with parsed data.
     *
     * @param username The username to login
     * @param password The password for authentication
     * @return LoginResult containing status, message, role, and accountId
     */
    public LoginResult login(String username, String password) {
        String data     = "    <password>" + CommonUtils.escapeXML(password) + "</password>";
        String request  = SocketClient.buildRequest("LOGIN", username, data);
        String response = SocketClient.sendRequest(request);

        String status = SocketClient.getStatus(response);
        String message = SocketClient.getMessage(response);

        if ("SUCCESS".equals(status)) {
            // Parse the role and accountId from response
            String dataBlock = SocketClient.getDataBlock(response);
            String role = CommonUtils.extractTag(dataBlock, "role");
            String accountId = CommonUtils.extractTag(dataBlock, "accountId");
            return new LoginResult(status, message, role, accountId);
        }

        // Return result with null role/accountId for non-success cases
        return new LoginResult(status, message, null, null);
    }

    /**
     * Registers a new account.
     * role should be "BUYER" (sellers register via SellerRegistrationController).
     * Returns full XML response string.
     */
    public RegisterResult register(String username, String password, String role) {
        String data = " <password>" + CommonUtils.escapeXML(password) + "</password>\n"
                + " <role>" + CommonUtils.escapeXML(role) + "</role>";
        String request = SocketClient.buildRequest("REGISTER", username, data);
        String response = SocketClient.sendRequest(request);

        String status = SocketClient.getStatus(response);
        String message = SocketClient.getMessage(response);

        return new RegisterResult(status, message);
    }

    /**
     * Submits a seller registration request.
     * Role is set to SELLER — server will mark status as PENDING.
     */
    public RegisterResult registerSeller(String username, String password) {
        return register(username, password, "SELLER");
    }

    /**
     * Changes the password of the currently logged-in user.
     */
    public ChangePasswordResult changePassword(String username, String oldPassword, String newPassword) {
        String data = " <oldPassword>" + CommonUtils.escapeXML(oldPassword) + "</oldPassword>\n"
                + " <newPassword>" + CommonUtils.escapeXML(newPassword) + "</newPassword>";
        String request = SocketClient.buildRequest("CHANGE_PASSWORD", username, data);
        String response = SocketClient.sendRequest(request);

        String status = SocketClient.getStatus(response);
        String message = SocketClient.getMessage(response);

        return new ChangePasswordResult(status, message);
    }

    // =====================================================
    // RESULT CLASSES - Encapsulate parsed response data
    // =====================================================

    /**
     * LoginResult --- Holds the parsed result of a login attempt.
     */
    public static class LoginResult {
        private final String status;
        private final String message;
        private final String role;
        private final String accountId;

        public LoginResult(String status, String message, String role, String accountId) {
            this.status = status;
            this.message = message;
            this.role = role;
            this.accountId = accountId;
        }

        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public String getRole() { return role; }
        public String getAccountId() { return accountId; }

        public boolean isSuccess() { return "SUCCESS".equals(status); }
        public boolean isPending() { return "PENDING".equals(status); }
        public boolean isDenied() { return "DENIED".equals(status); }
        public boolean isFailed() { return "FAILED".equals(status); }
    }

    /**
     * RegisterResult --- Holds the parsed result of a registration attempt.
     */
    public static class RegisterResult {
        private final String status;
        private final String message;

        public RegisterResult(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() { return status; }
        public String getMessage() { return message; }

        public boolean isSuccess() { return "SUCCESS".equals(status); }
    }

    /**
     * ChangePasswordResult --- Holds the parsed result of a password change.
     */
    public static class ChangePasswordResult {
        private final String status;
        private final String message;

        public ChangePasswordResult(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() { return status; }
        public String getMessage() { return message; }

        public boolean isSuccess() { return "SUCCESS".equals(status); }
    }
}