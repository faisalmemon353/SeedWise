-- ============================================================
-- SeedWise - Agricultural Subsidy and Input Management System
-- SQL Server Database Schema (T-SQL)
-- Run in SSMS: Open this file and execute (F5)
-- ============================================================

-- Create database
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'seedwise')
    CREATE DATABASE seedwise;
GO

USE seedwise;
GO

-- ==================== USER ACCOUNT ====================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'user_account')
CREATE TABLE user_account (
    user_id       VARCHAR(36)  PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('FARMER','OFFICER','DISTRIBUTOR'))
);
GO

-- ==================== FARMER ====================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'farmer')
CREATE TABLE farmer (
    farmer_id           VARCHAR(36) PRIMARY KEY,
    user_id             VARCHAR(36) NOT NULL,
    full_name           VARCHAR(100) NOT NULL,
    cnic                VARCHAR(15)  NOT NULL UNIQUE,
    contact             VARCHAR(15),
    address             VARCHAR(255),
    status              VARCHAR(10) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE')),
    remaining_allocation INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES user_account(user_id)
);
GO

-- ==================== AGRICULTURE OFFICER ====================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'agriculture_officer')
CREATE TABLE agriculture_officer (
    officer_id  VARCHAR(36) PRIMARY KEY,
    user_id     VARCHAR(36) NOT NULL,
    name        VARCHAR(100) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user_account(user_id)
);
GO

-- ==================== DISTRIBUTOR ====================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'distributor')
CREATE TABLE distributor (
    distributor_id VARCHAR(36) PRIMARY KEY,
    user_id        VARCHAR(36) NOT NULL,
    name           VARCHAR(100) NOT NULL,
    contact        VARCHAR(15),
    FOREIGN KEY (user_id) REFERENCES user_account(user_id)
);
GO

-- ==================== CROP ====================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'crop')
CREATE TABLE crop (
    crop_id     VARCHAR(36) PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(MAX)
);
GO

-- ==================== SEASONAL RULE ====================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'seasonal_rule')
CREATE TABLE seasonal_rule (
    rule_id     VARCHAR(36) PRIMARY KEY,
    crop_id     VARCHAR(36) NOT NULL,
    start_month INT NOT NULL,
    end_month   INT NOT NULL,
    is_active   BIT DEFAULT 1,
    FOREIGN KEY (crop_id) REFERENCES crop(crop_id)
);
GO

-- ==================== ALLOCATION LIMIT ====================
CREATE TABLE allocation_limit (
    limit_id     VARCHAR(36) PRIMARY KEY,
    item_type_id VARCHAR(36) NOT NULL,
    season       VARCHAR(20) NOT NULL,
    max_bags     INT NOT NULL,
    is_active    BIT DEFAULT 1,
    FOREIGN KEY (item_type_id) REFERENCES item_type(item_type_id)
);
GO

-- ==================== SUBSIDY REQUEST ====================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'subsidy_request')
CREATE TABLE subsidy_request (
    request_id       VARCHAR(36) PRIMARY KEY,
    farmer_id        VARCHAR(36) NOT NULL,
    crop_id          VARCHAR(36) NOT NULL,
    status           VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING','APPROVED','REJECTED','DISTRIBUTED')),
    quantity         INT NOT NULL,
    request_date     DATETIME DEFAULT GETDATE(),
    season           VARCHAR(20) NOT NULL,
    rejection_reason VARCHAR(MAX),
    verification     VARCHAR(50),
    requested_item_id VARCHAR(36),
    FOREIGN KEY (farmer_id) REFERENCES farmer(farmer_id),
    FOREIGN KEY (crop_id)   REFERENCES crop(crop_id),
    FOREIGN KEY (requested_item_id) REFERENCES item_type(item_type_id)
);
GO

