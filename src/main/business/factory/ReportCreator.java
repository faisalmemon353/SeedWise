package business.factory;

import business.model.Report;
import java.util.Map;

/**
 * GoF Factory Method abstract creator.
 * Subclasses decide the concrete report format.
 * OOP: Abstraction defines report creation contract.
 */
public abstract class ReportCreator {

    /**
     * Factory method  subclasses implement to create specific report types.
     * @return Report instance of the appropriate type
     */
    public abstract Report createReport();

    /**
     * Template method that uses the factory method.
     * @param type    report type name
     * @param filters map of filter key-value pairs
     * @return configured Report object
     */
    public Report generateReportByType(String type, Map<String, Object> filters) {
        Report r = createReport();
        r.setType(type);
        r.setFilters(filters != null ? filters.toString() : "");
        r.setGeneratedAt(new java.util.Date());
        return r;
    }
}
