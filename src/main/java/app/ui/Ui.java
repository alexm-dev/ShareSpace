package app.ui;

import app.model.Asset;
import app.model.Location;
import app.model.User;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.*;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * UI utility class for building JavaFX UI components with consistent styling.
 *
 * Is used throughout the UI package to create various UI elements like labels, buttons, tiles etc.
 */
public final class Ui {


    /** Global font scale */
    static final double FONT_SCALE = 1.2;

    private static int scaled(int sizePx) {
        return (int) Math.round(sizePx * FONT_SCALE);
    }

    static Label label(String text, int sizePx, String extraStyle) {
        Label l = new Label(text);
        l.setWrapText(true);
        l.setStyle("-fx-font-size: " + scaled(sizePx) + "px;" + extraStyle);
        return l;
    }

    // bold labels hold short single-line text (names, titles, the logo); keep
    static Label bold(String text, int sizePx) {
        Label l = label(text, sizePx, "-fx-font-weight: bold;");
        l.setWrapText(false);
        l.setTextOverrun(OverrunStyle.ELLIPSIS);
        return l;
    }

    static Label light(String text, int sizePx) {
        return label(text, sizePx, "-fx-text-fill: #888888;");
    }

    private static void roundedClip(Region r) {
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(r.widthProperty());
        clip.heightProperty().bind(r.heightProperty());
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        r.setClip(clip);
    }

    static Region image(double aspectRatio) {
        AspectPane r = new AspectPane(aspectRatio);
        r.setStyle("-fx-background-color: #d9d9d9; -fx-background-radius: 8;");
        return r;
    }

    static Region image(double aspectRatio, byte[] data) {
        Image img = (data == null || data.length == 0) ? null : new Image(new ByteArrayInputStream(data));
        if (img == null || img.isError() || img.getWidth() == 0 || img.getHeight() == 0) {
            return image(aspectRatio);
        }

        AspectPane r = new AspectPane(aspectRatio);
        r.setBackground(new Background(coverBackground(img)));
        roundedClip(r);
        return r;
    }

    private static BackgroundImage backgroundImage(Image img, boolean contain) {
        BackgroundSize size = new BackgroundSize(
                BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, contain, !contain);
        return new BackgroundImage(img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, size);
    }

    /** A BackgroundImage that scales an image to cover its region, centred. */
    private static BackgroundImage coverBackground(Image img) {
        return backgroundImage(img, false);
    }

    /**
     * Fit an image to a blurred background of the same image.
     */
    static Region fittedImage(double aspectRatio, byte[] data) {
        Image img = (data == null || data.length == 0) ? null : new Image(new ByteArrayInputStream(data));
        if (img == null || img.isError() || img.getWidth() == 0 || img.getHeight() == 0) {
            return image(aspectRatio);
        }

        Region back = new Region();
        back.setBackground(new Background(coverBackground(img)));
        back.setEffect(new GaussianBlur(24));

        Region front = new Region();
        front.setBackground(new Background(backgroundImage(img, true)));

        AspectPane pane = new AspectPane(aspectRatio);
        pane.getChildren().addAll(back, front);
        roundedClip(pane);
        return pane;
    }

    /**
     * Fixed image box with a given width and height, showing an image if data is present,
     */
    static Region imageBox(double width, double height, byte[] data) {
        Region r = new Region();
        r.setMinSize(width, height);
        r.setPrefSize(width, height);
        r.setMaxSize(width, height);

        Image img = (data == null || data.length == 0) ? null : new Image(new ByteArrayInputStream(data));
        if (img != null && !img.isError() && img.getWidth() > 0 && img.getHeight() > 0) {
            r.setBackground(new Background(coverBackground(img)));
        } else {
            r.setStyle("-fx-background-color: #d9d9d9; -fx-background-radius: 8;");
        }

        Rectangle clip = new Rectangle(width, height);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        r.setClip(clip);
        return r;
    }