-- ==================== ELIGIBILITY RESULT ====================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'eligibility_result')
CREATE TABLE eligibility_result (
    result_id   VARCHAR(36) PRIMARY KEY,
    request_id  VARCHAR(36) NOT NULL UNIQUE,
    result      BIT NOT NULL,
    reason      VARCHAR(255),
    checked_at  DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (request_id) REFERENCES subsidy_request(request_id)
);
GO

-- ==================== ITEM TYPE ====================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'item_type')
CREATE TABLE item_type (
    item_type_id VARCHAR(36) PRIMARY KEY,
    name         VARCHAR(100) NOT NULL UNIQUE,
    unit         VARCHAR(20),
    description  VARCHAR(MAX)
);
GO

-- ==================== INVENTORY STOCK ====================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'inventory_stock')
CREATE TABLE inventory_stock (
    stock_id      VARCHAR(36) PRIMARY KEY,
    item_type_id  VARCHAR(36) NOT NULL,
    quantity      INT NOT NULL DEFAULT 0,
    last_updated  DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (item_type_id) REFERENCES item_type(item_type_id)
);
GO

-- ==================== STOCK THRESHOLD ====================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'stock_threshold')
CREATE TABLE stock_threshold (
    threshold_id  VARCHAR(36) PRIMARY KEY,
    item_type_id  VARCHAR(36) NOT NULL UNIQUE,
    min_quantity  INT NOT NULL,
    FOREIGN KEY (item_type_id) REFERENCES item_type(item_type_id)
);
GO

-- ==================== DISTRIBUTION EVENT ====================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'distribution_event')
CREATE TABLE distribution_event (
    dist_id        VARCHAR(36) PRIMARY KEY,
    request_id     VARCHAR(36) NOT NULL,
    distributor_id VARCHAR(36) NOT NULL,
    quantity       INT NOT NULL,
    timestamp      DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (request_id)     REFERENCES subsidy_request(request_id),
    FOREIGN KEY (distributor_id) REFERENCES distributor(distributor_id)
);
GO

-- ==================== AUDIT TRAIL ====================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'audit_trail')
CREATE TABLE audit_trail (
    audit_id    VARCHAR(36) PRIMARY KEY,
    entity_id   VARCHAR(36) NOT NULL,
    action      VARCHAR(50) NOT NULL,
    actor_id    VARCHAR(36) NOT NULL,
    actor_role  VARCHAR(20) NOT NULL,
    timestamp   DATETIME DEFAULT GETDATE(),
    details     VARCHAR(MAX)
);
GO

-- ==================== REPORT ====================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'report')
CREATE TABLE report (
    report_id      VARCHAR(36) PRIMARY KEY,
    officer_id     VARCHAR(36),
    distributor_id VARCHAR(36),
    type           VARCHAR(50) NOT NULL,
    generated_at   DATETIME DEFAULT GETDATE(),
    filters        VARCHAR(MAX)
);
GO

-- ==================== STOCK ALERT ====================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'stock_alert')
CREATE TABLE stock_alert (
    alert_id      VARCHAR(36) PRIMARY KEY,
    item_type_id  VARCHAR(36) NOT NULL,
    triggered_at  DATETIME DEFAULT GETDATE(),
    status        VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','ACKNOWLEDGED')),
    note          VARCHAR(MAX),
    FOREIGN KEY (item_type_id) REFERENCES item_type(item_type_id)
);
GO

-- ============================================================
-- SEED DATA
-- ============================================================

-- Default officer account (password: admin123)
IF NOT EXISTS (SELECT 1 FROM user_account WHERE user_id = 'u-officer-01')
    INSERT INTO user_account VALUES ('u-officer-01','officer1','admin123','OFFICER');

IF NOT EXISTS (SELECT 1 FROM user_account WHERE user_id = 'u-dist-01')
    INSERT INTO user_account VALUES ('u-dist-01','distributor1','dist123','DISTRIBUTOR');

-- Officer profile
IF NOT EXISTS (SELECT 1 FROM agriculture_officer WHERE officer_id = 'off-01')
    INSERT INTO agriculture_officer VALUES ('off-01', 'u-officer-01', 'Admin Officer');

