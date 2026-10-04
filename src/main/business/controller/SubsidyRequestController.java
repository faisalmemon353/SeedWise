package business.controller;

import business.model.*;
import business.service.EligibilityService;
import business.singleton.AuditLogger;
import data.*;
import java.sql.SQLException;
import java.util.*;

/**
 * GRASP Controller handles all subsidy-request use case events.
 * GRASP Information Expert â€” owns logic for UC-01, UC-02, UC-03, UC-10.
 * 
 * UC-01: registerFarmer()
 * UC-02: submitRequest()
 * UC-03: approveRequest() / rejectRequest()
 * UC-10: verifyEligibility()
 */
public class SubsidyRequestController {

    private final FarmerDAO farmerDAO;
    private final SubsidyRequestDAO requestDAO;
    private final UserAccountDAO userDAO;
    private final EligibilityService eligibilityService;
    private final AuditLogger auditLogger;

    public SubsidyRequestController() throws SQLException {
        this.farmerDAO = new FarmerDAO();
        this.requestDAO = new SubsidyRequestDAO();
        this.userDAO = new UserAccountDAO();
        this.eligibilityService = new EligibilityService();
        this.auditLogger = AuditLogger.getInstance();
    }

    /**
     * Validates CNIC uniqueness and format, creates UserAccount + Farmer records.
     * GRASP: Creator â€” Controller creates UserAccount and Farmer because it has
     * initializing data.
     * 
     * @return new farmerId on success
     * @throws IllegalArgumentException if CNIC invalid or already exists
     */
    public String registerFarmer(String fullName, String cnic, String contact,
            String address, String username, String password) throws SQLException {
        // 1. Validate CNIC format (XXXXX-XXXXXXX-X)
        if (!cnic.matches("\\d{5}-\\d{7}-\\d")) {
            throw new IllegalArgumentException("Invalid CNIC format. Expected: XXXXX-XXXXXXX-X");
        }
        // 2. Check CNIC uniqueness
        if (farmerDAO.cnicExists(cnic)) {
            throw new IllegalArgumentException("CNIC already registered in the system.");
        }
        // 3. Check username uniqueness
        if (userDAO.usernameExists(username)) {
            throw new IllegalArgumentException("Username already taken.");
        }
        // 4. Create UserAccount
        String userId = "USR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        UserAccount account = new UserAccount(userId, username, password, "FARMER");
        userDAO.insert(account);
        // 5. Create Farmer profile
        Farmer farmer = new Farmer();
        farmer.setFarmerId("FARM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        farmer.setUserId(userId);
        farmer.setFullName(fullName);
        farmer.setCnic(cnic);
        farmer.setContact(contact);
        farmer.setAddress(address);
        farmer.setStatus("ACTIVE");
        farmer.setRemainingAllocation(1000);
        String farmerId = farmerDAO.insertFarmer(farmer);
        // 6. Audit log
        auditLogger.logAction(farmerId, "REGISTER_FARMER", userId, "FARMER", "Farmer registered: " + fullName);
        return farmerId;
    }

    /**
     * Validates seasonal rule, duplicate check, allocation limit, then persists
     * request.
     * GRASP: Controller â€” coordinates multiple objects to fulfill the use case.
     */
    public SubsidyRequest submitRequest(String farmerId, String cropId,
            int quantity, String season, String requestedItemId) throws SQLException {
        // Validate quantity
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be a positive number.");
        }
        // Check for duplicate request for the SAME item
        if (requestDAO.duplicateExists(farmerId, cropId, season, requestedItemId)) {
            throw new IllegalStateException(
                    "A pending or approved request already exists for this specific item, crop, and season.");
        }
        // Enforce allocation limit (Item-specific)
        AllocationLimitDAO limitDAO = new AllocationLimitDAO();
        AllocationLimit itemLimit = limitDAO.findByItemAndSeason(requestedItemId, season);
        if (itemLimit != null && quantity > itemLimit.getMaxBags()) {
            quantity = itemLimit.getMaxBags(); // cap to item limit per UC-02 extension 3
        }

        // Enforce farmer's individual quota check
        Farmer farmer = farmerDAO.findByFarmerId(farmerId);
        if (farmer != null && quantity > farmer.getRemainingAllocation()) {
            throw new IllegalArgumentException("Your remaining seasonal quota is " + farmer.getRemainingAllocation()
                    + " units. You cannot request " + quantity + " units.");
        }
        // Persist request
        SubsidyRequest request = new SubsidyRequest();
        request.setRequestId("REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        request.setFarmerId(farmerId);
        request.setCropId(cropId);
        request.setQuantity(quantity);
        request.setSeason(season);
        request.setStatus("PENDING");
        request.setRequestDate(new Date());
        request.setRequestedItemId(requestedItemId);
        requestDAO.insert(request);
        // Audit log
        auditLogger.logAction(request.getRequestId(), "SUBMIT_REQUEST", farmerId, "FARMER",
                "Request for cropId=" + cropId);
        return request;
    }

    /**
     * Runs eligibility check (UC-10), validates allocation, approves request.
     * GRASP: Information Expert has all data needed to make approval decision.
     */
    public EligibilityResult approveRequest(String requestId, String officerId,
            int approvedQty) throws SQLException {
        SubsidyRequest request = requestDAO.findById(requestId);
        if (request == null)
            throw new IllegalStateException("Request not found");

        // Run eligibility check (UC-10 is included in UC-03)
        EligibilityResult eligibility = verifyEligibility(requestId, officerId);
        if (!eligibility.isEligible()) {
            throw new IllegalStateException("Farmer is ineligible: " + eligibility.getReason());
        }

        // Validate approved qty against remaining allocation
        Farmer farmer = farmerDAO.findByFarmerId(request.getFarmerId());
        if (farmer != null && approvedQty > farmer.getRemainingAllocation()) {
            throw new IllegalArgumentException("Approval Failed: Farmer's remaining quota is "
                    + farmer.getRemainingAllocation() + " units. Cannot approve " + approvedQty + " units.");
        }

        // Update status to APPROVED
        requestDAO.updateStatus(requestId, "APPROVED", null);

        // Update quantity to the approved quantity
        requestDAO.updateQuantity(requestId, approvedQty);

        // Reduce allocation
        int newAllocation = Math.max(0, farmer.getRemainingAllocation() - approvedQty);
        farmerDAO.updateRemainingAllocation(request.getFarmerId(), newAllocation);

        // Audit log
        auditLogger.logAction(requestId, "APPROVE_REQUEST", officerId, "OFFICER", "Approved qty=" + approvedQty);
        return eligibility;
    }

    /**
     * Rejects a request with a mandatory reason.
     */
    public void rejectRequest(String requestId, String officerId, String reason) throws SQLException {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason is mandatory.");
        }
        requestDAO.updateStatus(requestId, "REJECTED", reason);
        auditLogger.logAction(requestId, "REJECT_REQUEST", officerId, "OFFICER", "Reason: " + reason);
    }

