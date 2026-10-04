package ui.farmer;

import business.controller.SubsidyRequestController;
import business.model.*;
import data.FarmerDAO;
import ui.LoginView;
import ui.SessionManager;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.List;
import ui.components.CustomAlert;

/**
 * Farmer Dashboard - sidebar navigation with UC-01 (profile), UC-02 (submit
 * request), view requests.
 */
public class FarmerDashboard {

    private final Scene scene;
    private final BorderPane mainLayout;
    private final StackPane rootPane;
    private final SessionManager session = SessionManager.getInstance();
    private Button[] activeBtn;
    private Button myRequestsBtn;

    public FarmerDashboard(Stage stage) {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: transparent;");

        // --- Top Navigation (Consistent with Officer/Distributor) ---
        HBox topNav = new HBox(15);
        topNav.setPadding(new Insets(12, 30, 12, 30));
        topNav.setStyle("-fx-background-color: rgba(45, 80, 22, 0.95); -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);");
        topNav.setAlignment(Pos.CENTER_LEFT);

        Label logo = new Label("SeedWise");
        logo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #D4E6A5;");
        Label roleLabel = new Label("FARMER");
        roleLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #8BC34A; -fx-padding: 6 0 0 10;");
        HBox logoArea = new HBox(logo, roleLabel);
        logoArea.setAlignment(Pos.CENTER_LEFT);

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        Button overviewBtn = topBtn("Overview");
        Button submitBtn = topBtn("Submit Request");
        myRequestsBtn = topBtn("My Requests");
        activeBtn = new Button[]{overviewBtn};

        HBox navLinks = new HBox(8, overviewBtn, submitBtn, myRequestsBtn);
        navLinks.setAlignment(Pos.CENTER);

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        ImageView avatar = new javafx.scene.image.ImageView();
        try { avatar.setImage(new javafx.scene.image.Image(new java.io.File("resources/images/avatar.png").toURI().toString(), 36, 36, true, true)); } catch(Exception ignored) {}
        avatar.setFitWidth(36); avatar.setFitHeight(36);
        javafx.scene.shape.Circle clipCircle = new javafx.scene.shape.Circle(18, 18, 18);
        avatar.setClip(clipCircle);

        Label userInfo = new Label(session.getCurrentUsername());
        userInfo.setStyle("-fx-text-fill: #D4E6A5; -fx-font-size: 13px; -fx-font-weight: bold;");
        Button logoutBtn = new Button("Sign Out");
        logoutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: rgba(255,255,255,0.5); -fx-border-radius: 4; -fx-cursor: hand;");
        
        HBox profileArea = new HBox(10, avatar, userInfo, logoutBtn);
        profileArea.setAlignment(Pos.CENTER);

        topNav.getChildren().addAll(logoArea, spacer1, navLinks, spacer2, profileArea);
        mainLayout.setTop(topNav);

        overviewBtn.setOnAction(e -> { setActiveNav(overviewBtn); showOverview(); });
        submitBtn.setOnAction(e -> { setActiveNav(submitBtn); showSubmitRequest(); });
        myRequestsBtn.setOnAction(e -> { setActiveNav(myRequestsBtn); showMyRequests(); });
        logoutBtn.setOnAction(e -> { session.logout(); stage.setScene(new LoginView(stage).getScene()); });

        setActiveNav(overviewBtn);
        showOverview();

        rootPane = new StackPane(mainLayout);
        ui.components.ImageBackground.applyTo(rootPane);
        this.scene = new Scene(rootPane, 1200, 800);
    }

    private Button topBtn(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: transparent; -fx-text-fill: #D4E6A5; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-cursor: hand; -fx-background-radius: 6;");
        b.setOnMouseEntered(e -> {
            if (activeBtn[0] != b) b.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-cursor: hand; -fx-background-radius: 6;");
            b.setScaleX(1.05); b.setScaleY(1.05);
        });
        b.setOnMouseExited(e -> {
            if (activeBtn[0] != b) b.setStyle("-fx-background-color: transparent; -fx-text-fill: #D4E6A5; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-cursor: hand; -fx-background-radius: 6;");
            b.setScaleX(1); b.setScaleY(1);
        });
        return b;
    }

    private void setActiveNav(Button b) {
        if (activeBtn[0] != null) activeBtn[0].setStyle("-fx-background-color: transparent; -fx-text-fill: #D4E6A5; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-cursor: hand; -fx-background-radius: 6;");
        activeBtn[0] = b;
        b.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-cursor: hand; -fx-background-radius: 6;");
    }

    // --- Overview Panel ---
    private void showOverview() {
        VBox content = new VBox(25);
        content.setPadding(new Insets(40));

        Label title = new Label("Farmer Dashboard");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 0, 2);");