-- Distributor profile
IF NOT EXISTS (SELECT 1 FROM distributor WHERE distributor_id = 'dist-01')
    INSERT INTO distributor VALUES ('dist-01', 'u-dist-01', 'Main Distributor', '03001234567');

-- ============================================================
-- CROPS (5 Rabi + 5 Kharif = 10 total)
-- ============================================================

-- RABI CROPS
IF NOT EXISTS (SELECT 1 FROM crop WHERE crop_id = 'crop-wheat')
    INSERT INTO crop VALUES ('crop-wheat','Wheat','Rabi winter staple crop');
IF NOT EXISTS (SELECT 1 FROM crop WHERE crop_id = 'crop-barley')
    INSERT INTO crop VALUES ('crop-barley','Barley','Rabi cereal crop');
IF NOT EXISTS (SELECT 1 FROM crop WHERE crop_id = 'crop-gram')
    INSERT INTO crop VALUES ('crop-gram','Gram (Chickpea)','Rabi pulse crop');
IF NOT EXISTS (SELECT 1 FROM crop WHERE crop_id = 'crop-mustard')
    INSERT INTO crop VALUES ('crop-mustard','Mustard / Canola','Rabi oilseed crop');
IF NOT EXISTS (SELECT 1 FROM crop WHERE crop_id = 'crop-peas')
    INSERT INTO crop VALUES ('crop-peas','Peas','Rabi vegetable crop');

-- KHARIF CROPS
IF NOT EXISTS (SELECT 1 FROM crop WHERE crop_id = 'crop-rice')
    INSERT INTO crop VALUES ('crop-rice','Rice','Kharif season crop');
IF NOT EXISTS (SELECT 1 FROM crop WHERE crop_id = 'crop-cotton')
    INSERT INTO crop VALUES ('crop-cotton','Cotton','Kharif cash crop');
IF NOT EXISTS (SELECT 1 FROM crop WHERE crop_id = 'crop-maize')
    INSERT INTO crop VALUES ('crop-maize','Maize','Kharif cereal crop');
IF NOT EXISTS (SELECT 1 FROM crop WHERE crop_id = 'crop-sugarcane')
    INSERT INTO crop VALUES ('crop-sugarcane','Sugarcane','Kharif cash crop');
IF NOT EXISTS (SELECT 1 FROM crop WHERE crop_id = 'crop-bajra')
    INSERT INTO crop VALUES ('crop-bajra','Bajra (Millet)','Kharif coarse grain');

-- ============================================================
-- ITEM TYPES - Seeds (per crop), Fertilizers, Pesticides
-- ============================================================

-- SEEDS (one per crop)
IF NOT EXISTS (SELECT 1 FROM item_type WHERE item_type_id = 'seed-wheat')
    INSERT INTO item_type VALUES ('seed-wheat','Wheat Seeds','Bags','Certified wheat seed');
IF NOT EXISTS (SELECT 1 FROM item_type WHERE item_type_id = 'seed-barley')
    INSERT INTO item_type VALUES ('seed-barley','Barley Seeds','Bags','Certified barley seed');
IF NOT EXISTS (SELECT 1 FROM item_type WHERE item_type_id = 'seed-gram')
    INSERT INTO item_type VALUES ('seed-gram','Gram Seeds','Bags','Certified gram seed');
IF NOT EXISTS (SELECT 1 FROM item_type WHERE item_type_id = 'seed-mustard')
    INSERT INTO item_type VALUES ('seed-mustard','Mustard Seeds','Bags','Certified mustard seed');
IF NOT EXISTS (SELECT 1 FROM item_type WHERE item_type_id = 'seed-peas')
    INSERT INTO item_type VALUES ('seed-peas','Peas Seeds','Bags','Certified peas seed');
IF NOT EXISTS (SELECT 1 FROM item_type WHERE item_type_id = 'seed-rice')
    INSERT INTO item_type VALUES ('seed-rice','Rice Seeds','Bags','Certified rice seed');
