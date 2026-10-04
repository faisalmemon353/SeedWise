package data;

import business.model.UserAccount;
import java.sql.*;

/**
 * Data Access Object for UserAccount entity.
 * GRASP: Low Coupling - UI and BL never touch SQL directly.
 */
public class UserAccountDAO {

    private final Connection conn;

    public UserAccountDAO() throws SQLException {
        this.conn = DBConnection.getInstance().getConnection();
    }

    /** Insert a new user account. */
    public void insert(UserAccount account) throws SQLException {
        String sql = "INSERT INTO user_account (user_id, username, password_hash, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, account.getUserId());
            ps.setString(2, account.getUsername());
            ps.setString(3, account.getPasswordHash());
            ps.setString(4, account.getRole());
            ps.executeUpdate();
        }
    }

    /** Authenticate user by username and password. Returns UserAccount or null. */
    public UserAccount authenticate(String username, String password) throws SQLException {
        String sql = "SELECT * FROM user_account WHERE username = ? AND password_hash = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new UserAccount(
                    rs.getString("user_id"),
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    rs.getString("role")
                );
            }
            return null;
        }
    }

    /** Check if username already exists. */
    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user_account WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    /** Find user by ID. */
    public UserAccount findById(String userId) throws SQLException {
        String sql = "SELECT * FROM user_account WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new UserAccount(
                    rs.getString("user_id"),
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    rs.getString("role")
                );
            }
            return null;
        }
    }

    /** Delete user by ID. */
    public void delete(String userId) throws SQLException {
        String sql = "DELETE FROM user_account WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.executeUpdate();
        }
    }

    /** Disable user account (prevents login). */
    public void disableAccount(String userId) throws SQLException {
        String sql = "UPDATE user_account SET password_hash = ? WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "DISABLED_" + java.util.UUID.randomUUID().toString());
            ps.setString(2, userId);
            ps.executeUpdate();
        }
    }
}
