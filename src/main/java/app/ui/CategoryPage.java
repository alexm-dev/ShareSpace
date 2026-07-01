package app.ui;

import app.model.Category;
import app.model.SubCategory;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Page that shows all sub-categories of a given category.
 */
public class CategoryPage {

    private final Category category;

    public CategoryPage(Category category) {
        this.category = category;
    }

    public StackPane build() {

        HBox title = new HBox(16,
                Ui.bold(category.getName().toUpperCase(), 28),
                Ui.spacer(),
                Ui.light("PICK A SUB-CATEGORY", 11));
        title.setAlignment(Pos.BOTTOM_LEFT);

        List<SubCategory> subs = ShareS.catalogService.getSubCategoriesByCategoryId(category.getId());
        if (subs.isEmpty()) {
            return Ui.buildPage(title, Ui.light("No sub-categories in this category yet.", 13));
        }

        Node[] tiles = subs.stream()
                .map(s -> (Node) Ui.tile(s.getName().toUpperCase(), "", 0.48,
                        Ui.subCategoryImage(category.getName(), s.getName()),
                        () -> ShareS.showListingsPage(s)))
                .toArray(Node[]::new);
        GridPane grid = Ui.grid(3, 16, tiles);

        return Ui.buildPage(title, grid);
    }
}
