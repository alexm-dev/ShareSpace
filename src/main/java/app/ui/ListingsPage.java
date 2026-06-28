package app.ui;

import app.model.Asset;
import app.model.SubCategory;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Page that shows all listings of a given sub-category.
 */
public class ListingsPage {

    private final SubCategory subCategory;

    public ListingsPage(SubCategory subCategory) {
        this.subCategory = subCategory;
    }

    public VBox build() {
        Region header = Ui.header(
                new String[]{"CATALOG", "BOOKINGS", "PROFILE"},
                new Runnable[]{ShareS::showCatalogPage, ShareS::showBookingPage, ShareS::showProfilePage},
                ShareS::showStartPage);

        HBox title = new HBox(16,
                Ui.bold(subCategory.getName().toUpperCase(), 28),
                Ui.spacer(),
                Ui.light("PICK A LISTING TO BOOK", 11));
        title.setAlignment(Pos.BOTTOM_LEFT);

        List<Asset> assets = ShareS.catalogService.getAssetsBySubCategory(subCategory.getId());
        if (assets.isEmpty()) {
            return Ui.page(header, title, Ui.light("No listings in this sub-category yet.", 13), Ui.footer());
        }

        Node[] tiles = assets.stream()
                .map(a -> (Node) Ui.tile(
                        a.getModel().toUpperCase(),
                        "€" + String.format("%.0f", a.getDailyRate()) + "/DAY",
                        0.55,
                        () -> ShareS.showBookingFlowPage(a)))
                .toArray(Node[]::new);
        GridPane grid = Ui.grid(3, 16, tiles);

        return Ui.page(header, title, grid, Ui.footer());
    }
}