IF NOT EXISTS (SELECT 1 FROM item_type WHERE item_type_id = 'seed-cotton')
    INSERT INTO item_type VALUES ('seed-cotton','Cotton Seeds','Bags','Certified cotton seed');
IF NOT EXISTS (SELECT 1 FROM item_type WHERE item_type_id = 'seed-maize')
    INSERT INTO item_type VALUES ('seed-maize','Maize Seeds','Bags','Certified maize seed');
IF NOT EXISTS (SELECT 1 FROM item_type WHERE item_type_id = 'seed-sugarcane')
    INSERT INTO item_type VALUES ('seed-sugarcane','Sugarcane Setts','Bags','Sugarcane planting setts');
IF NOT EXISTS (SELECT 1 FROM item_type WHERE item_type_id = 'seed-bajra')
    INSERT INTO item_type VALUES ('seed-bajra','Bajra Seeds','Bags','Certified bajra seed');

-- FERTILIZERS
IF NOT EXISTS (SELECT 1 FROM item_type WHERE item_type_id = 'fert-urea')
    INSERT INTO item_type VALUES ('fert-urea','Urea','Bags','Nitrogen fertilizer (46-0-0)');
IF NOT EXISTS (SELECT 1 FROM item_type WHERE item_type_id = 'fert-dap')
    INSERT INTO item_type VALUES ('fert-dap','DAP','Bags','Di-Ammonium Phosphate (18-46-0)');
IF NOT EXISTS (SELECT 1 FROM item_type WHERE item_type_id = 'fert-npk')
    INSERT INTO item_type VALUES ('fert-npk','NPK','Bags','Compound NPK fertilizer');
IF NOT EXISTS (SELECT 1 FROM item_type WHERE item_type_id = 'fert-ssp')
    INSERT INTO item_type VALUES ('fert-ssp','SSP','Bags','Single Super Phosphate');
IF NOT EXISTS (SELECT 1 FROM item_type WHERE item_type_id = 'fert-sop')
    INSERT INTO item_type VALUES ('fert-sop','SOP','Bags','Sulphate of Potash');
IF NOT EXISTS (SELECT 1 FROM item_type WHERE item_type_id = 'fert-zinc')
    INSERT INTO item_type VALUES ('fert-zinc','Zinc Sulphate','Bags','Zinc micro-nutrient');
IF NOT EXISTS (SELECT 1 FROM item_type WHERE item_type_id = 'fert-ammsul')
    INSERT INTO item_type VALUES ('fert-ammsul','Ammonium Sulfate','Bags','Nitrogen + Sulphur fertilizer');

-- PESTICIDES
IF NOT EXISTS (SELECT 1 FROM item_type WHERE item_type_id = 'pest-confidor')
    INSERT INTO item_type VALUES ('pest-confidor','Confidor','Litres','Systemic insecticide for aphids/whitefly');
IF NOT EXISTS (SELECT 1 FROM item_type WHERE item_type_id = 'pest-mospilan')
    INSERT INTO item_type VALUES ('pest-mospilan','Mospilan','Litres','Insecticide for sucking pests');
IF NOT EXISTS (SELECT 1 FROM item_type WHERE item_type_id = 'pest-sectin')
    INSERT INTO item_type VALUES ('pest-sectin','Sectin','Litres','Fungicide for rust and fungal diseases');
IF NOT EXISTS (SELECT 1 FROM item_type WHERE item_type_id = 'pest-talstar')
    INSERT INTO item_type VALUES ('pest-talstar','Talstar','Litres','Broad-spectrum insecticide');
IF NOT EXISTS (SELECT 1 FROM item_type WHERE item_type_id = 'pest-fame')
    INSERT INTO item_type VALUES ('pest-fame','Fame','Litres','Insecticide for borers and caterpillars');
