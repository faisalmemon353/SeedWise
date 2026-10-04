package business.controller;

import business.factory.*;
import business.model.*;
import business.singleton.AuditLogger;
import business.util.SimplePDFWriter;
import data.*;
import java.sql.SQLException;
import java.util.*;

/**
 * GRASP Controller for UC-08: Generate Report.
 * Uses Factory Method pattern (PDFReportCreator / ExcelReportCreator).
 */
public class ReportController {

    private final ReportDAO reportDAO;
    private final SubsidyRequestDAO requestDAO;
    private final DistributionDAO distributionDAO;
    private final AuditLogger auditLogger;

    public ReportController() throws SQLException {
        this.reportDAO = new ReportDAO();
        this.requestDAO = new SubsidyRequestDAO();
        this.distributionDAO = new DistributionDAO();
        this.auditLogger = AuditLogger.getInstance();
    }

    /**
     * Generate a report using Factory Method pattern.
     * @param type     Report type ("Distribution Report" or "Subsidy Utilization Report")
     * @param format   Report format ("PDF" or "EXCEL")
     * @param filters  Map of filter values
     * @param actorId  User who generated the report
     * @param actorRole User's role
     * @return Report object with generated content
     */
    public Report generateReport(String type, String format, Map<String, Object> filters,
                                 String actorId, String actorRole) throws SQLException {
        // GoF Factory Method select creator based on format
        ReportCreator creator;
        if ("EXCEL".equalsIgnoreCase(format) || "CSV".equalsIgnoreCase(format)) {
            creator = new ExcelReportCreator();
        } else {
            creator = new PDFReportCreator();
        }

        // Create report using factory
        Report report = creator.generateReportByType(type, filters);
        report.setReportId(UUID.randomUUID().toString());

        // Set actor info
        if ("OFFICER".equals(actorRole)) {
            report.setOfficerId(actorId);
        } else {
            report.setDistributorId(actorId);
        }

        // Generate content based on type
        StringBuilder content = new StringBuilder();
        content.append("===================================================\n");
        content.append("  SeedWise - ").append(type).append("\n");
        content.append("  Format: ").append(format).append("\n");
        content.append("  Generated: ").append(new Date()).append("\n");
        content.append("===================================================\n\n");

        if ("Distribution Report".equals(type)) {
            List<DistributionEvent> events;
            if ("OFFICER".equalsIgnoreCase(actorRole)) {
                events = distributionDAO.findAll();
            } else {
                events = distributionDAO.findByDistributor(actorId);
            }
            content.append("Total Distribution Events: ").append(events.size()).append("\n\n");
            for (DistributionEvent e : events) {
                content.append("  Distribution ID: ").append(e.getDistId()).append("\n");
                content.append("  Request ID: ").append(e.getRequestId()).append("\n");
                content.append("  Quantity: ").append(e.getQuantity()).append("\n");
                content.append("  Date: ").append(e.getTimestamp()).append("\n");
                content.append("  ---------------------\n");
            }
        } else {
            List<SubsidyRequest> requests;
            if ("OFFICER".equalsIgnoreCase(actorRole)) {
                requests = requestDAO.searchByFilters(
                    (String) filters.get("farmerName"), null,
                    (String) filters.get("cropId"), null, null
                );
            } else {
                requests = requestDAO.searchByDistributor(
                    actorId, (String) filters.get("farmerName"), null
                );
            }
            content.append("Total Requests Found: ").append(requests.size()).append("\n\n");
            for (SubsidyRequest r : requests) {
                content.append("  Request ID: ").append(r.getRequestId()).append("\n");
                content.append("  Farmer: ").append(r.getFarmerName()).append("\n");
                content.append("  Crop: ").append(r.getCropName()).append("\n");
                content.append("  Qty: ").append(r.getQuantity()).append(" | Status: ").append(r.getStatus()).append("\n");
                content.append("  ---------------------\n");
            }
        }

        report.setContent(content.toString());

        // Target: Task 2 PDF save to Folder
        try {
            String dir = "OFFICER".equalsIgnoreCase(actorRole) ? "reportofficer" : "reportdistributor";
            java.nio.file.Path path = java.nio.file.Paths.get(dir);
            if (!java.nio.file.Files.exists(path)) {
                java.nio.file.Files.createDirectories(path);
            }
            
            boolean isCsv = "EXCEL".equalsIgnoreCase(format) || "CSV".equalsIgnoreCase(format);
            String ext = isCsv ? ".csv" : ".pdf";
            String safeType = type.replaceAll("\\s+", "_");
            String fileName = safeType + "_" + new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ext;
            java.nio.file.Path filePath = path.resolve(fileName);
            
            String fileContent = content.toString();
            if (isCsv) {
                // Convert to basic CSV format
                fileContent = fileContent.replace("===================================================", "")
                                       .replace("  ", "")
                                       .replace(": ", ",")
                                       .replace("|", ",");
                java.nio.file.Files.writeString(filePath, fileContent);
            } else {
                // Use the new SimplePDFWriter for real PDF output
                SimplePDFWriter.writePDF(filePath.toString(), "SeedWise Report: " + type, fileContent);
            }
        } catch (Exception ex) {
            System.err.println("Failed to write report: " + ex.getMessage());
        }

        // Persist report record
        reportDAO.insert(report);

        // Audit log
        auditLogger.logAction(report.getReportId(), "GENERATE_REPORT", actorId, actorRole,
            "Type: " + type + " | Format: " + format);

        return report;
    }

    /** Count total reports. */
    public int countReports() throws SQLException {
        return reportDAO.countAll();
    }
}
