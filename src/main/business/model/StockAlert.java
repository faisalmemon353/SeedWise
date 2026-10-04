package business.model;

import java.util.Date;

/**
 * Domain model for StockAlert entity.
 * Tracks low-stock alert notifications.
 */
public class StockAlert {
    private String alertId;
    private String itemTypeId;
    private Date triggeredAt;
    private String status;           // "ACTIVE" | "ACKNOWLEDGED"
    private String note;

    // Display fields
    private String itemName;
    private String unit;
    private int currentQty;
    private int minQuantity;

    /** Default constructor. */
    public StockAlert() {}

    // --- Getters and Setters ---

    public String getAlertId() { return alertId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }

    public String getItemTypeId() { return itemTypeId; }
    public void setItemTypeId(String itemTypeId) { this.itemTypeId = itemTypeId; }

    public Date getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(Date triggeredAt) { this.triggeredAt = triggeredAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public int getCurrentQty() { return currentQty; }
    public void setCurrentQty(int currentQty) { this.currentQty = currentQty; }

    public int getMinQuantity() { return minQuantity; }
    public void setMinQuantity(int minQuantity) { this.minQuantity = minQuantity; }
}
