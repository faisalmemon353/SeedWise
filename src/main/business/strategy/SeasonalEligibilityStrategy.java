package business.strategy;

import business.model.*;
import data.FarmerDAO;
import java.sql.SQLException;

/**
 * GoF Strategy  Concrete Strategy.
 * Checks account status, CNIC validity, and farmer eligibility status.
 * OOP: Polymorphism  implements EligibilityStrategy interface.
 */
public class SeasonalEligibilityStrategy implements EligibilityStrategy {

    private FarmerDAO farmerDAO;

    public SeasonalEligibilityStrategy() {
        try {
            this.farmerDAO = new FarmerDAO();
        } catch (SQLException e) {
            System.err.println("[SeasonalEligibilityStrategy] DAO init error: " + e.getMessage());
        }
    }

    /**
     * Check farmer account status and eligibility conditions.
     * Checks: (1) account status Active, (2) CNIC validity.
     * @param request the subsidy request containing farmerId
     * @return EligibilityResult with pass/fail and reason
     */
    @Override
    public EligibilityResult checkEligibility(SubsidyRequest request) {
        try {
            if (request.getFarmerId() == null || request.getFarmerId().isEmpty()) {
                return new EligibilityResult(true, "No farmer-specific checks needed");
            }

            Farmer farmer = farmerDAO.findByFarmerId(request.getFarmerId());
            if (farmer == null) {
                return new EligibilityResult(false, "Farmer record not found");
            }

            // Check 1: Account status must be ACTIVE
            if (!"ACTIVE".equals(farmer.getStatus())) {
                return new EligibilityResult(false, "Farmer account is inactive");
            }

            // Check 2: CNIC format validation
            if (!farmer.getCnic().matches("\\d{5}-\\d{7}-\\d")) {
                return new EligibilityResult(false, "Invalid CNIC format on file");
            }

            // All checks passed
            return new EligibilityResult(true, "Farmer status and CNIC verified");
        } catch (SQLException e) {
            return new EligibilityResult(false, "System error during farmer eligibility check: " + e.getMessage());
        }
    }
}
