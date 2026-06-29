package app.ui;

import app.model.Asset;
import app.model.Booking;
import app.model.User;
import app.model.enums.BookingStatus;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookingPage {

    private static final String IC_PERSON  = "M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z";
    private static final String IC_INVOICE = "M18 17H6v-2h12v2zm0-4H6v-2h12v2zm0-4H6V7h12v2zM3 22l1.5-1.5L6 22l1.5-1.5L9 22l1.5-1.5L12 22l1.5-1.5L15 22l1.5-1.5L18 22l1.5-1.5L21 22V2l-1.5 1.5L18 2l-1.5 1.5L15 2l-1.5 1.5L12 2l-1.5 1.5L9 2 7.5 3.5 6 2 4.5 3.5 3 2v20z";
    private static final String IC_EYE     = "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17a5 5 0 1 1 0-10 5 5 0 0 1 0 10zm0-8a3 3 0 1 0 0 6 3 3 0 0 0 0-6z";
    private static final String IC_CHECK   = "M9 16.17 4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z";
    private static final String IC_BLOCK   = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.42 0-8-3.58-8-8 0-1.85.63-3.55 1.69-4.9L16.9 18.31C15.55 19.37 13.85 20 12 20zm6.31-3.1L7.1 5.69C8.45 4.63 10.15 4 12 4c4.42 0 8 3.58 8 8 0 1.85-.63 3.55-1.69 4.9z";

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public StackPane build() {

        HBox title = new HBox(16,
                Ui.bold("BOOKINGS", 28), Ui.spacer(), Ui.light("MY RENTALS", 11));
        title.setAlignment(Pos.BOTTOM_LEFT);

        List<Node> children = new ArrayList<>();
        children.add(title);

        User activeUser = ShareS.session.getActiveUser();
        if (activeUser == null) {
            children.add(Ui.light("Log in to see your bookings.", 13));
            return Ui.buildPage(children.toArray(new Node[0]));
        }

        boolean isRenter = ShareS.userService.hasRole(activeUser.getId(), "renter");
        boolean isLender = ShareS.userService.hasRole(activeUser.getId(), "lender");

        // what you booked from others (the booker's own bookings)
        if (isRenter) {
            children.add(Ui.light("MY BOOKINGS", 11));
            children.add(buildRenterTable(activeUser));
        }

        // your listings and the requests coming in on them
        if (isLender) {
            children.add(buildListingsSection(activeUser));
            children.add(Ui.light("INCOMING BOOKINGS", 11));
            children.add(buildTable());
        }

        return Ui.buildPage(children.toArray(new Node[0]));
    }

    private VBox buildListingsSection(User user) {
        List<Asset> mine = ShareS.assetService.findByOwner(user.getId());
        List<Node> tiles = new ArrayList<>();
        tiles.add(Ui.addTile("NEW LISTING", 0.55, ShareS::showCreateListingPage));
        for (Asset a : mine) {
            boolean locked = ShareS.assetService.hasActiveBookings(a.getId());
            tiles.add(Ui.ownerTile(
                    a.getModel().toUpperCase(),
                    "€" + String.format("%.0f", a.getDailyRate()) + "/DAY · "
                            + ShareS.catalogService.getCategoryPath(a.getSubCategoryId()).toUpperCase(),
                    0.55,
                    ShareS.assetService.getImage(a.getId()),
                    locked ? "Locked! Has active bookings" : null,
                    () -> ShareS.showListingDetailPage(a),
                    () -> ShareS.showEditListingPage(a),
                    () -> confirmDelete(a)));
        }

        GridPane grid = Ui.grid(3, 16, tiles.toArray(new Node[0]));
        return new VBox(16, Ui.light("MY LISTINGS", 11), grid);
    }

    private void confirmDelete(Asset asset) {
        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete listing \"" + asset.getModel() + "\"?", deleteButton, cancelButton);
        alert.setTitle("Delete listing");
        alert.setHeaderText("Warning");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == deleteButton) {
            try {
                ShareS.assetService.deleteAsset(asset.getId(), ShareS.session.getActiveUser().getId());
            } catch (RuntimeException ex) {
                new Alert(Alert.AlertType.ERROR,
                        "Could not delete this listing. It may have active bookings.").showAndWait();
            }
            ShareS.showBookingPage();
        }
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

            String renterName;
            if (renter == null) {
                renterName = "#" + booking.getRenterId();
            } else {
                renterName = renter.getFullName() != null ? renter.getFullName() : renter.getUsername();
            }

            addBookingRow(g, i + 1, booking, itemName, renterName, renter, asset);
        }

        return g;
    }

    /** The active user's own bookings (what they booked from other lenders). */
    private GridPane buildRenterTable(User user) {
        GridPane g = new GridPane();
        g.setHgap(12);
        g.setVgap(14);
        g.setMaxWidth(Double.MAX_VALUE);

        double[] widths = {11, 11, 22, 18, 12, 10, 16};
        for (double w : widths) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(w);
            g.getColumnConstraints().add(cc);
        }

        String[] heads = {"FROM", "TO", "ITEM", "OWNER", "STATUS", "AMOUNT", ""};
        for (int i = 0; i < heads.length; i++) {
            g.add(Ui.light(heads[i], 11), i, 0);
        }

        List<Booking> bookings = ShareS.bookingService.findByRenter(user.getId());
        for (int i = 0; i < bookings.size(); i++) {
            Booking booking = bookings.get(i);
            Asset asset = ShareS.assetService.findById(booking.getAssetId());

            String itemName = asset != null ? asset.getModel() : "#" + booking.getAssetId();

            // the renter and the owner are the two parties of this booking, so the
            // renter sees the owner's real name here (fallback to username)
            String ownerName = "#" + booking.getAssetId();
            if (asset != null) {
                User owner = ShareS.userService.findById(asset.getOwnerId());
                if (owner != null) {
                    ownerName = owner.getFullName() != null ? owner.getFullName() : owner.getUsername();
                }
            }

            addRenterRow(g, i + 1, booking, itemName, ownerName, asset);
        }

        return g;
    }

    private void addRenterRow(GridPane g, int row, Booking booking,
                              String itemName, String ownerName, Asset asset) {
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
        g.add(Ui.light(ownerName, 13), 3, row);
        g.add(Ui.light(status.getDbValue(), 13), 4, row);
        g.add(Ui.bold("€" + String.format("%.0f", booking.getTotalCost()), 13), 5, row);

        int bookingId = booking.getId();

        //TODO: Add invoice template
        // Button invoiceBtn = Ui.iconButton(IC_INVOICE, "#ffd000", "#333333", "Show invoice",
        //         () -> showInvoice(booking, itemName, "Owner", ownerName));
        Button viewBtn = Ui.iconButton(IC_EYE, "#ffe680", "#333333", "View listing",
                asset != null ? () -> ShareS.showListingDetailPage(asset) : null);
        viewBtn.setDisable(asset == null);
        Button cancelBtn = Ui.iconButton(IC_BLOCK, "#e53935", "#ffffff", "Cancel booking",
                active ? () -> { ShareS.bookingService.cancelBooking(bookingId); ShareS.showBookingPage(); } : null);
        cancelBtn.setDisable(!active);

        HBox actions = new HBox(6, viewBtn, cancelBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);
        g.add(actions, 6, row);
    }

    private void addBookingRow(GridPane g, int row, Booking booking,
                               String itemName, String renterName, User renter, Asset asset) {
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

        Button profileBtn = Ui.iconButton(IC_PERSON, "#d9d9d9", "#555555", "Renter profile",
                renter != null ? () -> ShareS.showUserProfilePage(renter) : null);
        profileBtn.setDisable(renter == null);
        // Button invoiceBtn = Ui.iconButton(IC_INVOICE, "#ffd000", "#333333", "Show invoice",
        //         () -> showInvoice(booking, itemName, "Renter", renterName));
        Button viewBtn = Ui.iconButton(IC_EYE, "#ffe680", "#333333", "View listing",
                asset != null ? () -> ShareS.showListingDetailPage(asset) : null);
        viewBtn.setDisable(asset == null);

        HBox actions = new HBox(6, profileBtn, viewBtn, acceptBtn, declineBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);
        g.add(actions, 6, row);
    }

    // /** Simple invoice popup with the booking's key details. */
    // private void showInvoice(Booking booking, String itemName, String partyLabel, String partyName) {
    //     String body = "Item: " + itemName + "\n"
    //             + partyLabel + ": " + partyName + "\n"
    //             + "Period: " + booking.getStartTime().format(DATE_FMT)
    //             + " - " + booking.getEndTime().format(DATE_FMT) + "\n"
    //             + "Status: " + booking.getStatus().getDbValue() + "\n"
    //             + "Total: €" + String.format("%.2f", booking.getTotalCost());
    //     Alert alert = new Alert(Alert.AlertType.INFORMATION, body);
    //     alert.setTitle("Invoice");
    //     alert.setHeaderText("Booking #" + booking.getId());
    //     alert.showAndWait();
    // }
}
