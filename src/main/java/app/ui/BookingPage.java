package app.ui;

import app.model.Asset;
import app.model.Booking;
import app.model.User;
import app.model.enums.BookingStatus;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BookingPage {

    private static final String IC_PERSON  = "M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z";
    private static final String IC_INVOICE = "M18 17H6v-2h12v2zm0-4H6v-2h12v2zm0-4H6V7h12v2zM3 22l1.5-1.5L6 22l1.5-1.5L9 22l1.5-1.5L12 22l1.5-1.5L15 22l1.5-1.5L18 22l1.5-1.5L21 22V2l-1.5 1.5L18 2l-1.5 1.5L15 2l-1.5 1.5L12 2l-1.5 1.5L9 2 7.5 3.5 6 2 4.5 3.5 3 2v20z";
    private static final String IC_EYE     = "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17a5 5 0 1 1 0-10 5 5 0 0 1 0 10zm0-8a3 3 0 1 0 0 6 3 3 0 0 0 0-6z";
    private static final String IC_CHECK   = "M9 16.17 4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z";
    private static final String IC_BLOCK   = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.42 0-8-3.58-8-8 0-1.85.63-3.55 1.69-4.9L16.9 18.31C15.55 19.37 13.85 20 12 20zm6.31-3.1L7.1 5.69C8.45 4.63 10.15 4 12 4c4.42 0 8 3.58 8 8 0 1.85-.63 3.55-1.69 4.9z";

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

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

        User activeUser = ShareS.session.getActiveUser();
        if (activeUser == null) return g;

        List<Asset> ownedAssets = ShareS.assetService.findByOwner(activeUser.getId());
        List<Booking> bookings = new ArrayList<>();
        for (Asset asset : ownedAssets) {
            bookings.addAll(ShareS.bookingService.findByAsset(asset.getId()));
        }

        for (int i = 0; i < bookings.size(); i++) {
            Booking booking = bookings.get(i);
            Asset asset = ShareS.assetService.findById(booking.getAssetId());
            User renter = ShareS.userService.findById(booking.getRenterId());

            String itemName   = asset  != null ? asset.getModel()    : "#" + booking.getAssetId();
            String renterName = renter != null ? renter.getUsername() : "#" + booking.getRenterId();

            addBookingRow(g, i + 1, booking, itemName, renterName);
        }

        return g;
    }

    private void addBookingRow(GridPane g, int row, Booking booking,
                               String itemName, String renterName) {
        BookingStatus status = booking.getStatus();
        boolean active = status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;

        Region bg = new Region();
        bg.setMinHeight(48);
        if (!active) {
            bg.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 10;");
        }
        GridPane.setColumnSpan(bg, 7);
        GridPane.setMargin(bg, new Insets(0, -12, 0, -12));
        g.add(bg, 0, row);

        g.add(Ui.bold(booking.getStartTime().format(DATE_FMT), 13), 0, row);
        g.add(Ui.bold(booking.getEndTime().format(DATE_FMT), 13), 1, row);
        g.add(Ui.bold(itemName, 13), 2, row);
        g.add(Ui.light(renterName, 13), 3, row);
        g.add(Ui.light(status.getDbValue(), 13), 4, row);
        g.add(Ui.bold("€" + String.format("%.0f", booking.getTotalCost()), 13), 5, row);

        boolean canAct = status == BookingStatus.PENDING;
        int bookingId = booking.getId();

        Button acceptBtn = Ui.iconButton(IC_CHECK, "#4caf50", "#ffffff", "Accept booking",
                canAct ? () -> { ShareS.bookingService.confirmBooking(bookingId); ShareS.showBookingPage(); } : null);
        Button declineBtn = Ui.iconButton(IC_BLOCK, "#e53935", "#ffffff", "Decline booking",
                canAct ? () -> { ShareS.bookingService.cancelBooking(bookingId); ShareS.showBookingPage(); } : null);
        acceptBtn.setDisable(!canAct);
        declineBtn.setDisable(!canAct);

        HBox actions = new HBox(6,
                Ui.iconButton(IC_PERSON,  "#d9d9d9", "#555555", "Renter profile", null),
                Ui.iconButton(IC_INVOICE, "#ffd000", "#333333", "Show invoice",   null),
                Ui.iconButton(IC_EYE,     "#ffe680", "#333333", "Show / hide",    null),
                acceptBtn, declineBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);
        g.add(actions, 6, row);
    }
}
