package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton DB connection manager for Microsoft SQL Server.
 * GRASP: Protected Variation isolates database details from all layers above.
 * GoF Singleton ensures a single connection instance across the app.
 */
public class DBConnection {

    // SQL Server connection settings
    // Uses Windows Authentication (Integrated Security) by default.
    // If using SQL Server Authentication, set integratedSecurity=false
    // and provide user/password.
    private static final String URL = "jdbc:sqlserver://localhost\\SQLEXPRESS;" +
            "databaseName=seedwise;" +
            "encrypt=true;" +
            "trustServerCertificate=true;" +
            "integratedSecurity=true;";

    // For SQL Server Authentication (uncomment and use instead):
    // private static final String URL =
    // "jdbc:sqlserver://localhost:1433;" +
    // "databaseName=seedwise;" +
    // "encrypt=true;" +
    // "trustServerCertificate=true;";
    // private static final String USER = "sa";
    // private static final String PASS = "your_password";

    private static DBConnection instance;
    private Connection connection;

    /** Private constructor enforces Singleton pattern. */
    private DBConnection() throws SQLException {
        try {
            // Load SQL Server JDBC driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQL Server JDBC Driver not found. " +
                    "Add mssql-jdbc-*.jar to classpath.", e);
        }
        // Windows Authentication (no user/pass needed)
        this.connection = DriverManager.getConnection(URL);

        // For SQL Server Authentication use:
        // this.connection = DriverManager.getConnection(URL, USER, PASS);
    }

    /**
     * GoF Singleton thread-safe lazy initialization.
     * 
     * @return the single DBConnection instance
     * @throws SQLException if connection fails
     */
    public static synchronized DBConnection getInstance() throws SQLException {
        if (instance == null || instance.connection.isClosed()) {
            instance = new DBConnection();
        }
        return instance;
    }

    /**
     * Get the underlying JDBC connection.
     * 
     * @return active Connection object
     */
    public Connection getConnection() {
        return connection;
    }
}