        try {
            FarmerDAO farmerDAO = new FarmerDAO();
            Farmer farmer = farmerDAO.findByFarmerId(session.getFarmerId());

            HBox topRow = new HBox(20);
            
            // --- Profile Card ---
            VBox profileCard = new VBox(15);
            profileCard.setPadding(new Insets(25));
            profileCard.setPrefWidth(400);
            profileCard.setStyle("-fx-background-color: rgba(0, 30, 0, 0.7); -fx-background-radius: 15; -fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 1; -fx-border-radius: 15;");
            
            HBox header = new HBox(15);
            header.setAlignment(Pos.CENTER_LEFT);
            Label avatar = new Label(farmer != null ? farmer.getFullName().substring(0,1).toUpperCase() : "F");
            avatar.setMinSize(50, 50); avatar.setAlignment(Pos.CENTER);
            avatar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-background-radius: 25;");
            
            VBox nameBox = new VBox(2);
            Label nameL = new Label(farmer != null ? farmer.getFullName() : "Farmer Name");
            nameL.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
            Label cnicL = new Label("CNIC: " + (farmer != null ? farmer.getCnic() : "N/A"));
            cnicL.setStyle("-fx-font-size: 12px; -fx-text-fill: #A5D6A7;");
            nameBox.getChildren().addAll(nameL, cnicL);
            header.getChildren().addAll(avatar, nameBox);

            GridPane info = new GridPane(); info.setHgap(20); info.setVgap(10);
            addModernRow(info, 0, "Contact", farmer != null ? farmer.getContact() : "-");
            addModernRow(info, 1, "Location", farmer != null ? farmer.getAddress() : "-");
            addModernRow(info, 2, "Status", farmer != null ? farmer.getStatus() : "-");
            
            profileCard.getChildren().addAll(header, new Separator(), info);

            // --- Allocation Tracker (Functional) ---
            VBox allocCard = new VBox(15);
            allocCard.setPadding(new Insets(25));
            allocCard.setPrefWidth(350);
            allocCard.setStyle("-fx-background-color: rgba(0, 30, 0, 0.7); -fx-background-radius: 15; -fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 1; -fx-border-radius: 15;");
            
            Label allocTitle = new Label("Seasonal Allocation");
            allocTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
            
            double remaining = farmer != null ? farmer.getRemainingAllocation() : 0;
            double totalQuota = 1000.0; 
            double used = totalQuota - remaining;
            double progress = used / totalQuota;
            
            ProgressBar pb = new ProgressBar(progress);
            pb.setPrefWidth(Double.MAX_VALUE);
            pb.setPrefHeight(15);
            // Change color to orange if more than 80% used
            String pbColor = progress > 0.8 ? "#FF5722" : "#4CAF50";
            pb.setStyle("-fx-accent: " + pbColor + ";");
            
            Label remLbl = new Label(String.format("%,.0f / %,.0f units used", used, totalQuota));
            remLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: bold;");
            
            Label subtitleLbl = new Label(String.format("%,.0f units remaining for this season", remaining));
            subtitleLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #A5D6A7;");

            VBox tips = new VBox(5);
            Label tipTitle = new Label("[Tip] Planting Tip");
            tipTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #A5D6A7;");
            
            // Dynamic Tip based on current month
            int month = java.time.LocalDate.now().getMonthValue();
            String tipText;
            if (month >= 10 || month <= 3) {
                tipText = "Rabi season is active. Ensure timely sowing of Wheat and Mustard for maximum yield.";
            } else {
                tipText = "Kharif season is approaching. Prepare your land for Rice and Cotton sowing.";
            }
            
            Label tipContent = new Label(tipText);
            tipContent.setWrapText(true);
            tipContent.setStyle("-fx-font-size: 11px; -fx-text-fill: white;");
            tips.getChildren().addAll(tipTitle, tipContent);

            allocCard.getChildren().addAll(allocTitle, pb, remLbl, subtitleLbl, new Region(), tips);
            VBox.setVgrow(new Region(), Priority.ALWAYS);

            // --- Soil Health & Advisory Widget ---
            VBox advisoryCard = new VBox(10);
            advisoryCard.setPadding(new Insets(20));
            advisoryCard.setPrefWidth(260);
            advisoryCard.setAlignment(Pos.TOP_CENTER);
            advisoryCard.setStyle("-fx-background-color: rgba(10, 30, 0, 0.85); -fx-background-radius: 15; -fx-border-color: rgba(255,255,255,0.2); -fx-border-width: 1; -fx-border-radius: 15;");
            
            Label advTitle = new Label("Soil Advisory");
            advTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
            
            Label instruction = new Label("Select your soil type:");
            instruction.setStyle("-fx-text-fill: #A5D6A7; -fx-font-size: 12px;");

            ComboBox<String> soilSelect = new ComboBox<>();
            soilSelect.getItems().addAll("Sandy", "Clay", "Loam");
            soilSelect.setValue("Loam");
            soilSelect.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-font-size: 13px; -fx-pref-width: 150px;");

            Label recommendation = new Label();
            recommendation.setWrapText(true);
            recommendation.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 10 0 0 0; -fx-text-alignment: center;");
            
            soilSelect.setOnAction(e -> {
                String type = soilSelect.getValue();
                if (type.equals("Sandy")) {
                    recommendation.setText("Low moisture retention. Needs High Nitrogen (Urea) and frequent watering. Ideal for groundnuts.");
                } else if (type.equals("Clay")) {
                    recommendation.setText("High moisture. Needs aeration. Recommended: Phosphorus (DAP). Best for Rice & Wheat.");
                } else {
                    recommendation.setText("Balanced soil. Use standard NPK fertilizers. Great for Cotton, Sugarcane, and Wheat.");
                }
            });
            soilSelect.getOnAction().handle(null); // Init text

            advisoryCard.getChildren().addAll(advTitle, instruction, soilSelect, new Separator(), recommendation);

            topRow.getChildren().addAll(profileCard, allocCard, advisoryCard);
            HBox.setHgrow(profileCard, Priority.ALWAYS);

            // Stats Row
            SubsidyRequestController ctrl = new SubsidyRequestController();
            int pending = ctrl.countByStatusAndFarmerId("PENDING", session.getFarmerId());
            int approved = ctrl.countByStatusAndFarmerId("APPROVED", session.getFarmerId());
            int rejected = ctrl.countByStatusAndFarmerId("REJECTED", session.getFarmerId());

            HBox stats = new HBox(20);
            stats.getChildren().addAll(
                    createStatCard("Pending", String.valueOf(pending), "#FF9800"),
                    createStatCard("Approved", String.valueOf(approved), "#4CAF50"),
                    createStatCard("Rejected", String.valueOf(rejected), "#F44336"));

            // --- Seed & Fertilizer Calculator Widget ---
            VBox calcCard = new VBox(15);
            calcCard.setPadding(new Insets(20));
            calcCard.setPrefWidth(350);
            calcCard.setStyle("-fx-background-color: rgba(10, 30, 0, 0.85); -fx-background-radius: 15; -fx-border-color: rgba(255,255,255,0.2); -fx-border-width: 1; -fx-border-radius: 15;");

            Label calcTitle = new Label("Input Requirement Calculator");
            calcTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

            HBox inputRow = new HBox(10);
            inputRow.setAlignment(Pos.CENTER_LEFT);
            
            TextField acresInput = new TextField();
            acresInput.setPromptText("Acres");
            acresInput.setPrefWidth(60);
            acresInput.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
            
            ComboBox<String> cropSelect = new ComboBox<>();
            java.util.List<String> cropIds = business.model.CropItemMapping.getAllCropIds();
            java.util.Map<String, String> nameToId = new java.util.HashMap<>();
            for (String cid : cropIds) {
                String name = business.model.CropItemMapping.getCropName(cid);
                cropSelect.getItems().add(name);
                nameToId.put(name, cid);
            }
            if (!cropIds.isEmpty()) cropSelect.setValue(business.model.CropItemMapping.getCropName(cropIds.get(0)));
            cropSelect.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-font-size: 12px;");
            
            Label landLbl = new Label("Land Size (Acres):");
            landLbl.setStyle("-fx-text-fill: #A5D6A7;");
            inputRow.getChildren().addAll(landLbl, acresInput, cropSelect);

            VBox resultsBox = new VBox(5);
            Label seedReq = new Label("Seed: -");
            Label fertReq = new Label("Fertilizers: -");
            Label pestReq = new Label("Pesticides: -");
            seedReq.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
            fertReq.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
            pestReq.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
            resultsBox.getChildren().addAll(seedReq, fertReq, pestReq);

            Runnable calculate = () -> {
                try {
                    double acres = Double.parseDouble(acresInput.getText().trim());
                    if (acres <= 0) {
                        seedReq.setText("Error: Enter valid acres (>0)");
                        fertReq.setText("Fertilizers: -");
                        pestReq.setText("Pesticides: -");
                        return;
                    }

                    String selectedName = cropSelect.getValue();
                    String cropId = nameToId.get(selectedName);

                    double seedPerAcre = 1; double ureaPerAcre = 2; double pestPerAcre = 1.5;
                    if(selectedName.contains("Wheat")) { seedPerAcre = 1; ureaPerAcre = 1.5; pestPerAcre = 0.5; }
                    else if(selectedName.contains("Rice")) { seedPerAcre = 0.5; ureaPerAcre = 2; pestPerAcre = 1; }
                    else if(selectedName.contains("Cotton")) { seedPerAcre = 0.5; ureaPerAcre = 2.5; pestPerAcre = 1.5; }
                    else if(selectedName.contains("Sugarcane")) { seedPerAcre = 10; ureaPerAcre = 3; pestPerAcre = 2; }
                    else if(selectedName.contains("Maize")) { seedPerAcre = 0.5; ureaPerAcre = 2; pestPerAcre = 1; }

                    java.util.List<business.model.CropItemMapping.ItemEntry> seeds = business.model.CropItemMapping.getItems(cropId, "Seeds");
                    java.util.List<business.model.CropItemMapping.ItemEntry> ferts = business.model.CropItemMapping.getItems(cropId, "Fertilizers");
                    java.util.List<business.model.CropItemMapping.ItemEntry> pests = business.model.CropItemMapping.getItems(cropId, "Pesticides");

                    String seedName = seeds.isEmpty() ? "Seed" : seeds.get(0).getDisplayName();
                    seedReq.setText(String.format("Seed: %s (%,.1f Bags)", seedName, acres * seedPerAcre));

                    String fertName = ferts.isEmpty() ? "None" : ferts.get(0).getDisplayName();
                    fertReq.setText(String.format("Fertilizer: %s (%,.1f Bags)", fertName, acres * ureaPerAcre));

                    String pestName = pests.isEmpty() ? "None" : pests.get(0).getDisplayName();
                    pestReq.setText(String.format("Pesticide: %s (%,.1f Litres)", pestName, acres * pestPerAcre));
                } catch(Exception ex) {
                    seedReq.setText("Seed: -");
                    fertReq.setText("Fertilizers: -");
                    pestReq.setText("Pesticides: -");
                }
            };

            acresInput.textProperty().addListener((obs, oldV, newV) -> calculate.run());
            cropSelect.setOnAction(e -> calculate.run());

            calcCard.getChildren().addAll(calcTitle, inputRow, new Separator(), resultsBox);

            HBox bottomRow = new HBox(20);
            Region spacer2 = new Region();
            HBox.setHgrow(spacer2, Priority.ALWAYS);
            bottomRow.getChildren().addAll(stats, spacer2, calcCard);

            content.getChildren().addAll(title, topRow, bottomRow);

        } catch (Exception ex) {
            content.getChildren().add(new Label("Error: " + ex.getMessage()));
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        mainLayout.setCenter(scroll);
    }



