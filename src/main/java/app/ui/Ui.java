package app.ui;

import app.model.Location;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;

public final class Ui {

    private Ui() {}

    static Label label(String text, int sizePx, String extraStyle) {
        Label l = new Label(text);
        l.setWrapText(true);
        l.setStyle("-fx-font-size: " + sizePx + "px;" + extraStyle);
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

    static Region image(double aspectRatio) {
        Region r = new Region();
        r.setStyle("-fx-background-color: #d9d9d9; -fx-background-radius: 8;");
        r.setMaxWidth(Double.MAX_VALUE);
        r.setMinHeight(Region.USE_PREF_SIZE);
        r.prefHeightProperty().bind(r.widthProperty().multiply(aspectRatio));
        return r;
    }

    static Region image(double aspectRatio, byte[] data) {
        Image img = (data == null || data.length == 0) ? null : new Image(new ByteArrayInputStream(data));
        if (img == null || img.isError() || img.getWidth() == 0 || img.getHeight() == 0) {
            return image(aspectRatio);
        }

        Region r = new Region();
        r.setMaxWidth(Double.MAX_VALUE);
        r.setMinHeight(Region.USE_PREF_SIZE);
        r.prefHeightProperty().bind(r.widthProperty().multiply(aspectRatio));
        r.setBackground(new Background(coverBackground(img)));

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(r.widthProperty());
        clip.heightProperty().bind(r.heightProperty());
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        r.setClip(clip);
        return r;
    }

    /** A BackgroundImage that scales an image to cover its region, centred. */
    private static BackgroundImage coverBackground(Image img) {
        BackgroundSize cover = new BackgroundSize(
                BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true);
        return new BackgroundImage(img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, cover);
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
            Label glyph = new Label("+");
            glyph.setStyle("-fx-font-size: " + (diameter * 0.4) + "px; -fx-text-fill: #9e9e9e;");
            pane.getChildren().add(glyph);
        }

        pane.setClip(new Circle(diameter / 2, diameter / 2, diameter / 2));
        if (onClick != null) {
            pane.setCursor(javafx.scene.Cursor.HAND);
            pane.setOnMouseClicked(e -> onClick.run());
        }
        return pane;
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

    /** One-line human-readable address, in the shape the listing pages display. */
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
        b.setStyle("-fx-font-size: " + sizePx + "px; -fx-background-radius: 20; -fx-cursor: hand;" + extraStyle);
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

        VBox box = new VBox(6, head, image(aspectRatio, imageData));
        box.setMaxWidth(Double.MAX_VALUE);
        if (onClick != null) {
            box.setStyle("-fx-cursor: hand;");
            box.setOnMouseClicked(e -> onClick.run());
        }
        return box;
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

        StackPane imageStack = new StackPane(image(aspectRatio, imageData), kebab);
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

    static VBox addTile(String name, double aspectRatio, Runnable onClick) {
        HBox head = new HBox(6, bold(name, 13));
        head.setAlignment(Pos.BOTTOM_LEFT);

        Label plus = new Label("+");
        plus.setStyle("-fx-font-size: 64px; -fx-font-weight: bold; -fx-text-fill: #9e9e9e;");
        StackPane imageStack = new StackPane(image(aspectRatio), plus);

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

    // private method for building a page
    // is called by buildPage()
    // manages building a page and combining header footer and content
    private static final double MENU_WIDTH = 200;
    private static StackPane buildPagerInternal(Node... children) {
        // logo with event
        Label logo = bold("ShareSpace®", 19);
        logo.setStyle("-fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 20;");
        logo.setOnMouseClicked(event -> ShareS.showStartPage()); // always has ShareSpace top left wit event

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

        // content for the menuPanel
        // ADD NEW BUTTONS HERE
        // don't forget to add to panel (method below)
        Button login = button(
                "Login/Sign up",
                13,
                "-fx-background-color: white;");
        login.setOnAction(event -> {
            closeMenu(tt);
            ShareS.showLoginPage();
        });
        Button profile = button(
                "Profile/My listings",
                13,
                "-fx-background-color: white;");
        profile.setOnAction(event -> {
                closeMenu(tt);
                ShareS.showProfilePage();
        });
        Button catalog = button(
                "Catalog",
                13,
                "-fx-background-color: white;");
        catalog.setOnAction(event -> {
            closeMenu(tt);
            ShareS.showCatalogPage();
        });
        Button bookings = button(
                "My Bookings",
                13,
                "-fx-background-color: white;");
        bookings.setOnAction(event -> {
            closeMenu(tt);
            ShareS.showBookingPage();
        });
        Button ratings = button(
                "My Ratings)",
                13,
                "-fx-background-color: white;");
        ratings.setOnAction(event -> {
            closeMenu(tt);
            ShareS.showRatingPage();
        });
        Button about = button(
                "About us",
                13,
                "-fx-background-color: white;");
        about.setOnAction(event -> {
            closeMenu(tt);
            ShareS.showAboutPage();
        });
        Button settings = button(
                "Settings",
                13,
                "-fx-background-color: white;");
        settings.setOnAction(event -> {
            closeMenu(tt);
            ShareS.showProfileSettingsPage();
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

        // TODO: change visibility for buttons/rework the following code!
        // add all buttons to menuPanel
        if (ShareS.session.getActiveUser() == null)/* not logged in */ {
            menuPanel.getChildren().add(login);
        } else {
            menuPanel.getChildren().add(profile);
        }
        // if new button was created, add here
        menuPanel.getChildren().addAll(catalog, bookings, ratings, about, settings);
        // if logged in show logout else not
        if (ShareS.session.getActiveUser() != null) {
            menuPanel.getChildren().add(logout);
        }


        // toggleMenu button in header
        Button toggleOn = getButton(tt);

        // heading is logo + toggleOn button
        HBox heading = new HBox(20, logo, spacer(), toggleOn);
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
    private static boolean isOpen = false;
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
            isOpen = true;
            tt.setToX(0);
            tt.play();
        });
        return toggle;
    }

    // event for closing the drawer menu
    private static void closeMenu(TranslateTransition tt) {
        isOpen = false;
        tt.setToX(MENU_WIDTH);
        tt.play();
    }

    // public method other classes can use for building a page
    //builds a page with header, drawer menu, children (content) and footer
    static StackPane buildPage(Node... children) {
        return buildPagerInternal(children);
    }
}
