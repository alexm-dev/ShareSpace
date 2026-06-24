package app.ui;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

public final class Ui {

    private Ui() {}

    static Label label(String text, int sizePx, String extraStyle) {
        Label l = new Label(text);
        l.setWrapText(true);
        l.setStyle("-fx-font-size: " + sizePx + "px;" + extraStyle);
        return l;
    }

    static Label bold(String text, int sizePx)  { return label(text, sizePx, "-fx-font-weight: bold;"); }
    static Label light(String text, int sizePx) { return label(text, sizePx, "-fx-text-fill: #888888;"); }

    static Region image(double aspectRatio) {
        Region r = new Region();
        r.setStyle("-fx-background-color: #d9d9d9; -fx-background-radius: 8;");
        r.setMaxWidth(Double.MAX_VALUE);
        r.setMinHeight(Region.USE_PREF_SIZE);
        r.prefHeightProperty().bind(r.widthProperty().multiply(aspectRatio));
        return r;
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
        if (tooltip != null) b.setTooltip(new Tooltip(tooltip));
        if (action != null) b.setOnAction(e -> action.run());
        return b;
    }

    static VBox tile(String name, String price, double aspectRatio) {
        HBox head = new HBox(6, bold(name, 13), light(price, 11));
        head.setAlignment(Pos.BOTTOM_LEFT);

        VBox box = new VBox(6, head, image(aspectRatio));
        box.setMaxWidth(Double.MAX_VALUE);
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

    static HBox header(String[] navItems, Runnable[] navActions, Runnable logoAction) {
        Label logo = bold("ShareSpace®", 19);
        logo.setStyle("-fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 20;");
        if (logoAction != null) {
            logo.setOnMouseClicked(e -> logoAction.run());
        }

        HBox bar = new HBox(20, logo, spacer());
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(16, 0, 16, 0));
        bar.setStyle("-fx-border-color: transparent transparent #e5e5e5 transparent; -fx-border-width: 0 0 1 0;");

        for (int i = 0; i < navItems.length; i++) {
            Button link = new Button(navItems[i]);
            link.setStyle("-fx-font-size: 13px; -fx-background-color: transparent; -fx-cursor: hand;");
            final int index = i;
            if (navActions != null && navActions[i] != null) {
                link.setOnAction(e -> navActions[index].run());
            }
            bar.getChildren().add(link);
        }
        return bar;
    }

    static VBox page(Node... children) {
        VBox root = new VBox(40, children);
        root.setFillWidth(true);
        root.setPadding(new Insets(0, 60, 40, 60));
        root.setStyle("-fx-background-color: white;");
        return root;
    }