    private void addModernRow(GridPane g, int r, String k, String v) {
        Label kl = new Label(k + ":"); kl.setStyle("-fx-text-fill: #D4E6A5; -fx-font-size: 13px;");
        Label vl = new Label(v); vl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        g.add(kl, 0, r); g.add(vl, 1, r);
    }

    // --- UC-02: Submit Request Panel ---
    // --- UC-02: Submit Request Panel ---
    // --- UC-02: Submit Request Panel (Multi-step wizard) ---
    private int currentStep = 1;
    private String selectedSeason = "";
    private String selectedCropId = "";
    private String selectedCategory = "";
    private CropItemMapping.ItemEntry selectedItem = null;
    private int selectedQty = 0;

    private void showSubmitRequest() {
        currentStep = 1;
        selectedSeason = ""; selectedCropId = ""; selectedCategory = "";
        selectedItem = null; selectedQty = 0;

        VBox content = new VBox(25);
        content.setPadding(new Insets(40));
        content.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Submit Subsidy Request");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 0, 2);");

        // --- User pill ---
        HBox userPill = new HBox(8);
        userPill.setAlignment(Pos.CENTER_LEFT);
        userPill.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-background-radius: 30; " +
                "-fx-border-color: rgba(255,255,255,0.3); -fx-border-radius: 30; -fx-padding: 5 12 5 5;");
        try {
            Farmer farmer = new FarmerDAO().findByFarmerId(session.getFarmerId());
            if (farmer != null) {
                String initials = "";
                String[] parts = farmer.getFullName().split(" ");
                for (String p : parts) if (!p.isEmpty()) initials += p.charAt(0);
                Label avatar = new Label(initials.toUpperCase());
                avatar.setMinSize(26, 26); avatar.setMaxSize(26, 26);
                avatar.setAlignment(Pos.CENTER);
                avatar.setStyle("-fx-background-color: #EAF3DE; -fx-background-radius: 50; " +
                        "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #27500A;");
                VBox nameBox = new VBox(1);
                Label nameL = new Label(farmer.getFullName());
                nameL.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #fff;");
                Label metaL = new Label("CNIC: " + farmer.getCnic());
                metaL.setStyle("-fx-font-size: 11px; -fx-text-fill: #eee;");
                nameBox.getChildren().addAll(nameL, metaL);
                userPill.getChildren().addAll(avatar, nameBox);
            }
        } catch (Exception ignored) {}

        // --- Progress bar ---
        String[] stepLabels = {"Season", "Category", "Item & Qty", "Review"};
        HBox progBar = new HBox();
        progBar.setAlignment(Pos.CENTER);
        progBar.setPadding(new Insets(0, 0, 10, 0));

        // --- Step container ---
        StackPane stepContainer = new StackPane();

        // Build steps
        Runnable[] refreshUI = new Runnable[1];
        refreshUI[0] = () -> {
            // Update progress bar
            progBar.getChildren().clear();
            for (int i = 0; i < 4; i++) {
                VBox stepVBox = new VBox(5);
                stepVBox.setAlignment(Pos.CENTER);
                Label dot = new Label(String.valueOf(i + 1));
                dot.setMinSize(28, 28); dot.setMaxSize(28, 28);
                dot.setAlignment(Pos.CENTER);
                dot.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 50; " +
                    (i + 1 <= currentStep ? "-fx-background-color: #3B6D11; -fx-text-fill: white; -fx-border-color: #3B6D11;" :
                     "-fx-background-color: white; -fx-text-fill: #999; -fx-border-color: #ccc;") +
                    " -fx-border-radius: 50; -fx-border-width: 1.5;");
                Label sLabel = new Label(stepLabels[i]);
                sLabel.setStyle("-fx-font-size: 11px; " +
                    (i + 1 == currentStep ? "-fx-text-fill: white; -fx-font-weight: bold;" : "-fx-text-fill: #ddd;"));
                stepVBox.getChildren().addAll(dot, sLabel);
                HBox.setHgrow(stepVBox, Priority.ALWAYS);
                progBar.getChildren().add(stepVBox);
                if (i < 3) {
                    Region line = new Region();
                    line.setMinHeight(1.5); line.setMaxHeight(1.5);
                    line.setPrefWidth(60);
                    line.setStyle("-fx-background-color: " + (i + 1 < currentStep ? "#3B6D11" : "#ddd") + ";");
                    HBox lineBox = new HBox(line);
                    lineBox.setAlignment(Pos.CENTER);
                    lineBox.setPadding(new Insets(12, 0, 0, 0));
                    progBar.getChildren().add(lineBox);
                }
            }

            // Build current step content
            stepContainer.getChildren().clear();
            VBox stepContent = new VBox(12);

            if (currentStep == 1) {
                // STEP 1: Season
                VBox seasonCard = buildSectionCard("SELECT SEASON");
                HBox seasonOpts = new HBox(10);
                String[][] seasons = {{"Rabi", "Oct - Mar"}, {"Kharif", "Apr - Sep"}};
                for (String[] ss : seasons) {
                    VBox optCard = new VBox(5);
                    optCard.setAlignment(Pos.CENTER);
                    optCard.setPadding(new Insets(12, 20, 12, 20));
                    optCard.setStyle("-fx-border-color: " + (selectedSeason.equalsIgnoreCase(ss[0]) ? "#3B6D11" : "#ddd") + "; " +
                        "-fx-border-radius: 8; -fx-border-width: 1.5; -fx-cursor: hand; " +
                        "-fx-background-color: " + (selectedSeason.equalsIgnoreCase(ss[0]) ? "#EAF3DE" : "white") + "; " +
                        "-fx-background-radius: 8;");
                    Label seasonName = new Label(ss[0]);
                    seasonName.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #333;");
                    Label seasonRange = new Label(ss[1]);
                    seasonRange.setStyle("-fx-font-size: 11px; -fx-text-fill: #777;");
                    optCard.getChildren().addAll(seasonName, seasonRange);
                    HBox.setHgrow(optCard, Priority.ALWAYS);
                    final String sVal = ss[0];
                    optCard.setOnMouseClicked(ev -> {
                        selectedSeason = sVal; selectedCategory = "";
                        refreshUI[0].run();
                    });
                    seasonOpts.getChildren().add(optCard);
                }
                seasonCard.getChildren().add(seasonOpts);
                stepContent.getChildren().add(seasonCard);

                HBox btnRow = buildBtnRow(null, "Next ->", () -> {
                    if (selectedSeason.isEmpty()) {
                        CustomAlert.show(rootPane, "Validation", "Please select a season.", true);
                    } else { currentStep = 2; refreshUI[0].run(); }
                }, !selectedSeason.isEmpty());
                stepContent.getChildren().add(btnRow);

            } else if (currentStep == 2) {
                // STEP 2: Category
                VBox typeCard = buildSectionCard("WHAT DO YOU NEED?");
                HBox typeOpts = new HBox(10);
                String[][] types = {{"Seeds", "Certified varieties"}, {"Fertilizers", "Nutrients & soil"}, {"Pesticides", "Crop protection"}};
                for (String[] tt : types) {
                    VBox optCard = new VBox(5);
                    optCard.setAlignment(Pos.CENTER);
                    optCard.setPadding(new Insets(12, 15, 12, 15));
                    optCard.setStyle("-fx-border-color: " + (selectedCategory.equals(tt[0]) ? "#3B6D11" : "#ddd") + "; " +
                        "-fx-border-radius: 8; -fx-border-width: 1.5; -fx-cursor: hand; " +
                        "-fx-background-color: " + (selectedCategory.equals(tt[0]) ? "#EAF3DE" : "white") + "; " +
                        "-fx-background-radius: 8;");
                    Label typeName = new Label(tt[0]);
                    typeName.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #333;");
                    Label typeSub = new Label(tt[1]);
                    typeSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #777;");
                    optCard.getChildren().addAll(typeName, typeSub);
                    HBox.setHgrow(optCard, Priority.ALWAYS);
                    final String tVal = tt[0];
                    optCard.setOnMouseClicked(ev -> { selectedCategory = tVal; selectedItem = null; refreshUI[0].run(); });
                    typeOpts.getChildren().add(optCard);
                }
                typeCard.getChildren().add(typeOpts);
                stepContent.getChildren().add(typeCard);

                HBox btnRow = buildBtnRow("<- Back", "Next ->",
                    () -> { currentStep = 3; refreshUI[0].run(); },
                    !selectedCategory.isEmpty());
                btnRow.getChildren().get(0).setOnMouseClicked(ev -> { currentStep = 1; refreshUI[0].run(); });
                stepContent.getChildren().add(btnRow);

            } else if (currentStep == 3) {
                // STEP 3: Item & Qty
                String unitText = CropItemMapping.getUnitForCategory(selectedCategory);
                VBox itemCard = buildSectionCard("SELECT " + selectedCategory.toUpperCase().replaceAll("S$", ""));
                VBox itemList = new VBox(6);
                
                // Fetch items filtered by season and category
                List<CropItemMapping.ItemEntry> items = CropItemMapping.getItemsBySeasonAndCategory(selectedSeason, selectedCategory);
                
                for (CropItemMapping.ItemEntry entry : items) {
                    HBox chip = new HBox();
                    chip.setAlignment(Pos.CENTER_LEFT);
                    chip.setPadding(new Insets(9, 12, 9, 12));
                    boolean isSel = selectedItem != null && selectedItem.getItemTypeId().equals(entry.getItemTypeId());
                    chip.setStyle("-fx-border-color: " + (isSel ? "#3B6D11" : "#eee") + "; " +
                        "-fx-border-radius: 8; -fx-border-width: 1; -fx-cursor: hand; " +
                        "-fx-background-color: " + (isSel ? "#EAF3DE" : "white") + "; -fx-background-radius: 8;");
                    Label itemName = new Label(entry.getDisplayName());
                    itemName.setStyle("-fx-font-size: 13px; " + (isSel ? "-fx-font-weight: bold; -fx-text-fill: #27500A;" : "-fx-text-fill: #333;"));
                    Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
                    
                    // Show which crop this belongs to (optional but helpful)
                    String matchingCrop = CropItemMapping.findCropForItemAndSeason(entry.getItemTypeId(), selectedSeason);
                    Label sub = new Label(CropItemMapping.getCropName(matchingCrop));
                    sub.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (isSel ? "#3B6D11" : "#999") + ";");
                    
                    chip.getChildren().addAll(itemName, spacer, sub);
                    chip.setOnMouseClicked(ev -> { 
                        selectedItem = entry; 
                        selectedCropId = matchingCrop;
                        selectedQty = 0; 
                        refreshUI[0].run(); 
                    });
                    itemList.getChildren().add(chip);
                }
                
                if (items.isEmpty()) {
                    itemList.getChildren().add(new Label("No items available for this selection."));
                }
                
                itemCard.getChildren().add(itemList);
                stepContent.getChildren().add(itemCard);

                // Qty card (shown after item selected)
                if (selectedItem != null) {
                    VBox qtyCard = buildSectionCard("QUANTITY REQUIRED");
                    Label qtyLbl = new Label("Number of " + unitText.toLowerCase());
                    qtyLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
                    HBox qtyRow = new HBox(8);
                    qtyRow.setAlignment(Pos.CENTER_LEFT);
                    TextField qtyInput = new TextField();
                    qtyInput.setPromptText("e.g. 10");
                    HBox.setHgrow(qtyInput, Priority.ALWAYS);
                    Label unitBadge = new Label(unitText);
                    unitBadge.setStyle("-fx-padding: 8 14; -fx-background-color: #f5f5f5; -fx-background-radius: 6; " +
                        "-fx-border-color: #ddd; -fx-border-radius: 6; -fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #777;");
                    if (selectedQty > 0) qtyInput.setText(String.valueOf(selectedQty));
                    qtyInput.textProperty().addListener((obs, o, n) -> {
                        try { selectedQty = Integer.parseInt(n.trim()); } catch (Exception ex) { selectedQty = 0; }
                    });
                    qtyRow.getChildren().addAll(qtyInput, unitBadge);
                    qtyCard.getChildren().addAll(qtyLbl, qtyRow);
                    stepContent.getChildren().add(qtyCard);
                }

                HBox btnRow = buildBtnRow("<- Back", "Review ->",
                    () -> { 
                        if (selectedItem == null) {
                            CustomAlert.show(rootPane, "Validation", "Please select an item.", true);
                        } else if (selectedQty <= 0) {
                            CustomAlert.show(rootPane, "Validation", "Please enter a positive number for quantity.", true);
                        } else { 
                            currentStep = 4; refreshUI[0].run(); 
                        } 
                    },
                    selectedItem != null);
                btnRow.getChildren().get(0).setOnMouseClicked(ev -> { currentStep = 2; refreshUI[0].run(); });
                stepContent.getChildren().add(btnRow);

            } else if (currentStep == 4) {
                // STEP 4: Review
                String unitText = CropItemMapping.getUnitForCategory(selectedCategory);
                VBox reviewCard = buildSectionCard("REQUEST SUMMARY");
                GridPane table = new GridPane();
                table.setHgap(20); table.setVgap(12);
                String[][] rows = {
                    {"Season", selectedSeason},
                    {"Input type", selectedCategory},
                    {"Item", selectedItem != null ? selectedItem.getDisplayName() : ""},
                    {"Associated Crop", CropItemMapping.getCropName(selectedCropId)},
                    {"Quantity requested", selectedQty + " " + unitText}
                };
                for (int i = 0; i < rows.length; i++) {
                    Label k = new Label(rows[i][0]);
                    k.setStyle("-fx-text-fill: #777; -fx-font-size: 14px;");
                    k.setMinWidth(150);
                    Label v = new Label(rows[i][1]);
                    v.setStyle("-fx-font-weight: bold; -fx-text-fill: #333; -fx-font-size: 14px;");
                    table.add(k, 0, i); table.add(v, 1, i);
                }
                reviewCard.getChildren().add(table);
                stepContent.getChildren().add(reviewCard);

                HBox btnRow = buildBtnRow("<- Edit", "Submit request", () -> {
                    try {
                        SubsidyRequestController ctrl = new SubsidyRequestController();
                        SubsidyRequest req = ctrl.submitRequest(session.getFarmerId(),
                                selectedCropId, selectedQty, selectedSeason,
                                selectedItem.getItemTypeId());
                        CustomAlert.show(rootPane, "Request Submitted",
                            "Reference: " + req.getRequestId()
                            + "\nCrop: " + CropItemMapping.getCropName(selectedCropId)
                            + "\nItem: " + selectedItem.getDisplayName()
                            + "\nQuantity: " + selectedQty + " " + unitText.toLowerCase()
                            + "\nSeason: " + selectedSeason
                            + "\nStatus: PENDING",
                            false, () -> showMyRequests());
                    } catch (Exception ex) {
                        CustomAlert.show(rootPane, "Error", ex.getMessage(), true);
                    }
                }, true);
                btnRow.getChildren().get(0).setOnMouseClicked(ev -> { currentStep = 3; refreshUI[0].run(); });
                stepContent.getChildren().add(btnRow);
            }

            stepContainer.getChildren().add(stepContent);
        };

        refreshUI[0].run();
        content.getChildren().addAll(title, userPill, progBar, stepContainer);
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        mainLayout.setCenter(scroll);
    }

    /** Helper: build a section card with title label. */
    private VBox buildSectionCard(String titleText) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15, 18, 15, 18));
        card.setStyle("-fx-background-color: rgba(255,255,255,0.85); -fx-background-radius: 12; " +
                "-fx-border-color: rgba(0,0,0,0.08); -fx-border-radius: 12; -fx-border-width: 0.5;");
        Label t = new Label(titleText);
        t.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #777;");
        card.getChildren().add(t);
        return card;
    }

    /** Helper: build Back/Next button row. */
    private HBox buildBtnRow(String backText, String nextText, Runnable onNext, boolean nextEnabled) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(10, 0, 0, 0));

        Button backBtn = new Button(backText != null ? backText : "");
        backBtn.setVisible(backText != null);
        backBtn.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #bbb; -fx-border-radius: 6; " +
                "-fx-padding: 8 20; -fx-font-size: 13px; -fx-cursor: hand; -fx-text-fill: #333; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button nextBtn = new Button(nextText);
        nextBtn.setDisable(!nextEnabled);
        nextBtn.setStyle("-fx-background-color: " + (nextEnabled ? "#3B6D11" : "#ccc") + "; " +
                "-fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 24; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;");
        nextBtn.setOnAction(ev -> onNext.run());

        row.getChildren().addAll(backBtn, spacer, nextBtn);
        return row;
    }


    // --- My Requests Panel ---
    // --- My Requests Panel (Modern Card List) ---
    private String currentFilter = "ALL";

    private void showMyRequests() {
        if (myRequestsBtn != null) setActiveNav(myRequestsBtn); // Ensure correct nav is highlighted
        
        VBox content = new VBox(25);
        content.setPadding(new Insets(40));
        content.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("My Subsidy Requests");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 0, 2);");

        // --- Filter Chips ---
        HBox filterBox = new HBox(12);
        filterBox.setAlignment(Pos.CENTER);
        String[] filters = {"ALL", "PENDING", "APPROVED", "REJECTED"};
        for (String f : filters) {
            Button chip = new Button(f);
            boolean isSel = currentFilter.equals(f);
            chip.setStyle("-fx-background-color: " + (isSel ? "#4CAF50" : "rgba(255,255,255,0.15)") + "; " +
                          "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; " +
                          "-fx-padding: 8 20; -fx-background-radius: 20; -fx-cursor: hand; " +
                          "-fx-border-color: " + (isSel ? "white" : "rgba(255,255,255,0.3)") + "; -fx-border-radius: 20;");
            chip.setOnAction(e -> { currentFilter = f; showMyRequests(); });
            filterBox.getChildren().add(chip);
        }

        // --- Requests List ---
        VBox listContainer = new VBox(15);
        listContainer.setMaxWidth(900);
        
        try {
            SubsidyRequestController ctrl = new SubsidyRequestController();
            List<SubsidyRequest> all = ctrl.getRequestsByFarmerId(session.getFarmerId());
            
            int count = 0;
            for (SubsidyRequest req : all) {
                if (currentFilter.equals("ALL") || req.getStatus().equalsIgnoreCase(currentFilter)) {
                    listContainer.getChildren().add(createRequestCard(req));
                    count++;
                }
            }
            
            if (count == 0) {
                Label empty = new Label("No " + currentFilter.toLowerCase() + " requests found.");
                empty.setStyle("-fx-text-fill: #A5D6A7; -fx-font-style: italic; -fx-font-size: 16px;");
                listContainer.getChildren().add(empty);
            }
            
        } catch (Exception ex) {
            listContainer.getChildren().add(new Label("Error: " + ex.getMessage()));
        }

        content.getChildren().addAll(title, filterBox, listContainer);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        mainLayout.setCenter(scroll);
    }

    private VBox createRequestCard(SubsidyRequest req) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: rgba(0, 30, 0, 0.7); -fx-background-radius: 15; -fx-border-color: rgba(255,255,255,0.2); -fx-border-width: 1; -fx-border-radius: 15; -fx-cursor: hand;");
        
        HBox topRow = new HBox(15);
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        VBox idBox = new VBox(2);
        Label idL = new Label(req.getRequestId());
        idL.setStyle("-fx-font-family: 'Courier New'; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 15px;");
        Label dateL = new Label(req.getRequestDate().toString().substring(0, 10));
        dateL.setStyle("-fx-text-fill: #A5D6A7; -fx-font-size: 11px;");
        idBox.getChildren().addAll(idL, dateL);
        
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Status Badge
        Label statusBadge = new Label(req.getStatus());
        String color = req.getStatus().equals("PENDING") ? "#FF9800" : 
                       req.getStatus().equals("APPROVED") ? "#4CAF50" : "#F44336";
        statusBadge.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 10;");
        
        topRow.getChildren().addAll(idBox, spacer, statusBadge);
        
        GridPane grid = new GridPane(); grid.setHgap(30); grid.setVgap(8);
        addSummaryItem(grid, 0, 0, "Item", req.getRequestedItemName());
        addSummaryItem(grid, 0, 1, "Quantity", req.getQuantity() + " " + CropItemMapping.getUnitForCategory(CropItemMapping.getCategoryForItem(req.getRequestedItemId())));
        addSummaryItem(grid, 1, 0, "Crop", req.getCropName());
        addSummaryItem(grid, 1, 1, "Season", req.getSeason());
        
        card.getChildren().addAll(topRow, new Separator(), grid);
        
        if ("REJECTED".equals(req.getStatus()) && req.getRejectionReason() != null) {
            Label r = new Label("Reason: " + req.getRejectionReason());
            r.setStyle("-fx-text-fill: #FFCDD2; -fx-font-style: italic; -fx-font-size: 12px;");
            card.getChildren().add(r);
        }
        
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: rgba(0, 50, 0, 0.85); -fx-background-radius: 15; -fx-border-color: white; -fx-border-width: 1; -fx-border-radius: 15; -fx-translate-y: -2;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: rgba(0, 30, 0, 0.7); -fx-background-radius: 15; -fx-border-color: rgba(255,255,255,0.2); -fx-border-width: 1; -fx-border-radius: 15; -fx-translate-y: 0;"));
        
        return card;
    }

    private void addSummaryItem(GridPane g, int col, int row, String k, String v) {
        Label kl = new Label(k + ": "); kl.setStyle("-fx-text-fill: #A5D6A7; -fx-font-size: 13px;");
        Label vl = new Label(v); vl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        HBox box = new HBox(kl, vl);
        g.add(box, col, row);
    }



    // --- UI Helpers ---





    private VBox createStatCard(String label, String value, String color) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(220);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 12; -fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 1; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5); -fx-cursor: hand;");
        
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + color + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 2, 0, 0, 1);");
        
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: bold;");
        
        card.getChildren().addAll(val, lbl);
        card.setOnMouseEntered(e -> card.setTranslateY(-5));
        card.setOnMouseExited(e -> card.setTranslateY(0));
        return card;
    }



    public Scene getScene() {
        ui.components.ImageBackground.applyTheme(scene);
        return scene;
    }
}
