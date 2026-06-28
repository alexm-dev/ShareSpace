package app.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public final class MetadataSchema {

    private MetadataSchema() {}

    private static final Map<String, List<String>> CATEGORY_SCHEMA = Map.ofEntries(
        Map.entry("Electronics", List.of("brand")),
        Map.entry("Tools", List.of("brand")),
        Map.entry("Gaming", List.of("brand")),
        Map.entry("Outdoor", List.of("brand")),
        Map.entry("Fashion", List.of("brand")),
        Map.entry("Home", List.of("brand")),
        Map.entry("Music", List.of("brand")),
        Map.entry("Designer Goods", List.of("brand")),
        Map.entry("Cooking", List.of("brand")),
        Map.entry("Toys & Collectibles", List.of("brand")),
        Map.entry("Driveables", List.of("brand")),
        Map.entry("Health & Beauty", List.of("brand")),
        Map.entry("Jewelry & Watches", List.of("brand")),
        Map.entry("Sporting", List.of("brand")),
        Map.entry("Baby", List.of("brand"))
    );

    private static final Map<String, List<String>> SUB_CATEGORY_SCHEMA = Map.of(
        "Smartphone", List.of("color", "storage", "batteryHealth"),
        "TV", List.of("screenSize", "resolution", "panelType", "smart"),
        "Laptop", List.of("cpu", "ram", "storage", "screenSize"),
        "Tablet", List.of("storage", "screenSize", "cellular"),
        "Camera", List.of("megapixels", "lens", "shutterCount"),
        "Drill", List.of("voltage", "batteryIncluded", "chuckSize"),
        "Saw", List.of("type", "bladeSize", "powered"),
        "Bicycle", List.of("type", "frameSize", "gears"),
        "Car", List.of("year", "transmission", "mileage", "fuel")
    );

    public static List<String> keysFor(String categoryName, String subCategoryName) {
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        if (categoryName != null) {
            keys.addAll(CATEGORY_SCHEMA.getOrDefault(categoryName, List.of()));
        }
        if (subCategoryName != null) {
            keys.addAll(SUB_CATEGORY_SCHEMA.getOrDefault(subCategoryName, List.of()));
        }
        return new ArrayList<>(keys);
    }
}
