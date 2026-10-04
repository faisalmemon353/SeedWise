package ui.distributor;

import business.controller.InventoryController;
import business.controller.ReportController;
import business.controller.SubsidyRequestController;
import business.model.*;
import ui.LoginView;
import ui.SessionManager;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.chart.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.*;

public class DistributorDashboard {

    private final Scene scene;
    private final BorderPane mainLayout;
    private final SessionManager session = SessionManager.getInstance();
    private Button[] activeBtn;

    public DistributorDashboard(Stage stage) {
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
        Label roleLabel = new Label("DISTRIBUTOR");
        roleLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #8BC34A; -fx-padding: 6 0 0 10;");
        HBox logoArea = new HBox(logo, roleLabel);
        logoArea.setAlignment(Pos.CENTER_LEFT);

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        Button overviewBtn = topBtn("Overview");
        Button addStockBtn = topBtn("Add Stock");
        Button searchBtn = topBtn("Search");
        Button thresholdBtn = topBtn("Thresholds");
        Button reportBtn = topBtn("Reports");
        activeBtn = new Button[]{overviewBtn};

        overviewBtn.setTooltip(new Tooltip("Dashboard overview with charts and stats"));
        addStockBtn.setTooltip(new Tooltip("Add new inventory stock"));
        searchBtn.setTooltip(new Tooltip("Search subsidy requests by farmer"));
        thresholdBtn.setTooltip(new Tooltip("Configure low stock alert thresholds"));
        reportBtn.setTooltip(new Tooltip("Generate distribution and utilization reports"));

        HBox navLinks = new HBox(8, overviewBtn, addStockBtn, searchBtn, thresholdBtn, reportBtn);
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
        addStockBtn.setOnAction(e -> { setActiveNav(addStockBtn); showAddInventory(); });
        searchBtn.setOnAction(e -> { setActiveNav(searchBtn); showSearch(); });
        thresholdBtn.setOnAction(e -> { setActiveNav(thresholdBtn); showThresholds(); });
        reportBtn.setOnAction(e -> { setActiveNav(reportBtn); showReport(); });
        logoutBtn.setOnAction(e -> { session.logout(); stage.setScene(new LoginView(stage).getScene()); });

        setActiveNav(overviewBtn);
        showOverview();
        StackPane rootPane = new StackPane(mainLayout);
        ui.components.ImageBackground.applyTo(rootPane);
        this.scene = new Scene(rootPane, 1200, 800);
    }

    // --- Overview with clickable stat cards ---
    private void showOverview() {
        VBox content = new VBox(25);
        content.setPadding(new Insets(40));

        Label title = new Label("Distributor Dashboard");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 0, 2);");

        VBox dynamicArea = new VBox(20);
        String[] currentView = {"inventory"};

        try {
            InventoryController ctrl = new InventoryController();
            List<InventoryStock> stocks = ctrl.getAllStock();
            int totalStock = stocks.stream().mapToInt(InventoryStock::getQuantity).sum();
            SubsidyRequestController reqCtrl = new SubsidyRequestController();
            int awaiting = reqCtrl.countByStatus("APPROVED");
            int alertCount = ctrl.getActiveAlerts().size();

            HBox stats = new HBox(20);
            VBox invCard = createClickableStatCard("Total Inventory", String.valueOf(totalStock), "#4CAF50");
            VBox distCard = createClickableStatCard("Awaiting Distribution", String.valueOf(awaiting), "#FF9800");
            VBox alertCard = createClickableStatCard("Active Alerts", String.valueOf(alertCount), "#FF5252");

            stats.getChildren().addAll(invCard, distCard, alertCard);

            HBox chartsRow = new HBox(20);

            Runnable showInventoryView = () -> {
                currentView[0] = "inventory";
                chartsRow.setVisible(false); chartsRow.setManaged(false);
                dynamicArea.getChildren().clear();
                
                Label sub = new Label("Current Inventory Levels");
                sub.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 0, 2);");

                // Search & Filter Bar
                HBox filterBar = new HBox(12);
                filterBar.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
                filterBar.setAlignment(Pos.CENTER_LEFT);

                TextField searchField = new TextField();
                searchField.setPromptText("Search by item name...");
                searchField.setPrefWidth(220);
                searchField.setStyle("-fx-padding: 8; -fx-background-radius: 6;");

                ComboBox<String> categoryFilter = new ComboBox<>(FXCollections.observableArrayList("All Categories", "Seeds", "Fertilizers", "Pesticides"));
                categoryFilter.setValue("All Categories");
                categoryFilter.setStyle("-fx-padding: 4;");

                ComboBox<String> stockFilter = new ComboBox<>(FXCollections.observableArrayList("All Stock", "In Stock", "Low Stock", "Out of Stock"));
                stockFilter.setValue("All Stock");
                stockFilter.setStyle("-fx-padding: 4;");

                Button filterBtn = new Button("Apply");
                filterBtn.setStyle("-fx-background-color: #3B6D11; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");
                Button clearBtn = new Button("Clear");
                clearBtn.setStyle("-fx-background-color: #999; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 6; -fx-cursor: hand;");

                Label countLabel = new Label(stocks.size() + " items");
                countLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12px; -fx-font-weight: bold;");

                Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
                Button backBtn = new Button("<- Back");
                backBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");
                backBtn.setOnAction(e -> showOverview());

                filterBar.getChildren().addAll(searchField, categoryFilter, stockFilter, filterBtn, clearBtn, countLabel, sp, backBtn);

                FlowPane grid = new FlowPane(); grid.setHgap(20); grid.setVgap(20);

                Runnable applyFilter = () -> {
                    grid.getChildren().clear();
                    String query = searchField.getText().trim().toLowerCase();
                    String cat = categoryFilter.getValue();
                    String stockStatus = stockFilter.getValue();
                    int count = 0;

                    for (InventoryStock s : stocks) {
                        String name = s.getItemName().toLowerCase();
                        // Name filter
                        if (!query.isEmpty() && !name.contains(query)) continue;
                        // Category filter
                        if ("Seeds".equals(cat) && !name.contains("seed") && !name.contains("sett")) continue;
                        if ("Fertilizers".equals(cat) && !isItemFertilizer(name)) continue;
                        if ("Pesticides".equals(cat) && !isItemPesticide(name)) continue;
                        // Stock status filter
                        if ("Out of Stock".equals(stockStatus) && s.getQuantity() > 0) continue;
                        if ("In Stock".equals(stockStatus) && s.getQuantity() <= 0) continue;
                        if ("Low Stock".equals(stockStatus) && s.getQuantity() > 50) continue;

                        grid.getChildren().add(createInventoryCard(s));
                        count++;
                    }
                    countLabel.setText(count + " items");
                    if (count == 0) {
                        Label empty = new Label("No items match the selected filters.");
                        empty.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");
                        grid.getChildren().add(empty);
                    }
                };

                applyFilter.run();
                filterBtn.setOnAction(ev -> applyFilter.run());
                clearBtn.setOnAction(ev -> { searchField.clear(); categoryFilter.setValue("All Categories"); stockFilter.setValue("All Stock"); applyFilter.run(); });
                // Also filter on Enter key
                searchField.setOnAction(ev -> applyFilter.run());

                dynamicArea.getChildren().addAll(sub, filterBar, grid);
            };

