package app.ui;
import app.util.Palette;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Page that shows information about ShareSpace.
 */
public class AboutPage {

    public StackPane build() {

        VBox intro = new VBox(12,
                Ui.light("WHO WE ARE", 11),
                Ui.boldCentered("ShareSpace was built on a simple idea - the things we own sit unused far more often than they're used.", 28));
        intro.setAlignment(Pos.CENTER);
        intro.setMaxWidth(Double.MAX_VALUE);

        VBox mission = new VBox(8,
                Ui.bold("OUR MISSION", 28),
                Ui.boldCentered("We connect neighbors so that gear gets used instead of gathering dust, and owners earn from items they already have.", 20));
        mission.setAlignment(Pos.CENTER);

        VBox values = new VBox(28,
                value("TRUST", "Every booking ends with a rating, so the community keeps each other accountable."),
                value("SIMPLICITY", "List an item or find one nearby in minutes, with no unnecessary steps."),
                value("COMMUNITY", "We're built for neighbors helping neighbors, not a faceless rental chain."));

        VBox team = new VBox(12,
                Ui.light("THE TEAM", 11),
                Ui.boldCentered("Make sharing as easy as buying.", 28),
                Ui.button("BROWSE THE CATALOG", 13, "-fx-background-color: " + Palette.BRAND_YELLOW + ";"));
        team.setAlignment(Pos.CENTER);
        for (var n : team.getChildren()) {
            if (n instanceof javafx.scene.control.Button b) {
                b.setOnAction(e -> ShareS.showCatalogPage());
                Ui.addHoverPop(b);
            }
        }

        return Ui.buildPage(intro,mission,values,team);
    }

    private VBox value(String title, String desc) {
        VBox v = new VBox(10,
                Ui.bold(title, 24),
                Ui.light(desc, 13));
        v.setAlignment(Pos.CENTER);
        v.setMaxWidth(Double.MAX_VALUE);
        return v;
    }
}
