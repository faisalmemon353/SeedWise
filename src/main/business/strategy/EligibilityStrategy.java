package business.strategy;

import business.model.SubsidyRequest;
import business.model.EligibilityResult;

/**
 * GoF Strategy interface.
 * Allows switching eligibility-check algorithms at runtime without changing client code.
 * GRASP: Protected Variation - new rules can be added without modifying EligibilityService.
 * OOP: Abstraction - defines contract for eligibility checks.
 */
public interface EligibilityStrategy {
    /**
     * Check eligibility for a subsidy request.
     * @param request the request to evaluate
     * @return EligibilityResult with pass/fail and reason
     */
    EligibilityResult checkEligibility(SubsidyRequest request);
}
