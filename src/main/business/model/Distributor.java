package business.model;

/**
 * Domain model for Distributor entity.
 * OOP: Inheritance â€” extends UserAccount for common login/role logic.
 */
public class Distributor extends UserAccount {
    private String distributorId;
    private String name;
    private String contact;
    private String cnic;
    private String address;

    // â”€â”€â”€ Constructors â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** Default constructor. */
    public Distributor() { super(); }

    /** Full constructor. */
    public Distributor(String distributorId, String userId, String name, String contact, String cnic, String address) {
        super();
        this.setUserId(userId);
        this.distributorId = distributorId;
        this.name = name;
        this.contact = contact;
        this.cnic = cnic;
        this.address = address;
    }

    // â”€â”€â”€ Getters and Setters â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public String getDistributorId() { return distributorId; }
    public void setDistributorId(String distributorId) { this.distributorId = distributorId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getCnic() { return cnic; }
    public void setCnic(String cnic) { this.cnic = cnic; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