IF NOT EXISTS (SELECT 1 FROM item_type WHERE item_type_id = 'pest-machete')
    INSERT INTO item_type VALUES ('pest-machete','Machete','Litres','Pre-emergent herbicide for weeds');
IF NOT EXISTS (SELECT 1 FROM item_type WHERE item_type_id = 'pest-glyphosate')
    INSERT INTO item_type VALUES ('pest-glyphosate','Glyphosate','Litres','Non-selective systemic herbicide');
IF NOT EXISTS (SELECT 1 FROM item_type WHERE item_type_id = 'pest-curacron')
    INSERT INTO item_type VALUES ('pest-curacron','Curacron','Litres','Organophosphate for bollworms');
IF NOT EXISTS (SELECT 1 FROM item_type WHERE item_type_id = 'pest-match')
    INSERT INTO item_type VALUES ('pest-match','Match','Litres','IGR insecticide for armyworm');

-- ============================================================
-- INVENTORY STOCK (one entry per item type)
-- ============================================================

-- Seeds stock
IF NOT EXISTS (SELECT 1 FROM inventory_stock WHERE stock_id = 'stk-seed-wheat')
    INSERT INTO inventory_stock VALUES ('stk-seed-wheat','seed-wheat',500,GETDATE());
IF NOT EXISTS (SELECT 1 FROM inventory_stock WHERE stock_id = 'stk-seed-barley')
    INSERT INTO inventory_stock VALUES ('stk-seed-barley','seed-barley',400,GETDATE());
IF NOT EXISTS (SELECT 1 FROM inventory_stock WHERE stock_id = 'stk-seed-gram')
    INSERT INTO inventory_stock VALUES ('stk-seed-gram','seed-gram',350,GETDATE());
IF NOT EXISTS (SELECT 1 FROM inventory_stock WHERE stock_id = 'stk-seed-mustard')
    INSERT INTO inventory_stock VALUES ('stk-seed-mustard','seed-mustard',300,GETDATE());
IF NOT EXISTS (SELECT 1 FROM inventory_stock WHERE stock_id = 'stk-seed-peas')
    INSERT INTO inventory_stock VALUES ('stk-seed-peas','seed-peas',300,GETDATE());
IF NOT EXISTS (SELECT 1 FROM inventory_stock WHERE stock_id = 'stk-seed-rice')
    INSERT INTO inventory_stock VALUES ('stk-seed-rice','seed-rice',600,GETDATE());
IF NOT EXISTS (SELECT 1 FROM inventory_stock WHERE stock_id = 'stk-seed-cotton')
    INSERT INTO inventory_stock VALUES ('stk-seed-cotton','seed-cotton',400,GETDATE());
IF NOT EXISTS (SELECT 1 FROM inventory_stock WHERE stock_id = 'stk-seed-maize')
    INSERT INTO inventory_stock VALUES ('stk-seed-maize','seed-maize',350,GETDATE());
IF NOT EXISTS (SELECT 1 FROM inventory_stock WHERE stock_id = 'stk-seed-sugarcane')
    INSERT INTO inventory_stock VALUES ('stk-seed-sugarcane','seed-sugarcane',250,GETDATE());
IF NOT EXISTS (SELECT 1 FROM inventory_stock WHERE stock_id = 'stk-seed-bajra')
    INSERT INTO inventory_stock VALUES ('stk-seed-bajra','seed-bajra',300,GETDATE());

-- Fertilizers stock
IF NOT EXISTS (SELECT 1 FROM inventory_stock WHERE stock_id = 'stk-fert-urea')
    INSERT INTO inventory_stock VALUES ('stk-fert-urea','fert-urea',800,GETDATE());
IF NOT EXISTS (SELECT 1 FROM inventory_stock WHERE stock_id = 'stk-fert-dap')
    INSERT INTO inventory_stock VALUES ('stk-fert-dap','fert-dap',600,GETDATE());
IF NOT EXISTS (SELECT 1 FROM inventory_stock WHERE stock_id = 'stk-fert-npk')
    INSERT INTO inventory_stock VALUES ('stk-fert-npk','fert-npk',400,GETDATE());
