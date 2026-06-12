package com.sharespace;

import javafx.geometry.Pos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

public class BookingPage {
    //Piktogramm
    private static final String IC_PERSON  = "M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z";
    private static final String IC_INVOICE = "M18 17H6v-2h12v2zm0-4H6v-2h12v2zm0-4H6V7h12v2zM3 22l1.5-1.5L6 22l1.5-1.5L9 22l1.5-1.5L12 22l1.5-1.5L15 22l1.5-1.5L18 22l1.5-1.5L21 22V2l-1.5 1.5L18 2l-1.5 1.5L15 2l-1.5 1.5L12 2l-1.5 1.5L9 2 7.5 3.5 6 2 4.5 3.5 3 2v20z";
    private static final String IC_EYE     = "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17a5 5 0 1 1 0-10 5 5 0 0 1 0 10zm0-8a3 3 0 1 0 0 6 3 3 0 0 0 0-6z";
    private static final String IC_CHECK   = "M9 16.17 4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z";
    private static final String IC_BLOCK   = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.42 0-8-3.58-8-8 0-1.85.63-3.55 1.69-4.9L16.9 18.31C15.55 19.37 13.85 20 12 20zm6.31-3.1L7.1 5.69C8.45 4.63 10.15 4 12 4c4.42 0 8 3.58 8 8 0 1.85-.63 3.55-1.69 4.9z";

    public VBox build() {
        Region header = Ui.header(
                new String[]{"CATALOG", "BOOKINGS", "PROFILE"},
                new Runnable[]{ShareS::showCatalogPage, ShareS::showBookingPage, ShareS::showProfilePage},
                ShareS::showStartPage);

        HBox title = new HBox(16,
                Ui.bold("BOOKINGS", 28), Ui.spacer(), Ui.light("MY RENTALS", 11));
        title.setAlignment(Pos.BOTTOM_LEFT);

        return Ui.page(header, title, buildTable(), Ui.footer());
    }

    private GridPane buildTable() {
        GridPane g = new GridPane();
        g.setHgap(12);
        g.setVgap(14);
        g.setMaxWidth(Double.MAX_VALUE);

        // From, To, Item, Renter, Status, Amount, Actions
        double[] widths = {11, 11, 18, 15, 10, 9, 26};
        for (double w : widths) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(w);
            g.getColumnConstraints().add(cc);
        }

        String[] heads = {"FROM", "TO", "ITEM", "RENTER", "STATUS", "AMOUNT", ""};
        for (int i = 0; i < heads.length; i++) {
            g.add(Ui.light(heads[i], 11), i, 0);
        }

        addBooking(g, 1, "10.06.2026", "14.06.2026", "Sewing Machine", "Anna Becker",  true,  "€68");
        addBooking(g, 2, "12.06.2026", "13.06.2026", "Computer",       "Lukas Maier",  true,  "€20");
        addBooking(g, 3, "15.06.2026", "20.06.2026", "Guitar",         "Sara Klein",   false, "€65");
        addBooking(g, 4, "18.06.2026", "19.06.2026", "Bike",           "Tom Richter",  false, "€31");
        addBooking(g, 5, "21.06.2026", "28.06.2026", "Snowboard",      "Mia Hoffmann", true,  "€91");

        return g;
    }

    private void addBooking(GridPane g, int row, String from, String to, String item,
                            String renter, boolean active, String amount) {

        Region rowBackground = new Region();
        rowBackground.setMinHeight(48);
        if (!active) {
            rowBackground.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 10;");
        }
        GridPane.setColumnSpan(rowBackground, 7);
        GridPane.setMargin(rowBackground, new Insets(0, -12, 0, -12));
        g.add(rowBackground, 0, row);

        g.add(Ui.bold(from, 13), 0, row);
        g.add(Ui.bold(to, 13), 1, row);
        g.add(Ui.bold(item, 13), 2, row);
        g.add(Ui.light(renter, 13), 3, row);
        g.add(Ui.light(active ? "Active" : "Passive", 13), 4, row);
        g.add(Ui.bold(amount, 13), 5, row);

        HBox actions = new HBox(6,
                Ui.iconButton(IC_PERSON,  "#d9d9d9", "#555555", "Renter profile", null),
                Ui.iconButton(IC_INVOICE, "#ffd000", "#333333", "Show invoice",   null),
                Ui.iconButton(IC_EYE,     "#ffe680", "#333333", "Show / hide",    null),
                Ui.iconButton(IC_CHECK,   "#4caf50", "#ffffff", "Accept booking", null),
                Ui.iconButton(IC_BLOCK,   "#e53935", "#ffffff", "Decline booking", null));
        actions.setAlignment(Pos.CENTER_RIGHT);
        g.add(actions, 6, row);
    }
}