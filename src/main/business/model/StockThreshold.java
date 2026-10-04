package business.model;

/**
 * Domain model for StockThreshold entity.
 * Minimum stock level before alert is triggered.
 */
public class StockThreshold {
    private String thresholdId;
    private String itemTypeId;
    private int minQuantity;

    // Display fields
    private String itemName;
    private String unit;
    private int currentQty;

    /** Default constructor. */
    public StockThreshold() {}

    /** Full constructor. */
    public StockThreshold(String thresholdId, String itemTypeId, int minQuantity) {
        this.thresholdId = thresholdId;
        this.itemTypeId = itemTypeId;
        this.minQuantity = minQuantity;
    }

    // --- Getters and Setters ---

    public String getThresholdId() { return thresholdId; }
    public void setThresholdId(String thresholdId) { this.thresholdId = thresholdId; }

    public String getItemTypeId() { return itemTypeId; }
    public void setItemTypeId(String itemTypeId) { this.itemTypeId = itemTypeId; }

    public int getMinQuantity() { return minQuantity; }
    public void setMinQuantity(int minQuantity) { this.minQuantity = minQuantity; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public int getCurrentQty() { return currentQty; }
    public void setCurrentQty(int currentQty) { this.currentQty = currentQty; }
}
