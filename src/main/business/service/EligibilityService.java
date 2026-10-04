package business.service;

import business.model.*;
import business.strategy.*;
import java.util.ArrayList;
import java.util.List;

/**
 * GRASP Pure Fabrication  EligibilityService.
 * Does not correspond to a domain concept.
 * Coordinates multiple EligibilityStrategy implementations.
 * GoF Strategy strategies are interchangeable at runtime.
 */
public class EligibilityService {

    private final List<EligibilityStrategy> strategies;

    public EligibilityService() {
        strategies = new ArrayList<>();
        strategies.add(new CropEligibilityStrategy());
        strategies.add(new SeasonalEligibilityStrategy());
    }

    /**
     * Run all strategies; return first failure, or success if all pass.
     * OOP: Polymorphism calls checkEligibility() on each strategy polymorphically.
     * @param request the subsidy request to evaluate
     * @return EligibilityResult with pass/fail and reason
     */
    public EligibilityResult evaluate(SubsidyRequest request) {
        for (EligibilityStrategy strategy : strategies) {
            EligibilityResult result = strategy.checkEligibility(request);
            if (!result.isEligible()) return result;
        }
        return new EligibilityResult(true, "All eligibility checks passed");
    }

    /**
     * Check only the seasonal window for a given crop.
     * @param cropId crop to check
     * @return EligibilityResult
     */
    public EligibilityResult checkSeasonalWindow(String cropId) {
        SubsidyRequest dummy = new SubsidyRequest();
        dummy.setCropId(cropId);
        return new CropEligibilityStrategy().checkEligibility(dummy);
    }

    /**
     * Add a new strategy without modifying this class (Open/Closed Principle).
     * @param strategy new strategy to add
     */
    public void addStrategy(EligibilityStrategy strategy) {
        strategies.add(strategy);
    }
}
