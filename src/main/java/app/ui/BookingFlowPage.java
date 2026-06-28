package app.ui;

import app.model.Asset;
import app.model.Booking;
import app.model.Location;
import app.model.User;
import app.util.MetadataUtil;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.Map;

/**
 * Flow of the booking process of a specific asset.
 */
public class BookingFlowPage {

    private final Asset asset;

    public BookingFlowPage(Asset asset) {
        this.asset = asset;
    }

    public VBox build() {
        Region header = Ui.header(
                new String[]{"CATALOG", "BOOKINGS", "PROFILE"},
                new Runnable[]{ShareS::showCatalogPage, ShareS::showBookingPage, ShareS::showProfilePage},
                ShareS::showStartPage);

        HBox title = new HBox(16,
                Ui.bold("BOOK " + asset.getModel().toUpperCase(), 28),
                Ui.spacer(),
                Ui.light("BOOKING", 11));
        title.setAlignment(Pos.BOTTOM_LEFT);

        if (!ShareS.session.isLoggedIn()) {
            Button login = Ui.button("Log in to book", 13,
                    "-fx-background-color: #bdbdbd; -fx-text-fill: white;");
            login.setOnAction(e -> ShareS.showLoginPage());
            return Ui.page(header, title,
                    Ui.light("You need to be logged in to book a listing.", 13), login, Ui.footer());
        }

        User me = ShareS.session.getActiveUser();
        if (asset.getOwnerId() == me.getId()) {
            return Ui.page(header, title,
                    Ui.light("You cannot book your own listing.", 13), Ui.footer());
        }

        Location loc = ShareS.assetService.getLocationFor(asset.getId(), me.getId());
        VBox summary = new VBox(4,
                Ui.bold(asset.getModel().toUpperCase(), 20),
                Ui.light("€" + String.format("%.0f", asset.getDailyRate()) + " / day", 13));
        if (asset.getCondition() != null && !asset.getCondition().isBlank()) {
            summary.getChildren().add(Ui.light("Condition: " + asset.getCondition(), 11));
        }
        if (loc != null) {
            summary.getChildren().add(Ui.light(formatLocation(loc), 11));
        }
        if (asset.getDescription() != null && !asset.getDescription().isBlank()) {
            summary.getChildren().add(Ui.light(asset.getDescription(), 11));
        }
        Map<String, String> meta = MetadataUtil.parse(asset.getMetadata());
        for (Map.Entry<String, String> entry : meta.entrySet()) {
            summary.getChildren().add(Ui.light(MetadataUtil.humanizeKey(entry.getKey()) + ": " + entry.getValue(), 11));
        }

        DatePicker start = new DatePicker();
        start.setPromptText("Start date");
        start.setMaxWidth(300);
        start.setEditable(false);
        DatePicker end = new DatePicker();
        end.setPromptText("End date");
        end.setMaxWidth(300);
        end.setEditable(false);

        Label cost = Ui.bold("", 16);
        Label error = Ui.light("", 12);

        Runnable recompute = () -> {
            LocalDate s = start.getValue();
            LocalDate e = end.getValue();
            if (s != null && e != null && !e.isBefore(s)) {
                double c = ShareS.bookingService.calculateCost(asset.getId(), s.atStartOfDay(), e.atStartOfDay());
                cost.setText("Estimated cost: €" + String.format("%.2f", c));
            } else {
                cost.setText("");
            }
        };
        start.valueProperty().addListener((o, a, b) -> recompute.run());
        end.valueProperty().addListener((o, a, b) -> recompute.run());

        boolean needsLocation = !ShareS.userService.hasLocation(me.getId());
        TextField city = new TextField();
        city.setPromptText("city");
        city.setMaxWidth(300);
        TextField postalCode = new TextField();
        postalCode.setPromptText("postal code");
        postalCode.setMaxWidth(300);
        TextField district = new TextField();
        district.setPromptText("district (optional)");
        district.setMaxWidth(300);
        TextField streetAddress = new TextField();
        streetAddress.setPromptText("street address");
        streetAddress.setMaxWidth(300);
        TextField country = new TextField();
        country.setPromptText("country");
        country.setMaxWidth(300);

        VBox locationBox = new VBox(10,
                Ui.light("We need your location for your first booking", 11),
                Ui.light("City", 11), city,
                Ui.light("Postal Code", 11), postalCode,
                Ui.light("District", 11), district,
                Ui.light("Street Address", 11), streetAddress,
                Ui.light("Country", 11), country);

        Button confirm = Ui.button("Confirm booking", 13,
                "-fx-background-color: #bdbdbd; -fx-text-fill: white;");
        confirm.setMaxWidth(300);
        confirm.setOnAction(e -> {
            LocalDate s = start.getValue();
            LocalDate en = end.getValue();
            if (s == null || en == null) {
                showError(error, "Please pick a start and end date.");
                return;
            }
            if (en.isBefore(s)) {
                showError(error, "End date must be on or after the start date.");
                return;
            }

            if (needsLocation) {
                String cityText = city.getText().trim();
                String postalText = postalCode.getText().trim();
                String districtText = district.getText().trim();
                String streetText = streetAddress.getText().trim();
                String countryText = country.getText().trim();
                if (cityText.isEmpty() || postalText.isEmpty() || streetText.isEmpty() || countryText.isEmpty()) {
                    showError(error, "City, postal code, street address and country are required.");
                    return;
                }
                Location toSave = new Location(cityText, postalText,
                        districtText.isEmpty() ? null : districtText, streetText, countryText);
                if (!ShareS.userService.updateLocation(me.getId(), toSave)) {
                    showError(error, "Could not save your location.");
                    return;
                }
            }

            Booking booking = ShareS.bookingService.createBooking(
                    asset.getId(), me.getId(), s.atStartOfDay(), en.atStartOfDay());
            if (booking != null) {
                error.setText("Booking created (status: " + booking.getStatus().getDbValue() + ").");
                error.setStyle("-fx-text-fill: green;");
                confirm.setDisable(true);
            } else {
                showError(error, "Failed to create the booking.");
            }
        });

        VBox form = new VBox(12, summary,
                Ui.light("Start date", 11), start,
                Ui.light("End date", 11), end,
                cost);
        if (needsLocation) {
            form.getChildren().add(locationBox);
        }
        form.getChildren().addAll(error, confirm);

        return Ui.page(header, title, form, Ui.footer());
    }

    private void showError(Label error, String message) {
        error.setText(message);
        error.setStyle("-fx-text-fill: #e53935;");
    }

    private String formatLocation(Location l) {
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
}
