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
import java.util.Comparator;
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
        g.add(Ui.light("COMMENT", 11), 1, 0);
        g.add(Ui.light("FROM", 11), 2, 0);

        for (int i = 0; i < received.size(); i++) {
            Rating r = received.get(i);
            User reviewer = ShareS.userService.findById(r.getReviewerId());
            String commentText = r.getComment() != null ? r.getComment() : "—";

            g.add(Ui.label(stars(r.getRatingValue()), 14, "-fx-text-fill: #ffd000;"), 0, i + 1);
            g.add(Ui.light(commentText, 13), 1, i + 1);
            g.add(reviewer != null
                    ? Ui.ownerCard(reviewer.getId())
                    : Ui.light("#" + r.getReviewerId(), 13), 2, i + 1);
        }

        return new VBox(16, title, g);
    }

    private VBox buildRatingRow(Booking booking, User me) {
        Asset asset = ShareS.assetService.findById(booking.getAssetId());
        String itemName = asset != null ? asset.getModel().toUpperCase() : "#" + booking.getAssetId();
        Integer ratedUserId = asset != null ? asset.getOwnerId() : null;

        Label itemLabel = Ui.bold(itemName, 15);
        Label dateLabel = Ui.light(
                booking.getStartTime().format(DATE_FMT) + " → " + booking.getEndTime().format(DATE_FMT), 12);

        List<Rating> bookingRatings = ShareS.ratingService.findByBooking(booking.getId())
                .stream()
                .filter(r -> r.getReviewerId() == me.getId())
                .toList();
        Rating existingRating = bookingRatings.stream()
                .max(Comparator.comparing(Rating::getCreatedTime).thenComparingInt(Rating::getId))
                .orElse(null);
        int[] currentRating = {existingRating != null ? existingRating.getRatingValue() : 0};
        String[] currentComment = {existingRating != null ? existingRating.getComment() : null};
        boolean[] hasRating = {existingRating != null};
        int[] selectedRating = {currentRating[0]};
        Label[] starLabels = new Label[5];
        HBox starBox = new HBox(4);

        Runnable refreshStars = () -> {
            for (int j = 0; j < 5; j++) {
                starLabels[j].setStyle(j < selectedRating[0]
                        ? "-fx-text-fill: #ffd000; -fx-cursor: hand;"
                        : "-fx-text-fill: #cccccc; -fx-cursor: hand;");
            }
        };

        for (int s = 0; s < 5; s++) {
            final int starIndex = s + 1;
            Label star = Ui.label("★", 28, s < currentRating[0]
                    ? "-fx-text-fill: #ffd000; -fx-cursor: hand;"
                    : "-fx-text-fill: #cccccc; -fx-cursor: hand;");
            starLabels[s] = star;
            star.setOnMouseClicked(e -> {
                selectedRating[0] = starIndex;
                refreshStars.run();
            });
            starBox.getChildren().add(star);
        }

        Label feedback = Ui.light("", 12);
        javafx.scene.control.TextArea comment = new javafx.scene.control.TextArea();
        comment.setPromptText("Leave a comment (optional)");
        comment.setPrefRowCount(2);
        comment.setMinWidth(400);
        comment.setPrefWidth(400);
        comment.setMaxWidth(400);
        comment.setWrapText(true);
        if (currentComment[0] != null) {
            comment.setText(currentComment[0]);
        }

        VBox row = new VBox(8);
        row.setPadding(new Insets(16));
        row.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 10;");
        row.setMinWidth(500);
        row.setPrefWidth(500);
        row.setMaxWidth(500);

        Button edit = Ui.button("Edit Rating", 13,
                "-fx-background-color: #bdbdbd; -fx-text-fill: white;");
        Button submit = Ui.button("Submit Rating", 13,
                "-fx-background-color: #ffd000; -fx-text-fill: #333333;");

        Runnable[] showEditor = new Runnable[1];
        Runnable[] showSummary = new Runnable[1];

        showEditor[0] = () -> {
            selectedRating[0] = currentRating[0];
            comment.setText(currentComment[0] != null ? currentComment[0] : "");
            refreshStars.run();
            submit.setText(hasRating[0] ? "Save Changes" : "Submit Rating");
            row.getChildren().setAll(
                    itemLabel, dateLabel,
                    Ui.light("Rating (1-5 stars)", 11), starBox,
                    Ui.light("Comment (optional)", 11), comment,
                    feedback, submit);
        };

        showSummary[0] = () -> {
            String shownComment = currentComment[0] != null && !currentComment[0].isBlank() ? currentComment[0] : "—";
            row.getChildren().setAll(
                    itemLabel, dateLabel,
                    Ui.light("Your rating", 11),
                    Ui.label(stars(currentRating[0]), 18, "-fx-text-fill: #ffd000;"),
                    Ui.light(shownComment, 13),
                    edit,
                    feedback);
        };

        edit.setOnAction(e -> {
            feedback.setText("");
            showEditor[0].run();
        });

        submit.setOnAction(e -> {
            if (selectedRating[0] == 0) {
                feedback.setText("Please select a star rating.");
                feedback.setStyle("-fx-text-fill: #e53935;");
                return;
            }
            String commentText = comment.getText().isBlank() ? null : comment.getText().trim();

            Rating rating = new Rating(
                    booking.getId(),
                    me.getId(),
                    ratedUserId,
                    selectedRating[0],
                    commentText);
            Rating result = ShareS.ratingService.submitRating(rating);
            if (result != null) {
                currentRating[0] = selectedRating[0];
                currentComment[0] = commentText;
                hasRating[0] = true;
                feedback.setText("Rating saved.");
                feedback.setStyle("-fx-text-fill: green;");
                showSummary[0].run();
            } else {
                feedback.setText("Failed to submit rating.");
                feedback.setStyle("-fx-text-fill: #e53935;");
            }
        });

        if (hasRating[0]) {
            showSummary[0].run();
        } else {
            showEditor[0].run();
        }
        return row;
    }

    private String stars(double value) {
        int full = (int) Math.round(value);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) sb.append(i < full ? "★" : "☆");
        return sb.toString();
    }
}