IF NOT EXISTS (SELECT 1 FROM inventory_stock WHERE stock_id = 'stk-fert-ssp')
    INSERT INTO inventory_stock VALUES ('stk-fert-ssp','fert-ssp',300,GETDATE());
IF NOT EXISTS (SELECT 1 FROM inventory_stock WHERE stock_id = 'stk-fert-sop')
    INSERT INTO inventory_stock VALUES ('stk-fert-sop','fert-sop',250,GETDATE());
IF NOT EXISTS (SELECT 1 FROM inventory_stock WHERE stock_id = 'stk-fert-zinc')
    INSERT INTO inventory_stock VALUES ('stk-fert-zinc','fert-zinc',200,GETDATE());
IF NOT EXISTS (SELECT 1 FROM inventory_stock WHERE stock_id = 'stk-fert-ammsul')
    INSERT INTO inventory_stock VALUES ('stk-fert-ammsul','fert-ammsul',300,GETDATE());

-- Pesticides stock
IF NOT EXISTS (SELECT 1 FROM inventory_stock WHERE stock_id = 'stk-pest-confidor')
    INSERT INTO inventory_stock VALUES ('stk-pest-confidor','pest-confidor',200,GETDATE());
IF NOT EXISTS (SELECT 1 FROM inventory_stock WHERE stock_id = 'stk-pest-mospilan')
    INSERT INTO inventory_stock VALUES ('stk-pest-mospilan','pest-mospilan',180,GETDATE());
IF NOT EXISTS (SELECT 1 FROM inventory_stock WHERE stock_id = 'stk-pest-sectin')
    INSERT INTO inventory_stock VALUES ('stk-pest-sectin','pest-sectin',150,GETDATE());
IF NOT EXISTS (SELECT 1 FROM inventory_stock WHERE stock_id = 'stk-pest-talstar')
    INSERT INTO inventory_stock VALUES ('stk-pest-talstar','pest-talstar',170,GETDATE());
IF NOT EXISTS (SELECT 1 FROM inventory_stock WHERE stock_id = 'stk-pest-fame')
    INSERT INTO inventory_stock VALUES ('stk-pest-fame','pest-fame',160,GETDATE());
IF NOT EXISTS (SELECT 1 FROM inventory_stock WHERE stock_id = 'stk-pest-machete')
    INSERT INTO inventory_stock VALUES ('stk-pest-machete','pest-machete',140,GETDATE());
IF NOT EXISTS (SELECT 1 FROM inventory_stock WHERE stock_id = 'stk-pest-glyphosate')
    INSERT INTO inventory_stock VALUES ('stk-pest-glyphosate','pest-glyphosate',130,GETDATE());
IF NOT EXISTS (SELECT 1 FROM inventory_stock WHERE stock_id = 'stk-pest-curacron')
    INSERT INTO inventory_stock VALUES ('stk-pest-curacron','pest-curacron',120,GETDATE());
IF NOT EXISTS (SELECT 1 FROM inventory_stock WHERE stock_id = 'stk-pest-match')
    INSERT INTO inventory_stock VALUES ('stk-pest-match','pest-match',110,GETDATE());

-- ============================================================
-- SEASONAL RULES (Rabi: Oct-Mar, Kharif: Apr-Sep)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM seasonal_rule WHERE rule_id = 'rule-wheat')
    INSERT INTO seasonal_rule VALUES ('rule-wheat','crop-wheat',10,3,1);
IF NOT EXISTS (SELECT 1 FROM seasonal_rule WHERE rule_id = 'rule-barley')
    INSERT INTO seasonal_rule VALUES ('rule-barley','crop-barley',10,3,1);
IF NOT EXISTS (SELECT 1 FROM seasonal_rule WHERE rule_id = 'rule-gram')
    INSERT INTO seasonal_rule VALUES ('rule-gram','crop-gram',10,3,1);