            Runnable showDistributeView = () -> {
                currentView[0] = "distribute";
                chartsRow.setVisible(false); chartsRow.setManaged(false);
                dynamicArea.getChildren().clear();

                Label sub = new Label("Awaiting Distribution");
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

                Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
                Button backBtn = new Button("<- Back");
                backBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");
                backBtn.setOnAction(e -> showOverview());

                filterBar.getChildren().addAll(fName, fCnic, fBtn, fClr, sp, backBtn);

                FlowPane grid = new FlowPane(); grid.setHgap(20); grid.setVgap(20);

                Runnable loadRequests = () -> {
                    grid.getChildren().clear();
                    try {
                        InventoryController c2 = new InventoryController();
                        // Distributor only sees APPROVED requests in the 'Awaiting' section
                        List<SubsidyRequest> reqs = c2.searchRequests(fName.getText().trim(), fCnic.getText().trim(), null, "APPROVED", null);
                        for (SubsidyRequest r : reqs) {
                            grid.getChildren().add(createDistributeCard(r, c2));
                        }
                        if (reqs.isEmpty()) {
                            Label empty = new Label("No approved requests awaiting distribution.");
                            empty.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");
                            grid.getChildren().add(empty);
                        }
                    } catch (Exception ex) { showAlert("Error", ex.getMessage()); }
                };

                loadRequests.run();
                fBtn.setOnAction(ev -> loadRequests.run());
                fClr.setOnAction(ev -> { fName.clear(); fCnic.clear(); loadRequests.run(); });
                dynamicArea.getChildren().addAll(sub, filterBar, grid);
            };

            Runnable showAlertsView = () -> {
                currentView[0] = "alerts";
                chartsRow.setVisible(false); chartsRow.setManaged(false);
                dynamicArea.getChildren().clear();
                showAlertsInline(dynamicArea);
            };

            invCard.setOnMouseClicked(e -> { showInventoryView.run(); highlightCard(invCard, distCard, alertCard); });
            distCard.setOnMouseClicked(e -> { showDistributeView.run(); highlightCard(distCard, invCard, alertCard); });
            alertCard.setOnMouseClicked(e -> { showAlertsView.run(); highlightCard(alertCard, invCard, distCard); });

            highlightCard(null, invCard, distCard, alertCard);
            dynamicArea.getChildren().clear();
            chartsRow.setVisible(true); chartsRow.setManaged(true);

            // --- Charts Section ---

            // --- BAR CHART ---
            Label barTitle = new Label("Stock vs Threshold");
            barTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

            CategoryAxis xAxis = new CategoryAxis();
            xAxis.setLabel("Items");
            xAxis.setTickLabelRotation(-45);
            xAxis.setTickLabelFont(javafx.scene.text.Font.font("Segoe UI", 10));
            xAxis.setStyle("-fx-tick-label-fill: #333; -fx-border-color: #333 transparent transparent transparent; -fx-border-width: 1 0 0 0;");
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("Quantity");
            yAxis.setStyle("-fx-tick-label-fill: #333; -fx-border-color: transparent #333 transparent transparent; -fx-border-width: 0 1 0 0;");
            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
            barChart.setLegendVisible(false);
            barChart.setAnimated(true);
            barChart.setMinHeight(320);
            barChart.setPrefHeight(320);
            barChart.setCategoryGap(6);
            barChart.setBarGap(2);
            barChart.setTitle(null);

            XYChart.Series<String, Number> stockSeries = new XYChart.Series<>();
            stockSeries.setName("Current Stock");
            XYChart.Series<String, Number> thresholdSeries = new XYChart.Series<>();
            thresholdSeries.setName("Min Threshold");

            try {
                List<StockThreshold> thresholds = ctrl.getAllThresholds();
                for (InventoryStock s : stocks) {
                    String rawName = s.getItemName();
                    String shortName = rawName.contains(" ") ? rawName.substring(0, rawName.indexOf(' ')) : rawName;
                    if (shortName.length() > 10) shortName = shortName.substring(0, 10);
                    stockSeries.getData().add(new XYChart.Data<>(shortName, s.getQuantity()));
                    int minT = 0;
                    for (StockThreshold t : thresholds) {
                        if (t.getItemName().equalsIgnoreCase(s.getItemName())) { minT = t.getMinQuantity(); break; }
                    }
                    thresholdSeries.getData().add(new XYChart.Data<>(shortName, minT));
                }
            } catch (Exception ignored) {}
            barChart.getData().add(stockSeries);
            barChart.getData().add(thresholdSeries);

            // Apply colors and hover tooltips to bars
            javafx.application.Platform.runLater(() -> {
                for (XYChart.Data<String, Number> d : stockSeries.getData()) {
                    if (d.getNode() != null) {
                        d.getNode().setStyle("-fx-bar-fill: #4CAF50;");
                        Tooltip tt = new Tooltip(d.getXValue() + "\nStock: " + d.getYValue());
                        tt.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
                        Tooltip.install(d.getNode(), tt);
                        d.getNode().setOnMouseEntered(ev -> d.getNode().setStyle("-fx-bar-fill: #66BB6A; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 0);"));
                        d.getNode().setOnMouseExited(ev -> d.getNode().setStyle("-fx-bar-fill: #4CAF50;"));
                    }
                }
                for (XYChart.Data<String, Number> d : thresholdSeries.getData()) {
                    if (d.getNode() != null) {
                        d.getNode().setStyle("-fx-bar-fill: #FF9800;");
                        Tooltip tt = new Tooltip(d.getXValue() + "\nThreshold: " + d.getYValue());
                        tt.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
                        Tooltip.install(d.getNode(), tt);
                        d.getNode().setOnMouseEntered(ev -> d.getNode().setStyle("-fx-bar-fill: #FFA726; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 0);"));
                        d.getNode().setOnMouseExited(ev -> d.getNode().setStyle("-fx-bar-fill: #FF9800;"));
                    }
                }
            });

            // Manual legend for bar chart
            HBox barLegend = new HBox(25);
            barLegend.setAlignment(Pos.CENTER);
            barLegend.setPadding(new Insets(8, 0, 0, 0));
            HBox leg1 = new HBox(6); leg1.setAlignment(Pos.CENTER);
            Region dot1 = new Region(); dot1.setMinSize(14, 14); dot1.setMaxSize(14, 14);
            dot1.setStyle("-fx-background-color: #4CAF50; -fx-background-radius: 3;");
            Label lt1 = new Label("Current Stock"); lt1.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333;");
            leg1.getChildren().addAll(dot1, lt1);
            HBox leg2 = new HBox(6); leg2.setAlignment(Pos.CENTER);
            Region dot2 = new Region(); dot2.setMinSize(14, 14); dot2.setMaxSize(14, 14);
            dot2.setStyle("-fx-background-color: #FF9800; -fx-background-radius: 3;");
            Label lt2 = new Label("Min Threshold"); lt2.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333;");
            leg2.getChildren().addAll(dot2, lt2);
            barLegend.getChildren().addAll(leg1, leg2);

