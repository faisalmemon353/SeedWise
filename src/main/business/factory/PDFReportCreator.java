package business.factory;

import business.model.Report;
import java.util.UUID;

/**
 * GoF Factory Method Concrete Creator for PDF reports.
 * OOP: Polymorphism overrides createReport() from ReportCreator.
 */
public class PDFReportCreator extends ReportCreator {

    @Override
    public Report createReport() {
        Report r = new Report();
        r.setReportId(UUID.randomUUID().toString());
        r.setType("PDF");
        return r;
    }
}
