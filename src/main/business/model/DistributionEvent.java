package business.model;

import java.util.Date;

/**
 * Domain model for DistributionEvent entity.
 * Records when supplies are distributed to a farmer.
 */
public class DistributionEvent {
    private String distId;
    private String requestId;
    private String distributorId;
    private int quantity;
    private Date timestamp;

    /** Default constructor. */
    public DistributionEvent() {}

    /** Full constructor. */
    public DistributionEvent(String distId, String requestId, String distributorId, int quantity) {
        this.distId = distId;
        this.requestId = requestId;
        this.distributorId = distributorId;
        this.quantity = quantity;
        this.timestamp = new Date();
    }

    // â”€â”€â”€ Getters and Setters â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public String getDistId() { return distId; }
    public void setDistId(String distId) { this.distId = distId; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getDistributorId() { return distributorId; }
    public void setDistributorId(String distributorId) { this.distributorId = distributorId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
