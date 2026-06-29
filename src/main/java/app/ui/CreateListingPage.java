package app.ui;

import app.model.Asset;
import app.model.Category;
import app.model.Location;
import app.model.SubCategory;
import app.util.MetadataSchema;
import app.util.MetadataUtil;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Page for creating or editing a listing.
 */
public class CreateListingPage {

    private final Asset editing;

    public CreateListingPage(Asset editing) {
        this.editing = editing;
    }

    public StackPane build() {
        boolean isEdit = editing != null;

        HBox title = new HBox(16,
                Ui.bold(isEdit ? "EDIT LISTING" : "NEW LISTING", 28),
                Ui.spacer(),
                Ui.light("MY LISTINGS", 11));
        title.setAlignment(Pos.BOTTOM_LEFT);

        int me = ShareS.session.getActiveUser().getId();
        if (!isEdit && !ShareS.userService.hasRole(me, "lender")) {
            Button settings = Ui.button("Go to settings", 13, "-fx-background-color: #bdbdbd; -fx-text-fill: white;");
            settings.setOnAction(e -> ShareS.showProfileSettingsPage());
            return Ui.buildPage(title,
                    Ui.light("You need the lender role to create listings. Add it in settings.", 13),
                    settings);
        }

        if (isEdit && ShareS.assetService.hasActiveBookings(editing.getId())) {
            Button back = Ui.button("Back to profile", 13, "-fx-background-color: #bdbdbd; -fx-text-fill: white;");
            back.setOnAction(e -> ShareS.showProfilePage());
            return Ui.buildPage(title,
                    Ui.light("This listing has active bookings and can't be edited.", 13),
                    back);
        }

        ComboBox<Category> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(ShareS.catalogService.getAllCategories());
        categoryBox.setConverter(categoryConverter());
        categoryBox.setMaxWidth(300);

        ComboBox<SubCategory> subBox = new ComboBox<>();
        subBox.setConverter(subConverter());
        subBox.setMaxWidth(300);

        VBox metadataRows = new VBox(8);

        Runnable applySchemaRows = () -> {
            metadataRows.getChildren().clear();
            Category cat = categoryBox.getValue();
            SubCategory sub = subBox.getValue();
            Map<String, String> existing = isEdit ? MetadataUtil.parse(editing.getMetadata()) : Map.of();
            List<String> schemaKeys = MetadataSchema.keysFor(
                    cat == null ? null : cat.getName(),
                    sub == null ? null : sub.getName());

            Set<String> shown = new LinkedHashSet<>();
            for (String key : schemaKeys) {
                addSchemaRow(metadataRows, key, existing.getOrDefault(key, ""));
                shown.add(key);
            }
            for (Map.Entry<String, String> entry : existing.entrySet()) {
                if (shown.add(entry.getKey())) {
                    addCustomRow(metadataRows, entry.getKey(), entry.getValue());
                }
            }
            if (metadataRows.getChildren().isEmpty()) {
                addCustomRow(metadataRows, "", "");
            }
        };

        categoryBox.valueProperty().addListener((obs, old, cat) -> {
            subBox.getItems().setAll(cat == null ? List.of()
                    : ShareS.catalogService.getSubCategoriesByCategoryId(cat.getId()));
            subBox.setValue(null);
            applySchemaRows.run();
        });
        subBox.valueProperty().addListener((obs, old, sub) -> applySchemaRows.run());
        applySchemaRows.run();

        TextField model = new TextField();
        model.setPromptText("model");
        model.setMaxWidth(300);
        TextField description = new TextField();
        description.setPromptText("description");
        description.setMaxWidth(300);
        TextField condition = new TextField();
        condition.setPromptText("condition");
        condition.setMaxWidth(300);
        TextField dailyRate = new TextField();
        dailyRate.setPromptText("daily rate");
        dailyRate.setMaxWidth(300);

        TextField city = new TextField();
        city.setPromptText("city");
        city.setMaxWidth(300);
        TextField postalCode = new TextField();
        postalCode.setPromptText("postal code");
        postalCode.setMaxWidth(300);
        TextField district = new TextField();
        district.setPromptText("district (optional)");
        district.setMaxWidth(300);
        TextField streetAddress = new TextField();
        streetAddress.setPromptText("street address");
        streetAddress.setMaxWidth(300);
        TextField country = new TextField();
        country.setPromptText("country");
        country.setMaxWidth(300);

        VBox locationBox = new VBox(10,
                Ui.light("Location", 11),
                Ui.light("City", 11), city,
                Ui.light("Postal Code", 11), postalCode,
                Ui.light("District", 11), district,
                Ui.light("Street Address", 11), streetAddress,
                Ui.light("Country", 11), country);

        Label error = Ui.light("", 12);

        final byte[][] imageData = { isEdit ? ShareS.assetService.getImage(editing.getId()) : null };
        final String[] imageMime = { null };
        final boolean[] imageDirty = { false };

        StackPane preview = new StackPane();
        preview.setMaxWidth(300);
        preview.setStyle("-fx-cursor: hand;");
        Label addPhoto = Ui.light("Click to add a photo", 12);
        Runnable renderPreview = () -> {
            preview.getChildren().setAll(Ui.imageBox(300, 165, imageData[0]));
            if (imageData[0] == null) {
                preview.getChildren().add(addPhoto);
            }
        };
        renderPreview.run();
        Runnable chooseImage = () -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose an image");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            File file = chooser.showOpenDialog(ShareS.primaryStage);
            if (file == null) {
                return;
            }
            try {
                byte[] cropped = CropDialog.crop(Files.readAllBytes(file.toPath()), 0.55);
                if (cropped == null) {
                    return;
                }
                imageData[0] = cropped;
                imageMime[0] = "image/jpeg";
                imageDirty[0] = true;
                renderPreview.run();
            } catch (IOException ex) {
                showError(error, "Could not read that image file.");
            }
        };
        Runnable removeImage = () -> {
            imageData[0] = null;
            imageMime[0] = null;
            imageDirty[0] = true;
            renderPreview.run();
        };
        preview.setOnMouseClicked(ev -> {
            if (ev.getButton() == MouseButton.PRIMARY) {
                chooseImage.run();
            }
        });
        preview.setOnContextMenuRequested(ev -> {
            if (imageData[0] != null) {
                Ui.showImageMenu(preview, true, chooseImage, removeImage);
            }
        });