    /**
     * Checks: (1) account status Active, (2) CNIC validity, (3) seasonal window.
     * Result is persisted against the request record.
     * GRASP: Controller â€” delegates to EligibilityService (Pure Fabrication).
     */
    public EligibilityResult verifyEligibility(String requestId, String officerId) throws SQLException {
        SubsidyRequest request = requestDAO.findById(requestId);
        if (request == null)
            throw new IllegalStateException("Request not found");

        EligibilityResult result = eligibilityService.evaluate(request);

        // Persist result
        EligibilityResultDAO resultDAO = new EligibilityResultDAO();
        result.setResultId("RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        resultDAO.upsert(result, requestId);

        // Update request verification field
        requestDAO.updateVerification(requestId, result.isEligible() ? "ELIGIBLE" : "INELIGIBLE");

        // Audit log
        auditLogger.logAction(requestId, "VERIFY_ELIGIBILITY", officerId, "OFFICER",
                "Result: " + result.isEligible() + " | " + result.getReason());

        return result;
    }

    // UC-06: CONFIGURE SEASONAL RULES
    public void configureSeasonalRule(String cropId, int startMonth, int endMonth, String actorId) throws SQLException {
        if (startMonth < 1 || startMonth > 12 || endMonth < 1 || endMonth > 12) {
            throw new IllegalArgumentException("Months must be between 1 and 12.");
        }
        SeasonalRuleDAO ruleDAO = new SeasonalRuleDAO();
        ruleDAO.deactivateByCropId(cropId);

        SeasonalRule rule = new SeasonalRule(java.util.UUID.randomUUID().toString(), cropId, startMonth, endMonth,
                true);
        ruleDAO.insert(rule);
        auditLogger.logAction(cropId, "CONFIGURE_SEASONAL_RULE", actorId, "OFFICER",
                "Set season window: " + startMonth + " to " + endMonth);
    }

    public List<SeasonalRule> getAllActiveSeasonalRules() throws SQLException {
        return new SeasonalRuleDAO().findAllActive();
    }

    // UC-07: SET ALLOCATION LIMITS
    public void setAllocationLimit(String itemTypeId, String itemName, int maxBags, String actorId)
            throws SQLException {
        if (maxBags <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }

        // Determine seasons based on name
        List<String> seasons = new ArrayList<>();
        String n = itemName.toLowerCase();

        if (n.contains("rice") || n.contains("cotton") || n.contains("maize") || n.contains("sugarcane")
                || n.contains("bajra")) {
            seasons.add("KHARIF");
        } else if (n.contains("wheat") || n.contains("barley") || n.contains("gram") || n.contains("mustard")
                || n.contains("peas")) {
            seasons.add("RABI");
        } else {
            // Fertilizers, Pesticides, or general items apply to BOTH
            seasons.add("RABI");
            seasons.add("KHARIF");
        }

        AllocationLimitDAO limitDAO = new AllocationLimitDAO();
        for (String s : seasons) {
            AllocationLimit lim = new AllocationLimit(java.util.UUID.randomUUID().toString(), itemTypeId, s, maxBags,
                    true);
            limitDAO.upsert(lim);
        }

        auditLogger.logAction(itemTypeId, "SET_ALLOCATION_LIMIT", actorId, "OFFICER",
                "Set limit to " + maxBags + " for seasons: " + seasons);
    }

    public List<AllocationLimit> getAllActiveAllocationLimits() throws SQLException {
        return new AllocationLimitDAO().findAllActive();
    }

    // RESET FARMER ALLOCATIONS
    public void resetAllFarmerAllocations(String actorId) throws SQLException {
        List<Farmer> farmers = farmerDAO.findAll();
        for (Farmer f : farmers) {
            farmerDAO.updateRemainingAllocation(f.getFarmerId(), 1000); // Default seasonal quota
        }
        auditLogger.logAction("SYSTEM", "RESET_ALLOCATIONS", actorId, "OFFICER",
                "Reset seasonal quota to 1000 for all farmers.");
    }

    // Helper methods
    public List<SubsidyRequest> getPendingRequests() throws SQLException {
        return requestDAO.findPending();
    }

    public List<SubsidyRequest> getApprovedRequests() throws SQLException {
        return requestDAO.findApproved();
    }

    public List<SubsidyRequest> getRequestsByFarmerId(String farmerId) throws SQLException {
        return requestDAO.findByFarmerId(farmerId);
    }

    public int countByStatus(String status) throws SQLException {
        return requestDAO.countByStatus(status);
    }

    public int countByStatusAndFarmerId(String status, String farmerId) throws SQLException {
        return requestDAO.countByStatusAndFarmerId(status, farmerId);
    }
}
