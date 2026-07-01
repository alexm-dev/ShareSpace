package app.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JSON helpers for Asset.metadata. Uses Jackson under the hood.
 */
public final class MetadataUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<LinkedHashMap<String, String>> MAP_TYPE =
        new TypeReference<>() {};

    private MetadataUtil() {}

    /**
     * Parses a metadata JSON string into a key-ordered map.
     *
     * @param json the stored JSON string (may be null or blank)
     * @return a mutable, insertion-ordered map of metadata
     */
    public static Map<String, String> parse(String json) {
        if (json == null || json.isBlank()) {
            return new LinkedHashMap<>();
        }

        try {
            return MAPPER.readValue(json, MAP_TYPE);
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    /**
     * Serializes a map into a metadata JSON string.
     *
     * @param map the metadata map to serialize
     * @return JSON string, or null for empty input
     */
    public static String serialize(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        try {
            return MAPPER.writeValueAsString(map);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize metadata", e);
        }
    }

    /**
     * Converts a metadata key into a display label.
     * Example: "fooBar_baz" -> "Foo bar baz"
     *
     * @param key the metadata key
     * @return the display label
     */
    public static String humanizeKey(String key) {
        if (key == null || key.isBlank()) {
            return key;
        }
        String spaced = key.trim()
            .replaceAll("([a-z0-9])([A-Z])", "$1 $2")
            .replace('_', ' ')
            .replace('-', ' ')
            .replaceAll("\\s+", " ")
            .trim()
            .toLowerCase();
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }
}
