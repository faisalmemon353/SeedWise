package ui.officer;

import business.controller.ReportController;
import business.controller.SubsidyRequestController;
import business.model.*;
import data.*;
import ui.LoginView;
import ui.SessionManager;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.*;

public class OfficerDashboard {

    private final Scene scene;
    private final BorderPane mainLayout;
    private final SessionManager session = SessionManager.getInstance();
    private Button[] activeBtn;

    public OfficerDashboard(Stage stage) {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: transparent;");

        HBox topNav = new HBox(15);
        topNav.setPadding(new Insets(12, 30, 12, 30));
        topNav.setStyle("-fx-background-color: rgba(45, 80, 22, 0.95); -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);");
        topNav.setAlignment(Pos.CENTER_LEFT);

        Label logo = new Label("SeedWise");
        logo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #D4E6A5;");
        logo.setOnMouseEntered(e -> { logo.setScaleX(1.05); logo.setScaleY(1.05); });
        logo.setOnMouseExited(e -> { logo.setScaleX(1); logo.setScaleY(1); });
        Label roleLabel = new Label("OFFICER");
        roleLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #8BC34A; -fx-padding: 6 0 0 10;");
        HBox logoArea = new HBox(logo, roleLabel);
        logoArea.setAlignment(Pos.CENTER_LEFT);

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        Button overviewBtn = topBtn("Overview");
        Button limitsBtn = topBtn("Allocation Limits");
        Button addDistBtn = topBtn("Add Distributor");
        Button reportBtn = topBtn("Reports");
        Button rulesBtn = topBtn("Seasonal Rules");
        activeBtn = new Button[]{overviewBtn};

        overviewBtn.setTooltip(new Tooltip("Dashboard overview with charts and stats"));
        limitsBtn.setTooltip(new Tooltip("Set and manage crop allocation limits"));
        addDistBtn.setTooltip(new Tooltip("Register new distributors to the system"));
        reportBtn.setTooltip(new Tooltip("Generate distribution and utilization reports"));
        rulesBtn.setTooltip(new Tooltip("Configure active application months for crops"));

        HBox navLinks = new HBox(8, overviewBtn, limitsBtn, addDistBtn, reportBtn, rulesBtn);
        navLinks.setAlignment(Pos.CENTER);

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        ImageView avatar = new ImageView();
        try { avatar.setImage(new Image(new java.io.File("resources/images/avatar.png").toURI().toString(), 36, 36, true, true)); } catch(Exception ignored) {}
        avatar.setFitWidth(36); avatar.setFitHeight(36);
        Circle clipCircle = new Circle(18, 18, 18);
        avatar.setClip(clipCircle);

        Label userInfo = new Label(session.getCurrentUsername());
        userInfo.setStyle("-fx-text-fill: #D4E6A5; -fx-font-size: 13px; -fx-font-weight: bold;");
        Button logoutBtn = new Button("Sign Out");
        logoutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: rgba(255,255,255,0.5); -fx-border-radius: 4; -fx-cursor: hand;");
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 4; -fx-cursor: hand;"));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: rgba(255,255,255,0.5); -fx-border-radius: 4; -fx-cursor: hand;"));

        HBox profileArea = new HBox(10, avatar, userInfo, logoutBtn);
        profileArea.setAlignment(Pos.CENTER);

        topNav.getChildren().addAll(logoArea, spacer1, navLinks, spacer2, profileArea);
        mainLayout.setTop(topNav);

        overviewBtn.setOnAction(e -> { setActiveNav(overviewBtn); showOverview(); });
        limitsBtn.setOnAction(e -> { setActiveNav(limitsBtn); showAllocationLimits(); });
        addDistBtn.setOnAction(e -> { setActiveNav(addDistBtn); showAddDistributor(); });
        reportBtn.setOnAction(e -> { setActiveNav(reportBtn); showGenerateReport(); });
        rulesBtn.setOnAction(e -> { setActiveNav(rulesBtn); showSeasonalRules(); });
        logoutBtn.setOnAction(e -> { session.logout(); stage.setScene(new LoginView(stage).getScene()); });

        setActiveNav(overviewBtn);
        showOverview();
        StackPane rootPane = new StackPane(mainLayout);
        ui.components.ImageBackground.applyTo(rootPane);
        this.scene = new Scene(rootPane, 1200, 800);
    }

    private Button topBtn(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: transparent; -fx-text-fill: #D4E6A5; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-cursor: hand; -fx-background-radius: 6;");
        b.setOnMouseEntered(e -> {
            if (activeBtn[0] != b) {
                b.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-cursor: hand; -fx-background-radius: 6;");
            }
            b.setScaleX(1.05); b.setScaleY(1.05);
        });
        b.setOnMouseExited(e -> {
            if (activeBtn[0] != b) {
                b.setStyle("-fx-background-color: transparent; -fx-text-fill: #D4E6A5; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-cursor: hand; -fx-background-radius: 6;");
            }
            b.setScaleX(1); b.setScaleY(1);
        });
        return b;
    }

    private void setActiveNav(Button b) {
        if (activeBtn[0] != null) {
            activeBtn[0].setStyle("-fx-background-color: transparent; -fx-text-fill: #D4E6A5; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-cursor: hand; -fx-background-radius: 6;");
        }
        activeBtn[0] = b;
        b.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-cursor: hand; -fx-background-radius: 6;");
    }

    private void highlightCard(VBox active, VBox... others) {
        if(active != null) {
            active.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-background-radius: 12; -fx-border-color: rgba(255,255,255,0.8); -fx-border-width: 2; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 8); -fx-cursor: hand;");
        }
        for (VBox o : others) {
            o.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 12; -fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 1; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5); -fx-cursor: hand;");
        }
    }

    // --- Overview with modern cards and charts ---
    private void showOverview() {
        VBox content = new VBox(25);
        content.setPadding(new Insets(40));

        Label title = new Label("Officer Dashboard");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 0, 2);");

        VBox dynamicArea = new VBox(20);
        HBox chartsRow = new HBox(20);

        try {
            SubsidyRequestController ctrl = new SubsidyRequestController();
            int pending = ctrl.countByStatus("PENDING");
            int approved = ctrl.countByStatus("APPROVED");
            int rejected = ctrl.countByStatus("REJECTED");
            int distributed = ctrl.countByStatus("DISTRIBUTED");

            HBox stats = new HBox(20);
            VBox pendingCard = createStatCard("Pending", String.valueOf(pending), "#FF9800");
            VBox approvedCard = createStatCard("Approved", String.valueOf(approved), "#4CAF50");
            VBox rejectedCard = createStatCard("Rejected", String.valueOf(rejected), "#D32F2F");
            VBox distCard = createStatCard("Distributed", String.valueOf(distributed), "#2196F3");

            stats.getChildren().addAll(pendingCard, approvedCard, rejectedCard, distCard);

            // Fetch all requests for charts
            SubsidyRequestDAO dao = new SubsidyRequestDAO();
            List<SubsidyRequest> allRequests = dao.searchByFilters(null, null, null, "ALL", null);

            // --- PIE CHART ---
            Label pieTitle = new Label("Request Status Breakdown");
            pieTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

            int totalReqs = allRequests.size();
            String pLbl = String.format("Pending (%.1f%%)", totalReqs > 0 ? (pending * 100.0 / totalReqs) : 0);
            String aLbl = String.format("Approved (%.1f%%)", totalReqs > 0 ? (approved * 100.0 / totalReqs) : 0);
            String rLbl = String.format("Rejected (%.1f%%)", totalReqs > 0 ? (rejected * 100.0 / totalReqs) : 0);
            String dLbl = String.format("Distributed (%.1f%%)", totalReqs > 0 ? (distributed * 100.0 / totalReqs) : 0);

            PieChart pieChart = new PieChart(FXCollections.observableArrayList(
                new PieChart.Data(pLbl, pending),
                new PieChart.Data(aLbl, approved),
                new PieChart.Data(rLbl, rejected),
                new PieChart.Data(dLbl, distributed)
            ));
            pieChart.setLegendVisible(false);
            pieChart.setAnimated(true);
            pieChart.setMinHeight(280);
            pieChart.setPrefHeight(280);
            pieChart.setLabelsVisible(true);

            String[] pieColors = {"#FF9800", "#4CAF50", "#D32F2F", "#2196F3"};
            String[] pieLabels = {pLbl, aLbl, rLbl, dLbl};
            int[] pieCounts = {pending, approved, rejected, distributed};

            javafx.application.Platform.runLater(() -> {
                int idx = 0;
                for (PieChart.Data d : pieChart.getData()) {
                    if (d.getNode() != null) {
                        String clr = pieColors[idx];
                        d.getNode().setStyle("-fx-pie-color: " + clr + ";");
                        double pct = totalReqs > 0 ? (pieCounts[idx] * 100.0 / totalReqs) : 0;
                        Tooltip tt = new Tooltip(pieLabels[idx] + ": " + pieCounts[idx] + " requests\n" + String.format("%.1f%%", pct));
                        tt.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
                        Tooltip.install(d.getNode(), tt);
                        final String baseClr = clr;
                        d.getNode().setOnMouseEntered(ev -> {
                            d.getNode().setStyle("-fx-pie-color: " + baseClr + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 12, 0, 0, 0);");
                            d.getNode().setScaleX(1.05); d.getNode().setScaleY(1.05);
                        });
                        d.getNode().setOnMouseExited(ev -> {
                            d.getNode().setStyle("-fx-pie-color: " + baseClr + ";");
                            d.getNode().setScaleX(1); d.getNode().setScaleY(1);
                        });
                    }
                    idx++;
                }
            });

            HBox pieLegend = new HBox(15);
            pieLegend.setAlignment(Pos.CENTER);
            pieLegend.setPadding(new Insets(8, 0, 0, 0));
            String[] shortLabels = {"Pending", "Approved", "Rejected", "Dist"};
            for (int li = 0; li < 4; li++) {
                HBox legItem = new HBox(6); legItem.setAlignment(Pos.CENTER);
                Region legDot = new Region(); legDot.setMinSize(12, 12); legDot.setMaxSize(12, 12);
                legDot.setStyle("-fx-background-color: " + pieColors[li] + "; -fx-background-radius: 50;");
                Label legLbl = new Label(shortLabels[li]);
                legLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #333;");
                legItem.getChildren().addAll(legDot, legLbl);
                pieLegend.getChildren().add(legItem);
            }

            VBox pieCard = new VBox(8, pieTitle, pieChart, pieLegend);
            pieCard.setPadding(new Insets(15));
            pieCard.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5);");

            // --- BAR CHART ---
            Label barTitle = new Label("Subsidy Demand by Crop");
            barTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

            CategoryAxis xAxis = new CategoryAxis();
            xAxis.setLabel("Crops");
            xAxis.setTickLabelRotation(-45);
            xAxis.setTickLabelFont(javafx.scene.text.Font.font("Segoe UI", 10));
            xAxis.setTickLabelFill(Color.web("#333"));
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("Total Requested Qty");
            yAxis.setTickLabelFill(Color.web("#333"));
            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
            barChart.setLegendVisible(false);
            barChart.setAnimated(true);
            barChart.setMinHeight(280);
            barChart.setPrefHeight(280);
            barChart.setTitle(null);

            XYChart.Series<String, Number> cropSeries = new XYChart.Series<>();
            Map<String, Integer> cropDemands = new HashMap<>();
            for (SubsidyRequest r : allRequests) {
                if (!r.getStatus().equals("REJECTED")) {
                    String cropName = r.getCropName() != null ? r.getCropName() : "Unknown";
                    cropDemands.put(cropName, cropDemands.getOrDefault(cropName, 0) + r.getQuantity());
                }
            }

            for (Map.Entry<String, Integer> entry : cropDemands.entrySet()) {
                String cName = entry.getKey();
                if (cName.length() > 10) cName = cName.substring(0, 10);
                cropSeries.getData().add(new XYChart.Data<>(cName, entry.getValue()));
            }
            barChart.getData().add(cropSeries);

            javafx.application.Platform.runLater(() -> {
                for (XYChart.Data<String, Number> d : cropSeries.getData()) {
                    if (d.getNode() != null) {
                        d.getNode().setStyle("-fx-bar-fill: #8BC34A;");
                        Tooltip tt = new Tooltip(d.getXValue() + "\nDemand: " + d.getYValue());
                        tt.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
                        Tooltip.install(d.getNode(), tt);
                        d.getNode().setOnMouseEntered(ev -> d.getNode().setStyle("-fx-bar-fill: #9CCC65; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 0);"));
                        d.getNode().setOnMouseExited(ev -> d.getNode().setStyle("-fx-bar-fill: #8BC34A;"));
                    }
                }
                try {
                    if (xAxis.lookup(".axis-label") != null) xAxis.lookup(".axis-label").setStyle("-fx-text-fill: #333; -fx-font-weight: bold; -fx-font-size: 13px;");
                    if (yAxis.lookup(".axis-label") != null) yAxis.lookup(".axis-label").setStyle("-fx-text-fill: #333; -fx-font-weight: bold; -fx-font-size: 13px;");
                } catch(Exception ignored){}
            });

            VBox barCard = new VBox(8, barTitle, barChart);
            barCard.setPadding(new Insets(15));
            barCard.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5);");

            chartsRow.getChildren().addAll(pieCard, barCard);
            HBox.setHgrow(barCard, Priority.ALWAYS);

            // Setup dynamic views
            Runnable showChartsView = () -> {
                chartsRow.setVisible(true); chartsRow.setManaged(true);
                dynamicArea.getChildren().clear();
                highlightCard(null, pendingCard, approvedCard, rejectedCard, distCard);
            };

            @SuppressWarnings("unchecked")
            java.util.function.Consumer<String>[] showStatusViewRef = new java.util.function.Consumer[1];
            showStatusViewRef[0] = (status) -> {
                chartsRow.setVisible(false); chartsRow.setManaged(false);
                dynamicArea.getChildren().clear();
                
                Label sub = new Label(status + " Requests");
                sub.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 0, 2);");

                HBox filterBar = new HBox(12);
                filterBar.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 15; -fx-background-radius: 10;");
                filterBar.setAlignment(Pos.CENTER_LEFT);
                TextField fName = new TextField(); fName.setPromptText("Farmer Name");
                TextField fCnic = new TextField(); fCnic.setPromptText("CNIC");
                Button fBtn = new Button("Filter");
                fBtn.setStyle("-fx-background-color: #3B6D11; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");
                Button fClr = new Button("Clear");
                fClr.setStyle("-fx-background-color: #999; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 6; -fx-cursor: hand;");
                Button backBtn = new Button("<- Back");
                backBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");
                
                Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
                filterBar.getChildren().addAll(fName, fCnic, fBtn, fClr, sp, backBtn);

                FlowPane grid = new FlowPane(); grid.setHgap(20); grid.setVgap(20);

                Runnable loadReqs = () -> {
                    grid.getChildren().clear();
                    try {
                        List<SubsidyRequest> reqs = dao.searchByFilters(fName.getText().trim(), fCnic.getText().trim(), null, status, null);
                        for(SubsidyRequest r : reqs) {
                            VBox c = new VBox(10); c.setPadding(new Insets(20)); c.setPrefWidth(280);
                            c.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5);");
                            Label fn = new Label(r.getFarmerName()); fn.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #222;");
                            Label cc = new Label("CNIC: " + r.getFarmerCnic()); cc.setStyle("-fx-font-size: 13px; -fx-text-fill: #444;");
                            Label ci = new Label("Item: " + (r.getRequestedItemName() != null ? r.getRequestedItemName() : "N/A")); ci.setStyle("-fx-font-size: 13px; -fx-text-fill: #444;");
                            Label cs = new Label(r.getStatus());
                            String col = r.getStatus().equals("APPROVED") ? "#4CAF50" : r.getStatus().equals("REJECTED") ? "#D32F2F" : r.getStatus().equals("DISTRIBUTED") ? "#2196F3" : "#FF9800";
                            cs.setStyle("-fx-font-weight: bold; -fx-text-fill: " + col + "; -fx-padding: 5 10; -fx-background-color: rgba(0,0,0,0.05); -fx-background-radius: 20;");
                            c.getChildren().addAll(fn, cc, ci, cs);
                            if (status.equals("PENDING")) {
                                Button procBtn = new Button("Process Request");
                                procBtn.setStyle("-fx-background-color: #3B6D11; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6;");
                                procBtn.setMaxWidth(Double.MAX_VALUE);
                                procBtn.setOnAction(ev -> showProcessRequestDetail(r, () -> showStatusViewRef[0].accept("PENDING"), dynamicArea));
                                c.getChildren().add(procBtn);
                            }
                            grid.getChildren().add(c);
                        }
                        if(reqs.isEmpty()) {
                            Label empty = new Label("No " + status + " requests found.");
                            empty.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
                            grid.getChildren().add(empty);
                        }
                    } catch(Exception e){}
                };

                loadReqs.run();
                fBtn.setOnAction(ev -> loadReqs.run());
                fClr.setOnAction(ev -> { fName.clear(); fCnic.clear(); loadReqs.run(); });
                backBtn.setOnAction(ev -> showChartsView.run());
                
                dynamicArea.getChildren().addAll(sub, filterBar, grid);
            };

            pendingCard.setOnMouseClicked(e -> { showStatusViewRef[0].accept("PENDING"); highlightCard(pendingCard, approvedCard, rejectedCard, distCard); });
            approvedCard.setOnMouseClicked(e -> { showStatusViewRef[0].accept("APPROVED"); highlightCard(approvedCard, pendingCard, rejectedCard, distCard); });
            rejectedCard.setOnMouseClicked(e -> { showStatusViewRef[0].accept("REJECTED"); highlightCard(rejectedCard, pendingCard, approvedCard, distCard); });
            distCard.setOnMouseClicked(e -> { showStatusViewRef[0].accept("DISTRIBUTED"); highlightCard(distCard, pendingCard, approvedCard, rejectedCard); });
            
            showChartsView.run();

            ScrollPane scroll = new ScrollPane(new VBox(15, stats, chartsRow, dynamicArea));
            scroll.setFitToWidth(true);
            scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            content.getChildren().addAll(title, scroll);
            VBox.setVgrow(scroll, Priority.ALWAYS);
        } catch (Exception ex) {
            content.getChildren().addAll(title, new Label("Error: " + ex.getMessage()));
        }
        mainLayout.setCenter(content);
    }

    private VBox createStatCard(String label, String value, String color) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(220);
        card.setPrefHeight(120);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 12; -fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 1; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5); -fx-cursor: hand;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + color + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 2, 0, 0, 1);");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: bold;");
        card.getChildren().addAll(val, lbl);
        card.setOnMouseEntered(e -> { card.setTranslateY(-5); });
        card.setOnMouseExited(e -> { card.setTranslateY(0); });
        return card;
    }

    // --- UC-03 + UC-10: Process Request Inline Detail ---
    private void showProcessRequestDetail(SubsidyRequest sel, Runnable onBack, VBox dynamicArea) {
        dynamicArea.getChildren().clear();
        
        Button backBtn = new Button("<- Back to List");
        backBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");
        backBtn.setOnAction(e -> onBack.run());

        VBox mainCard = new VBox(15);
        mainCard.setPadding(new Insets(30));
        mainCard.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5);");

        Label selLbl = new Label("PROCESSING REQUEST FOR: " + sel.getFarmerName().toUpperCase());
        selLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

        VBox infoArea = new VBox(15);
        showOfficerRequestDetail(sel, infoArea);

        VBox actionBox = new VBox(15);

        Label eligibilityLabel = new Label();
        try {
            SubsidyRequestController ctrl = new SubsidyRequestController();
            EligibilityResult res = ctrl.verifyEligibility(sel.getRequestId(), session.getCurrentUserId());
            eligibilityLabel.setText(res.isEligible() ? "\u2713 SYSTEM CHECK: ELIGIBLE - " + res.getReason() : "\u2717 SYSTEM CHECK: INELIGIBLE - " + res.getReason());
            eligibilityLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 6; -fx-background-color: " + (res.isEligible() ? "#E8F5E9" : "#FFEBEE") + "; -fx-text-fill: " + (res.isEligible() ? "#388E3C" : "#D32F2F") + ";");
        } catch (Exception ex) { ex.printStackTrace(); }

        HBox approveRow = new HBox(12);
        approveRow.setAlignment(Pos.CENTER_LEFT);
        TextField approvedQtyInput = new TextField(String.valueOf(sel.getQuantity()));
        approvedQtyInput.setPromptText("Approved Qty");
        approvedQtyInput.setPrefWidth(120);
        approvedQtyInput.setStyle("-fx-padding: 8; -fx-background-radius: 6;");
        Button approveBtn = new Button("Approve Request");
        approveBtn.setStyle("-fx-background-color: #3B6D11; -fx-text-fill: white; -fx-padding: 8 25; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 6;");

        approveRow.getChildren().addAll(new Label("Approved Qty:"), approvedQtyInput, approveBtn);

        VBox rejectBox = new VBox(8);
        TextField rejectReasonInput = new TextField();
        rejectReasonInput.setPromptText("Enter mandatory rejection reason...");
        rejectReasonInput.setStyle("-fx-padding: 8; -fx-background-radius: 6;");
        Button rejectBtn = new Button("Reject Request");
        rejectBtn.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-padding: 8 25; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 6;");
        rejectBox.getChildren().addAll(new Label("Rejection Reason:"), rejectReasonInput, rejectBtn);

        actionBox.getChildren().addAll(new Separator(), eligibilityLabel, approveRow, new Label("OR"), rejectBox);

        approveBtn.setOnAction(e -> {
            try {
                int qty = Integer.parseInt(approvedQtyInput.getText().trim());
                if (qty <= 0) {
                    showAlert("Error", "Approved quantity must be a positive number.");
                    return;
                }
                if (qty > sel.getQuantity()) {
                    showAlert("Error", "Cannot approve more than the farmer requested (" + sel.getQuantity() + " units).");
                    return;
                }
                

                data.AllocationLimitDAO limitDAO = new data.AllocationLimitDAO();
                business.model.AllocationLimit limit = limitDAO.findByItemAndSeason(sel.getRequestedItemId(), sel.getSeason());
                
                if (limit == null) {
                    showAlert("Error", "No allocation limit set for " + sel.getRequestedItemName() + " in the " + sel.getSeason() + " season. This item may not be available for this season.");
                    return;
                }

                int maxAllowed = limit.getMaxBags();
                if (qty > maxAllowed) {
                    showAlert("Error", "Quantity must be within or equal to the system allocation limit of " + maxAllowed + " units.");
                    return;
                }
                new SubsidyRequestController().approveRequest(sel.getRequestId(), session.getCurrentUserId(), qty);
                showAlert("Success", "Request approved successfully.");
                onBack.run();
                showOverview(); // Refresh charts and counts
            } catch (Exception ex) { showAlert("Error", ex.getMessage()); }
        });

        rejectBtn.setOnAction(e -> {
            String reason = rejectReasonInput.getText().trim();
            if (reason.isEmpty()) { showAlert("Error", "Rejection reason is mandatory."); return; }
            try {
                new SubsidyRequestController().rejectRequest(sel.getRequestId(), session.getCurrentUserId(), reason);
                showAlert("Success", "Request rejected.");
                onBack.run();
                showOverview(); // Refresh charts and counts
            } catch (Exception ex) { showAlert("Error", ex.getMessage()); }
        });

        mainCard.getChildren().addAll(selLbl, infoArea, actionBox);
        dynamicArea.getChildren().addAll(backBtn, mainCard);
    }

    private void showOfficerRequestDetail(SubsidyRequest req, VBox container) {
        container.getChildren().clear();
        VBox card = new VBox(12);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #FAFAFA; -fx-background-radius: 8; -fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-border-radius: 8;");

        GridPane g = new GridPane();
        g.setHgap(20); g.setVgap(8);
        
        String[][] data = {
            {"Farmer Name", req.getFarmerName()},
            {"CNIC", req.getFarmerCnic()},
            {"Crop", req.getCropName()},
            {"Item", req.getRequestedItemName()},
            {"Requested Qty", String.valueOf(req.getQuantity())},
            {"Season", req.getSeason()},
            {"Date", req.getRequestDate().toString()}
        };
        
        for (int i = 0; i < data.length; i++) {
            Label k = new Label(data[i][0] + ":"); k.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");
            Label v = new Label(data[i][1]); v.setStyle("-fx-text-fill: #333;");
            g.add(k, 0, i); g.add(v, 1, i);
        }
        
        card.getChildren().add(g);
        container.getChildren().add(card);
    }

    // --- UC-07: Allocation Limits ---
    private void showAllocationLimits() {
        VBox content = new VBox(25); content.setPadding(new Insets(40));
        
        HBox titleRow = new HBox(20);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Configure Allocation Limits");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 0, 2);");
        
        Region tsp = new Region(); HBox.setHgrow(tsp, Priority.ALWAYS);
        Button resetBtn = new Button("\u21BB Reset All Farmer Quotas");
        resetBtn.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");
        resetBtn.setOnAction(e -> {
            ui.components.CustomAlert.showConfirm((StackPane)scene.getRoot(), "Confirm Reset", 
                "Are you sure you want to reset the remaining quota for ALL farmers to 1000 units for the new season?", () -> {
                try {
                    new SubsidyRequestController().resetAllFarmerAllocations(session.getCurrentUserId());
                    showAlert("Success", "All farmer seasonal quotas have been reset to 1000 units.");
                } catch (Exception ex) { showAlert("Error", ex.getMessage()); }
            });
        });
        
        titleRow.getChildren().addAll(title, tsp, resetBtn);
        
        HBox split = new HBox(30);
        
        VBox form = new VBox(20); form.setPadding(new Insets(30));
        form.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5);");
        form.setPrefWidth(350);
        
        Label formTitle = new Label("Set Limit per Farmer");
        formTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        ComboBox<ItemType> itemBox = new ComboBox<>();
        itemBox.setPromptText("Select Item...");
        itemBox.setMaxWidth(Double.MAX_VALUE);
        itemBox.setStyle("-fx-padding: 8; -fx-background-radius: 6;");

        try {
            InventoryDAO invDAO = new InventoryDAO();
            itemBox.setItems(FXCollections.observableArrayList(invDAO.findAllItemTypes()));
        } catch(Exception e) {}
        
        TextField maxQtyField = new TextField(); 
        maxQtyField.setPromptText("Maximum Quantity (Bags/Units)"); 
        maxQtyField.setStyle("-fx-padding: 10; -fx-background-radius: 6; -fx-font-size: 14px;");
        
        Button saveBtn = new Button("Save Limit"); saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setStyle("-fx-background-color: #3B6D11; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 20; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 15px;");
        
        form.getChildren().addAll(formTitle, new Label("Select Item"), itemBox, new Label("Max Bags/Units"), maxQtyField, saveBtn);
        
        ScrollPane scroll = new ScrollPane(); scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        try {
            business.controller.SubsidyRequestController reqController = new business.controller.SubsidyRequestController();
            
            saveBtn.setOnAction(e -> {
                ItemType selItem = itemBox.getValue();
                if (selItem == null || maxQtyField.getText().trim().isEmpty()) {
                    showAlert("Validation Error", "Please select item and enter quantity."); return;
                }
                try {
                    int max = Integer.parseInt(maxQtyField.getText().trim());
                    if (max <= 0) { showAlert("Validation Error", "Quantity must be positive."); return; }
                    
                    reqController.setAllocationLimit(selItem.getItemTypeId(), selItem.getName(), max, session.getCurrentUserId());
                    showAlert("Success", "Limit for " + selItem.getName() + " saved successfully.");
                    showAllocationLimits(); // Refresh view
                } catch (NumberFormatException ex) { showAlert("Validation Error", "Enter a valid number.");
                } catch (Exception ex) { showAlert("Error", ex.getMessage()); }
            });

            FlowPane grid = new FlowPane(); grid.setHgap(15); grid.setVgap(15);
            for (AllocationLimit lim : reqController.getAllActiveAllocationLimits()) {
                VBox card = new VBox(0); card.setPrefWidth(240);
                card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-border-color: #E8F5E9; -fx-border-width: 2; -fx-border-radius: 12;");
                
                ImageView tiv = new ImageView();
                try { 
                    String url = getCropImageUrl(lim.getItemName());
                    tiv.setImage(new Image(url, 240, 100, false, true, true)); 
                } catch(Exception ignored) {}
                
                tiv.setFitWidth(240); tiv.setFitHeight(100);
                Rectangle tcl = new Rectangle(240, 100); tcl.setArcWidth(24); tcl.setArcHeight(24); tiv.setClip(tcl);
                
                VBox td = new VBox(8); td.setPadding(new Insets(15));
                Label tn = new Label(lim.getItemName().toUpperCase()); 
                tn.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #333;");
                
                Label ts = new Label(lim.getSeason());
                ts.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #3B6D11; -fx-padding: 3 10; -fx-background-radius: 10;");

                Label tc = new Label("Max Limit: " + lim.getMaxBags() + " units");
                tc.setStyle("-fx-text-fill: #3B6D11; -fx-font-weight: bold; -fx-font-size: 14px;");
                
                td.getChildren().addAll(tn, ts, tc); 
                card.getChildren().addAll(tiv, td); 
                grid.getChildren().add(card);
            }
            scroll.setContent(grid);
        } catch (Exception ex) { form.getChildren().add(new Label("Error: " + ex.getMessage())); }
        
        split.getChildren().addAll(form, scroll); HBox.setHgrow(scroll, Priority.ALWAYS);
        content.getChildren().addAll(titleRow, split);
        mainLayout.setCenter(content);
    }