    private static String slug(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]+", "");
    }

    /** Reads a bundled classpath resource into bytes, or null if it isn't there. */
    static byte[] resourceBytes(String path) {
        try (InputStream in = Ui.class.getResourceAsStream(path)) {
            return in == null ? null : in.readAllBytes();
        } catch (IOException e) {
            return null;
        }
    }

    /** Cover photo bundled for a category, or null if none ships for it. */
    static byte[] categoryImage(String categoryName) {
        return resourceBytes("/images/categories/" + slug(categoryName) + "/cover.jpg");
    }

    /** Cover photo bundled for a subcategory */
    static byte[] subCategoryImage(String categoryName, String subCategoryName) {
        return resourceBytes(
                "/images/categories/" + slug(categoryName) + "/" + slug(subCategoryName) + ".jpg");
    }

    static Region avatar(double diameter, byte[] data, Runnable onClick) {
        StackPane pane = new StackPane();
        pane.setMinSize(diameter, diameter);
        pane.setPrefSize(diameter, diameter);
        pane.setMaxSize(diameter, diameter);

        Image img = (data == null || data.length == 0) ? null : new Image(new ByteArrayInputStream(data));
        if (img != null && !img.isError() && img.getWidth() > 0 && img.getHeight() > 0) {
            pane.setBackground(new Background(coverBackground(img)));
        } else {
            pane.setStyle("-fx-background-color: #d9d9d9;");
            if (onClick != null) {
                pane.getChildren().add(plusIcon(diameter * 0.4, "#9e9e9e"));
            }
        }

        pane.setClip(new Circle(diameter / 2, diameter / 2, diameter / 2));
        if (onClick != null) {
            pane.setCursor(javafx.scene.Cursor.HAND);
            pane.setOnMouseClicked(e -> onClick.run());
        }
        return pane;
    }

    /** Clickable card showing an owners avatar, username and rating. */
    static Node ownerCard(int ownerId) {
        User owner = ShareS.userService.findById(ownerId);
        if (owner == null) {
            return new HBox();
        }

        Region avatar = avatar(44, ShareS.userService.getProfileImage(owner.getId()), null);

        double avg = ShareS.ratingService.getAverageForUser(owner.getId());

        VBox text = new VBox(2,
                bold("@" + owner.getUsername().toUpperCase(), 13),
                label(stars(avg), 12, "-fx-text-fill: #ffd000;"));
        text.setAlignment(Pos.CENTER_LEFT);

        HBox card = new HBox(12, avatar, text);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(Region.USE_PREF_SIZE);
        card.setStyle("-fx-cursor: hand;");
        card.setOnMouseClicked(e -> ShareS.showUserProfilePage(owner));
        return card;
    }

    private static String stars(double value) {
        int full = (int) Math.round(value);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(i < full ? "★" : "☆");
        }
        return sb.toString();
    }

    /**
     * Pops up a small menu under an image area to choose, change or remove its
     * image. The remove entry only appears when there is an image to remove.
     */
    static void showImageMenu(Node anchor, boolean hasImage, Runnable onChoose, Runnable onRemove) {
        ContextMenu menu = new ContextMenu();
        MenuItem choose = new MenuItem(hasImage ? "Change image…" : "Choose image…");
        choose.setOnAction(e -> onChoose.run());
        menu.getItems().add(choose);
        if (hasImage && onRemove != null) {
            MenuItem remove = new MenuItem("Remove image");
            remove.setOnAction(e -> onRemove.run());
            menu.getItems().add(remove);
        }
        menu.show(anchor, Side.BOTTOM, 0, 0);
    }

    static String discountText(Asset asset) {
        if (asset.getDiscountPercentage() <= 0) {
            return null;
        }
        String when = asset.getDiscountAfterDays() <= 0
                ? "from day 1"
                : "after " + asset.getDiscountAfterDays() + " days";
        return String.format("%.0f%% off %s", asset.getDiscountPercentage(), when);
    }

    static String formatLocation(Location l) {
        StringBuilder sb = new StringBuilder();
        if (l.getStreetAddress() != null && !l.getStreetAddress().isBlank()) {
            sb.append(l.getStreetAddress()).append(", ");
        }
        sb.append(l.getPostalCode()).append(" ").append(l.getCity());
        if (l.getDistrict() != null && !l.getDistrict().isBlank()) {
            sb.append(" (").append(l.getDistrict()).append(")");
        }
        sb.append(", ").append(l.getCountry());
        return sb.toString();
    }

    static Region box(int heightPx, String style) {
        Region r = new Region();
        r.setStyle(style);
        r.setPrefHeight(heightPx);
        r.setMinHeight(heightPx);
        r.setMaxWidth(Double.MAX_VALUE);
        return r;
    }

    static Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    static Label boldCentered(String text, int sizePx) {
        Label l = label(text, sizePx, "-fx-font-weight: bold;");
        l.setMaxWidth(400);
        l.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        return l;
    }

    static Button button(String text, int sizePx, String extraStyle) {
        Button b = new Button(text);
        b.setStyle("-fx-font-size: " + scaled(sizePx) + "px; -fx-background-radius: 20; -fx-cursor: hand;" + extraStyle);
        return b;
    }

    static Button iconButton(String svgPath, String bgColor, String iconColor,
            String tooltip, Runnable action) {
        SVGPath icon = new SVGPath();
        icon.setContent(svgPath);
        icon.setFill(Color.web(iconColor));
        icon.setScaleX(0.5);
        icon.setScaleY(0.5);

        Button b = new Button();
        b.setGraphic(icon);
        b.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 20;"
                + " -fx-cursor: hand; -fx-min-width: 30px; -fx-min-height: 30px;"
                + " -fx-padding: 6;");
        if (tooltip != null)
            b.setTooltip(new Tooltip(tooltip));
        if (action != null)
            b.setOnAction(e -> action.run());
        return b;
    }

    static VBox tile(String name, String price, double aspectRatio) {
        return tile(name, price, aspectRatio, null, null);
    }

    static VBox tile(String name, String price, double aspectRatio, Runnable onClick) {
        return tile(name, price, aspectRatio, null, onClick);
    }

    static VBox tile(String name, String price, double aspectRatio, byte[] imageData, Runnable onClick) {
        HBox head = new HBox(6, bold(name, 13), light(price, 11));
        head.setAlignment(Pos.BOTTOM_LEFT);

        VBox box = new VBox(6, head, fittedImage(aspectRatio, imageData));
        box.setMaxWidth(Double.MAX_VALUE);
        if (onClick != null) {
            box.setStyle("-fx-cursor: hand;");
            box.setOnMouseClicked(e -> onClick.run());
            addHoverPop(box);
        }
        return box;
    }

    private static void addHoverPop(Node node) {
        node.setCache(true);
        node.setCacheHint(CacheHint.QUALITY);

        ScaleTransition st = new ScaleTransition(Duration.millis(130), node);
        st.setOnFinished(e -> node.setCacheHint(CacheHint.QUALITY));

        node.setOnMouseEntered(e -> { node.setCacheHint(CacheHint.SPEED); st.stop(); st.setToX(1.03); st.setToY(1.03); st.play(); });
        node.setOnMouseExited(e -> { node.setCacheHint(CacheHint.SPEED); st.stop(); st.setToX(1.0); st.setToY(1.0); st.play(); });
        node.setOnMousePressed(e -> { node.setScaleX(0.985); node.setScaleY(0.985); });
        node.setOnMouseReleased(e -> { st.stop(); st.setToX(1.03); st.setToY(1.03); st.play(); });
    }

    static VBox ownerTile(String name, String price, double aspectRatio, byte[] imageData,
            String lockedNote, Runnable onOpen, Runnable onEdit, Runnable onDelete) {
        HBox head = new HBox(6, bold(name, 13), light(price, 11));
        head.setAlignment(Pos.BOTTOM_LEFT);

        Button kebab = new Button("⋯");
        kebab.setStyle("-fx-background-color: rgba(255,255,255,0.85); -fx-background-radius: 12;"
                + " -fx-font-weight: bold; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 0 8 4 8;");
        ContextMenu menu = new ContextMenu();
        if (lockedNote != null) {
            // a listing with active bookings can't be edited or deleted; show why
            MenuItem locked = new MenuItem(lockedNote);
            locked.setDisable(true);
            menu.getItems().add(locked);
        } else {
            if (onEdit != null) {
                MenuItem edit = new MenuItem("Edit");
                edit.setOnAction(e -> onEdit.run());
                menu.getItems().add(edit);
            }
            if (onDelete != null) {
                MenuItem delete = new MenuItem("Delete");
                delete.setOnAction(e -> onDelete.run());
                menu.getItems().add(delete);
            }
        }
        kebab.setOnAction(e -> menu.show(kebab, Side.BOTTOM, 0, 0));

        StackPane imageStack = new StackPane(fittedImage(aspectRatio, imageData), kebab);
        StackPane.setAlignment(kebab, Pos.TOP_RIGHT);
        StackPane.setMargin(kebab, new Insets(8));

        VBox box = new VBox(6, head, imageStack);
        box.setMaxWidth(Double.MAX_VALUE);
        if (onOpen != null) {
            box.setStyle("-fx-cursor: hand;");
            box.setOnMouseClicked(e -> onOpen.run());
        }
        return box;
    }

    private static SVGPath plusIcon(double sizePx, String color) {
        SVGPath plus = new SVGPath();
        plus.setContent("M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z");
        plus.setFill(Color.web(color));
        double scale = sizePx / 24.0;
        plus.setScaleX(scale);
        plus.setScaleY(scale);
        return plus;
    }

    static VBox addTile(String name, double aspectRatio, Runnable onClick) {
        HBox head = new HBox(6, bold(name, 13));
        head.setAlignment(Pos.BOTTOM_LEFT);

        StackPane imageStack = new StackPane(image(aspectRatio), plusIcon(64, "#9e9e9e"));

        VBox box = new VBox(6, head, imageStack);
        box.setMaxWidth(Double.MAX_VALUE);
        if (onClick != null) {
            box.setStyle("-fx-cursor: hand;");
            box.setOnMouseClicked(e -> onClick.run());
        }
        return box;
    }

    static GridPane grid(int columns, double gap, Node... items) {
        GridPane g = new GridPane();
        g.setHgap(gap);
        g.setVgap(28);
        g.setMaxWidth(Double.MAX_VALUE);
        for (int c = 0; c < columns; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / columns);
            g.getColumnConstraints().add(cc);
        }
        for (int i = 0; i < items.length; i++) {
            g.add(items[i], i % columns, i / columns);
        }
        return g;
    }

    static StackPane footer() {
        Region bg = box(260, "-fx-background-color: #ffd000; -fx-background-radius: 12;");

        HBox links = new HBox(
                label("Share.", 13, "-fx-text-fill: white; -fx-font-weight: bold;"),
                spacer(),
                label("Contact", 13, "-fx-text-fill: white;"));

        VBox content = new VBox(links);
        content.setAlignment(Pos.BOTTOM_LEFT);
        content.setPadding(new Insets(20));

        StackPane sp = new StackPane(bg, content);
        StackPane.setAlignment(content, Pos.BOTTOM_LEFT);
        return sp;
    }

    private static final double MENU_WIDTH = 200;
    /**
     * internal method for building a page
     * only accessed in Ui.java
     * builds a page with header, drawer menu, content, footer
     *
     * @param children the page content
     * @return the page as a StackPane object
     */
    private static StackPane buildPagerInternal(Node... children) {
        // logo with event
        Label logo = bold("ShareSpace®", 19);
        logo.setStyle("-fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 20;");
        logo.setOnMouseClicked(event -> ShareS.showStartPage());

        // create sliding menuPanel
        VBox menuPanel = new VBox(10);
        menuPanel.setPrefWidth(MENU_WIDTH);
        menuPanel.setMaxWidth(MENU_WIDTH);
        menuPanel.setStyle("-fx-background-color: #2c2c2c;");
        menuPanel.setPadding(new Insets(60, 20, 20, 20));
        StackPane.setAlignment(menuPanel, Pos.TOP_RIGHT);
        menuPanel.setTranslateX(MENU_WIDTH);

        //create transition for menuPanel
        TranslateTransition tt = new TranslateTransition(Duration.millis(500), menuPanel);

        // buttons for the menuPanel
        Button about = button(
                "About us",
                13,
                "-fx-background-color: white;");
        about.setOnAction(event -> {
            closeMenu(tt);
            ShareS.showAboutPage();
        });
        Button bookings = button(
                "Bookings",
                13,
                "-fx-background-color: white;");
        bookings.setOnAction(event -> {
            closeMenu(tt);
            ShareS.showBookingPage();
        });
        Button catalog = button(
                "Catalog",
                13,
                "-fx-background-color: white;");
        catalog.setOnAction(event -> {
            closeMenu(tt);
            ShareS.showCatalogPage();
        });
        Button login = button(
                "Login/Sign up",
                13,
                "-fx-background-color: white;");
        login.setOnAction(event -> {
            closeMenu(tt);
            ShareS.showLoginPage();
        });
        Button profile = button(
                "Profile",
                13,
                "-fx-background-color: white;");
        profile.setOnAction(event -> {
            closeMenu(tt);
            ShareS.showProfilePage();
        });
        Button settings = button(
                "Settings",
                13,
                "-fx-background-color: white;");
        settings.setOnAction(event -> {
            closeMenu(tt);
            ShareS.showProfileSettingsPage();
        });
        Button ratings = button(
                "Ratings",
                13,
                "-fx-background-color: white;");
        ratings.setOnAction(event -> {
            closeMenu(tt);
            ShareS.showRatingPage();
        });

        Button logout = button(
                "Logout",
                13,
                "-fx-background-color: white;");
        logout.setOnAction(event -> {
            closeMenu(tt);
            ShareS.session.logout();
            ShareS.showStartPage();
        });


        // toggleMenu button inside menuPanel
        Button toggleOff = getButton(tt);
        toggleOff.setOnAction(event -> closeMenu(tt));
        menuPanel.getChildren().add(toggleOff);

        boolean isLoggedIn = ShareS.session.isLoggedIn();

        // menuPanel for logged-in user
        if (isLoggedIn) {
            menuPanel.getChildren().addAll(profile, catalog, bookings, ratings, settings, about, logout);
        }

        //menuPanel for logged-out user / guest
        if (!isLoggedIn) {
            menuPanel.getChildren().addAll(login, catalog, about);
        }


        // toggleMenu button in header
        Button toggleOn = getButton(tt);

        HBox heading = new HBox(20, logo, spacer(), getBackButton(), toggleOn);
        heading.setAlignment(Pos.CENTER_LEFT);
        heading.setPadding(new Insets(16, 0, 16, 0));
        heading.setStyle("-fx-border-color: transparent transparent #e5e5e5 transparent; -fx-border-width: 0 0 1 0;");

        Region grow = new Region();
        VBox.setVgrow(grow, Priority.ALWAYS);

        // combining all parts together
        // header, children args, footer
        VBox mainPage = new VBox();
        mainPage.setSpacing(40);
        mainPage.getChildren().add(heading);
        mainPage.getChildren().addAll(children);
        mainPage.getChildren().add(grow);
        mainPage.getChildren().add(footer());
        mainPage.setFillWidth(true);
        mainPage.setPadding(new Insets(0, 60, 40, 60));
        mainPage.setStyle("-fx-background-color: white;");

        // adding drawer to page (hidden)
        StackPane root = new StackPane(mainPage, menuPanel);
        menuPanel.setAlignment(Pos.TOP_RIGHT);

        return root;
    }

    // button for drawer menu with default event = open drawer
    private static Button getButton(TranslateTransition tt) {
        // icon for toggle button
        final String MenuIcon = "M4 18h16v-2H4v2zM4 13h16v-2H4v2zM4 8h16V6H4v2z";
        SVGPath icon = new SVGPath();
        icon.setContent(MenuIcon);
        icon.setFill(Color.web("black"));

        Button toggle = new Button();
        toggle.setGraphic(icon);
        toggle.setStyle("-fx-background-color: white; -fx-background-radius: 20;"
                + " -fx-cursor: hand; -fx-min-width: 30px; -fx-min-height: 30px;"
                + " -fx-padding: 6;");

        toggle.setOnAction(event -> {
            tt.setToX(0);
            tt.play();
        });
        return toggle;
    }

    private static Button getBackButton() {
        SVGPath icon = new SVGPath();
        icon.setContent("M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z");
        icon.setFill(Color.web("black"));

        Button back = new Button();
        back.setGraphic(icon);
        back.setStyle("-fx-background-color: white; -fx-background-radius: 20;"
                + " -fx-cursor: hand; -fx-min-width: 30px; -fx-min-height: 30px;"
                + " -fx-padding: 6;");
        back.setOnAction(event -> ShareS.goBack());

        boolean canGoBack = ShareS.canGoBack();
        back.setVisible(canGoBack);
        back.setManaged(canGoBack);
        return back;
    }

    // event for closing the drawer menu
    private static void closeMenu(TranslateTransition tt) {
        tt.setToX(MENU_WIDTH);
        tt.play();
    }

    /**
     * public method for usage in every ...Page class
     * adds header, drawer and footer
     *
     * @param children the page content
     * @return the page as a StackPane object
     */
    static StackPane buildPage(Node... children) {
        return buildPagerInternal(children);
    }
}
