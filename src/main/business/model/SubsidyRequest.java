package business.model;

import java.util.Date;

/**
 * Domain model for SubsidyRequest entity.
 * Tracks farmer's subsidy application through its lifecycle.
 */
public class SubsidyRequest {
    private String requestId;
    private String farmerId;
    private String cropId;
    private String status; // "PENDING" | "APPROVED" | "REJECTED" | "DISTRIBUTED"
    private int quantity;
    private Date requestDate;
    private String season;
    private String rejectionReason;
    private String verification;

    // Extra fields for display (from JOINs)
    private String farmerName;
    private String farmerCnic;
    private String cropName;

    private String requestedItemId; // nullable (legacy compatibility)
    private String requestedItemName;

    /** Default constructor. */
    public SubsidyRequest() {
    }

    /** Submit the request â€” sets status to PENDING. */
    public void submit() {
        this.status = "PENDING";
    }

    /** Cancel the request. */
    public void cancel() {
        this.status = "REJECTED";
    }

    /** Get current status. */
    public String getStatus() {
        return status;
    }

    // Getters and Setters

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }

    public String getCropId() {
        return cropId;
    }

    public void setCropId(String cropId) {
        this.cropId = cropId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getVerification() {
        return verification;
    }

    public void setVerification(String verification) {
        this.verification = verification;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

    public String getFarmerCnic() {
        return farmerCnic;
    }

    public void setFarmerCnic(String farmerCnic) {
        this.farmerCnic = farmerCnic;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public String getRequestedItemId() {
        return requestedItemId;
    }

    public void setRequestedItemId(String requestedItemId) {
        this.requestedItemId = requestedItemId;
    }

    public String getRequestedItemName() {
        return requestedItemName;
    }

    public void setRequestedItemName(String requestedItemName) {
        this.requestedItemName = requestedItemName;
    }
}
