package ui;

import business.model.UserAccount;
import business.model.Farmer;
import data.UserAccountDAO;
import data.FarmerDAO;
import ui.farmer.FarmerDashboard;
import ui.officer.OfficerDashboard;
import ui.distributor.DistributorDashboard;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import javafx.stage.Stage;
import ui.components.CustomAlert;

/**
 * Login View - first screen shown to users.
 * Routes to appropriate dashboard based on user role.
 */
public class LoginView {

    private final Scene scene;

    public LoginView(Stage stage) {

        // --- Shared background layer ---
        StackPane root = new StackPane();
        ui.components.ImageBackground.applyTo(root);

        // --- Login card (glassmorphism) ---
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(420);
        card.setPadding(new Insets(40));
        card.setStyle(
            "-fx-background-color: rgba(0, 30, 0, 0.7);" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: rgba(255, 255, 255, 0.3);" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 8);"
        );

        // Logo / Title
        Label logo = new Label("\uD83C\uDF3E SeedWise");
        logo.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");

        Label subtitle = new Label("Agricultural Subsidy & Input Management");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.8);");

        // Separator
        Separator sep = new Separator();
        sep.setStyle("-fx-padding: 5 0 5 0;");
        sep.setOpacity(0.4);

        // Username
        Label userLabel = new Label("Username");
        userLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #FFFFFF; -fx-font-weight: bold;");
        TextField userField = new TextField();
        userField.setPromptText("Enter your username");
        userField.setStyle(
            "-fx-padding: 10; -fx-font-size: 13px; -fx-background-radius: 8;" +
            "-fx-background-color: rgba(0,0,0,0.3); -fx-text-fill: white;" +
            "-fx-prompt-text-fill: rgba(255,255,255,0.5); -fx-border-color: rgba(255,255,255,0.2);" +
            "-fx-border-radius: 8;"
        );

        // Password
        Label passLabel = new Label("Password");
        passLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #FFFFFF; -fx-font-weight: bold;");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter your password");
        passField.setStyle(
            "-fx-padding: 10; -fx-font-size: 13px; -fx-background-radius: 8;" +
            "-fx-background-color: rgba(0,0,0,0.3); -fx-text-fill: white;" +
            "-fx-prompt-text-fill: rgba(255,255,255,0.5); -fx-border-color: rgba(255,255,255,0.2);" +
            "-fx-border-radius: 8;"
        );

        // Error label removed, using interactive Alert dialogs instead

        // Login button
        Button loginBtn = new Button("Sign In");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #43A047, #66BB6A);" +
            "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-padding: 12; -fx-cursor: hand; -fx-background-radius: 8;"
        );

        // Register link
        Hyperlink registerLink = new Hyperlink("New farmer? Register here");
        registerLink.setStyle("-fx-font-size: 12px; -fx-text-fill: #A5D6A7;");

        card.getChildren().addAll(logo, subtitle, sep, userLabel, userField,
                                  passLabel, passField, loginBtn, registerLink);

        // --- Center wrapper for the card ---
        VBox centerWrapper = new VBox(card);
        centerWrapper.setAlignment(Pos.CENTER);
        root.getChildren().add(centerWrapper);

        // --- Event handlers ---

        loginBtn.setOnAction(e -> {
            String username = userField.getText().trim();
            String password = passField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                CustomAlert.show(root, "Login Failed", "Please enter both username and password.", true);
                return;
            }

            try {
                UserAccountDAO userDAO = new UserAccountDAO();
                UserAccount user = userDAO.authenticate(username, password);

                if (user != null) {
                    // Set session
                    SessionManager session = SessionManager.getInstance();
                    session.login(user.getUserId(), user.getUsername(), user.getRole());

                    // Route to dashboard based on role
                    switch (user.getRole()) {
                        case "FARMER":
                            FarmerDAO farmerDAO = new FarmerDAO();
                            Farmer farmer = farmerDAO.findByUserId(user.getUserId());
                            if (farmer != null) session.setFarmerId(farmer.getFarmerId());
                            stage.setScene(new FarmerDashboard(stage).getScene());
                            break;
                        case "OFFICER":
                            stage.setScene(new OfficerDashboard(stage).getScene());
                            break;
                        case "DISTRIBUTOR":
                            data.DistributorDAO distDAO = new data.DistributorDAO();
                            String distId = distDAO.findByUserId(user.getUserId());
                            if (distId != null) session.setDistributorId(distId);
                            stage.setScene(new DistributorDashboard(stage).getScene());
                            break;
                    }
                } else {
                    CustomAlert.show(root, "Login Failed", "Invalid username or password.", true);
                }
            } catch (Exception ex) {
                CustomAlert.show(root, "Connection Error", ex.getMessage(), true);
                ex.printStackTrace();
            }
        });

        registerLink.setOnAction(e -> {
            stage.setScene(new RegisterFarmerView(stage).getScene());
        });

        // Enter key triggers login
        passField.setOnAction(e -> loginBtn.fire());

        this.scene = new Scene(root, 1200, 800);
    }

    public Scene getScene() { 
        ui.components.ImageBackground.applyTheme(scene);
        return scene; 
    }
}
