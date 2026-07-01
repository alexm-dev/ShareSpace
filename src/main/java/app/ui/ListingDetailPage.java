package app.ui;

import app.model.Asset;
import app.model.Location;
import app.model.User;
import app.util.MetadataUtil;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The listing detail page.
 * Shows the assets details and allows the user to book if logged in.
 */
public class ListingDetailPage {

    private final Asset asset;

    public ListingDetailPage(Asset asset) {
        this.asset = asset;
    }

    public StackPane build() {

        HBox title = new HBox(16,
                Ui.bold(asset.getModel().toUpperCase(), 28),
                Ui.spacer(),
                Ui.light("LISTING", 11));
        title.setAlignment(Pos.BOTTOM_LEFT);

        User me = ShareS.session.getActiveUser();
        int viewerId = me != null ? me.getId() : -1;

        VBox info = new VBox(6,
                Ui.bold(asset.getModel().toUpperCase(), 22),
                Ui.light("€" + String.format("%.0f", asset.getDailyRate()) + " / day", 14));
        String discount = Ui.discountText(asset);
        if (discount != null) {
            info.getChildren().add(Ui.light(discount, 12));
        }
        if (asset.getCondition() != null && !asset.getCondition().isBlank()) {
            info.getChildren().add(Ui.light("Condition: " + asset.getCondition(), 12));
        }
        Location loc = ShareS.assetService.getLocationFor(asset.getId(), viewerId);
        if (loc != null) {
            info.getChildren().add(Ui.light(Ui.formatLocation(loc), 12));
        }
        Map<String, String> meta = MetadataUtil.parse(asset.getMetadata());
        if (!meta.isEmpty()) {
            info.getChildren().add(Ui.light("DETAILS", 11));
            for (Map.Entry<String, String> entry : meta.entrySet()) {
                info.getChildren().add(
                        Ui.light(MetadataUtil.humanizeKey(entry.getKey()) + ": " + entry.getValue(), 12));
            }
        }

        VBox infoCol = new VBox(18, info, action(me));
        infoCol.setMinWidth(300);
        infoCol.setPrefWidth(340);

        VBox picture = new VBox(12,
                Ui.imageBox(560, 320, ShareS.assetService.getImage(asset.getId())),
                Ui.ownerCard(asset.getOwnerId()));
        picture.setMaxWidth(560);

        HBox top = new HBox(48, infoCol, picture);
        top.setAlignment(Pos.TOP_LEFT);
        top.setMaxWidth(Region.USE_PREF_SIZE);

        HBox topWrap = new HBox(top);
        topWrap.setAlignment(Pos.CENTER);
        topWrap.setMaxWidth(Double.MAX_VALUE);

        List<Node> children = new ArrayList<>();
        children.add(title);
        children.add(topWrap);

        if (asset.getDescription() != null && !asset.getDescription().isBlank()) {
            Label body = Ui.label(asset.getDescription(), 14, "");
            body.setMaxWidth(820);
            VBox descBox = new VBox(10, Ui.light("DESCRIPTION", 11), body);
            descBox.setMaxWidth(820);
            VBox descWrap = new VBox(descBox);
            descWrap.setMaxWidth(Double.MAX_VALUE);
            descWrap.setAlignment(Pos.TOP_CENTER);
            children.add(descWrap);
        }

        return Ui.buildPage(children.toArray(new Node[0]));
    }

    private Region action(User me) {
        if (me == null) {
            Button login = primaryButton("Log in to book");
            login.setOnAction(e -> ShareS.showLoginPage());
            return new VBox(8, Ui.light("Log in to book this listing.", 11), login);
        }
        if (asset.getOwnerId() == me.getId()) {
            Button edit = primaryButton("Edit listing");
            if (ShareS.assetService.hasActiveBookings(asset.getId())) {
                edit.setDisable(true);
                return new VBox(8,
                        Ui.light("This is your listing.", 11),
                        Ui.light("Locked! Has active bookings.", 11), edit);
            }
            edit.setOnAction(e -> ShareS.showEditListingPage(asset));
            return new VBox(8, Ui.light("This is your listing.", 11), edit);
        }
        Button book = primaryButton("Book it");
        book.setOnAction(e -> ShareS.showBookingFlowPage(asset));
        return book;
    }

    private Button primaryButton(String text) {
        Button b = Ui.button(text, 13, "-fx-background-color: #bdbdbd; -fx-text-fill: white;");
        b.setMaxWidth(300);
        return b;
    }
}
