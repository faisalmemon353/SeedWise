package business.model;

/**
 * Domain model for ItemType entity.
 * Represents types of agricultural inputs (Seeds, Fertilizer, Pesticide).
 */
public class ItemType {
    private String itemTypeId;
    private String name;
    private String unit;
    private String description;

    /** Default constructor. */
    public ItemType() {}

    /** Full constructor. */
    public ItemType(String itemTypeId, String name, String unit, String description) {
        this.itemTypeId = itemTypeId;
        this.name = name;
        this.unit = unit;
        this.description = description;
    }

    // --- Getters and Setters ---

    public String getItemTypeId() { return itemTypeId; }
    public void setItemTypeId(String itemTypeId) { this.itemTypeId = itemTypeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() { return name + " (" + unit + ")"; }
}
