package com.sharespace;

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
}