        if (isEdit) {
            SubCategory sub = ShareS.catalogService.getSubCategoryById(editing.getSubCategoryId());
            if (sub != null) {
                Category cat = ShareS.catalogService.getAllCategories().stream()
                        .filter(c -> c.getId() == sub.getCategoryId())
                        .findFirst().orElse(null);
                categoryBox.setValue(cat);
                subBox.setValue(sub);
            }
            categoryBox.setDisable(true);
            subBox.setDisable(true);
            model.setText(editing.getModel());
            if (editing.getDescription() != null) description.setText(editing.getDescription());
            if (editing.getCondition() != null) condition.setText(editing.getCondition());
            dailyRate.setText(String.valueOf(editing.getDailyRate()));
            applySchemaRows.run();
        }

        Button submit = Ui.button(isEdit ? "Save changes" : "Create listing", 13,
                "-fx-background-color: #bdbdbd; -fx-text-fill: white;");
        submit.setMaxWidth(300);
        submit.setOnAction(e -> {
            SubCategory sub = subBox.getValue();
            if (sub == null) {
                showError(error, "Please pick a category and sub-category.");
                return;
            }
            String modelText = model.getText().trim();
            if (modelText.isEmpty()) {
                showError(error, "Model is required.");
                return;
            }
            Double rate = parseDouble(dailyRate.getText().trim());
            if (rate == null) {
                showError(error, "Daily rate must be a number.");
                return;
            }

            Map<String, String> meta = new LinkedHashMap<>();
            for (Node node : metadataRows.getChildren()) {
                if (!(node instanceof HBox row) || row.getChildren().size() < 2) {
                    continue;
                }
                String key = row.getUserData() instanceof String canonical
                        ? canonical
                        : ((TextField) row.getChildren().get(0)).getText().trim();
                String value = ((TextField) row.getChildren().get(1)).getText().trim();
                if (!key.isEmpty() && !value.isEmpty()) {
                    meta.put(key, value);
                }
            }
            String metadata = MetadataUtil.serialize(meta);
            String descText = description.getText().trim();
            String conditionText = condition.getText().trim();

            if (isEdit) {
                editing.setModel(modelText);
                editing.setDescription(descText);
                editing.setCondition(conditionText);
                editing.setDailyRate(rate);
                editing.setMetadata(metadata);
                if (ShareS.assetService.updateAsset(editing, me)) {
                    if (imageDirty[0]) {
                        if (imageData[0] != null) {
                            ShareS.assetService.saveImage(editing.getId(), imageData[0], imageMime[0], me);
                        } else {
                            ShareS.assetService.deleteImage(editing.getId(), me);
                        }
                    }
                    ShareS.showProfilePage();
                } else {
                    showError(error, "Could not update the listing.");
                }
                return;
            }

            String cityText = city.getText().trim();
            String postalText = postalCode.getText().trim();
            String districtText = district.getText().trim();
            String streetText = streetAddress.getText().trim();
            String countryText = country.getText().trim();
            if (cityText.isEmpty() || postalText.isEmpty() || streetText.isEmpty() || countryText.isEmpty()) {
                showError(error, "City, postal code, street address and country are required.");
                return;
            }

            Asset asset = new Asset(me, sub.getId(), modelText, descText, conditionText, 0, rate);
            asset.setMetadata(metadata);
            Location loc = new Location(cityText, postalText,
                    districtText.isEmpty() ? null : districtText, streetText, countryText);
            Asset created = ShareS.assetService.createAsset(asset, loc);
            if (created != null) {
                if (imageData[0] != null) {
                    ShareS.assetService.saveImage(created.getId(), imageData[0], imageMime[0], me);
                }
                ShareS.showProfilePage();
            } else {
                showError(error, "Could not create the listing.");
            }
        });

