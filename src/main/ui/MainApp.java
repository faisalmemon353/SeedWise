package ui;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;

/**
 * JavaFX entry point.
 * Loads LoginView as the first scene.
 */
public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        stage.setTitle("SeedWise - Agricultural Subsidy & Input Management System");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.setWidth(1200);
        stage.setHeight(800);

        // Load login view as first scene
        LoginView loginView = new LoginView(stage);
        Scene scene = loginView.getScene();
        ui.components.ImageBackground.applyTheme(scene);
        stage.setScene(scene);
        stage.show();
    }

    /** Get primary stage (for scene switching). */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
