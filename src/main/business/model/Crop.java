package business.model;

/**
 * Domain model for Crop entity.
 */
public class Crop {
    private String cropId;
    private String name;
    private String description;

    /** Default constructor. */
    public Crop() {}

    /** Full constructor. */
    public Crop(String cropId, String name, String description) {
        this.cropId = cropId;
        this.name = name;
        this.description = description;
    }

    // --- Getters and Setters ---

    public String getCropId() { return cropId; }
    public void setCropId(String cropId) { this.cropId = cropId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() { return name; }
}
