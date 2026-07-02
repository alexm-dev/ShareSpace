package app.ui;

import app.model.Asset;
import app.model.Rating;
import app.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ProfilePage is the UI page for viewing a users profile.
 * If a user is viewing another users profile, then the ProfilePage becomes read-only.
 * It displays user information, listings and ratings.
 */
public class ProfilePage {

    /** The user whose profile to show; null means "the logged-in users own". */
    private final User viewedUser;

    public ProfilePage() {
        this.viewedUser = null;
    }

    public ProfilePage(User viewedUser) {
        this.viewedUser = viewedUser;
    }

    public StackPane build() {

        User active = ShareS.session.getActiveUser();
        User user = viewedUser != null ? viewedUser : active;
        boolean own = user != null && active != null && user.getId() == active.getId();
        String displayName = user != null ? "@" + user.getUsername().toUpperCase() : "@GUEST";

        List<Rating> allRatings = user != null
            ? ShareS.ratingService.findByRatedUser(user.getId())
            : List.of();
        double avg = allRatings.stream().mapToInt(Rating::getRatingValue).average().orElse(0.0);

        VBox heading = new VBox(4,
                Ui.bold(displayName, 28),
                Ui.label(stars(avg), 13, "-fx-text-fill: #ffd000;"));

        Region avatar = Ui.avatar(72,
                user != null ? ShareS.userService.getProfileImage(user.getId()) : null,
                own ? () -> chooseAvatar(user) : null);
        if (own) {
            avatar.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    chooseAvatar(user);
                }
            });
            avatar.setOnContextMenuRequested(e -> {
                if (ShareS.userService.getProfileImage(user.getId()) != null) {
                    Ui.showImageMenu(avatar, true,
                            () -> chooseAvatar(user),
                            () -> {
                                ShareS.userService.deleteProfileImage(user.getId());
                                ShareS.showProfilePage();
                            });
                }
            });
        }

        HBox identity = new HBox(16, avatar, heading);
        identity.setAlignment(Pos.CENTER_LEFT);

        HBox titleRow = new HBox(16, identity, Ui.spacer(), Ui.light("FOR RENT", 11));
        titleRow.setAlignment(Pos.BOTTOM_LEFT);

        List<Asset> assets = user != null
            ? ShareS.assetService.findByOwner(user.getId())
            : List.of();
        List<Node> tileList = new ArrayList<>();
        if (own) {
            tileList.add(Ui.addTile("NEW LISTING", 0.55, ShareS::showCreateListingPage));
        }
        for (Asset a : assets) {
            String caption = "€" + String.format("%.0f", a.getDailyRate()) + "/DAY · "
                + ShareS.catalogService.getCategoryPath(a.getSubCategoryId()).toUpperCase();
            if (own) {
                boolean locked = ShareS.assetService.hasActiveBookings(a.getId());
                tileList.add(Ui.ownerTile(
                            a.getModel().toUpperCase(), caption, 0.55,
                            ShareS.assetService.getImage(a.getId()),
                            locked ? "Locked! Has active bookings" : null,
                            () -> ShareS.showListingDetailPage(a),
                            () -> ShareS.showEditListingPage(a),
                            () -> confirmDelete(a)));
            } else {
                // read-only tile when viewing someone else's profile
                tileList.add(Ui.tile(
                            a.getModel().toUpperCase(), caption, 0.55,
                            ShareS.assetService.getImage(a.getId()),
                            () -> ShareS.showListingDetailPage(a)));
            }
        }
        GridPane items = Ui.grid(3, 16, tileList.toArray(new Node[0]));

        List<Rating> latestUniqueRatings = newestUniqueRatings(allRatings);
        VBox ratingSection;
        if (!latestUniqueRatings.isEmpty()) {
            List<Node> ratingNodes = new ArrayList<>();
            ratingNodes.add(Ui.light("LATEST RATINGS", 11));
            for (int i = 0; i < latestUniqueRatings.size() && i < 3; i++) {
                ratingNodes.add(ratingSnippet(latestUniqueRatings.get(i)));
            }
            ratingSection = new VBox(16, ratingNodes.toArray(new Node[0]));
        } else {
            Label noRatings = Ui.boldCentered("No ratings yet.", 28);
            noRatings.setMaxWidth(Double.MAX_VALUE);
            noRatings.setAlignment(Pos.CENTER);
            noRatings.setWrapText(true);
            ratingSection = new VBox(16,
                    Ui.light("LATEST RATINGS", 11),
                    noRatings);
        }
        ratingSection.setAlignment(Pos.CENTER);
        ratingSection.setMaxWidth(Double.MAX_VALUE);

        return Ui.buildPage(titleRow, items, ratingSection);
    }

    private VBox ratingSnippet(Rating rating) {
        Label comment = Ui.boldCentered(
                rating.getComment() != null && !rating.getComment().isBlank() ? rating.getComment() : "—", 20);
        comment.setMaxWidth(Double.MAX_VALUE);
        comment.setAlignment(Pos.CENTER);
        comment.setWrapText(true);

        VBox block = new VBox(8,
                comment,
                Ui.label(stars(rating.getRatingValue()), 22, "-fx-text-fill: #ffd000;"),
                Ui.light("FROM", 11),
                Ui.ownerCard(rating.getReviewerId()));
        block.setAlignment(Pos.CENTER);
        block.setMaxWidth(Double.MAX_VALUE);
        block.setPadding(new Insets(10, 14, 10, 14));
        block.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 10;");
        Ui.addHoverPop(block);
        return block;
    }

    private List<Rating> newestUniqueRatings(List<Rating> ratings) {
        Map<String, Rating> unique = new LinkedHashMap<>();
        ratings.stream()
            .sorted((a, b) -> {
                int time = b.getCreatedTime().compareTo(a.getCreatedTime());
                if (time != 0) return time;
                return Integer.compare(b.getId(), a.getId());
            })
        .forEach(r -> unique.putIfAbsent(r.getBookingId() + ":" + r.getReviewerId(), r));
        return new ArrayList<>(unique.values());
    }

    private void chooseAvatar(User user) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose a profile photo");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File file = chooser.showOpenDialog(ShareS.primaryStage);
        if (file == null) {
            return;
        }
        try {
            byte[] cropped = CropDialog.crop(Files.readAllBytes(file.toPath()), 1.0);
            if (cropped == null) {
                return;
            }
            ShareS.userService.saveProfileImage(user.getId(), cropped, "image/jpeg");
            ShareS.showProfilePage();
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "Could not read that image file.").showAndWait();
        }
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
            ShareS.showProfilePage();
        }
    }

    private String stars(double value) {
        int full = (int) Math.round(value);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) sb.append(i < full ? "★" : "☆");
        return sb.toString();
    }
}
