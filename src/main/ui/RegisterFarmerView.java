package ui;

import business.controller.SubsidyRequestController;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import ui.components.CustomAlert;

/**
 * UC-01 - Register Farmer View.
 * Fields: Full Name, CNIC, Contact, Address, Username, Password, Confirm Password.
 */
public class RegisterFarmerView {

    private final Scene scene;

    public RegisterFarmerView(Stage stage) {

        // --- Shared background layer ---
        StackPane root = new StackPane();
        ui.components.ImageBackground.applyTo(root);

        // --- Registration card (glassmorphism) ---
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(470);
        card.setPadding(new Insets(35));
        card.setStyle(
            "-fx-background-color: rgba(0, 30, 0, 0.7);" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: rgba(255, 255, 255, 0.3);" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 8);"
        );

        Label title = new Label("\uD83C\uDF3E Register as Farmer");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");

        // Fields
        TextField nameField = createField("Full Name");
        TextField cnicField = createField("CNIC (XXXXX-XXXXXXX-X)");
        TextField contactField = createField("Contact Number (XXXX-XXXXXXX)");
        TextField addressField = createField("Address");
        TextField usernameField = createField("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle(
            "-fx-padding: 10; -fx-font-size: 13px; -fx-background-radius: 8;" +
            "-fx-background-color: rgba(0,0,0,0.3); -fx-text-fill: white;" +
            "-fx-prompt-text-fill: rgba(255,255,255,0.5); -fx-border-color: rgba(255,255,255,0.2);" +
            "-fx-border-radius: 8;"
        );
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm Password");
        confirmField.setStyle(
            "-fx-padding: 10; -fx-font-size: 13px; -fx-background-radius: 8;" +
            "-fx-background-color: rgba(0,0,0,0.3); -fx-text-fill: white;" +
            "-fx-prompt-text-fill: rgba(255,255,255,0.5); -fx-border-color: rgba(255,255,255,0.2);" +
            "-fx-border-radius: 8;"
        );

        // Error label removed, using interactive Alert dialogs instead

        Button registerBtn = new Button("Register");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #43A047, #66BB6A);" +
            "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-padding: 12; -fx-cursor: hand; -fx-background-radius: 8;"
        );

        Hyperlink backLink = new Hyperlink("\u2190 Back to Login");
        backLink.setStyle("-fx-font-size: 12px; -fx-text-fill: #A5D6A7;");

        card.getChildren().addAll(title, nameField, cnicField, contactField, addressField,
                                  usernameField, passwordField, confirmField, registerBtn, backLink);

        // --- Center the card in the viewport ---
        StackPane centerWrapper = new StackPane(card);
        centerWrapper.setAlignment(Pos.CENTER);
        root.getChildren().add(centerWrapper);

        // --- Event handlers ---

        registerBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String cnic = cnicField.getText().trim();
            String contact = contactField.getText().trim();
            String address = addressField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String confirm = confirmField.getText();

            // Validate
            if (name.isEmpty() || cnic.isEmpty() || username.isEmpty() || password.isEmpty() || contact.isEmpty() || address.isEmpty()) {
                CustomAlert.show(root, "Validation Error", "All required fields must be filled.", true);
                return;
            }
            if (!password.equals(confirm)) {
                CustomAlert.show(root, "Validation Error", "Passwords do not match.", true);
                return;
            }
            if (!cnic.matches("\\d{5}-\\d{7}-\\d")) {
                CustomAlert.show(root, "Validation Error", "Invalid CNIC format. Expected: XXXXX-XXXXXXX-X", true);
                return;
            }
            if (!contact.matches("\\d{4}-\\d{7}")) {
                CustomAlert.show(root, "Validation Error", "Invalid Contact format. Expected: XXXX-XXXXXXX", true);
                return;
            }

            try {
                SubsidyRequestController controller = new SubsidyRequestController();
                String farmerId = controller.registerFarmer(name, cnic, contact, address, username, password);

                CustomAlert.show(root, "Registration Successful",
                    "Welcome to SeedWise!\nYour account has been created.\nFarmer ID: " + farmerId + "\nYou can now login with your credentials.",
                    false, () -> {
                        stage.setScene(new LoginView(stage).getScene());
                    });
            } catch (Exception ex) {
                CustomAlert.show(root, "Error", ex.getMessage(), true);
            }
        });

        backLink.setOnAction(e -> {
            stage.setScene(new LoginView(stage).getScene());
        });

        this.scene = new Scene(root, 1200, 800);
    }

    private TextField createField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setStyle(
            "-fx-padding: 10; -fx-font-size: 13px; -fx-background-radius: 8;" +
            "-fx-background-color: rgba(0,0,0,0.3); -fx-text-fill: white;" +
            "-fx-prompt-text-fill: rgba(255,255,255,0.5); -fx-border-color: rgba(255,255,255,0.2);" +
            "-fx-border-radius: 8;"
        );
        return f;
    }

    public Scene getScene() { 
        ui.components.ImageBackground.applyTheme(scene);
        return scene; 
    }
}