// --- UC-08: Generate Report ---
    private void showGenerateReport() {
        VBox content = new VBox(25);
        content.setPadding(new Insets(40));

        Label title = new Label("Generate Report");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 0, 2);");

        HBox splitLayout = new HBox(40);

        // Left: Report-themed illustration card
        VBox imgCard = new VBox(15);
        imgCard.setPrefSize(350, 400);
        imgCard.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5);");
        imgCard.setAlignment(Pos.CENTER);
        imgCard.setPadding(new Insets(30));

        javafx.scene.layout.StackPane reportIcon = new javafx.scene.layout.StackPane();
        Rectangle clipboard = new Rectangle(180, 220);
        clipboard.setArcWidth(20); clipboard.setArcHeight(20);
        clipboard.setFill(Color.web("#E8F5E9")); clipboard.setStroke(Color.web("#3B6D11")); clipboard.setStrokeWidth(3);
        Rectangle clipTab = new Rectangle(60, 16);
        clipTab.setArcWidth(8); clipTab.setArcHeight(8);
        clipTab.setFill(Color.web("#3B6D11"));
        clipTab.setTranslateY(-118);
        VBox bars = new VBox(6);
        bars.setAlignment(Pos.BOTTOM_CENTER);
        bars.setTranslateY(10);
        HBox barRow = new HBox(8);
        barRow.setAlignment(Pos.BOTTOM_CENTER);
        double[] barH = {40, 80, 55, 100, 65};
        String[] barC = {"#4CAF50", "#FF9800", "#2196F3", "#3B6D11", "#FFC107"};
        for (int bIdx = 0; bIdx < 5; bIdx++) {
            Rectangle b = new Rectangle(20, barH[bIdx]);
            b.setArcWidth(4); b.setArcHeight(4); b.setFill(Color.web(barC[bIdx]));
            barRow.getChildren().add(b);
        }
        Label checkMark = new Label("\u2713");
        checkMark.setStyle("-fx-font-size: 40px; -fx-text-fill: #3B6D11; -fx-font-weight: bold;");
        checkMark.setTranslateY(50);
        reportIcon.getChildren().addAll(clipboard, clipTab, barRow, checkMark);

        Label reportTitle = new Label("Report Generator");
        reportTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3B6D11;");
        Label reportDesc = new Label("Create distribution and\nsubsidy utilization reports");
        reportDesc.setStyle("-fx-font-size: 13px; -fx-text-fill: #777; -fx-text-alignment: center;");
        reportDesc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        imgCard.getChildren().addAll(reportIcon, reportTitle, reportDesc);

        // Right: Wizard form
        VBox form = new VBox(20);
        form.setPadding(new Insets(0));
        form.setStyle("-fx-background-color: transparent;");
        form.setPrefWidth(450);

        HBox progBar = new HBox(15);
        progBar.setAlignment(Pos.CENTER_LEFT);
        progBar.setPadding(new Insets(0, 0, 20, 0));
        
        VBox stepContainer = new VBox();
        int[] step = {1};
        String[] state = {"", ""}; 
        Runnable[] refreshUI = new Runnable[1];

        refreshUI[0] = () -> {
            progBar.getChildren().clear();
            String[] sLabels = {"Type", "Format", "Generate"};
            for (int i = 0; i < 3; i++) {
                VBox stepVBox = new VBox(5);
                stepVBox.setAlignment(Pos.CENTER);
                Circle dot = new Circle(14, (i + 1 <= step[0]) ? javafx.scene.paint.Color.web("#3B6D11") : javafx.scene.paint.Color.web("rgba(255,255,255,0.8)"));
                dot.setStroke(Color.web((i + 1 <= step[0]) ? "#3B6D11" : "#ccc"));
                dot.setStrokeWidth(2);
                Label sLabel = new Label(sLabels[i]);
                sLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + (i + 1 <= step[0] ? "white" : "rgba(255,255,255,0.6)") + ";");
                stepVBox.getChildren().addAll(dot, sLabel);
                HBox.setHgrow(stepVBox, Priority.ALWAYS);
                progBar.getChildren().add(stepVBox);
                if (i < 2) {
                    Region line = new Region();
                    line.setMinHeight(2); line.setMaxHeight(2);
                    line.setPrefWidth(80);
                    line.setStyle("-fx-background-color: " + (i + 1 < step[0] ? "#3B6D11" : "rgba(255,255,255,0.4)") + ";");
                    HBox lineBox = new HBox(line);
                    lineBox.setAlignment(Pos.CENTER);
                    lineBox.setPadding(new Insets(14, 0, 0, 0));
                    progBar.getChildren().add(lineBox);
                }
            }

            stepContainer.getChildren().clear();
            VBox stepContent = new VBox(20);

            if (step[0] == 1) {
                VBox typeCard = new VBox(15);
                typeCard.setPadding(new Insets(20));
                typeCard.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5);");
                Label tLbl = new Label("SELECT REPORT TYPE");
                tLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #777;");
                typeCard.getChildren().add(tLbl);

                HBox typeOpts = new HBox(15);
                String[] types = {"Distribution Report", "Subsidy Utilization Report"};
                for (String tt : types) {
                    VBox optCard = new VBox(5);
                    optCard.setAlignment(Pos.CENTER);
                    optCard.setPadding(new Insets(20, 25, 20, 25));
                    optCard.setStyle("-fx-border-color: " + (state[0].equalsIgnoreCase(tt) ? "#3B6D11" : "#E0E0E0") + "; " +
                        "-fx-border-radius: 8; -fx-border-width: 2; -fx-cursor: hand; " +
                        "-fx-background-color: " + (state[0].equalsIgnoreCase(tt) ? "#F1F8E9" : "white") + "; -fx-background-radius: 8;");
                    Label sn = new Label(tt);
                    sn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
                    optCard.getChildren().add(sn);
                    HBox.setHgrow(optCard, Priority.ALWAYS);
                    optCard.setOnMouseClicked(ev -> { state[0] = tt; refreshUI[0].run(); });
                    typeOpts.getChildren().add(optCard);
                }
                typeCard.getChildren().add(typeOpts);
                stepContent.getChildren().add(typeCard);

                HBox btnRow = createWizardBtnRow(null, "Next ->", () -> {
                    if (state[0].isEmpty()) { showAlert("Validation", "Please select a report type."); } else { step[0] = 2; refreshUI[0].run(); }
                }, !state[0].isEmpty());
                stepContent.getChildren().add(btnRow);

            } else if (step[0] == 2) {
                VBox fmtCard = new VBox(15);
                fmtCard.setPadding(new Insets(20));
                fmtCard.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5);");
                Label fLbl = new Label("SELECT FORMAT");
                fLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #777;");
                fmtCard.getChildren().add(fLbl);

                HBox fmtOpts = new HBox(15);
                String[] formats = {"PDF", "CSV"};
                for (String ff : formats) {
                    VBox optCard = new VBox(5);
                    optCard.setAlignment(Pos.CENTER);
                    optCard.setPadding(new Insets(20, 30, 20, 30));
                    optCard.setStyle("-fx-border-color: " + (state[1].equals(ff) ? "#3B6D11" : "#E0E0E0") + "; " +
                        "-fx-border-radius: 8; -fx-border-width: 2; -fx-cursor: hand; " +
                        "-fx-background-color: " + (state[1].equals(ff) ? "#F1F8E9" : "white") + "; -fx-background-radius: 8;");
                    Label fn = new Label(ff);
                    fn.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #333;");
                    optCard.getChildren().add(fn);
                    HBox.setHgrow(optCard, Priority.ALWAYS);
                    optCard.setOnMouseClicked(ev -> { state[1] = ff; refreshUI[0].run(); });
                    fmtOpts.getChildren().add(optCard);
                }
                fmtCard.getChildren().add(fmtOpts);
                stepContent.getChildren().add(fmtCard);

                HBox btnRow = createWizardBtnRow("<- Back", "Review ->",
                    () -> { if (state[1].isEmpty()) showAlert("Validation", "Please select a format."); else { step[0] = 3; refreshUI[0].run(); } }, !state[1].isEmpty());
                btnRow.getChildren().get(0).setOnMouseClicked(ev -> { step[0] = 1; refreshUI[0].run(); });
                stepContent.getChildren().add(btnRow);

            } else if (step[0] == 3) {
                VBox revCard = new VBox(15);
                revCard.setPadding(new Insets(20));
                revCard.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5);");
                Label rLbl = new Label("REPORT SUMMARY");
                rLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #777;");
                
                GridPane grid = new GridPane();
                grid.setHgap(30); grid.setVgap(15);
                String[][] rows = {
                    {"Report Type", state[0]},
                    {"Format", state[1]}
                };
                for (int i = 0; i < rows.length; i++) {
                    Label k = new Label(rows[i][0]); k.setStyle("-fx-text-fill: #777; -fx-font-size: 15px;"); k.setMinWidth(120);
                    Label v = new Label(rows[i][1]); v.setStyle("-fx-font-weight: bold; -fx-text-fill: #333; -fx-font-size: 15px;");
                    grid.add(k, 0, i); grid.add(v, 1, i);
                }
                revCard.getChildren().addAll(rLbl, grid);
                stepContent.getChildren().add(revCard);

                HBox btnRow = createWizardBtnRow("<- Edit", "Generate Report", () -> {
                    try {
                        ReportController rc = new ReportController();
                        java.util.Map<String, Object> filters = new java.util.HashMap<>();
                        // For simplicity, passing empty filters. In a real app, we'd collect them.
                        rc.generateReport(state[0], state[1], filters, session.getCurrentUserId(), "OFFICER");

                        String msg = state[1].equalsIgnoreCase("PDF") ? 
                            state[0] + " generated successfully as a PDF file." : 
                            state[0] + " generated successfully as a CSV file.";
                        showAlert("Success", msg + " Saved in reportofficer directory.");
                        step[0] = 1; state[0] = ""; state[1] = ""; refreshUI[0].run();
                    } catch (Exception ex) { showAlert("Error", ex.getMessage()); }
                }, true);
                btnRow.getChildren().get(0).setOnMouseClicked(ev -> { step[0] = 2; refreshUI[0].run(); });
                
                stepContent.getChildren().addAll(btnRow);
            }

            stepContainer.getChildren().add(stepContent);
        };

        refreshUI[0].run();
        form.getChildren().addAll(progBar, stepContainer);
        splitLayout.getChildren().addAll(imgCard, form);
        
        ScrollPane scroll = new ScrollPane(new VBox(15, splitLayout));
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        content.getChildren().addAll(title, scroll);
        mainLayout.setCenter(content);
    }

    // --- Add Distributor ---
    private void showAddDistributor() {
        VBox content = new VBox(25);
        content.setPadding(new Insets(40));
        content.setAlignment(Pos.CENTER);

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(500);
        card.setPadding(new Insets(40));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5);");

        Label title = new Label("Register New Distributor");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333; -fx-padding: 0 0 10 0;");

        TextField nameField = createRegistrationField("Full Name");
        TextField cnicField = createRegistrationField("CNIC (XXXXX-XXXXXXX-X)");
        TextField contactField = createRegistrationField("Contact Number (XXXX-XXXXXXX)");
        TextField addressField = createRegistrationField("Office Address");
        TextField usernameField = createRegistrationField("Username");
        
        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        styleRegField(passField);
        
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm Password");
        styleRegField(confirmField);

        Button saveBtn = new Button("Register Distributor");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setStyle("-fx-background-color: #3B6D11; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12; -fx-cursor: hand; -fx-background-radius: 8;");

        Button viewAllBtn = new Button("View Registered Distributors");
        viewAllBtn.setMaxWidth(Double.MAX_VALUE);
        viewAllBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #3B6D11; -fx-font-size: 14px; -fx-padding: 10; -fx-cursor: hand; -fx-background-radius: 8; -fx-border-color: #3B6D11; -fx-border-radius: 8;");
        viewAllBtn.setOnAction(e -> showViewDistributors());

        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String cnic = cnicField.getText().trim();
            String contact = contactField.getText().trim();
            String address = addressField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passField.getText();
            String confirm = confirmField.getText();

            if (name.isEmpty() || cnic.isEmpty() || contact.isEmpty() || address.isEmpty() || username.isEmpty() || password.isEmpty()) {
                showAlert("Validation Error", "All fields are mandatory."); return;
            }
            if (!password.equals(confirm)) {
                showAlert("Validation Error", "Passwords do not match."); return;
            }
            if (!cnic.matches("\\d{5}-\\d{7}-\\d")) {
                showAlert("Validation Error", "Invalid CNIC format. Expected: XXXXX-XXXXXXX-X"); return;
            }
            if (!contact.matches("\\d{4}-\\d{7}")) {
                showAlert("Validation Error", "Invalid Contact format. Expected: XXXX-XXXXXXX"); return;
            }

            try {
                UserAccountDAO userDAO = new UserAccountDAO();
                if (userDAO.usernameExists(username)) {
                    showAlert("Error", "Username already taken."); return;
                }
                
                String userId = UUID.randomUUID().toString();
                UserAccount user = new UserAccount(userId, username, password, "DISTRIBUTOR");
                userDAO.insert(user);
                
                DistributorDAO distDAO = new DistributorDAO();
                distDAO.insert(UUID.randomUUID().toString(), userId, name, contact, cnic, address);

                business.singleton.AuditLogger.getInstance().logAction(userId, "REGISTER_DISTRIBUTOR", session.getCurrentUserId(), "OFFICER", "Distributor " + name + " added.");
                showAlert("Success", "Distributor registered successfully!");
                
                nameField.clear(); cnicField.clear(); contactField.clear(); addressField.clear(); 
                usernameField.clear(); passField.clear(); confirmField.clear();
            } catch (Exception ex) { showAlert("Error", ex.getMessage()); }
        });

        card.getChildren().addAll(title, nameField, cnicField, contactField, addressField, usernameField, passField, confirmField, saveBtn, viewAllBtn);
        
        ScrollPane scroll = new ScrollPane(new VBox(card));
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        ((VBox)scroll.getContent()).setAlignment(Pos.CENTER);
        
        mainLayout.setCenter(scroll);
    }

    private void showViewDistributors() {
        VBox content = new VBox(25);
        content.setPadding(new Insets(40));
        content.setMaxWidth(900);

        Label title = new Label("Registered Distributors");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 0, 2);");

        VBox listContainer = new VBox(15);

        try {
            DistributorDAO dao = new DistributorDAO();
            List<Distributor> distributors = dao.getAllDistributors();
            
            if (distributors.isEmpty()) {
                Label empty = new Label("No distributors registered yet.");
                empty.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
                listContainer.getChildren().add(empty);
            } else {
                for (Distributor d : distributors) {
                    VBox dCard = new VBox(15);
                    dCard.setPadding(new Insets(20));
                    dCard.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5);");
                    
                    HBox header = new HBox(15);
                    Label nameLbl = new Label(d.getName());
                    nameLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3B6D11;");
                    Label idLbl = new Label("ID: " + d.getDistributorId());
                    idLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");
                    
                    Button removeBtn = new Button("Remove");
                    removeBtn.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-padding: 6 15; -fx-font-size: 12px; -fx-cursor: hand; -fx-background-radius: 6; -fx-font-weight: bold;");
                    removeBtn.setOnAction(e -> {
                        try {
                            DistributorDAO distDao = new DistributorDAO();
                            distDao.deleteByUserId(d.getUserId());
                            UserAccountDAO userDao = new UserAccountDAO();
                            userDao.disableAccount(d.getUserId());
                            
                            business.singleton.AuditLogger.getInstance().logAction(d.getUserId(), "REMOVE_DISTRIBUTOR", session.getCurrentUserId(), "OFFICER", "Distributor " + d.getName() + " removed.");
                            showAlert("Success", "Distributor " + d.getName() + " removed successfully.");
                            showViewDistributors();
                        } catch (Exception ex) { showAlert("Error", "Could not remove distributor: " + ex.getMessage()); }
                    });

                    Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
                    header.getChildren().addAll(nameLbl, s, idLbl, removeBtn);
                    header.setAlignment(Pos.CENTER_LEFT);

                    GridPane g = new GridPane();
                    g.setHgap(40); g.setVgap(8);
                    g.add(new Label("CNIC:"), 0, 0); g.add(new Label(d.getCnic()), 1, 0);
                    g.add(new Label("Contact:"), 0, 1); g.add(new Label(d.getContact()), 1, 1);
                    g.add(new Label("Address:"), 0, 2); g.add(new Label(d.getAddress()), 1, 2);
                    
                    g.getChildren().forEach(n -> {
                        if (GridPane.getColumnIndex(n) == 0) n.setStyle("-fx-font-weight: bold; -fx-text-fill: #666; -fx-font-size: 14px;");
                        else n.setStyle("-fx-text-fill: #333; -fx-font-size: 14px;");
                    });

                    dCard.getChildren().addAll(header, new Separator(), g);
                    listContainer.getChildren().add(dCard);
                }
            }
        } catch (Exception ex) { showAlert("Error", "Could not load distributors: " + ex.getMessage()); }

        Button backBtn = new Button("<- Back to Registration");
        backBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-border-color: rgba(255,255,255,0.5); -fx-border-radius: 6; -fx-padding: 10 20; -fx-cursor: hand; -fx-text-fill: white; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> showAddDistributor());

        ScrollPane scroll = new ScrollPane(listContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        content.getChildren().addAll(title, backBtn, scroll);
        mainLayout.setCenter(content);
    }

    private TextField createRegistrationField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        styleRegField(f);
        return f;
    }

    private void styleRegField(Control f) {
        f.setStyle("-fx-padding: 12; -fx-font-size: 14px; -fx-background-radius: 6; -fx-background-color: #FAFAFA; -fx-border-color: #E0E0E0; -fx-border-radius: 6;");
    }

    private HBox createWizardBtnRow(String backText, String nextText, Runnable onNext, boolean nextEnabled) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(10, 0, 0, 0));

        Button backBtn = new Button(backText != null ? backText : "");
        backBtn.setVisible(backText != null);
        backBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #ccc; -fx-border-radius: 6; " +
                "-fx-padding: 10 25; -fx-font-size: 14px; -fx-cursor: hand; -fx-text-fill: #777;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button nextBtn = new Button(nextText);
        nextBtn.setDisable(!nextEnabled);
        nextBtn.setStyle("-fx-background-color: " + (nextEnabled ? "#3B6D11" : "#ccc") + "; " +
                "-fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 10 30; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
        nextBtn.setOnAction(ev -> onNext.run());

        row.getChildren().addAll(backBtn, spacer, nextBtn);
        return row;
    }

    private void showAlert(String titleText, String msg) {
        if (scene != null && scene.getRoot() instanceof StackPane) {
            boolean isError = titleText.equals("Error") || titleText.equals("Validation") || titleText.equals("Validation Error");
            ui.components.CustomAlert.show((StackPane) scene.getRoot(), titleText, msg, isError);
        } else {
            Alert alert = new Alert(titleText.equals("Error") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
            alert.setTitle(titleText);
            alert.setContentText(msg);
            alert.showAndWait();
        }
    }
    public Scene getScene() {
        ui.components.ImageBackground.applyTheme(scene);
        return scene; 
    }
    private String getCropImageUrl(String itemName) {
        if (itemName == null) return new java.io.File("resources/images/farm.png").toURI().toString();
        String n = itemName.toLowerCase();
        String f = "farm.png";
        if (n.contains("wheat")) f = "wheat.png";
        else if (n.contains("rice") || n.contains("paddy")) f = "rice.png";
        else if (n.contains("cotton")) f = "cotton.png";
        else if (n.contains("maize") || n.contains("corn")) f = "maize.png";
        else if (n.contains("sugarcane") || n.contains("sett")) f = "sugarcane.png";
        else if (n.contains("bajra") || n.contains("millet")) f = "bajra.png";
        else if (n.contains("barley")) f = "barley.png";
        else if (n.contains("gram")) f = "gram.png";
        else if (n.contains("mustard")) f = "mustard.png";
        else if (n.contains("peas") || n.contains("pea")) f = "peas.png";
        else if (n.contains("urea")) f = "urea.png";
        else if (n.contains("dap")) f = "dap.png";
        else if (n.contains("npk") || n.contains("ssp") || n.contains("sop") || n.contains("zinc") 
            || n.contains("ammonium") || n.contains("sulphate") || n.contains("sulfate") || n.contains("fertiliz")) f = "fertilizer.png";
        else if (n.contains("confidor") || n.contains("mospilan") || n.contains("sectin") || n.contains("talstar")
            || n.contains("fame") || n.contains("machete") || n.contains("glyphosate") || n.contains("curacron")
            || n.contains("match") || n.contains("pest") || n.contains("insect") || n.contains("herbi") || n.contains("fungi")) f = "pesticide.png";
        else if (n.contains("seed")) f = "seeds.png";
        return new java.io.File("resources/images/" + f).toURI().toString();
    }

    // --- UC-06: Configure Seasonal Rules ---
    private void showSeasonalRules() {
        VBox content = new VBox(25); content.setPadding(new Insets(40));
        Label title = new Label("Configure Seasonal Rules");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 0, 2);");
        
        HBox split = new HBox(30);
        VBox form = new VBox(20); form.setPadding(new Insets(30));
        form.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5);");
        form.setPrefWidth(350);
        
        Label formTitle = new Label("Set Active Months");
        formTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        ComboBox<business.model.Crop> cropBox = new ComboBox<>();
        cropBox.setPromptText("Select Crop...");
        cropBox.setMaxWidth(Double.MAX_VALUE);
        cropBox.setStyle("-fx-padding: 8; -fx-background-radius: 6;");

        try {
            data.CropDAO cropDAO = new data.CropDAO();
            cropBox.setItems(javafx.collections.FXCollections.observableArrayList(cropDAO.findAll()));
        } catch(Exception e) {}
        
        TextField startMonth = new TextField(); 
        startMonth.setPromptText("Start Month (1-12)"); 
        startMonth.setStyle("-fx-padding: 10; -fx-background-radius: 6; -fx-font-size: 14px;");
        
        TextField endMonth = new TextField(); 
        endMonth.setPromptText("End Month (1-12)"); 
        endMonth.setStyle("-fx-padding: 10; -fx-background-radius: 6; -fx-font-size: 14px;");

        
        Button saveBtn = new Button("Save Rule"); saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setStyle("-fx-background-color: #3B6D11; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 20; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 15px;");
        
        form.getChildren().addAll(formTitle, new Label("Select Crop"), cropBox, new Label("Start Month"), startMonth, new Label("End Month"), endMonth, saveBtn);
        
        ScrollPane scroll = new ScrollPane(); scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        try {
            business.controller.SubsidyRequestController reqController = new business.controller.SubsidyRequestController();
            
            saveBtn.setOnAction(e -> {
                business.model.Crop selCrop = cropBox.getValue();
                if (selCrop == null || startMonth.getText().trim().isEmpty() || endMonth.getText().trim().isEmpty()) {
                    showAlert("Validation Error", "Please fill all fields."); return;
                }
                try {
                    int s = Integer.parseInt(startMonth.getText().trim());
                    int en = Integer.parseInt(endMonth.getText().trim());
                    reqController.configureSeasonalRule(selCrop.getCropId(), s, en, session.getCurrentUserId());
                    showAlert("Success", "Seasonal Rule saved successfully.");
                    showSeasonalRules(); // Refresh view
                } catch (NumberFormatException ex) { showAlert("Validation Error", "Enter valid numbers.");
                } catch (Exception ex) { showAlert("Error", ex.getMessage()); }
            });

            FlowPane grid = new FlowPane(); grid.setHgap(15); grid.setVgap(15);
            for (business.model.SeasonalRule rule : reqController.getAllActiveSeasonalRules()) {
                VBox card = new VBox(8); card.setPrefWidth(240); card.setPadding(new Insets(15));
                card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-border-color: #3B6D11; -fx-border-width: 2; -fx-border-radius: 12;");
                Label tn = new Label(rule.getCropName() != null ? rule.getCropName().toUpperCase() : rule.getCropId()); 
                tn.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #333;");
                Label range = new Label(rule.getStartMonth() + " to " + rule.getEndMonth());
                range.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
                card.getChildren().addAll(tn, range); 
                grid.getChildren().add(card);
            }
            scroll.setContent(grid);
        } catch (Exception ex) { form.getChildren().add(new Label("Error: " + ex.getMessage())); }
        
        split.getChildren().addAll(form, scroll); HBox.setHgrow(scroll, Priority.ALWAYS);
        content.getChildren().addAll(title, split);
        mainLayout.setCenter(content);
    }
}
