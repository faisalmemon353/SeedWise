package business.model;

/**
 * Domain model for Agriculture Officer entity.
 * OOP: Inheritance â€” extends UserAccount for common login/role logic.
 */
public class AgricultureOfficer extends UserAccount {
    private String officerId;
    private String name;

    // Constructors

    /** Default constructor. */
    public AgricultureOfficer() { super(); }

    /** Full constructor. */
    public AgricultureOfficer(String officerId, String userId, String name) {
        super();
        this.setUserId(userId);
        this.officerId = officerId;
        this.name = name;
    }

    //Getters and Setters

    public String getOfficerId() { return officerId; }
    public void setOfficerId(String officerId) { this.officerId = officerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
