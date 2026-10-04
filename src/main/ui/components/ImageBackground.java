package ui.components;

import javafx.scene.layout.Region;

import java.io.File;

/**
 * Utility to apply the common background image to all pages.
 */
public class ImageBackground {

    private static final String IMAGE_PATH = "resources/bg.png";

    public static final String GLASS_STYLE = 
        "-fx-background-color: rgba(255, 255, 255, 0.7); " +
        "-fx-control-inner-background: transparent; " +
        "-fx-background-radius: 15; " +
        "-fx-border-color: rgba(255, 255, 255, 0.4); " +
        "-fx-border-radius: 15; " +
        "-fx-border-width: 1; " +
        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5);";

    /**
     * Applies the background image to the given layout region (e.g. StackPane, BorderPane).
     */
    public static void applyTo(Region root) {
        String imgUrl = new File(IMAGE_PATH).toURI().toString();
        // Use single quotes around the url because it may contain spaces
        root.setStyle(
            "-fx-background-image: url('" + imgUrl + "');" +
            "-fx-background-size: cover;" +
            "-fx-background-position: center;" +
            "-fx-background-repeat: no-repeat;"
        );
    }

    /**
     * Applies the global CSS theme to the given scene.
     */
    public static void applyTheme(javafx.scene.Scene scene) {
        try {
            String css = ImageBackground.class.getResource("/seedwise.css").toExternalForm();
            if (!scene.getStylesheets().contains(css)) {
                scene.getStylesheets().add(css);
            }
        } catch (Exception e) {
            // Silently fail if CSS is not found
        }
    }
}