            VBox barCard = new VBox(8, barTitle, barChart, barLegend);
            barCard.setPadding(new Insets(15));
            barCard.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5);");

            // --- PIE CHART ---
            Label pieTitle = new Label("Inventory by Category");
            pieTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

            int seedCount = 0, fertCount = 0, pestCount = 0;
            for (InventoryStock s : stocks) {
                String n = s.getItemName().toLowerCase();
                if (n.contains("seed") || n.contains("sett")) seedCount += s.getQuantity();
                else if (isItemFertilizer(n)) fertCount += s.getQuantity();
                else if (isItemPesticide(n)) pestCount += s.getQuantity();
            }
            int totalQty = seedCount + fertCount + pestCount;
            final int fSeedCount = seedCount, fFertCount = fertCount, fPestCount = pestCount;
            
            String seedLbl = String.format("Seeds (%.1f%%)", totalQty > 0 ? (fSeedCount * 100.0 / totalQty) : 0);
            String fertLbl = String.format("Fertilizers (%.1f%%)", totalQty > 0 ? (fFertCount * 100.0 / totalQty) : 0);
            String pestLbl = String.format("Pesticides (%.1f%%)", totalQty > 0 ? (fPestCount * 100.0 / totalQty) : 0);

            PieChart pieChart = new PieChart(FXCollections.observableArrayList(
                new PieChart.Data(seedLbl, seedCount),
                new PieChart.Data(fertLbl, fertCount),
                new PieChart.Data(pestLbl, pestCount)
            ));
            pieChart.setLegendVisible(false);
            pieChart.setAnimated(true);
            pieChart.setMinHeight(280);
            pieChart.setPrefHeight(280);
            pieChart.setLabelsVisible(true);
            pieChart.setTitle(null);

            // Color pie slices and add hover tooltips
            String[] pieColors = {"#4CAF50", "#FF9800", "#2196F3"};
            String[] pieLabels = {seedLbl, fertLbl, pestLbl};
            int[] pieCounts = {fSeedCount, fFertCount, fPestCount};
            javafx.application.Platform.runLater(() -> {
                int idx = 0;
                for (PieChart.Data d : pieChart.getData()) {
                    if (d.getNode() != null) {
                        String clr = pieColors[idx];
                        d.getNode().setStyle("-fx-pie-color: " + clr + ";");
                        double pct = totalQty > 0 ? (pieCounts[idx] * 100.0 / totalQty) : 0;
                        Tooltip tt = new Tooltip(pieLabels[idx] + ": " + pieCounts[idx] + " units\n" + String.format("%.1f%%", pct));
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

            // Manual legend for pie chart
            HBox pieLegend = new HBox(20);
            pieLegend.setAlignment(Pos.CENTER);
            pieLegend.setPadding(new Insets(8, 0, 0, 0));
            for (int li = 0; li < 3; li++) {
                HBox legItem = new HBox(6); legItem.setAlignment(Pos.CENTER);
                Region legDot = new Region(); legDot.setMinSize(14, 14); legDot.setMaxSize(14, 14);
                legDot.setStyle("-fx-background-color: " + pieColors[li] + "; -fx-background-radius: 50;");
                Label legLbl = new Label(pieLabels[li] + " (" + pieCounts[li] + ")");
                legLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333;");
                legItem.getChildren().addAll(legDot, legLbl);
                pieLegend.getChildren().add(legItem);
            }

            VBox pieCard = new VBox(8, pieTitle, pieChart, pieLegend);
            pieCard.setPadding(new Insets(15));
            pieCard.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5);");

            chartsRow.getChildren().addAll(barCard, pieCard);
            HBox.setHgrow(barCard, Priority.ALWAYS);

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

    private void highlightCard(VBox active, VBox... others) {
        if (active != null) {
            active.setStyle("-fx-background-color: rgba(59,109,17,0.2); -fx-background-radius: 12; -fx-border-color: #3B6D11; -fx-border-width: 2; -fx-border-radius: 12; -fx-padding: 15; -fx-cursor: hand;");
        }
        for (VBox o : others) o.setStyle(ui.components.ImageBackground.GLASS_STYLE + " -fx-cursor: hand;");
    }

    private VBox createInventoryCard(InventoryStock s) {
        VBox card = new VBox(0);
        card.setPrefSize(220, 240);
        card.setStyle("-fx-background-color: #FAFAFA; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5); -fx-cursor: hand;");
        ImageView iv = new ImageView();
        try { iv.setImage(new Image(getCropImageUrl(s.getItemName()), 220, 140, false, true, true)); } catch(Exception ignored) {}
        iv.setFitWidth(220); iv.setFitHeight(140);
        Rectangle cl = new Rectangle(220, 140); cl.setArcWidth(24); cl.setArcHeight(24); iv.setClip(cl);
        VBox det = new VBox(5); det.setPadding(new Insets(15));
        Label nm = new Label(s.getItemName()); nm.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;"); nm.setWrapText(true);
        Label qt = new Label(s.getQuantity() + " " + s.getUnit());
        qt.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + (s.getQuantity() > 0 ? "#3B6D11" : "#D32F2F") + ";");
        det.getChildren().addAll(nm, qt); card.getChildren().addAll(iv, det);
        card.setOnMouseEntered(e -> { card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 8); -fx-cursor: hand;"); card.setTranslateY(-5); });
        card.setOnMouseExited(e -> { card.setStyle("-fx-background-color: #FAFAFA; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5); -fx-cursor: hand;"); card.setTranslateY(0); });
        return card;
    }

    private VBox createDistributeCard(SubsidyRequest r, InventoryController ctrl) {
        VBox card = new VBox(0);
        card.setPrefWidth(300);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5);");

        // Top Image
        ImageView iv = new ImageView();
        String itemName = r.getRequestedItemName();
        if (itemName == null) itemName = "farm";
        try { iv.setImage(new Image(getCropImageUrl(itemName), 300, 120, false, true, true)); } catch(Exception ignored) {}
        iv.setFitWidth(300); iv.setFitHeight(120);
        Rectangle cl = new Rectangle(300, 120); cl.setArcWidth(30); cl.setArcHeight(30); iv.setClip(cl);

        VBox det = new VBox(10);
        det.setPadding(new Insets(20));

        // Farmer Info
        VBox farmerInfo = new VBox(2);
        Label fn = new Label(r.getFarmerName() != null ? r.getFarmerName() : "Unknown Farmer");
        fn.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3B6D11;");
        Label ci = new Label("CNIC: " + r.getFarmerCnic());
        ci.setStyle("-fx-font-size: 12px; -fx-text-fill: #777;");
        farmerInfo.getChildren().addAll(fn, ci);

        det.getChildren().add(farmerInfo);
        det.getChildren().add(new Separator());

        // Item Details
        VBox itemInfo = new VBox(5);
        Label it = new Label("Item: " + (r.getRequestedItemName() != null ? r.getRequestedItemName() : "N/A"));
        it.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
        Label rq = new Label("Requested: " + r.getQuantity() + " units");
        rq.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
        itemInfo.getChildren().addAll(it, rq);
        det.getChildren().add(itemInfo);

        // Input & Button
        VBox actions = new VBox(10);
        actions.setPadding(new Insets(10, 0, 0, 0));
        
        Label ql = new Label("Quantity to Deliver:");
        ql.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        
        TextField qf = new TextField(String.valueOf(r.getQuantity()));
        qf.setPromptText("Qty to deliver"); 
        qf.setStyle("-fx-padding: 10; -fx-background-radius: 8; -fx-background-color: #F5F5F5; -fx-border-color: #DDD; -fx-border-radius: 8; -fx-font-size: 14px;");
        
        Button db = new Button("Distribute Now");
        db.setStyle("-fx-background-color: #3B6D11; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 14px;");
        db.setMaxWidth(Double.MAX_VALUE);
        db.setOnAction(e -> {
            try {
                int qty = Integer.parseInt(qf.getText().trim());
                if (qty <= 0) { showAlert("Validation Error", "Quantity must be positive."); return; }
                ctrl.recordDistribution(r.getRequestId(), session.getDistributorId(), qty, r.getRequestedItemId());
                showAlert("Success", "Distributed to " + r.getFarmerName());
                showOverview();
            } catch (NumberFormatException ex) { showAlert("Validation Error", "Enter valid number.");
            } catch (Exception ex) { showAlert("Error", ex.getMessage()); }
        });
        
        actions.getChildren().addAll(ql, qf, db);
        det.getChildren().add(actions);

        card.getChildren().addAll(iv, det);
        
        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setTranslateY(-5);
            card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 20, 0, 0, 10);");
        });
        card.setOnMouseExited(e -> {
            card.setTranslateY(0);
            card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5);");
        });

        return card;
    }

    private void showAlertsInline(VBox container) {
        Label sub = new Label("Active Stock Alerts");
        sub.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 0, 2);");
        
        container.getChildren().addAll(sub);

        try {
            InventoryController ctrl = new InventoryController();
            List<StockAlert> alerts = ctrl.getActiveAlerts();

            // Alert Filter Bar
            HBox alertFilter = new HBox(12);
            alertFilter.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 15; -fx-background-radius: 10;");
            alertFilter.setAlignment(Pos.CENTER_LEFT);
            ComboBox<String> catFilter = new ComboBox<>(FXCollections.observableArrayList("All Categories", "Seeds", "Fertilizers", "Pesticides"));
            catFilter.setValue("All Categories");
            Label countLabel = new Label();
            countLabel.setStyle("-fx-text-fill: #888; -fx-font-weight: bold;");

            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            Button backBtn = new Button("<- Back");
            backBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");
            backBtn.setOnAction(e -> showOverview());

            alertFilter.getChildren().addAll(new Label("Filter Alerts:"), catFilter, countLabel, sp, backBtn);
            container.getChildren().add(alertFilter);

            FlowPane grid = new FlowPane(); grid.setHgap(20); grid.setVgap(20);
            container.getChildren().add(grid);

            Runnable applyAlertFilter = () -> {
                grid.getChildren().clear();
                String cat = catFilter.getValue();
                int count = 0;
                for (StockAlert a : alerts) {
                    String name = a.getItemName().toLowerCase();
                    if ("Seeds".equals(cat) && !name.contains("seed") && !name.contains("sett")) continue;
                    if ("Fertilizers".equals(cat) && !isItemFertilizer(name)) continue;
                    if ("Pesticides".equals(cat) && !isItemPesticide(name)) continue;
                    
                    grid.getChildren().add(createAlertCard(a, ctrl));
                    count++;
                }
                countLabel.setText(count + " alerts found");
            };

            catFilter.setOnAction(e -> applyAlertFilter.run());
            applyAlertFilter.run();

            if (alerts.isEmpty()) {
                grid.getChildren().clear();
                Label ok = new Label("All stock levels are safely above minimum thresholds");
                ok.setStyle("-fx-font-size: 18px; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                VBox ec = new VBox(ok); ec.setPadding(new Insets(40)); ec.setAlignment(Pos.CENTER);
                ec.setStyle("-fx-background-color: #FAFAFA; -fx-background-radius: 12;");
                grid.getChildren().add(ec);
            }
        } catch (Exception ex) { container.getChildren().add(new Label("Error: " + ex.getMessage())); }
    }

    private VBox createAlertCard(StockAlert a, InventoryController ctrl) {
        VBox card = new VBox(0); card.setPrefSize(280, 300);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(211,47,47,0.5), 15, 0, 0, 0); -fx-border-color: #D32F2F; -fx-border-width: 2; -fx-border-radius: 12;");
        ImageView iv = new ImageView();
        try { iv.setImage(new Image(getCropImageUrl(a.getItemName()), 280, 100, false, true, true)); } catch(Exception ignored) {}
        iv.setFitWidth(280); iv.setFitHeight(100);
        Rectangle cl = new Rectangle(280, 100); cl.setArcWidth(24); cl.setArcHeight(24); iv.setClip(cl);
        VBox det = new VBox(8); det.setPadding(new Insets(12));
        Label nm = new Label("CRITICAL: " + a.getItemName()); nm.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #D32F2F;");
        Label qt = new Label("Current: " + a.getCurrentQty() + " (Min: " + a.getMinQuantity() + ")"); qt.setStyle("-fx-font-weight: bold;");
        TextField nf = new TextField(); nf.setPromptText("Acknowledgment Note..."); nf.setStyle("-fx-background-radius: 6; -fx-padding: 6;");
        Button ab = new Button("Acknowledge"); ab.setMaxWidth(Double.MAX_VALUE);
        ab.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8; -fx-cursor: hand;");
        ab.setOnAction(e -> {
            if (nf.getText().trim().isEmpty()) { showAlert("Validation Error", "Note is mandatory."); return; }
            try { ctrl.acknowledgeAlert(a.getAlertId(), nf.getText().trim(), session.getCurrentUserId()); showAlert("Success", "Alert Acknowledged"); showOverview(); } catch(Exception ex) { showAlert("Error", ex.getMessage()); }
        });
        det.getChildren().addAll(nm, qt, nf, ab); card.getChildren().addAll(iv, det);
        return card;
    }

    // --- UC-05: Add Inventory ---
    private void showAddInventory() {
        VBox content = new VBox(25); content.setPadding(new Insets(40));
        Label title = new Label("Add Inventory Stock");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 0, 2);");
        HBox split = new HBox(40);

        // Left: Image + Details panel
        VBox leftPanel = new VBox(0);
        leftPanel.setPrefWidth(350);

        VBox imgCard = new VBox();
        imgCard.setPrefSize(350, 220);
        imgCard.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15 15 0 0; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5);");
        imgCard.setAlignment(Pos.CENTER);
        ImageView iv = new ImageView();
        try { iv.setImage(new Image(getCropImageUrl("farm"), 350, 220, false, true, true)); } catch(Exception ignored) {}
        Rectangle cl = new Rectangle(350, 220); cl.setArcWidth(30); cl.setArcHeight(30); iv.setClip(cl);
        imgCard.getChildren().add(iv);

        VBox detailsPanel = new VBox(8);
        detailsPanel.setPadding(new Insets(18));
        detailsPanel.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 0 0 15 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3);");

        Label detTitle = new Label("Select an item to view details");
        detTitle.setStyle("-fx-font-size: 15px; -fx-text-fill: #999; -fx-font-style: italic;");
        Label detName = new Label(""); detName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #222;"); detName.setVisible(false);
        Label detCategory = new Label(""); detCategory.setVisible(false);
        Label detUnit = new Label(""); detUnit.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;"); detUnit.setVisible(false);
        Label detStock = new Label(""); detStock.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;"); detStock.setVisible(false);
        Label detThreshold = new Label(""); detThreshold.setStyle("-fx-font-size: 13px; -fx-text-fill: #777;"); detThreshold.setVisible(false);
        Label detDesc = new Label(""); detDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: #888; -fx-wrap-text: true;"); detDesc.setVisible(false); detDesc.setWrapText(true);
        detailsPanel.getChildren().addAll(detTitle, detName, detCategory, detUnit, detStock, detThreshold, detDesc);

        leftPanel.getChildren().addAll(imgCard, detailsPanel);

        // Right: Form (no current stock here)
        VBox form = new VBox(20); form.setPadding(new Insets(30));
        form.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5);");
        form.setPrefWidth(400);
        ComboBox<ItemType> itemBox = new ComboBox<>(); itemBox.setPromptText("Select Item Type"); itemBox.setPrefWidth(Double.MAX_VALUE);
        TextField qf = new TextField(); qf.setPromptText("Quantity to Add"); qf.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-radius: 6;");
        Button sb = new Button("Add Stock"); sb.setMaxWidth(Double.MAX_VALUE);
        sb.setStyle("-fx-background-color: #3B6D11; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 12; -fx-background-radius: 6; -fx-cursor: hand;");
        form.getChildren().addAll(new Label("Select Item"), itemBox, new Separator(), new Label("Quantity"), qf, sb);
        split.getChildren().addAll(leftPanel, form);
        try {
            InventoryController ctrl = new InventoryController();
            List<StockThreshold> thresholds = ctrl.getAllThresholds();
            itemBox.setItems(FXCollections.observableArrayList(ctrl.getAllItemTypes()));
            itemBox.setConverter(new javafx.util.StringConverter<ItemType>() {
                public String toString(ItemType i) { return i != null ? i.getName() + " (" + i.getUnit() + ")" : ""; }
                public ItemType fromString(String s) { return null; }
            });
            itemBox.setOnAction(e -> {
                if (itemBox.getValue() != null) {
                    ItemType sel = itemBox.getValue();
                    try {
                        iv.setImage(new Image(getCropImageUrl(sel.getName()), 350, 220, false, true, true));
                        InventoryStock st = ctrl.getAllStock().stream().filter(s -> s.getItemTypeId().equals(sel.getItemTypeId())).findFirst().orElse(null);
                        int curQty = st != null ? st.getQuantity() : 0;
                        String unit = st != null ? st.getUnit() : sel.getUnit();

                        // Find threshold
                        int minThreshold = 0;
                        for (StockThreshold t : thresholds) {
                            if (t.getItemName().equalsIgnoreCase(sel.getName())) { minThreshold = t.getMinQuantity(); break; }
                        }

                        detTitle.setVisible(false);
                        detName.setText(sel.getName()); detName.setVisible(true);
                        // Category badge
                        String nameLc = sel.getName().toLowerCase();
                        String catLabel; String catColor;
                        if (nameLc.contains("seed") || nameLc.contains("sett")) { catLabel = "SEED"; catColor = "#4CAF50"; }
                        else if (isItemFertilizer(nameLc)) { catLabel = "FERTILIZER"; catColor = "#FF9800"; }
                        else if (isItemPesticide(nameLc)) { catLabel = "PESTICIDE"; catColor = "#2196F3"; }
                        else { catLabel = "OTHER"; catColor = "#999"; }
                        detCategory.setText(catLabel);
                        detCategory.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: " + catColor + "; -fx-padding: 3 10; -fx-background-radius: 10;");
                        detCategory.setVisible(true);
                        detUnit.setText("Unit: " + unit); detUnit.setVisible(true);
                        detDesc.setText(sel.getDescription() != null ? sel.getDescription() : ""); detDesc.setVisible(sel.getDescription() != null);
                        detStock.setText("Current Stock: " + curQty + " " + unit);
                        detStock.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + (curQty > 0 ? "#3B6D11" : "#D32F2F") + ";");
                        detStock.setVisible(true);
                        if (minThreshold > 0) {
                            detThreshold.setText("Min Threshold: " + minThreshold + " " + unit + (curQty < minThreshold ? "  (BELOW!)" : ""));
                            detThreshold.setStyle("-fx-font-size: 13px; -fx-text-fill: " + (curQty < minThreshold ? "#D32F2F; -fx-font-weight: bold" : "#777") + ";");
                        } else {
                            detThreshold.setText("Threshold: Not configured");
                            detThreshold.setStyle("-fx-font-size: 13px; -fx-text-fill: #999;");
                        }
                        detThreshold.setVisible(true);
                    } catch (Exception ex) { detTitle.setText("Error loading details"); }
                }
            });
            sb.setOnAction(e -> {
                if (itemBox.getValue() == null) { showAlert("Validation Error", "Select an item."); return; }
                try {
                    int qty = Integer.parseInt(qf.getText().trim());
                    if (qty <= 0) { showAlert("Validation Error", "Quantity must be positive."); return; }
                    ctrl.addInventoryStock(itemBox.getValue().getItemTypeId(), qty, session.getCurrentUserId());
                    showAlert("Success", "Stock added!"); showAddInventory();
                } catch (NumberFormatException ex) { showAlert("Validation Error", "Enter a valid number.");
                } catch (Exception ex) { showAlert("Error", ex.getMessage()); }
            });
        } catch (Exception ex) { form.getChildren().add(new Label("Error: " + ex.getMessage())); }
        content.getChildren().addAll(title, split);
        mainLayout.setCenter(content);
    }

    // --- UC-11: Search Requests ---
    private void showSearch() {
        VBox content = new VBox(25); content.setPadding(new Insets(40));
        Label title = new Label("Search Subsidy Requests");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 0, 2);");
        HBox form = new HBox(15); form.setAlignment(Pos.CENTER_LEFT);
        form.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 20; -fx-background-radius: 12;");
        TextField nf = new TextField(); nf.setPromptText("Farmer Name");
        TextField cf = new TextField(); cf.setPromptText("CNIC");
        ComboBox<String> sb = new ComboBox<>(FXCollections.observableArrayList("All Distributions","APPROVED","DISTRIBUTED"));
        sb.setValue("All Distributions");
        Button sBtn = new Button("Search");
        sBtn.setStyle("-fx-background-color: #3B6D11; -fx-text-fill: white; -fx-padding: 8 20; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 6;");
        form.getChildren().addAll(nf, cf, sb, sBtn);
        ScrollPane scroll = new ScrollPane(); scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        sBtn.setOnAction(e -> {
            try {
                InventoryController ctrl = new InventoryController();
                String statusFilter = sb.getValue();
                List<SubsidyRequest> res;
                
                if (statusFilter.equals("All Distributions")) {
                    // Fetch all, then manually filter out PENDING/REJECTED for the distributor's view
                    List<SubsidyRequest> all = ctrl.searchRequests(nf.getText().trim(), cf.getText().trim(), null, "ALL", null);
                    res = all.stream()
                             .filter(r -> r.getStatus().equals("APPROVED") || r.getStatus().equals("DISTRIBUTED"))
                             .toList();
                } else {
                    res = ctrl.searchRequests(nf.getText().trim(), cf.getText().trim(), null, statusFilter, null);
                }
                FlowPane grid = new FlowPane(); grid.setHgap(20); grid.setVgap(20); grid.setPadding(new Insets(20, 0, 0, 0));
                for (SubsidyRequest r : res) {
                    VBox card = new VBox(0);
                    card.setPrefWidth(280);
                    card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 4);");

                    // Small top header with status color
                    String status = r.getStatus();
                    String statusColor = status.equals("APPROVED") ? "#4CAF50" : status.equals("REJECTED") ? "#D32F2F" : status.equals("DISTRIBUTED") ? "#2196F3" : "#FF9800";
                    Region header = new Region();
                    header.setPrefHeight(6);
                    header.setStyle("-fx-background-color: " + statusColor + "; -fx-background-radius: 12 12 0 0;");

                    VBox cardContent = new VBox(10);
                    cardContent.setPadding(new Insets(15));

                    Label cn = new Label(r.getFarmerName());
                    cn.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #222;");
                    
                    Label cc = new Label("CNIC: " + r.getFarmerCnic());
                    cc.setStyle("-fx-font-size: 12px; -fx-text-fill: #777;");
                    
                    VBox details = new VBox(4);
                    details.setStyle("-fx-background-color: #F9F9F9; -fx-padding: 10; -fx-background-radius: 8;");
                    Label ci = new Label("Item: " + (r.getRequestedItemName() != null ? r.getRequestedItemName() : "N/A"));
                    ci.setStyle("-fx-font-size: 13px; -fx-text-fill: #444;");
                    Label cq = new Label("Qty: " + r.getQuantity());
                    cq.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #444;");
                    details.getChildren().addAll(ci, cq);

                    HBox footer = new HBox(10);
                    footer.setAlignment(Pos.CENTER_LEFT);
                    Label cs = new Label(status);
                    cs.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: " + statusColor + "; -fx-padding: 4 10; -fx-background-radius: 10;");
                    footer.getChildren().add(cs);

                    cardContent.getChildren().addAll(cn, cc, details, footer);
                    card.getChildren().addAll(header, cardContent);
                    
                    card.setOnMouseEntered(ev -> card.setTranslateY(-3));
                    card.setOnMouseExited(ev -> card.setTranslateY(0));
                    
                    grid.getChildren().add(card);
                }
                scroll.setContent(grid);
            } catch (Exception ex) { showAlert("Error", ex.getMessage()); }
        });
        content.getChildren().addAll(title, form, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        mainLayout.setCenter(content);
    }

    // --- UC-12: Thresholds with images ---
    private void showThresholds() {
        VBox content = new VBox(25); content.setPadding(new Insets(40));
        Label title = new Label("Configure Stock Thresholds");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 0, 2);");
        HBox split = new HBox(30);
        VBox form = new VBox(20); form.setPadding(new Insets(30));
        form.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5);");
        form.setPrefWidth(350);
        ComboBox<ItemType> ib = new ComboBox<>(); ib.setPromptText("Select Item Type"); ib.setPrefWidth(Double.MAX_VALUE);
        TextField mf = new TextField(); mf.setPromptText("Minimum Quantity"); mf.setStyle("-fx-padding: 10; -fx-background-radius: 6;");
        Button svb = new Button("Save Threshold"); svb.setMaxWidth(Double.MAX_VALUE);
        svb.setStyle("-fx-background-color: #3B6D11; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;");
        form.getChildren().addAll(new Label("Set Alert Threshold"), ib, mf, svb);
        ScrollPane scroll = new ScrollPane(); scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        try {
            InventoryController ctrl = new InventoryController();
            ib.setItems(FXCollections.observableArrayList(ctrl.getAllItemTypes()));
            ib.setConverter(new javafx.util.StringConverter<ItemType>() {
                public String toString(ItemType i) { return i != null ? i.getName() + " (" + i.getUnit() + ")" : ""; }
                public ItemType fromString(String s) { return null; }
            });
            svb.setOnAction(e -> {
                if (ib.getValue() == null || mf.getText().trim().isEmpty()) { showAlert("Validation Error", "All fields required."); return; }
                try {
                    int min = Integer.parseInt(mf.getText().trim());
                    if (min < 0) { showAlert("Validation Error", "Cannot be negative."); return; }
                    String w = ctrl.configureThreshold(ib.getValue().getItemTypeId(), min, session.getCurrentUserId());
                    String msg = "Threshold saved."; if (w != null) msg += " " + w;
                    showAlert("Success", msg); showThresholds();
                } catch (NumberFormatException ex) { showAlert("Validation Error", "Enter valid number.");
                } catch (Exception ex) { showAlert("Error", ex.getMessage()); }
            });
            FlowPane grid = new FlowPane(); grid.setHgap(15); grid.setVgap(15);
            for (StockThreshold t : ctrl.getAllThresholds()) {
                VBox card = new VBox(0); card.setPrefWidth(220);
                card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
                ImageView tiv = new ImageView();
                try { tiv.setImage(new Image(getCropImageUrl(t.getItemName()), 220, 80, false, true, true)); } catch(Exception ignored) {}
                tiv.setFitWidth(220); tiv.setFitHeight(80);
                Rectangle tcl = new Rectangle(220, 80); tcl.setArcWidth(24); tcl.setArcHeight(24); tiv.setClip(tcl);
                VBox td = new VBox(5); td.setPadding(new Insets(10));
                Label tn = new Label(t.getItemName()); tn.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #222;");
                Label tm = new Label("Min: " + t.getMinQuantity()); tm.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
                Label tc = new Label("Current: " + t.getCurrentQty());
                tc.setStyle("-fx-text-fill: " + (t.getCurrentQty() < t.getMinQuantity() ? "#D32F2F" : "#3B6D11") + "; -fx-font-weight: bold; -fx-font-size: 13px;");
                td.getChildren().addAll(tn, tm, tc); card.getChildren().addAll(tiv, td); grid.getChildren().add(card);
            }
            scroll.setContent(grid);
        } catch (Exception ex) { form.getChildren().add(new Label("Error: " + ex.getMessage())); }
        split.getChildren().addAll(form, scroll); HBox.setHgrow(scroll, Priority.ALWAYS);
        content.getChildren().addAll(title, split);
        mainLayout.setCenter(content);
    }

    // --- UC-08: Generate Report ---
    private void showReport() {
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

        // Draw a report icon using shapes
        javafx.scene.layout.StackPane reportIcon = new javafx.scene.layout.StackPane();
        // Clipboard background
        Rectangle clipboard = new Rectangle(180, 220);
        clipboard.setArcWidth(20); clipboard.setArcHeight(20);
        clipboard.setFill(Color.web("#E8F5E9")); clipboard.setStroke(Color.web("#3B6D11")); clipboard.setStrokeWidth(3);
        // Clip tab
        Rectangle clipTab = new Rectangle(60, 16);
        clipTab.setArcWidth(8); clipTab.setArcHeight(8);
        clipTab.setFill(Color.web("#3B6D11"));
        clipTab.setTranslateY(-118);
        // Bar chart lines inside clipboard
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
        // Check mark
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
        form.setPadding(new Insets(30));
        form.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5);");
        form.setPrefWidth(500);

        int[] step = {1}; String[] state = {"", ""};
        Runnable[] refreshUI = new Runnable[1];

        refreshUI[0] = () -> {
            form.getChildren().clear();

            // Progress indicator inside card
            HBox progBar = new HBox(10);
            progBar.setAlignment(Pos.CENTER);
            progBar.setPadding(new Insets(0, 0, 15, 0));
            String[] sLabels = {"Type", "Format", "Generate"};
            for (int i = 0; i < 3; i++) {
                VBox sv = new VBox(4); sv.setAlignment(Pos.CENTER);
                javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(14, (i+1 <= step[0]) ? Color.web("#3B6D11") : Color.web("#ddd"));
                Label sl = new Label(sLabels[i]);
                sl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + (i+1 <= step[0] ? "#3B6D11" : "#aaa") + ";");
                sv.getChildren().addAll(dot, sl);
                HBox.setHgrow(sv, Priority.ALWAYS);
                progBar.getChildren().add(sv);
                if (i < 2) {
                    Region ln = new Region(); ln.setMinHeight(2); ln.setMaxHeight(2); ln.setPrefWidth(60);
                    ln.setStyle("-fx-background-color: " + (i+1 < step[0] ? "#3B6D11" : "#ddd") + ";");
                    HBox lb = new HBox(ln); lb.setAlignment(Pos.CENTER); lb.setPadding(new Insets(12, 0, 0, 0));
                    progBar.getChildren().add(lb);
                }
            }
            form.getChildren().add(progBar);
            form.getChildren().add(new Separator());

            if (step[0] == 1) {
                Label tl = new Label("SELECT REPORT TYPE");
                tl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

                VBox typeOpts = new VBox(12);
                String[] types = {"Distribution Report", "Subsidy Utilization Report"};
                for (String tt : types) {
                    HBox opt = new HBox(12);
                    opt.setAlignment(Pos.CENTER_LEFT);
                    opt.setPadding(new Insets(15, 20, 15, 20));
                    boolean selected = state[0].equalsIgnoreCase(tt);
                    opt.setStyle("-fx-border-color: " + (selected ? "#3B6D11" : "#ddd") + "; -fx-border-width: 2; " +
                        "-fx-background-color: " + (selected ? "rgba(59,109,17,0.08)" : "#FAFAFA") + "; " +
                        "-fx-background-radius: 10; -fx-border-radius: 10; -fx-cursor: hand;");

                    javafx.scene.shape.Circle radio = new javafx.scene.shape.Circle(8, selected ? Color.web("#3B6D11") : Color.web("#ccc"));
                    Label l = new Label(tt);
                    l.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #333;");
                    opt.getChildren().addAll(radio, l);
                    opt.setOnMouseClicked(e -> { state[0] = tt; refreshUI[0].run(); });
                    typeOpts.getChildren().add(opt);
                }

                Button nb = new Button("Next  \u2192");
                nb.setDisable(state[0].isEmpty());
                nb.setStyle("-fx-background-color: #3B6D11; -fx-text-fill: white; -fx-padding: 12 30; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-cursor: hand;");
                nb.setOnAction(e -> { step[0] = 2; refreshUI[0].run(); });
                HBox br = new HBox(nb); br.setAlignment(Pos.CENTER_RIGHT); br.setPadding(new Insets(10, 0, 0, 0));
                form.getChildren().addAll(tl, typeOpts, br);

            } else if (step[0] == 2) {
                Label fl = new Label("SELECT FORMAT");
                fl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

                HBox fmtOpts = new HBox(15);
                String[] fmts = {"PDF", "CSV"};
                for (String ff : fmts) {
                    VBox opt = new VBox(8);
                    opt.setAlignment(Pos.CENTER);
                    opt.setPadding(new Insets(25, 40, 25, 40));
                    boolean selected = state[1].equals(ff);
                    opt.setStyle("-fx-border-color: " + (selected ? "#3B6D11" : "#ddd") + "; -fx-border-width: 2; " +
                        "-fx-background-color: " + (selected ? "rgba(59,109,17,0.08)" : "#FAFAFA") + "; " +
                        "-fx-background-radius: 10; -fx-border-radius: 10; -fx-cursor: hand;");

                    Label icon = new Label(ff.equals("PDF") ? "\uD83D\uDCC4" : "\uD83D\uDCCA");
                    icon.setStyle("-fx-font-size: 28px;");
                    Label l = new Label(ff);
                    l.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #333;");
                    opt.getChildren().addAll(icon, l);
                    HBox.setHgrow(opt, Priority.ALWAYS);
                    opt.setOnMouseClicked(e -> { state[1] = ff; refreshUI[0].run(); });
                    fmtOpts.getChildren().add(opt);
                }

                Button bb = new Button("\u2190  Back");
                bb.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-border-color: #ccc; -fx-border-width: 1.5; -fx-border-radius: 8; -fx-padding: 12 25; -fx-cursor: hand; -fx-font-weight: bold;");
                bb.setOnAction(e -> { step[0] = 1; refreshUI[0].run(); });
                Button nb = new Button("Next  \u2192");
                nb.setDisable(state[1].isEmpty());
                nb.setStyle("-fx-background-color: #3B6D11; -fx-text-fill: white; -fx-padding: 12 30; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-cursor: hand;");
                nb.setOnAction(e -> { step[0] = 3; refreshUI[0].run(); });
                HBox br = new HBox(15, bb, nb); br.setAlignment(Pos.CENTER_RIGHT); br.setPadding(new Insets(10, 0, 0, 0));
                form.getChildren().addAll(fl, fmtOpts, br);

            } else if (step[0] == 3) {
                Label rl = new Label("REVIEW & GENERATE");
                rl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

                VBox summary = new VBox(12);
                summary.setPadding(new Insets(20));
                summary.setStyle("-fx-background-color: #F5F8F0; -fx-border-color: #C8E6C9; -fx-border-radius: 10; -fx-background-radius: 10;");

                HBox row1 = new HBox(10);
                Label k1 = new Label("Report Type:"); k1.setStyle("-fx-text-fill: #777; -fx-font-size: 14px; -fx-min-width: 120;");
                Label v1 = new Label(state[0]); v1.setStyle("-fx-font-weight: bold; -fx-text-fill: #222; -fx-font-size: 14px;");
                row1.getChildren().addAll(k1, v1);

                HBox row2 = new HBox(10);
                Label k2 = new Label("Format:"); k2.setStyle("-fx-text-fill: #777; -fx-font-size: 14px; -fx-min-width: 120;");
                Label v2 = new Label(state[1]); v2.setStyle("-fx-font-weight: bold; -fx-text-fill: #222; -fx-font-size: 14px;");
                row2.getChildren().addAll(k2, v2);

                HBox row3 = new HBox(10);
                Label k3 = new Label("Generated By:"); k3.setStyle("-fx-text-fill: #777; -fx-font-size: 14px; -fx-min-width: 120;");
                Label v3 = new Label(session.getCurrentUsername()); v3.setStyle("-fx-font-weight: bold; -fx-text-fill: #222; -fx-font-size: 14px;");
                row3.getChildren().addAll(k3, v3);

                summary.getChildren().addAll(row1, row2, row3);

                Button bb = new Button("\u2190  Edit");
                bb.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-border-color: #ccc; -fx-border-width: 1.5; -fx-border-radius: 8; -fx-padding: 12 25; -fx-cursor: hand; -fx-font-weight: bold;");
                bb.setOnAction(e -> { step[0] = 2; refreshUI[0].run(); });
                Button gb = new Button("Generate " + state[1] + "  \u2713");
                gb.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-padding: 12 30; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-cursor: hand;");
                gb.setOnAction(e -> {
                    try {
                        ReportController rc = new ReportController();
                        java.util.Map<String, Object> filters = new java.util.HashMap<>();
                        rc.generateReport(state[0], state[1], filters, session.getDistributorId(), "DISTRIBUTOR");
                        String msg = state[1].equalsIgnoreCase("PDF") ? 
                            state[0] + " generated successfully as a PDF file." : 
                            state[0] + " generated successfully as a CSV file.";
                        showAlert("Success", msg + " Saved in reportdistributor directory.");
                        step[0] = 1; state[0]=""; state[1]=""; refreshUI[0].run();
                    } catch (Exception ex) { showAlert("Error", ex.getMessage()); }
                });
                HBox br = new HBox(15, bb, gb); br.setAlignment(Pos.CENTER_RIGHT); br.setPadding(new Insets(10, 0, 0, 0));
                form.getChildren().addAll(rl, summary, br);
            }
        };
        refreshUI[0].run();

        splitLayout.getChildren().addAll(imgCard, form);
        HBox.setHgrow(form, Priority.ALWAYS);
        content.getChildren().addAll(title, splitLayout);
        mainLayout.setCenter(content);
    }

    // --- Category Helpers ---
    private boolean isItemFertilizer(String name) {
        return name.contains("urea") || name.contains("dap") || name.contains("npk") || name.contains("ssp")
            || name.contains("sop") || name.contains("zinc") || name.contains("ammonium") || name.contains("sulphate")
            || name.contains("sulfate") || name.contains("fertiliz");
    }

    private boolean isItemPesticide(String name) {
        return name.contains("confidor") || name.contains("mospilan") || name.contains("sectin") || name.contains("talstar")
            || name.contains("fame") || name.contains("machete") || name.contains("glyphosate") || name.contains("curacron")
            || name.contains("match") || name.contains("pest") || name.contains("insect") || name.contains("herbi") || name.contains("fungi");
    }

    // --- Image URL Mapper (covers all 25 item types) ---
    private String getCropImageUrl(String itemName) {
        String n = itemName.toLowerCase();
        String f = "farm.png";
        // Seeds (10)
        if (n.contains("wheat")) f = "wheat.png";
        else if (n.contains("barley")) f = "barley.png";
        else if (n.contains("gram") || n.contains("chana") || n.contains("chickpea")) f = "gram.png";
        else if (n.contains("mustard")) f = "mustard.png";
        else if (n.contains("peas") || n.contains("pea")) f = "peas.png";
        else if (n.contains("rice") || n.contains("paddy")) f = "rice.png";
        else if (n.contains("cotton")) f = "cotton.png";
        else if (n.contains("maize") || n.contains("corn")) f = "maize.png";
        else if (n.contains("sugarcane") || n.contains("sett")) f = "sugarcane.png";
        else if (n.contains("bajra") || n.contains("millet")) f = "bajra.png";
        // Fertilizers (7)
        else if (n.contains("urea")) f = "urea.png";
        else if (n.contains("dap")) f = "dap.png";
        else if (n.contains("npk") || n.contains("ssp") || n.contains("sop") || n.contains("zinc") || n.contains("ammonium") || n.contains("sulphate") || n.contains("sulfate")) f = "fertilizer.png";
        // Pesticides (8)
        else if (n.contains("confidor") || n.contains("mospilan") || n.contains("sectin") || n.contains("talstar") || n.contains("fame") || n.contains("machete") || n.contains("glyphosate") || n.contains("curacron") || n.contains("match") || n.contains("pest") || n.contains("insect") || n.contains("herbi") || n.contains("fungi")) f = "pesticide.png";
        else if (n.contains("seed")) f = "seeds.png";
        return new java.io.File("resources/images/" + f).toURI().toString();
    }

    // --- Helpers ---
    private void setActiveNav(Button btn) {
        if (activeBtn[0] != null) {
            activeBtn[0].setStyle("-fx-background-color: transparent; -fx-text-fill: #D4E6A5; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-cursor: hand; -fx-background-radius: 6;");
        }
        activeBtn[0] = btn;
        btn.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 12; -fx-cursor: hand; -fx-background-radius: 6; -fx-border-color: transparent transparent white transparent; -fx-border-width: 0 0 3 0;");
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

    private VBox createClickableStatCard(String label, String value, String color) {
        VBox card = new VBox(5); card.setPadding(new Insets(15)); card.setAlignment(Pos.CENTER); card.setPrefWidth(180);
        card.setStyle(ui.components.ImageBackground.GLASS_STYLE + " -fx-cursor: hand;");
        Label val = new Label(value); val.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label lbl = new Label(label); lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #666; -fx-font-weight: bold;");
        card.getChildren().addAll(val, lbl);
        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited(e -> card.setTranslateY(0));
        return card;
    }

    private void showAlert(String titleText, String msg) {
        if (scene != null && scene.getRoot() instanceof StackPane) {
            boolean isError = titleText.equals("Error") || titleText.equals("Validation") || titleText.equals("Validation Error");
            ui.components.CustomAlert.show((StackPane) scene.getRoot(), titleText, msg, isError);
        } else {
            Alert a = new Alert(titleText.equals("Error") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
            a.setTitle(titleText); a.setContentText(msg); a.showAndWait();
        }
    }

    public Scene getScene() {
        ui.components.ImageBackground.applyTheme(scene);
        return scene;
    }
}
