# 🌱 SeedWise — Agricultural Subsidy & Input Management System

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-17-FF6F00?style=for-the-badge&logo=java&logoColor=white)](https://openjfx.io/)
[![Database](https://img.shields.io/badge/Database-Microsoft_SQL_Server-CC292B?style=for-the-badge&logo=microsoftsqlserver&logoColor=white)](https://www.microsoft.com/sql-server)
[![Architecture](https://img.shields.io/badge/Architecture-3--Tier%20Layered-4A90E2?style=for-the-badge)](https://en.wikipedia.org/wiki/Multitier_architecture)
[![UI](https://img.shields.io/badge/UI-Modern%20Glassmorphism-2E7D32?style=for-the-badge)]()

**SeedWise** is a desktop agricultural input and subsidy lifecycle management system designed to streamline agricultural governance. It provides end-to-end management for farmer verification, subsidy quota allocation, seasonal crop eligibility checks, real-time warehouse inventory monitoring, supply distribution, and automated analytical reporting.

---

## 📋 Table of Contents
1. [Core Features](#-core-features)
2. [Software Architecture & Design Patterns](#-software-architecture--design-patterns)
3. [Prerequisites](#-prerequisites)
4. [Database Setup](#-database-setup)
5. [Configuration](#-configuration)
6. [How to Run](#-how-to-run)
7. [Default User Credentials](#-default-user-credentials)
8. [Directory Structure](#-directory-structure)
9. [Project Contributors](#-project-contributors)

---

## 🚀 Core Features

- **Multi-Role Authentication & Access Control:**
  - Dedicated portals with tailored capabilities for **Agriculture Officers**, **Distributors**, and **Farmers**.
- **Farmer Registration & Quota Tracking:**
  - Automated verification of CNIC, land acreage, crop types, and subsidy quota allowances.
- **Subsidy Request Processing Lifecycle:**
  - Multi-stage pipeline: Submission $\rightarrow$ Verification $\rightarrow$ Officer Approval/Rejection $\rightarrow$ Distributor Dispatch.
- **Rule-Based Eligibility Evaluation:**
  - Dynamic verification checking crop rules and seasonal calendars (Rabi vs. Kharif).
- **Warehouse Inventory & Stock Alerts:**
  - Real-time stock decrement upon distribution, replenishment workflows, and automated low-stock warnings.
- **Distribution & Analytical Reports:**
  - Exportable distribution logs and inventory summaries in PDF format with automated timestamping.
- **Audit Trails & Security:**
  - Centralized audit logging capturing state transitions and critical user operations.

---

## 🏛 Software Architecture & Design Patterns

SeedWise follows a strict **3-Tier Layered Architecture**:
```
  ┌─────────────────────────────────────────────────────────────┐
  │                 Presentation Layer (ui.*)                   │
  │     JavaFX 17 + Custom CSS (Glassmorphism & Theming)        │
  └──────────────────────────────┬──────────────────────────────┘
                                 │
  ┌──────────────────────────────▼──────────────────────────────┐
  │               Business Logic Layer (business.*)             │
  │     Controllers, Services, Domain Models, Pattern Impls     │
  └──────────────────────────────┬──────────────────────────────┘
                                 │
  ┌──────────────────────────────▼──────────────────────────────┐
  │                  Data Access Layer (data.*)                 │
  │         DAOs, Connection Pooling & T-SQL via JDBC           │
  └──────────────────────────────┬──────────────────────────────┘
                                 │
  ┌──────────────────────────────▼──────────────────────────────┐
  │            Microsoft SQL Server (seedwise DB)               │
  └─────────────────────────────────────────────────────────────┘
```

> **Data Flow Rule:** Communication flows strictly unidirectional (`ui` $\rightarrow$ `business` $\rightarrow$ `data`). Presentation classes never directly interact with DAOs or database connections.

### Design Patterns Implementation

| Pattern | Category | Key Classes | Responsibility |
|---------|----------|-------------|----------------|
| **Singleton** | GoF Creational | `DBConnection`, `SessionManager`, `AuditLogger` | Guarantees a single instance for DB connection pooling, active user session, and audit logging. |
| **Observer** | GoF Behavioral | `StockObserver`, `LowStockAlertObserver`, `InventoryController` | Decouples stock state changes from triggering threshold notifications. |
| **Strategy** | GoF Behavioral | `EligibilityStrategy`, `CropEligibilityStrategy`, `SeasonalEligibilityStrategy` | Encapsulates interchangeable eligibility calculation algorithms for season and crop criteria. |
| **Factory Method** | GoF Creational | `ReportCreator`, `PDFReportCreator`, `ExcelReportCreator` | Decouples document format rendering logic from business reporting workflows. |
| **Controller** | GRASP | `SubsidyRequestController`, `InventoryController`, `ReportController` | Orchestrates operations and coordinates interactions between UI and business models. |
| **Information Expert** | GRASP | DAOs (`FarmerDAO`, `SubsidyRequestDAO`, etc.) | Assigns data retrieval and persistence responsibilities to the entities that hold the data. |
| **Creator** | GRASP | `SubsidyRequestController.registerFarmer()` | Encapsulates instantiation of related domain entities (`UserAccount` + `Farmer`). |
| **Pure Fabrication** | GRASP | `EligibilityService`, `AuditLogger` | Provides high cohesion and clean separation for non-domain utility services. |
| **Protected Variation** | GRASP | `DBConnection` | Shields business layers from underlying database driver and connectivity mechanics. |

---

## 📦 Prerequisites

Ensure you have the following installed on your machine:
- **Java Development Kit (JDK):** Version 17 or higher
- **Database Server:** Microsoft SQL Server (2019 / 2022 / SQL Server Express / LocalDB)
- **Database Management Tool (Optional):** SQL Server Management Studio (SSMS) or Azure Data Studio

> **Note:** All required JavaFX 17 libraries and the Microsoft JDBC driver (`mssql-jdbc.jar` & native auth DLL) are pre-packaged in the `lib/` directory. No external Maven/Gradle installation is required.

---

## 🗄 Database Setup

1. Open **SQL Server Management Studio (SSMS)** or your preferred SQL tool.
2. Open the schema script located at:
   ```
   sql/seedwise_schema.sql
   ```
3. Execute the script (`F5`). It will automatically:
   - Create the `seedwise` database.
   - Construct all relational tables, primary keys, foreign keys, and check constraints.
   - Seed default seed data (users, crops, item types, initial inventory, seasonal rules, and stock thresholds).

Alternatively, execute via PowerShell / Command Prompt using `sqlcmd`:
```powershell
sqlcmd -S localhost\SQLEXPRESS -E -i sql/seedwise_schema.sql
```

---

## ⚙ Configuration

Database connection settings are configured in [`src/main/data/DBConnection.java`](src/main/data/DBConnection.java):

```java
// Default: Windows Integrated Security
private static final String URL = "jdbc:sqlserver://localhost\\SQLEXPRESS;" +
        "databaseName=seedwise;" +
        "encrypt=true;" +
        "trustServerCertificate=true;" +
        "integratedSecurity=true;";
```

- **Windows Authentication (Default):** Uses your Windows logged-in user credentials (`integratedSecurity=true`).
- **SQL Server Authentication:** If using `sa` or a dedicated SQL user, update the connection string with your credentials:
  ```java
  private static final String URL = "jdbc:sqlserver://localhost:1433;" +
          "databaseName=seedwise;" +
          "encrypt=true;" +
          "trustServerCertificate=true;";
  private static final String USER = "sa";
  private static final String PASS = "your_password";
  ```

---

## 🚀 How to Run

### Method 1: One-Click Execution (Windows Batch Script)
Double-click `run.bat` or run it from terminal:
```bat
run.bat
```
This script automatically:
1. Gathers all Java source files under `src/main/`.
2. Compiles them with UTF-8 encoding into the `out/` folder using libraries in `lib/`.
3. Launches `ui.MainApp` with the JavaFX controls & media modules enabled.

### Method 2: Running via Visual Studio Code
1. Open the `seedwise` folder in VS Code.
2. Install the **Extension Pack for Java** (if not already installed).
3. Press `F5` or navigate to **Run and Debug** $\rightarrow$ select **SeedWise App**.

### Method 3: Manual Command Line
From the `seedwise` root directory:

**Compilation:**
```powershell
$files = Get-ChildItem -Path 'src/main' -Filter '*.java' -Recurse | Select-Object -ExpandProperty FullName
javac -encoding UTF-8 --module-path lib --add-modules javafx.controls,javafx.media -d out -cp "lib/*" $files
```

**Execution:**
```powershell
java "-Djava.library.path=lib" --module-path lib --add-modules javafx.controls,javafx.media -cp "out;lib/*" ui.MainApp
```

---

## 🔑 Default User Credentials

Pre-seeded accounts created by `sql/seedwise_schema.sql`:

| Role | Username | Password | Notes |
|------|----------|----------|-------|
| **Agriculture Officer** | `officer1` | `admin123` | Full administrative review, request approvals, and officer reports |
| **Distributor** | `distributor1` | `dist123` | Inventory intake, physical supply dispatch, and distributor reports |
| **Farmer** | *(Self-register)* | *(Self-set)* | Register directly via the **Register as Farmer** button on login screen |

---

## 📂 Directory Structure

```
seedwise/
├── .vscode/                 # VS Code configuration (launch.json, settings.json)
├── lib/                     # JavaFX 17 JARs + Microsoft SQL Server JDBC Driver
│   ├── javafx-base-17.jar
│   ├── javafx-controls-17.jar
│   ├── javafx-graphics-17.jar
│   ├── javafx-media-17-win.jar
│   ├── mssql-jdbc.jar
│   └── mssql-jdbc_auth-12.4.2.x64.dll
├── resources/               # Application styling & graphic assets
│   ├── bg.png
│   ├── seedwise.css         # Modern glassmorphism theme
│   └── images/
├── sql/
│   └── seedwise_schema.sql  # Database schema definition and seed data
├── src/main/                # Application source code
│   ├── business/            # Business logic, models, controllers, patterns
│   │   ├── controller/      # SubsidyRequestController, InventoryController, etc.
│   │   ├── factory/         # ReportCreator, PDFReportCreator, ExcelReportCreator
│   │   ├── model/           # Farmer, UserAccount, SubsidyRequest, ItemType, etc.
│   │   ├── observer/        # StockObserver, LowStockAlertObserver
│   │   ├── service/         # EligibilityService, etc.
│   │   ├── singleton/       # AuditLogger, etc.
│   │   ├── strategy/        # CropEligibilityStrategy, SeasonalEligibilityStrategy
│   │   └── util/            # Helper utilities
│   ├── data/                # Data access objects (DAOs) & DBConnection
│   └── ui/                  # JavaFX views, dashboards, and custom UI components
├── .gitignore               # Git ignore patterns
├── README.md                # Project documentation
└── run.bat                  # Automated build and run script
```

---

## 👥 Project Contributors

- **Hassan Zahid** (`24I-3011`) — Fast NUCES
  - UC-01: Register Farmer
  - UC-02: Submit Subsidy Request
  - UC-03: Process Subsidy Request
  - UC-10: Verify Farmer Eligibility
- **Faisal Memon** (`24I-3032`) — Fast NUCES
  - UC-04: Distribute Supplies
  - UC-05: Add Inventory Stock
  - UC-08: Generate Report
  - UC-09: Monitor Stock Alerts
- **Shared Implementation:**
  - UC-06, UC-07, UC-11, UC-12, Architecture Design, Glassmorphism UI, Database Integration

---

## 📄 License
This project was developed for academic purposes at FAST National University of Computer and Emerging Sciences. All rights reserved.
