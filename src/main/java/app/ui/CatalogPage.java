package app.ui;

import app.model.Category;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class CatalogPage {

    public VBox build() {
        Region header = Ui.header(
                new String[]{"CATALOG", "BOOKINGS", "PROFILE"},
                new Runnable[]{ShareS::showCatalogPage, ShareS::showBookingPage, ShareS::showProfilePage},
                ShareS::showStartPage);

        HBox title = new HBox(16,
                Ui.bold("CATALOG", 28), Ui.spacer(), Ui.light("BROWSE CATEGORIES", 11));
        title.setAlignment(Pos.BOTTOM_LEFT);

        List<Category> cats = ShareS.catalogService.getAllCategories();
        Node[] tiles = cats.stream()
                .map(c -> (Node) Ui.tile(c.getName().toUpperCase(), c.getDescription(), 0.48))
                .toArray(Node[]::new);
        GridPane grid = Ui.grid(3, 16, tiles);

        return Ui.page(header, title, grid, Ui.footer());
    }
}
