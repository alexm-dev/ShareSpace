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

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * The main application class for the ShareSpace UI.
 * It initializes the database, services, and manages navigation between different pages.
 */
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

    public static void showLoginPage()                           { go("login", () -> showLogin(new LoginPage().build())); }
    public static void showRegistrationPage()                    { go("register", () -> showLogin(new RegistrationPage().build())); }
    public static void showStartPage()                           { go("start", () -> showPage(new ShareSpacePage().build())); }
    public static void showCatalogPage()                         { go("catalog", () -> showPage(new CatalogPage().build())); }
    public static void showProfilePage()                         { go("profile", () -> showPage(new ProfilePage().build())); }
    public static void showUserProfilePage(app.model.User user)  { go("profile:" + user.getId(), () -> showPage(new ProfilePage(user).build())); }
    public static void showBookingPage()                         { go("booking", () -> showPage(new BookingPage().build())); }
    public static void showProfileSettingsPage()                 { go("settings", () -> showPage(new ProfileSettingsPage().build())); }
    public static void showAboutPage()                           { go("about", () -> showPage(new AboutPage().build())); }
    public static void showRatingPage()                          { go("rating", () -> showPage(new RatingPage().build())); }
    public static void showCategoryPage(Category category)       { go("category:" + category.getId(), () -> showPage(new CategoryPage(category).build())); }
    public static void showListingsPage(SubCategory subCategory) { go("listings:" + subCategory.getId(), () -> showPage(new ListingsPage(subCategory).build())); }
    public static void showListingDetailPage(Asset asset)        { go("detail:" + asset.getId(), () -> showPage(new ListingDetailPage(asset).build())); }
    public static void showBookingFlowPage(Asset asset)          { go("bookflow:" + asset.getId(), () -> showPage(new BookingFlowPage(asset).build())); }
    public static void showCreateListingPage()                   { go("create", () -> showPage(new CreateListingPage(null).build())); }
    public static void showEditListingPage(Asset asset)          { go("edit:" + asset.getId(), () -> showPage(new CreateListingPage(asset).build())); }

    // Navigation to manage page history
    private record Nav(String key, Runnable render) {}
    private static final Deque<Nav> backStack = new ArrayDeque<>();
    private static Nav current;

    private static void go(String key, Runnable render) {
        if (current != null && !current.key().equals(key)) {
            backStack.push(current);
        }
        current = new Nav(key, render);
        render.run();
    }

    public static void goBack() {
        if (backStack.isEmpty()) {
            return;
        }
        current = backStack.pop();
        current.render().run();
    }

    public static boolean canGoBack() {
        return !backStack.isEmpty();
    }


    // only used for login and register
    private static void showLogin(VBox root) {
        ScrollPane scrollPane = new ScrollPane(root);
        // zoom the input controls (text fields, combos, date pickers) to match
        // the scaled label/button text from Ui
        scrollPane.setStyle("-fx-font-size: 15px;");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Scene scene = new Scene(scrollPane);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // responsible for showing page in UI
    private static void showPage(StackPane root) {
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setStyle("-fx-font-size: 15px;");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Clamp footer to bottom of window
        scrollPane.viewportBoundsProperty().addListener((obs, oldB, newB) -> {
            if (root.getMinHeight() != newB.getHeight()) {
                root.setMinHeight(newB.getHeight());
            }
        });

        Scene scene = new Scene(scrollPane);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