        Button addDetail = Ui.button("+ Add detail", 12, "-fx-background-color: #eeeeee;");
        addDetail.setOnAction(e -> addCustomRow(metadataRows, "", ""));

        VBox form = new VBox(12,
                Ui.light("Photo", 11), preview,
                Ui.light("Category", 11), categoryBox,
                Ui.light("Sub-category", 11), subBox,
                Ui.light("Model", 11), model,
                Ui.light("Description", 11), description,
                Ui.light("Condition", 11), condition,
                Ui.light("Daily Rate", 11), dailyRate,
                Ui.light("Details (optional)", 11), metadataRows, addDetail);
        if (!isEdit) {
            form.getChildren().add(locationBox);
        }
        form.getChildren().addAll(error, submit);
        form.setMaxWidth(460);
        form.setPrefWidth(460);

        HBox formWrap = new HBox(form);
        formWrap.setAlignment(Pos.CENTER);

        return Ui.buildPage(title, formWrap);
    }

    private void addSchemaRow(VBox container, String key, String value) {
        Label keyLabel = Ui.light(MetadataUtil.humanizeKey(key), 12);
        keyLabel.setWrapText(false);
        keyLabel.setMinWidth(150);
        TextField valueField = new TextField(value);
        valueField.setPromptText("value");
        valueField.setPrefWidth(200);
        HBox row = new HBox(8, keyLabel, valueField, removeButton());
        row.setAlignment(Pos.CENTER_LEFT);
        row.setUserData(key);
        wireRemove(row);
        container.getChildren().add(row);
    }

    private void addCustomRow(VBox container, String key, String value) {
        TextField keyField = new TextField(key);
        keyField.setPromptText("field (eg. color)");
        keyField.setPrefWidth(150);
        TextField valueField = new TextField(value);
        valueField.setPromptText("value");
        valueField.setPrefWidth(200);
        HBox row = new HBox(8, keyField, valueField, removeButton());
        row.setAlignment(Pos.CENTER_LEFT);
        wireRemove(row);
        container.getChildren().add(row);
    }

    private Button removeButton() {
        Button remove = new Button("✕");
        remove.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: #888888;");
        return remove;
    }

    private void wireRemove(HBox row) {
        Button remove = (Button) row.getChildren().get(row.getChildren().size() - 1);
        remove.setOnAction(e -> {
            if (row.getParent() instanceof VBox parent) {
                parent.getChildren().remove(row);
            }
        });
    }

    private void showError(Label error, String message) {
        error.setText(message);
        error.setStyle("-fx-text-fill: #e53935;");
    }

    private Double parseDouble(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private StringConverter<Category> categoryConverter() {
        return new StringConverter<>() {
            @Override public String toString(Category c) { return c == null ? "" : c.getName(); }
            @Override public Category fromString(String s) { return null; }
        };
    }

    private StringConverter<SubCategory> subConverter() {
        return new StringConverter<>() {
            @Override public String toString(SubCategory s) { return s == null ? "" : s.getName(); }
            @Override public SubCategory fromString(String s) { return null; }
        };
    }
}
