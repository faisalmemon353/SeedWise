package ui;

/**
 * NFR-2: Security â€” Session Manager Singleton.
 * Tracks the currently logged-in user for role-based access control (RBAC).
 */
public class SessionManager {

    private static SessionManager instance;
    private String currentUserId;
    private String currentUsername;
    private String currentRole;
    private String farmerId;       // Set only for FARMER role
    private String officerId;      // Set only for OFFICER role
    private String distributorId;  // Set only for DISTRIBUTOR role

    private SessionManager() {}

    /** GoF Singleton â€” thread-safe lazy init. */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /** Set session after login. */
    public void login(String userId, String username, String role) {
        this.currentUserId = userId;
        this.currentUsername = username;
        this.currentRole = role;
    }

    /** Clear session on logout. */
    public void logout() {
        this.currentUserId = null;
        this.currentUsername = null;
        this.currentRole = null;
        this.farmerId = null;
        this.officerId = null;
        this.distributorId = null;
    }

    /** Check if user is logged in. */
    public boolean isLoggedIn() {
        return currentUserId != null;
    }

    // â”€â”€â”€ Getters and Setters â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public String getCurrentUserId() { return currentUserId; }
    public String getCurrentUsername() { return currentUsername; }
    public String getCurrentRole() { return currentRole; }

    public String getFarmerId() { return farmerId; }
    public void setFarmerId(String farmerId) { this.farmerId = farmerId; }

    public String getOfficerId() { return officerId; }
    public void setOfficerId(String officerId) { this.officerId = officerId; }

    public String getDistributorId() { return distributorId; }
    public void setDistributorId(String distributorId) { this.distributorId = distributorId; }
}
