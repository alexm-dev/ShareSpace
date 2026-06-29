package app.dao;

import app.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object for single images stored as BLOBs in a side table.
 */
public class ImageDAO {

    private final Connection conn;
    private final String table;
    private final String keyColumn;

    public ImageDAO(String table, String keyColumn) {
        this.conn = Database.getConnection();
        this.table = table;
        this.keyColumn = keyColumn;
    }

    /**
     * Saves an image for an owner, replacing any existing image.
     *
     * @param ownerId the owner id
     * @param data the raw image bytes
     * @param mimeType the MIME type of the image
     * @return true if a row was inserted or updated
     */
    public boolean save(int ownerId, byte[] data, String mimeType) {
        String sql = "INSERT INTO " + table + " (" + keyColumn + ", image_data, mime_type) "
                   + "VALUES (?, ?, ?) ON CONFLICT(" + keyColumn + ") DO UPDATE SET "
                   + "image_data = excluded.image_data, mime_type = excluded.mime_type";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ownerId);
            stmt.setBytes(2, data);
            stmt.setString(3, mimeType);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save image in " + table, e);
        }
    }

    /**
     * Returns the image bytes for an owner.
     *
     * @param ownerId the owner id
     * @return the raw image bytes, or null
     */
    public byte[] find(int ownerId) {
        String sql = "SELECT image_data FROM " + table + " WHERE " + keyColumn + " = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ownerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getBytes("image_data");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load image from " + table, e);
        }
        return null;
    }

    /**
     * Deletes the image for an owner.
     * Deletion is already via ON DELETE CASCADE, but this method is provided for convenience.
     *
     * @param ownerId the owner id
     * @return true if a row was deleted
     */
    public boolean delete(int ownerId) {
        String sql = "DELETE FROM " + table + " WHERE " + keyColumn + " = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ownerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete image from " + table, e);
        }
    }
}