IF NOT EXISTS (SELECT 1 FROM seasonal_rule WHERE rule_id = 'rule-mustard')
    INSERT INTO seasonal_rule VALUES ('rule-mustard','crop-mustard',10,3,1);
IF NOT EXISTS (SELECT 1 FROM seasonal_rule WHERE rule_id = 'rule-peas')
    INSERT INTO seasonal_rule VALUES ('rule-peas','crop-peas',10,3,1);
IF NOT EXISTS (SELECT 1 FROM seasonal_rule WHERE rule_id = 'rule-rice')
    INSERT INTO seasonal_rule VALUES ('rule-rice','crop-rice',4,9,1);
IF NOT EXISTS (SELECT 1 FROM seasonal_rule WHERE rule_id = 'rule-cotton')
    INSERT INTO seasonal_rule VALUES ('rule-cotton','crop-cotton',4,9,1);
IF NOT EXISTS (SELECT 1 FROM seasonal_rule WHERE rule_id = 'rule-maize')
    INSERT INTO seasonal_rule VALUES ('rule-maize','crop-maize',4,9,1);
IF NOT EXISTS (SELECT 1 FROM seasonal_rule WHERE rule_id = 'rule-sugarcane')
    INSERT INTO seasonal_rule VALUES ('rule-sugarcane','crop-sugarcane',4,9,1);
IF NOT EXISTS (SELECT 1 FROM seasonal_rule WHERE rule_id = 'rule-bajra')
    INSERT INTO seasonal_rule VALUES ('rule-bajra','crop-bajra',4,9,1);

-- ============================================================
-- ALLOCATION LIMITS (per item type per season)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM allocation_limit WHERE limit_id = 'lim-seed-wheat')
    INSERT INTO allocation_limit VALUES ('lim-seed-wheat','seed-wheat','RABI',50,1);
IF NOT EXISTS (SELECT 1 FROM allocation_limit WHERE limit_id = 'lim-seed-rice')
    INSERT INTO allocation_limit VALUES ('lim-seed-rice','seed-rice','KHARIF',40,1);
IF NOT EXISTS (SELECT 1 FROM allocation_limit WHERE limit_id = 'lim-fert-urea')
    INSERT INTO allocation_limit VALUES ('lim-fert-urea','fert-urea','RABI',30,1);
IF NOT EXISTS (SELECT 1 FROM allocation_limit WHERE limit_id = 'lim-fert-dap')
    INSERT INTO allocation_limit VALUES ('lim-fert-dap','fert-dap','KHARIF',20,1);
IF NOT EXISTS (SELECT 1 FROM allocation_limit WHERE limit_id = 'lim-pest-confidor')
    INSERT INTO allocation_limit VALUES ('lim-pest-confidor','pest-confidor','KHARIF',10,1);

-- ============================================================
-- STOCK THRESHOLDS
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM stock_threshold WHERE threshold_id = 'thr-fert-urea')
    INSERT INTO stock_threshold VALUES ('thr-fert-urea','fert-urea',100);
IF NOT EXISTS (SELECT 1 FROM stock_threshold WHERE threshold_id = 'thr-fert-dap')
    INSERT INTO stock_threshold VALUES ('thr-fert-dap','fert-dap',80);
IF NOT EXISTS (SELECT 1 FROM stock_threshold WHERE threshold_id = 'thr-pest-confidor')
    INSERT INTO stock_threshold VALUES ('thr-pest-confidor','pest-confidor',30);
IF NOT EXISTS (SELECT 1 FROM stock_threshold WHERE threshold_id = 'thr-seed-wheat')
    INSERT INTO stock_threshold VALUES ('thr-seed-wheat','seed-wheat',50);
IF NOT EXISTS (SELECT 1 FROM stock_threshold WHERE threshold_id = 'thr-seed-rice')
    INSERT INTO stock_threshold VALUES ('thr-seed-rice','seed-rice',50);

GO
PRINT 'SeedWise database created and seeded successfully!';
GO
