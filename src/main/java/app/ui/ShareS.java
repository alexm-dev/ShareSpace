package com.sharespace;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ShareS extends Application {

    public static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setTitle("ShareSpace");
        stage.setWidth(1200);
        stage.setHeight(800);
        stage.setMaximized(false);

        showStartPage();
    }

    public static void showStartPage()   { showPage(new ShareSpacePage().build(), false); }
    public static void showCatalogPage() { showPage(new CatalogPage().build(), false); }
    public static void showProfilePage() { showPage(new ProfilePage().build(), false); }
    public static void showLoginPage()   { showPage(new LoginPage().build(), true); }
    public static void showBookingPage() { showPage(new BookingPage().build(), false); }

    private static void showPage(VBox root, boolean fitHeight) {
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(fitHeight);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Scene scene = new Scene(scrollPane);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}