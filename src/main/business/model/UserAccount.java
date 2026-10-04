package business.model;

/**
 * Domain model for user authentication.
 * Base class for Farmer, AgricultureOfficer, and Distributor.
 * OOP: Encapsulation - all fields private with controlled accessors.
 */
public class UserAccount {
    private String userId;        // UUID
    private String username;
    private String passwordHash;
    private String role;          // "FARMER" | "OFFICER" | "DISTRIBUTOR"

   

    /** Default constructor. */
    public UserAccount() {}

    /** Full constructor. */
    public UserAccount(String userId, String username, String passwordHash, String role) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    
    /**
     * Authenticate user by comparing credentials.
     * @param username entered username
     * @param pwd      entered password
     * @return true if credentials match
     */
    public boolean login(String username, String pwd) {
        return this.username.equals(username) && this.passwordHash.equals(pwd);
    }

    /** Logout  clears session (handled by SessionManager). */
    public void logout() {
        // Handled by SessionManager singleton
    }

    /** Change password  validates old password first. */
    public void changePassword(String oldPwd, String newPwd) {
        if (this.passwordHash.equals(oldPwd)) {
            this.passwordHash = newPwd;
        }
    }

   
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