    //private and final method for building a page
    private static VBox menuPanel;
    private static boolean isOpen = false;
    private static final double MENU_WIDTH = 200;
    private static StackPane buildPagerInternal(Node... children) {
        //logo with event
        Label logo = bold("ShareSpace®", 19);
        logo.setStyle("-fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 20;");
        logo.setOnMouseClicked(event -> ShareS.showStartPage()); //always has ShareSpace top left wit event

        //create sliding menuPanel
        menuPanel = new VBox(10);
        menuPanel.setPrefWidth(MENU_WIDTH);
        menuPanel.setMaxWidth(MENU_WIDTH);
        menuPanel.setStyle("-fx-background-color: #2c2c2c;");
        menuPanel.setPadding(new Insets(60, 20, 20, 20));
        StackPane.setAlignment(menuPanel, Pos.TOP_RIGHT);
        menuPanel.setTranslateX(MENU_WIDTH);

        //content for the menuPanel
        //ADD NEW BUTTONS HERE
        //don't forget to add to panel (method below)
        Button login = button(
                "Login/Sign up",
                13,
                "-fx-background-color: white;");
        login.setOnAction(event -> ShareS.showLoginPage());
        Button profile = button(
                "Profile/My listings",
                13,
                "-fx-background-color: white;");
        profile.setOnAction(event -> {
            if (ShareS.session.getActiveUser() != null) {
                ShareS.showProfilePage();
            }
        });
        Button catalog = button(
                "Catalog",
                13,
                "-fx-background-color: white;");
        catalog.setOnAction(event -> ShareS.showCatalogPage());
        Button bookings = button(
                "(My?) Bookings",
                13,
                "-fx-background-color: white;");
        bookings.setOnAction(event -> {
            if (ShareS.session.getActiveUser() != null) {
                ShareS.showBookingPage();
            }
        });
        Button ratings = button(
                "(My?) Ratings)",
                13,
                "-fx-background-color: white;");
        ratings.setOnAction(event -> {
            if (ShareS.session.getActiveUser() != null) {
                //TODO: show ratingsPage
            }
        });
        Button about = button(
                "About us",
                13,
                "-fx-background-color: white;");
        about.setOnAction(event -> ShareS.showAboutPage());
        Button settings = button(
                "Settings",
                13,
                "-fx-background-color: white;");
        settings.setOnAction(event -> {
            if (ShareS.session.getActiveUser() != null) {
                ShareS.showProfileSettingsPage();
            }
        });
        Button logout = button(
                "Logout",
                13,
                "-fx-background-color: white;");
        logout.setOnAction(event -> {
            if (ShareS.session.getActiveUser() != null) {
                ShareS.session.logout();
                ShareS.showStartPage();
            }
        });

        //add all buttons to menuPanel
        if (ShareS.session.getActiveUser() == null)/* not logged in*/ {
            menuPanel.getChildren().add(login);
        }else {
            menuPanel.getChildren().add(profile);
        }
        //if new button created add below
        menuPanel.getChildren().addAll(catalog, bookings, ratings, about, settings);
        //if logged in show logout else not
        if (ShareS.session.getActiveUser() != null) {menuPanel.getChildren().add(logout);}


        //toggleMenu button
        Button toggle = getButton();

        //heading is logo + toggle button
        HBox heading = new HBox(20, logo, spacer(), toggle);
        heading.setAlignment(Pos.CENTER_LEFT);
        heading.setPadding(new Insets(16, 0, 16, 0));
        heading.setStyle("-fx-border-color: transparent transparent #e5e5e5 transparent; -fx-border-width: 0 0 1 0;");

        //combining children(content) with heading and footer
        VBox mainPage = new VBox();
        mainPage.setSpacing(40);
        mainPage.getChildren().add(heading);
        mainPage.getChildren().addAll(children);
        mainPage.getChildren().add(footer());
        mainPage.setFillWidth(true);
        mainPage.setPadding(new Insets(0, 60, 40, 60));
        mainPage.setStyle("-fx-background-color: white;");

        StackPane root = new StackPane(mainPage, menuPanel);
        menuPanel.setAlignment(Pos.TOP_RIGHT);

        return root;
    }


    private static Button getButton() {
        //icon for toggle button
        final String MenuIcon = "M4 18h16v-2H4v2zM4 13h16v-2H4v2zM4 8h16V6H4v2z";
        SVGPath icon = new SVGPath();
        icon.setContent(MenuIcon);
        icon.setFill(Color.web("black"));
        icon.setScaleX(1);
        icon.setScaleY(1);

        Button toggle = new Button();
        toggle.setGraphic(icon);
        toggle.setStyle("-fx-background-color: white; -fx-background-radius: 20;"
                + " -fx-cursor: hand; -fx-min-width: 30px; -fx-min-height: 30px;"
                + " -fx-padding: 6;");
        toggle.setOnAction(event -> {
            TranslateTransition tt = new TranslateTransition(Duration.millis(1000), menuPanel);
            tt.setToX(isOpen ? MENU_WIDTH : 0);
            tt.play();
            isOpen = !isOpen;
        });
        return toggle;
    }


    //public method others can use for building a page
    static StackPane buildPage(Node... children) {
        return buildPagerInternal(children);
    }
}
