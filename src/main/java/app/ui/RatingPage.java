package app.ui;

import app.model.Asset;
import app.model.Booking;
import app.model.Rating;
import app.model.User;
import app.model.enums.BookingStatus;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;


/**
 * Page for managing ratings and reputation.
 *
 * Renters: submit ratings for completed bookings.
 * Lenders: see their average reputation score and average rating per asset,
 *          plus all individual ratings they have received.
 */

public class RatingPage {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public StackPane build() {

        User me = ShareS.session.getActiveUser();

        if (me == null) {
            return Ui.buildPage(
                    new VBox(8, Ui.bold("RATINGS", 20),
                            Ui.light("You need to be logged in.", 13)));
        }

        boolean isRenter = ShareS.userService.hasRole(me.getId(), "renter");
        boolean isLender = ShareS.userService.hasRole(me.getId(), "lender");

        VBox submitSection    = isRenter ? buildSubmitSection(me)   : null;
        VBox reputationSection = isLender ? buildReputationSection(me) : null;
        VBox itemSection      = isLender ? buildItemRatingsSection(me) : null;
        VBox receivedSection  = isLender ? buildReceivedSection(me)  : null;

        if (submitSection != null && reputationSection != null) {
            return Ui.buildPage(reputationSection, itemSection, submitSection, receivedSection);
        } else if (submitSection != null) {
            return Ui.buildPage(submitSection);
        } else if (reputationSection != null) {
            return Ui.buildPage(reputationSection, itemSection, receivedSection);
        } else {
            return Ui.buildPage(
                    new VBox(8, Ui.bold("RATINGS", 20),
                            Ui.light("No ratings available for your role.", 13)));
        }
    }

    private VBox buildReputationSection(User me) {
        double avg = ShareS.ratingService.getAverageForUser(me.getId());
        List<Rating> all = ShareS.ratingService.findByRatedUser(me.getId());

        HBox title = new HBox(Ui.bold("MY REPUTATION", 20));
        title.setAlignment(Pos.BOTTOM_LEFT);

        String starsText  = avg > 0 ? stars(avg) : "☆☆☆☆☆";
        String avgText    = avg > 0 ? String.format("%.1f / 5.0", avg) : "No ratings yet";
        String countText  = all.size() + (all.size() == 1 ? " rating" : " ratings");

        HBox scoreRow = new HBox(12,
                Ui.label(starsText, 28, "-fx-text-fill: #ffd000;"),
                Ui.bold(avgText, 20),
                Ui.light(countText, 13));
        scoreRow.setAlignment(Pos.CENTER_LEFT);

        VBox section = new VBox(12, title, scoreRow);
        section.setPadding(new Insets(16));
        section.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 10;");
        section.setMaxWidth(Double.MAX_VALUE);
        return section;
    }

    private VBox buildItemRatingsSection(User me) {
        HBox title = new HBox(Ui.bold("ITEM RATINGS", 20));
        title.setAlignment(Pos.BOTTOM_LEFT);

        List<Asset> assets = ShareS.assetService.findByOwner(me.getId());

        if (assets.isEmpty()) {
            return new VBox(8, title, Ui.light("No listings yet.", 13));
        }

        GridPane g = new GridPane();
        g.setHgap(24);
        g.setVgap(12);
        g.setMaxWidth(Double.MAX_VALUE);

        g.add(Ui.light("ITEM", 11), 0, 0);
        g.add(Ui.light("STARS", 11), 1, 0);
        g.add(Ui.light("AVERAGE", 11), 2, 0);
        g.add(Ui.light("RATINGS", 11), 3, 0);

        for (int i = 0; i < assets.size(); i++) {
            Asset a = assets.get(i);
            double avg = ShareS.ratingService.getAverageForAsset(a.getId());
            List<Rating> assetRatings = ShareS.ratingService.findByAsset(a.getId());

            String starsText = avg > 0 ? stars(avg) : "☆☆☆☆☆";
            String avgText   = avg > 0 ? String.format("%.1f / 5.0", avg) : "—";
            String countText = assetRatings.size() + (assetRatings.size() == 1 ? " rating" : " ratings");

            g.add(Ui.bold(a.getModel(), 13), 0, i + 1);
            g.add(Ui.label(starsText, 14, "-fx-text-fill: #ffd000;"), 1, i + 1);
            g.add(Ui.light(avgText, 13), 2, i + 1);
            g.add(Ui.light(countText, 13), 3, i + 1);
        }

        return new VBox(12, title, g);
    }

    private VBox buildSubmitSection(User me) {
        HBox title = new HBox(Ui.bold("SUBMIT A RATING", 20));
        title.setAlignment(Pos.BOTTOM_LEFT);

        List<Booking> completedBookings = ShareS.bookingService
                .findByRenter(me.getId())
                .stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .toList();

        if (completedBookings.isEmpty()) {
            return new VBox(8, title, Ui.light("No completed bookings to rate yet.", 13));
        }

        VBox bookingRows = new VBox(16);
        for (Booking booking : completedBookings) {
            bookingRows.getChildren().add(buildRatingRow(booking, me));
        }
        return new VBox(16, title, bookingRows);
    }

