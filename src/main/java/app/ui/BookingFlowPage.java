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
import javafx.scene.layout.*;

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

    public StackPane build() {

        HBox title = new HBox(16,
                Ui.bold("BOOK " + asset.getModel().toUpperCase(), 28),
                Ui.spacer(),
                Ui.light("BOOKING", 11));
        title.setAlignment(Pos.BOTTOM_LEFT);

        User me = ShareS.session.getActiveUser();
        if (asset.getOwnerId() == me.getId()) {
            return Ui.buildPage(title,
                    Ui.light("You cannot book your own listing.", 13));
        }

        Location loc = ShareS.assetService.getLocationFor(asset.getId(), me.getId());
        VBox summary = new VBox(4,
                Ui.bold(asset.getModel().toUpperCase(), 20),
                Ui.light("€" + String.format("%.0f", asset.getDailyRate()) + " / day", 13));
        String discount = Ui.discountText(asset);
        if (discount != null) {
            summary.getChildren().add(Ui.light(discount, 11));
        }
        if (asset.getCondition() != null && !asset.getCondition().isBlank()) {
            summary.getChildren().add(Ui.light("Condition: " + asset.getCondition(), 11));
        }
        if (loc != null) {
            summary.getChildren().add(Ui.light(Ui.formatLocation(loc), 11));
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

        boolean needsName = !ShareS.userService.hasName(me.getId());
        boolean needsLocation = !ShareS.userService.hasLocation(me.getId());

        TextField firstName = new TextField();
        firstName.setPromptText("first name");
        firstName.setMaxWidth(300);
        TextField lastName = new TextField();
        lastName.setPromptText("last name");
        lastName.setMaxWidth(300);

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

        VBox detailsBox = new VBox(10);
        if (needsName || needsLocation) {
            detailsBox.getChildren().add(Ui.light("We need a few details for your first booking", 11));
        }
        if (needsName) {
            detailsBox.getChildren().addAll(
                    Ui.light("First Name", 11), firstName,
                    Ui.light("Last Name", 11), lastName);
        }
        if (needsLocation) {
            detailsBox.getChildren().addAll(
                    Ui.light("City", 11), city,
                    Ui.light("Postal Code", 11), postalCode,
                    Ui.light("District", 11), district,
                    Ui.light("Street Address", 11), streetAddress,
                    Ui.light("Country", 11), country);
        }

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

            if (needsName) {
                String firstText = firstName.getText().trim();
                String lastText = lastName.getText().trim();
                if (firstText.isEmpty() || lastText.isEmpty()) {
                    showError(error, "First and last name are required.");
                    return;
                }
                if (!ShareS.userService.updateName(me.getId(), firstText, lastText)) {
                    showError(error, "Could not save your name.");
                    return;
                }
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
        if (needsName || needsLocation) {
            form.getChildren().add(detailsBox);
        }
        form.getChildren().addAll(error, confirm);
        form.setMinWidth(320);
        form.setPrefWidth(320);

        VBox picture = new VBox(12,
                Ui.imageBox(560, 320, ShareS.assetService.getImage(asset.getId())),
                Ui.ownerCard(asset.getOwnerId()));
        picture.setMaxWidth(560);

        HBox content = new HBox(48, form, picture);
        content.setAlignment(Pos.TOP_LEFT);
        content.setMaxWidth(Region.USE_PREF_SIZE);

        // centre the whole booking block on the page, like the listing view
        HBox contentWrap = new HBox(content);
        contentWrap.setAlignment(Pos.CENTER);

        return Ui.buildPage(title, contentWrap);
    }

    private void showError(Label error, String message) {
        error.setText(message);
        error.setStyle("-fx-text-fill: #e53935;");
    }
}
