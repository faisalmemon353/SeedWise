package ui.components;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/**
 * Custom glassmorphism alert overlay that perfectly matches the SeedWise theme,
 * replacing standard OS-native Alert dialogs.
 */
public class CustomAlert {

    /**
     * Shows a themed alert overlay inside the given StackPane root.
     */
    public static void show(StackPane root, String titleText, String messageText, boolean isError) {
        show(root, titleText, messageText, isError, null);
    }

    /**
     * Shows a themed alert overlay and executes onDismiss when closed.
     */
    public static void show(StackPane root, String titleText, String messageText, boolean isError, Runnable onDismiss) {
        // Overlay to dim the background
        Region overlay = new Region();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        overlay.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Themed Alert Box (Glassmorphism)
        VBox alertBox = new VBox(15);
        alertBox.setAlignment(Pos.CENTER);
        alertBox.setMaxWidth(400);
        alertBox.setMaxHeight(Region.USE_PREF_SIZE);
        alertBox.setPadding(new Insets(30));
        alertBox.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.15);" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: rgba(255, 255, 255, 0.3);" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 8);"
        );

        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + (isError ? "#FF6B6B" : "#A5D6A7") + ";");

        Label message = new Label(messageText);
        message.setStyle("-fx-font-size: 14px; -fx-text-fill: #FFFFFF;");
        message.setWrapText(true);
        message.setAlignment(Pos.CENTER);
        message.setTextAlignment(TextAlignment.CENTER);

        Button okBtn = new Button("OK");
        okBtn.setPrefWidth(120);
        okBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #43A047, #66BB6A);" +
            "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-padding: 10 20; -fx-cursor: hand; -fx-background-radius: 8;"
        );

        VBox contentLayout = new VBox(20, title, message, okBtn);
        contentLayout.setAlignment(Pos.CENTER);
        alertBox.getChildren().add(contentLayout);

        // Group overlay and alertBox into a layer
        StackPane alertLayer = new StackPane(overlay, alertBox);
        alertLayer.setAlignment(Pos.CENTER);
        alertLayer.setOpacity(0);
        
        // Add to root
        root.getChildren().add(alertLayer);

        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), alertLayer);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        // Dismiss action
        okBtn.setOnAction(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), alertLayer);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> {
                root.getChildren().remove(alertLayer);
                if (onDismiss != null) {
                    onDismiss.run();
                }
            });
            fadeOut.play();
        });
        
        // Let user hit enter to close
        okBtn.setDefaultButton(true);
    }

    /**
     * Shows a themed confirmation overlay with Yes/No buttons.
     */
    public static void showConfirm(StackPane root, String titleText, String messageText, Runnable onConfirm) {
        Region overlay = new Region();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        overlay.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        VBox alertBox = new VBox(15);
        alertBox.setAlignment(Pos.CENTER);
        alertBox.setMaxWidth(400);
        alertBox.setMaxHeight(Region.USE_PREF_SIZE);
        alertBox.setPadding(new Insets(30));
        alertBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.15); -fx-background-radius: 16; -fx-border-color: rgba(255, 255, 255, 0.3); -fx-border-radius: 16; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 8);");

        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #A5D6A7;");

        Label message = new Label(messageText);
        message.setStyle("-fx-font-size: 14px; -fx-text-fill: #FFFFFF;");
        message.setWrapText(true);
        message.setAlignment(Pos.CENTER);
        message.setTextAlignment(TextAlignment.CENTER);

        javafx.scene.layout.HBox btnRow = new javafx.scene.layout.HBox(15);
        btnRow.setAlignment(Pos.CENTER);

        Button yesBtn = new Button("YES");
        yesBtn.setPrefWidth(100);
        yesBtn.setStyle("-fx-background-color: #43A047; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-cursor: hand; -fx-background-radius: 6;");

        Button noBtn = new Button("NO");
        noBtn.setPrefWidth(100);
        noBtn.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-cursor: hand; -fx-background-radius: 6;");

        btnRow.getChildren().addAll(yesBtn, noBtn);
        alertBox.getChildren().addAll(title, message, btnRow);

        StackPane alertLayer = new StackPane(overlay, alertBox);
        alertLayer.setOpacity(0);
        root.getChildren().add(alertLayer);

        FadeTransition ftIn = new FadeTransition(Duration.millis(250), alertLayer);
        ftIn.setToValue(1.0);
        ftIn.play();

        Runnable close = () -> {
            FadeTransition ft = new FadeTransition(Duration.millis(200), alertLayer);
            ft.setToValue(0);
            ft.setOnFinished(ev -> root.getChildren().remove(alertLayer));
            ft.play();
        };

        yesBtn.setOnAction(e -> { close.run(); if (onConfirm != null) onConfirm.run(); });
        noBtn.setOnAction(e -> close.run());
    }
}
