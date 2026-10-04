package business.strategy;

import business.model.*;
import data.SeasonalRuleDAO;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

/**
 * GoF Strategy - Concrete Strategy.
 * Checks whether the farmer's requested crop is within its valid seasonal window.
 * OOP: Polymorphism - implements EligibilityStrategy interface.
 */
public class CropEligibilityStrategy implements EligibilityStrategy {

    private SeasonalRuleDAO ruleDAO;

    public CropEligibilityStrategy() {
        try {
            this.ruleDAO = new SeasonalRuleDAO();
        } catch (SQLException e) {
            System.err.println("[CropEligibilityStrategy] DAO init error: " + e.getMessage());
        }
    }

    /**
     * Check if the crop is within its seasonal window.
     * Handles cross-year seasons (e.g., Oct-Mar: start=10, end=3).
     * @param request the subsidy request containing cropId
     * @return EligibilityResult with pass/fail and reason
     */
    @Override
    public EligibilityResult checkEligibility(SubsidyRequest request) {
        try {
            int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1; // 1-12
            List<SeasonalRule> rules = ruleDAO.findByCropId(request.getCropId());
            for (SeasonalRule rule : rules) {
                if (rule.isActive()) {
                    if (rule.getStartMonth() <= rule.getEndMonth()) {
                        // Same-year range
                        if (currentMonth >= rule.getStartMonth() && currentMonth <= rule.getEndMonth()) {
                            return new EligibilityResult(true, "Crop within seasonal window");
                        }
                    } else {
                        // Cross-year range (e.g., Oct-Mar)
                        if (currentMonth >= rule.getStartMonth() || currentMonth <= rule.getEndMonth()) {
                            return new EligibilityResult(true, "Crop within seasonal window");
                        }
                    }
                }
            }

            if (rules.isEmpty()) {
                return new EligibilityResult(true, "No seasonal restrictions configured for this crop");
            }

            // --- Season Alignment Check (Crop vs Requested Season) ---
            String cropName = request.getCropName() != null ? request.getCropName().toLowerCase() : "";
            String reqSeason = request.getSeason() != null ? request.getSeason().toUpperCase() : "";
            
            if (!cropName.isEmpty() && !reqSeason.isEmpty()) {
                String naturalSeason = "";
                if (cropName.contains("rice") || cropName.contains("cotton") || cropName.contains("maize") || 
                    cropName.contains("sugarcane") || cropName.contains("bajra")) {
                    naturalSeason = "KHARIF";
                } else if (cropName.contains("wheat") || cropName.contains("barley") || cropName.contains("gram") || 
                           cropName.contains("mustard") || cropName.contains("peas")) {
                    naturalSeason = "RABI";
                }
                
                if (!naturalSeason.isEmpty() && !naturalSeason.equals(reqSeason)) {
                    return new EligibilityResult(false, "This crop is not available for requested season (" + reqSeason + ")");
                }
            }

            return new EligibilityResult(false, "Crop is out of its seasonal window");
        } catch (SQLException e) {
            return new EligibilityResult(false, "System error during crop eligibility check: " + e.getMessage());
        }
    }
}
