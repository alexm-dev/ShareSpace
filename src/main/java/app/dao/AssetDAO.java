package app.dao;

import app.model.Asset;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the Asset entity.
 *
 * Adds finders by owner and by sub-category on top of the inherited
 * CRUD operations, for use by AssetService and the listing/catalog UI.
 */
public class AssetDAO extends BaseDAO<Asset, Integer> {

    /** The columns to select for findById and findAll, in mapRow order. */
    private static final String[] COLUMNS = {
        "id", "owner_id", "sub_category_id", "model",
        "description", "condition", "asset_location_id", "daily_rate", "metadata",
        "discount_after_days", "discount_percentage"
    };

    /** The name of the database table this DAO manages. */
    @Override
    protected String tableName() {
        return "assets";
    }

    /** The columns to select for findById and findAll, in mapRow order. */
    @Override
    protected String[] selectColumns() {
        return COLUMNS;
    }

    /**
     * Maps a ResultSet row to an Asset object.
     *
     * @param rs The ResultSet to map.
     * @return An Asset object representing the current row.
     */
    @Override
    protected Asset mapRow(ResultSet rs) throws SQLException {
        Asset asset = new Asset(
            rs.getInt("id"),
            rs.getInt("owner_id"),
            rs.getInt("sub_category_id"),
            rs.getString("model"),
            rs.getString("description"),
            rs.getString("condition"),
            rs.getInt("asset_location_id"),
            rs.getDouble("daily_rate")
        );
        asset.setMetadata(rs.getString("metadata"));
        asset.setDiscountAfterDays(rs.getInt("discount_after_days"));
        asset.setDiscountPercentage(rs.getDouble("discount_percentage"));
        return asset;
    }

    /**
     * Creates a new asset in the database.
     *
     * @param asset The asset to create. The generated id will be set back on this object.
     * @return true if the asset was created successfully, false otherwise.
     */
    @Override
    public boolean create(Asset asset) {
        String sql = "INSERT INTO assets "
                   + "(owner_id, sub_category_id, model, description, condition, asset_location_id, daily_rate, metadata, "
                   + "discount_after_days, discount_percentage) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, asset.getOwnerId());
            stmt.setInt(2, asset.getSubCategoryId());
            stmt.setString(3, asset.getModel());
            stmt.setString(4, asset.getDescription());
            stmt.setString(5, asset.getCondition());
            stmt.setInt(6, asset.getAssetLocationId());
            stmt.setDouble(7, asset.getDailyRate());
            stmt.setString(8, asset.getMetadata());
            stmt.setInt(9, asset.getDiscountAfterDays());
            stmt.setDouble(10, asset.getDiscountPercentage());
            boolean success = stmt.executeUpdate() > 0;
            if (success) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) asset.setId(keys.getInt(1));
                }
            }
            return success;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create asset", e);
        }
    }

    /**
     * Updates an existing asset in the database. owner_id is intentionally not
     * updatable; ownership transfer is not supported in this design.
     *
     * @param asset The asset to update. Must have its id set.
     * @return true if the asset was updated, false if not found.
     */
    @Override
    public boolean update(Asset asset) {
        String sql = "UPDATE assets SET "
                   + "sub_category_id = ?, model = ?, description = ?, "
                   + "condition = ?, asset_location_id = ?, daily_rate = ?, metadata = ?, "
                   + "discount_after_days = ?, discount_percentage = ? "
                   + "WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, asset.getSubCategoryId());
            stmt.setString(2, asset.getModel());
            stmt.setString(3, asset.getDescription());
            stmt.setString(4, asset.getCondition());
            stmt.setInt(5, asset.getAssetLocationId());
            stmt.setDouble(6, asset.getDailyRate());
            stmt.setString(7, asset.getMetadata());
            stmt.setInt(8, asset.getDiscountAfterDays());
            stmt.setDouble(9, asset.getDiscountPercentage());
            stmt.setInt(10, asset.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update asset", e);
        }
    }

    /**
     * Returns all assets owned by a specific user.
     *
     * @param ownerId the user id of the owner
     * @return list of assets, empty if the user owns none
     */
    public List<Asset> findByOwnerId(int ownerId) {
        return findByIntColumn("owner_id", ownerId, "Failed to find assets by owner id");
    }

    /**
     * Returns all assets in a specific sub-category.
     *
     * @param subCategoryId the sub-category id
     * @return list of assets, empty if no assets match
     */
    public List<Asset> findBySubCategoryId(int subCategoryId) {
        return findByIntColumn("sub_category_id", subCategoryId, "Failed to find assets by sub_category id");
    }

    private List<Asset> findByIntColumn(String column, int value, String errorMessage) {
        List<Asset> list = new ArrayList<>();
        String cols = String.join(", ", COLUMNS);
        String sql  = "SELECT " + cols + " FROM assets WHERE " + column + " = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(errorMessage, e);
        }
        return list;
    }
}
