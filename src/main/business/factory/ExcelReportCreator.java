package business.factory;

import business.model.Report;
import java.util.UUID;

/**
 * GoF Factory Method  Concrete Creator for Excel reports.
 * OOP: Polymorphism  overrides createReport() from ReportCreator.
 */
public class ExcelReportCreator extends ReportCreator {

    @Override
    public Report createReport() {
        Report r = new Report();
        r.setReportId(UUID.randomUUID().toString());
        r.setType("EXCEL");
        return r;
    }
}
