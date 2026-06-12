package com.sharespace;

import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class CatalogPage {

    public VBox build() {
        Region header = Ui.header(
                new String[]{"CATALOG", "BOOKINGS", "PROFILE"},
                new Runnable[]{ShareS::showCatalogPage, ShareS::showBookingPage, ShareS::showProfilePage},
                ShareS::showStartPage);

        HBox title = new HBox(16,
                Ui.bold("CATALOG", 28), Ui.spacer(), Ui.light("BEST CATEGORY", 11));
        title.setAlignment(Pos.BOTTOM_LEFT);

        GridPane categories = Ui.grid(3, 16,
                Ui.tile("TOOLS", "FROM €5/DAY", 0.48),
                Ui.tile("ELECTRONICS", "FROM €20/DAY", 0.48),
                Ui.tile("EVENT", "FROM €10/DAY", 0.48),
                Ui.tile("OUTDOOR", "FROM €8/DAY", 0.48),
                Ui.tile("FASHION", "FROM €12/DAY", 0.48),
                Ui.tile("MOBILITY", "FROM €25/DAY", 0.48),
                Ui.tile("HOME", "FROM €5/DAY", 0.48),
                Ui.tile("KIDS", "FROM €5/DAY", 0.48),
                Ui.tile("SPORTS", "FROM €8/DAY", 0.48));

        return Ui.page(header, title, categories, Ui.footer());
    }

    private HBox service(String title, String desc) {
        VBox txt = new VBox(10, Ui.boldCentered(title, 28), Ui.light(desc, 13));
        txt.setAlignment(Pos.CENTER);
        HBox.setHgrow(txt, Priority.ALWAYS);

        HBox row = new HBox(50, txt);
        row.setAlignment(Pos.CENTER);
        return row;
    }
}