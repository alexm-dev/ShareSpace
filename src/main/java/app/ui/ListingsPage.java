package app.ui;

import app.model.Asset;
import app.model.SubCategory;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Page that shows all listings of a given sub-category.
 */
public class ListingsPage {

    private final SubCategory subCategory;

    public ListingsPage(SubCategory subCategory) {
        this.subCategory = subCategory;
    }

    public StackPane build() {

        HBox title = new HBox(16,
                Ui.bold(subCategory.getName().toUpperCase(), 28),
                Ui.spacer(),
                Ui.light("PICK A LISTING TO BOOK", 11));
        title.setAlignment(Pos.BOTTOM_LEFT);

        List<Asset> assets = ShareS.catalogService.getAssetsBySubCategory(subCategory.getId());
        if (assets.isEmpty()) {
            return Ui.buildPage(title, Ui.light("No listings in this sub-category yet.", 13));
        }

        Node[] tiles = assets.stream()
                .map(a -> (Node) Ui.tile(
                        a.getModel().toUpperCase(),
                        "€" + String.format("%.0f", a.getDailyRate()) + "/DAY",
                        0.55,
                        ShareS.assetService.getImage(a.getId()),
                        () -> ShareS.showListingDetailPage(a)))
                .toArray(Node[]::new);
        GridPane grid = Ui.grid(3, 16, tiles);

        return Ui.buildPage(title, grid);
    }
}
