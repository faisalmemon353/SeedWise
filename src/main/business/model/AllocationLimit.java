package business.model;

/**
 * Domain model for AllocationLimit entity.
 * Caps maximum bags a farmer can request per crop per season.
 */
public class AllocationLimit {
    private String limitId;
    private String itemTypeId;
    private String season;          // e.g. "RABI"
    private int maxBags;
    private boolean isActive;

    // Display fields
    private String itemName;

    /** Default constructor. */
    public AllocationLimit() {}

    /** Full constructor. */
    public AllocationLimit(String limitId, String itemTypeId, String season, int maxBags, boolean isActive) {
        this.limitId = limitId;
        this.itemTypeId = itemTypeId;
        this.season = season;
        this.maxBags = maxBags;
        this.isActive = isActive;
    }

    // ─── Getters and Setters ──────────────────────────────────────────

    public String getLimitId() { return limitId; }
    public void setLimitId(String limitId) { this.limitId = limitId; }

    public String getItemTypeId() { return itemTypeId; }
    public void setItemTypeId(String itemTypeId) { this.itemTypeId = itemTypeId; }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }

    public int getMaxBags() { return maxBags; }
    public void setMaxBags(int maxBags) { this.maxBags = maxBags; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
}
