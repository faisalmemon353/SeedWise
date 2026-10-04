package business.model;

/**
 * Domain model for SeasonalRule entity.
 * Defines valid planting windows for crops.
 */
public class SeasonalRule {
    private String ruleId;
    private String cropId;
    private int startMonth;       // 1=Jan, 12=Dec
    private int endMonth;
    
    private boolean isActive;

    // Display field
    private String cropName;

    /** Default constructor. */
    public SeasonalRule() {}

    /** Full constructor. */
    public SeasonalRule(String ruleId, String cropId, int startMonth, int endMonth, boolean isActive) {
        this.ruleId = ruleId;
        this.cropId = cropId;
        this.startMonth = startMonth;
        this.endMonth = endMonth;
        
        this.isActive = isActive;
    }

    // --- Getters and Setters ---

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public String getCropId() { return cropId; }
    public void setCropId(String cropId) { this.cropId = cropId; }

    public int getStartMonth() { return startMonth; }
    public void setStartMonth(int startMonth) { this.startMonth = startMonth; }

    public int getEndMonth() { return endMonth; }
    public void setEndMonth(int endMonth) { this.endMonth = endMonth;
         }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }
}
