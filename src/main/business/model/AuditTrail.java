package business.model;

import java.util.Date;

/**
 * Domain model for AuditTrail entity.
 * Records all significant system actions for traceability.
 */
public class AuditTrail {
    private String auditId;
    private String entityId;
    private String action;
    private String actorId;
    private String actorRole;
    private Date timestamp;
    private String details;

    /** Default constructor. */
    public AuditTrail() {}

    // â”€â”€â”€ Methods from class diagram â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** Log an action. */
    public void log(String entityId, String action, String actorId, String role, String details) {
        this.entityId = entityId;
        this.action = action;
        this.actorId = actorId;
        this.actorRole = role;
        this.details = details;
        this.timestamp = new Date();
    }

    // â”€â”€â”€ Getters and Setters â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public String getAuditId() { return auditId; }
    public void setAuditId(String auditId) { this.auditId = auditId; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getActorId() { return actorId; }
    public void setActorId(String actorId) { this.actorId = actorId; }

    public String getActorRole() { return actorRole; }
    public void setActorRole(String actorRole) { this.actorRole = actorRole; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
