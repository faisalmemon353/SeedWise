package business.model;

/**
 * Domain model for Farmer entity.
 * OOP: Inheritance  extends UserAccount for common login/role logic.
 * OOP: Encapsulation  remainingAllocation only modified via controller.
 */
public class Farmer extends UserAccount {
    private String farmerId;
    private String fullName;
    private String cnic;              // format: XXXXX-XXXXXXX-X
    private String contact;
    private String address;
    private String status;            // "ACTIVE" | "INACTIVE"
    private int remainingAllocation;

    // Constructors 

    /** Default constructor. */
    public Farmer() { super(); }

    /** Full constructor. */
    public Farmer(String farmerId, String userId, String fullName, String cnic,
                  String contact, String address, String status, int remainingAllocation) {
        super();
        this.setUserId(userId);
        this.farmerId = farmerId;
        this.fullName = fullName;
        this.cnic = cnic;
        this.contact = contact;
        this.address = address;
        this.status = status;
        this.remainingAllocation = remainingAllocation;
    }

    //Methods from class diagram 
    /** Return farmer profile information. */
    public Farmer getProfile() {
        return this;
    }

    /** Update contact information. */
    public void updateContactInfo(String contact, String address) {
        this.contact = contact;
        this.address = address;
    }

    //Getters and Setters 
    public String getFarmerId() { return farmerId; }
    public void setFarmerId(String farmerId) { this.farmerId = farmerId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getCnic() { return cnic; }
    public void setCnic(String cnic) { this.cnic = cnic; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getRemainingAllocation() { return remainingAllocation; }
    public void setRemainingAllocation(int remainingAllocation) { this.remainingAllocation = remainingAllocation; }
}