    private VBox buildReceivedSection(User me) {
        HBox title = new HBox(Ui.bold("MY RECEIVED RATINGS", 20));
        title.setAlignment(Pos.BOTTOM_LEFT);

        List<Rating> received = ShareS.ratingService.findByRatedUser(me.getId());
        if (received.isEmpty()) {
            return new VBox(8, title, Ui.light("No ratings received yet.", 13));
        }

        GridPane g = new GridPane();
        g.setHgap(16);
        g.setVgap(12);
        g.setMaxWidth(Double.MAX_VALUE);

        g.add(Ui.light("STARS", 11), 0, 0);
        g.add(Ui.light("FROM", 11), 1, 0);

        for (int i = 0; i < received.size(); i++) {
            Rating r = received.get(i);
            User reviewer = ShareS.userService.findById(r.getReviewerId());
            String reviewerName = reviewer != null ? "@" + reviewer.getUsername() : "#" + r.getReviewerId();

            g.add(Ui.label(stars(r.getRatingValue()), 14, "-fx-text-fill: #ffd000;"), 0, i + 1);
            g.add(Ui.light(reviewerName, 13), 1, i + 1);
        }

        return new VBox(16, title, g);
    }

    private VBox buildRatingRow(Booking booking, User me) {
        Asset asset = ShareS.assetService.findById(booking.getAssetId());
        String itemName = asset != null ? asset.getModel().toUpperCase() : "#" + booking.getAssetId();

        Label itemLabel = Ui.bold(itemName, 15);
        Label dateLabel = Ui.light(
                booking.getStartTime().format(DATE_FMT) + " → " + booking.getEndTime().format(DATE_FMT), 12);

        Rating existingRating = ShareS.ratingService.findByBooking(booking.getId())
                .stream()
                .filter(r -> r.getReviewerId() == me.getId())
                .findFirst()
                .orElse(null);

        int initialRating = existingRating != null ? existingRating.getRatingValue() : 0;

        // clickable star rating
        int[] selectedRating = {initialRating};
        Label[] starLabels = new Label[5];
        HBox starBox = new HBox(4);

        for (int s = 0; s < 5; s++) {
            final int starIndex = s + 1;
            Label star = Ui.label("★", 28, s < initialRating
                    ? "-fx-text-fill: #ffd000; -fx-cursor: hand;"
                    : "-fx-text-fill: #cccccc; -fx-cursor: hand;");
            starLabels[s] = star;
            star.setOnMouseClicked(e -> {
                selectedRating[0] = starIndex;
                for (int j = 0; j < 5; j++) {
                    starLabels[j].setStyle(j < starIndex
                            ? "-fx-text-fill: #ffd000; -fx-cursor: hand;"
                            : "-fx-text-fill: #cccccc; -fx-cursor: hand;");
                }
            });
            starBox.getChildren().add(star);
        }

        Label feedback = Ui.light("", 12);

        VBox row;
        if (existingRating != null) {
            // already rated — show stars as read-only
            row = new VBox(8,
                    itemLabel, dateLabel,
                    Ui.light("Your rating", 11), starBox);
        } else {
            Button submit = Ui.button("Submit Rating", 13,
                    "-fx-background-color: #ffd000; -fx-text-fill: #333333;");
            submit.setOnAction(e -> {
                if (selectedRating[0] == 0) {
                    feedback.setText("Please select a star rating.");
                    feedback.setStyle("-fx-text-fill: #e53935;");
                    return;
                }
                Integer ratedUserId = asset != null ? asset.getOwnerId() : null;

                Rating rating = new Rating(
                        booking.getId(),
                        me.getId(),
                        ratedUserId,
                        selectedRating[0],
                        null);

                Rating result = ShareS.ratingService.submitRating(rating);
                if (result != null) {
                    feedback.setText("Rating submitted!");
                    feedback.setStyle("-fx-text-fill: green;");
                    submit.setDisable(true);
                    starBox.setDisable(true);
                } else {
                    feedback.setText("Failed to submit rating.");
                    feedback.setStyle("-fx-text-fill: #e53935;");
                }
            });
            row = new VBox(8,
                    itemLabel, dateLabel,
                    Ui.light("Rating (1-5 stars)", 11), starBox,
                    feedback, submit);
        }
        row.setPadding(new Insets(16));
        row.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 10;");
        row.setMaxWidth(500);
        return row;
    }

    private String stars(double value) {
        int full = (int) Math.round(value);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) sb.append(i < full ? "★" : "☆");
        return sb.toString();
    }
}
