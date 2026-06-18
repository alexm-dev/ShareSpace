package app.ui;

import app.model.Asset;
import app.model.Rating;
import app.model.User;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class ProfilePage {

    public VBox build() {
        Region header = Ui.header(
                new String[]{"CATALOG", "BOOKINGS", "SETTINGS"},
                new Runnable[]{ShareS::showCatalogPage, ShareS::showBookingPage, ShareS::showProfileSettingsPage},
                ShareS::showStartPage);

        User user = ShareS.session.getActiveUser();
        String displayName = user != null ? "@" + user.getUsername().toUpperCase() : "@GUEST";

        List<Rating> allRatings = user != null
                ? ShareS.ratingService.findByRatedUser(user.getId())
                : List.of();
        double avg = allRatings.stream().mapToInt(Rating::getRatingValue).average().orElse(0.0);
        Rating latest = allRatings.isEmpty() ? null : allRatings.get(allRatings.size() - 1);

        VBox heading = new VBox(4,
                Ui.bold(displayName, 28),
                Ui.label(stars(avg), 13, "-fx-text-fill: #ffd000;"));

        HBox titleRow = new HBox(16, heading, Ui.spacer(), Ui.light("FOR RENT", 11));
        titleRow.setAlignment(Pos.TOP_LEFT);

        List<Asset> assets = user != null
                ? ShareS.assetService.findByOwner(user.getId())
                : List.of();
        Node[] tiles = assets.stream()
                .map(a -> (Node) Ui.tile(
                        a.getModel().toUpperCase(),
                        "€" + String.format("%.0f", a.getDailyRate()) + "/DAY",
                        0.55))
                .toArray(Node[]::new);
        GridPane items = Ui.grid(3, 16, tiles);

        VBox ratingSection;
        if (latest != null) {
            ratingSection = new VBox(16,
                    Ui.light("LATEST RATING", 11),
                    Ui.boldCentered(latest.getComment(), 28),
                    Ui.label(stars(latest.getRatingValue()), 28, "-fx-text-fill: #ffd000;"));
        } else {
            ratingSection = new VBox(16,
                    Ui.light("LATEST RATING", 11),
                    Ui.boldCentered("No ratings yet.", 28));
        }
        ratingSection.setAlignment(Pos.CENTER);
        ratingSection.setMaxWidth(Double.MAX_VALUE);

        return Ui.page(header, titleRow, items, ratingSection, Ui.footer());
    }

    private String stars(double value) {
        int full = (int) Math.round(value);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) sb.append(i < full ? "★" : "☆");
        return sb.toString();
    }
}
