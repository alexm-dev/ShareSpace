package app.ui;

import app.model.Category;
import app.model.SubCategory;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Page that shows all sub-categories of a given category.
 */
public class CategoryPage {

    private final Category category;

    public CategoryPage(Category category) {
        this.category = category;
    }

    public VBox build() {
        Region header = Ui.header(
                new String[]{"CATALOG", "BOOKINGS", "PROFILE"},
                new Runnable[]{ShareS::showCatalogPage, ShareS::showBookingPage, ShareS::showProfilePage},
                ShareS::showStartPage);

        HBox title = new HBox(16,
                Ui.bold(category.getName().toUpperCase(), 28),
                Ui.spacer(),
                Ui.light("PICK A SUB-CATEGORY", 11));
        title.setAlignment(Pos.BOTTOM_LEFT);

        List<SubCategory> subs = ShareS.catalogService.getSubCategoriesByCategoryId(category.getId());
        if (subs.isEmpty()) {
            return Ui.page(header, title, Ui.light("No sub-categories in this category yet.", 13), Ui.footer());
        }

        Node[] tiles = subs.stream()
                .map(s -> (Node) Ui.tile(s.getName().toUpperCase(), "", 0.48,
                        () -> ShareS.showListingsPage(s)))
                .toArray(Node[]::new);
        GridPane grid = Ui.grid(3, 16, tiles);

        return Ui.page(header, title, grid, Ui.footer());
    }
}
