package business.model;

import java.util.Date;

/**
 * Domain model for EligibilityResult entity.
 * Stores the outcome of a farmer eligibility check (UC-10).
 */
public class EligibilityResult {
    private String resultId;
    private String requestId;
    private boolean result;
    private String reason;
    private Date checkedAt;

    /** Default constructor. */
    public EligibilityResult() {}

    /** Convenience constructor. */
    public EligibilityResult(boolean result, String reason) {
        this.result = result;
        this.reason = reason;
        this.checkedAt = new Date();
    }

    // --- Methods from class diagram ---

    /** Get the eligibility result. */
    public boolean getResult() { return result; }

    /** Get the reason for the result. */
    public String getReason() { return reason; }

    /** Check if the farmer is eligible. */
    public boolean isEligible() { return result; }

    // --- Getters and Setters ---

    public String getResultId() { return resultId; }
    public void setResultId(String resultId) { this.resultId = resultId; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public void setResult(boolean result) { this.result = result; }
    public void setReason(String reason) { this.reason = reason; }

    public Date getCheckedAt() { return checkedAt; }
    public void setCheckedAt(Date checkedAt) { this.checkedAt = checkedAt; }
}
