package business.singleton;

import business.model.AuditTrail;
import data.AuditTrailDAO;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * GoF Singleton AuditLogger.
 * Guarantees a single AuditLogger instance across the entire application.
 * Delegates to AuditTrailDAO for persistence.
 * GRASP: Low Coupling  only this class writes to audit_trail table.
 */
public class AuditLogger {

    private static AuditLogger instance;
    private AuditTrailDAO auditDAO;

    /** Private constructor enforces Singleton pattern. */
    private AuditLogger() throws SQLException {
        this.auditDAO = new AuditTrailDAO();
    }

    /**
     * GoF Singleton thread-safe lazy init.
     * @return the single AuditLogger instance
     * @throws SQLException if DB connection fails
     */
    public static synchronized AuditLogger getInstance() throws SQLException {
        if (instance == null) {
            instance = new AuditLogger();
        }
        return instance;
    }

    /**
     * Log any system action.
     * @param entityId  The record being acted upon
     * @param action    Human-readable action name e.g. "APPROVE_REQUEST"
     * @param actorId   User ID of the actor
     * @param role      Actor role
     * @param details   Additional context
     */
    public void logAction(String entityId, String action, String actorId, String role, String details) {
        try {
            AuditTrail entry = new AuditTrail();
            entry.setAuditId(UUID.randomUUID().toString());
            entry.setEntityId(entityId);
            entry.setAction(action);
            entry.setActorId(actorId);
            entry.setActorRole(role);
            entry.setDetails(details);
            auditDAO.insert(entry);
        } catch (SQLException e) {
            System.err.println("[AuditLogger] Failed to log action: " + e.getMessage());
        }
    }

    /** Get audit history for a specific entity. */
    public List<AuditTrail> getHistoryByEntity(String entityId) throws SQLException {
        return auditDAO.findByEntityId(entityId);
    }

    /** Get audits filtered by role. */
    public List<AuditTrail> getAuditsByRole(String role) throws SQLException {
        return auditDAO.findByRole(role);
    }
}
