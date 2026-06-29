package app.ui;

import app.database.Database;
import app.model.Asset;
import app.model.Category;
import app.model.SubCategory;
import app.service.AssetService;
import app.service.BookingService;
import app.service.CatalogService;
import app.service.RatingService;
import app.service.SessionService;
import app.service.UserService;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ShareS extends Application {

    public static Stage primaryStage;

    public static SessionService session;
    public static UserService userService;
    public static AssetService assetService;
    public static CatalogService catalogService;
    public static BookingService bookingService;
    public static RatingService ratingService;

    @Override
    public void init() throws Exception {
        Database.initialize();
        session = new SessionService();
        session.restoreSession();
        userService = new UserService();
        assetService = new AssetService();
        catalogService = new CatalogService();
        bookingService = new BookingService();
        ratingService = new RatingService();
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setTitle("ShareSpace");
        stage.setWidth(1200);
        stage.setHeight(800);
        stage.setMinWidth(1100);
        stage.setMinHeight(640);
        stage.setMaximized(false);

        showStartPage();
    }

    public static void showStartPage()   { showPage(new ShareSpacePage().build(), false); }
    public static void showCatalogPage() { showPage(new CatalogPage().build(), false); }
    public static void showProfilePage() { showPage(new ProfilePage().build(), false); }
    public static void showLoginPage()   { showPage(new LoginPage().build(), true); }
    public static void showBookingPage() { showPage(new BookingPage().build(), false); }
    public static void showRegistrationPage() { showPage(new RegistrationPage().build(), true); }
    public static void showProfileSettingsPage() { showPage2(new ProfileSettingsPage().build()); }
    public static void showAboutPage() { showPage2(new AboutPage().build()); }
    public static void showRatingPage() { showPage2(new RatingPage().build()); }

    public static void showCategoryPage(Category category) { showPage(new CategoryPage(category).build(), false); }
    public static void showListingsPage(SubCategory subCategory) { showPage(new ListingsPage(subCategory).build(), false); }
    public static void showListingDetailPage(Asset asset) { showPage(new ListingDetailPage(asset).build(), false); }
    public static void showBookingFlowPage(Asset asset) { showPage(new BookingFlowPage(asset).build(), false); }
    public static void showCreateListingPage() { showPage(new CreateListingPage(null).build(), false); }
    public static void showEditListingPage(Asset asset) { showPage(new CreateListingPage(asset).build(), false); }

    private static void showPage(VBox root, boolean fitHeight) {
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Scene scene = new Scene(scrollPane);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // responsible for showing page in UI
    // TODO: change all pages that use showPAge to showPage2
    // TODO: give better fitting name
    private static void showPage2(StackPane root) {
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Scene scene = new Scene(scrollPane);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setAlwaysOnTop(true);
    }

}
