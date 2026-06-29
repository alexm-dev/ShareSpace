package app.ui;

import app.model.Category;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

public class ShareSpacePage {

    public StackPane build() {

        Region banner = Ui.image(0.31);

        VBox who = new VBox(12,
                Ui.light("WHO WE ARE", 11),
                Ui.boldCentered("Share the space, smart and light - get your gear just when it's right.", 28),
                Ui.button("ABOUT US", 13, "-fx-background-color: #ffd000;"));
        who.setAlignment(Pos.CENTER);
        who.setMaxWidth(Double.MAX_VALUE);

        VBox strip = featuredCategories("Electronics", "Gaming", "Outdoor", "Music", "Fashion");

        VBox what = new VBox(8,
                Ui.bold("WHAT WE DO", 28),
                Ui.boldCentered("List your unused items, earn money, and help others get what they need.", 20));
        what.setAlignment(Pos.CENTER);

        VBox steps = new VBox(28,
                step("1", "LIST YOUR ITEMS", "Add photos, a short description, and set your price."),
                step("2", "GET BOOKED", "Accept requests from people nearby who need it."),
                step("3", "HAND OVER AND EARN", "Meet, rent it out, and get paid."));

        Region slash = Ui.image(0.0001);

        VBox work = new VBox(12,
                Ui.light("WORK WITH US", 11),
                Ui.boldCentered("Work with us to turn unused inventory into revenue while serving your community.", 28),
                Ui.button("ABOUT US", 13, "-fx-background-color: #ffd000;"));
        work.setAlignment(Pos.CENTER);

        return Ui.buildPage(banner, who, strip, what, steps, slash, work);
    }

    private VBox featuredCategories(String... names) {
        List<Category> all = ShareS.catalogService.getAllCategories();
        List<Node> cards = new ArrayList<>();
        for (String name : names) {
            all.stream()
                    .filter(c -> c.getName().equalsIgnoreCase(name))
                    .findFirst()
                    .ifPresent(c -> cards.add(Ui.tile(c.getName().toUpperCase(), "", 1.0,
                            Ui.categoryImage(c.getName()), () -> ShareS.showCategoryPage(c))));
        }
        GridPane grid = Ui.grid(Math.max(1, cards.size()), 12, cards.toArray(new Node[0]));

        VBox box = new VBox(16, Ui.light("POPULAR CATEGORIES", 11), grid);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private VBox step(String number, String title, String desc) {
        VBox v = new VBox(10,
                Ui.bold(number, 24),
                Ui.bold(title, 24),
                Ui.light(desc, 13));
        v.setAlignment(Pos.CENTER);
        v.setMaxWidth(Double.MAX_VALUE);
        return v;
    }
}
