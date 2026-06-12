package com.sharespace;

import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ProfilePage {

    public VBox build() {
        Region header = Ui.header(
                new String[]{"CATALOG", "BOOKINGS", "SETTINGS"},
                new Runnable[]{ShareS::showCatalogPage, ShareS::showBookingPage, null},
                ShareS::showStartPage);

        VBox heading = new VBox(4,
                Ui.bold("@MAXMUSTERMANN", 28),
                Ui.label("★★★★☆", 13, "-fx-text-fill: #ffd000;"));

        HBox titleRow = new HBox(16, heading, Ui.spacer(), Ui.light("FOR RENT", 11));
        titleRow.setAlignment(Pos.TOP_LEFT);

        GridPane items = Ui.grid(3, 16,
                Ui.tile("SEWING MACHINE", "€17/DAY", 0.55),
                Ui.tile("COMPUTER", "€20/DAY", 0.55),
                Ui.tile("GUITAR", "€13/DAY", 0.55),
                Ui.tile("TENT", "€9/DAY", 0.55),
                Ui.tile("LUXURY BAG", "€26/DAY", 0.55),
                Ui.tile("BIKE", "€31/DAY", 0.55),
                Ui.tile("BOOK", "€5/DAY", 0.55),
                Ui.tile("TOY CAR", "€6/DAY", 0.55),
                Ui.tile("SNOWBOARD", "€13/DAY", 0.55));

        VBox rating = new VBox(16,
                Ui.light("LATEST RATING", 11),
                Ui.boldCentered("The item was exactly as described and worked perfectly. "
                        + "Communication was friendly and everything went smoothly from start to finish.", 28),
                Ui.label("★★★★★", 28, "-fx-text-fill: #ffd000;"));
        rating.setAlignment(Pos.CENTER);
        rating.setMaxWidth(Double.MAX_VALUE);

        return Ui.page(header, titleRow, items, rating, Ui.footer());
    }
}