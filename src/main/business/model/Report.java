package business.model;

import java.util.Date;

/**
 * Domain model for Report entity.
 * Tracks generated reports and their filters.
 */
public class Report {
    private String reportId;
    private String officerId;
    private String distributorId;
    private String type;             // "PDF" | "EXCEL"
    private Date generatedAt;
    private String filters;
    private String content;          // Generated report text content

    /** Default constructor. */
    public Report() {}

    // --- Getters and Setters ---

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getOfficerId() { return officerId; }
    public void setOfficerId(String officerId) { this.officerId = officerId; }

    public String getDistributorId() { return distributorId; }
    public void setDistributorId(String distributorId) { this.distributorId = distributorId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Date getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Date generatedAt) { this.generatedAt = generatedAt; }

    public String getFilters() { return filters; }
    public void setFilters(String filters) { this.filters = filters; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
