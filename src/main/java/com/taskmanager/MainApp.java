package com.taskmanager;

import com.taskmanager.database.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static Stage primaryStage;
    private static boolean darkMode = false;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        // تهيئة قاعدة البيانات مبكرًا (ينشئ الجداول عند أول تشغيل)
        DatabaseManager.getInstance();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1280, 800);
        applyTheme(scene, darkMode);

        stage.setTitle("إدارة المهام اليومية - Daily Task Manager");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.show();

        stage.setOnCloseRequest(e -> DatabaseManager.getInstance().close());
    }

    public static void applyTheme(Scene scene, boolean dark) {
        darkMode = dark;
        scene.getStylesheets().clear();
        String css = dark ? "/css/dark.css" : "/css/light.css";
        scene.getStylesheets().add(MainApp.class.getResource(css).toExternalForm());
    }

    public static boolean isDarkMode() { return darkMode; }
    public static Stage getPrimaryStage() { return primaryStage; }

    public static void main(String[] args) {
        launch(args);
    }
}
