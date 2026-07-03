package app.dao;

import app.model.Location;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Data Access Object for the Location entity.
 * This class provides methods to perform CRUD operations on the Location table in the database.
 */
public class LocationDAO extends BaseDAO<Location, Integer> {

    /**
     * The columns to select for findById and findAll, in mapRow order.
     */
    private static final String[] COLUMNS = { "id", "city", "postal_code", "district", "street_address", "country" };

    /**
     * The name of the database table this DAO manages.
     */
    @Override
    protected String tableName() {
        return "locations";
    }

    /**
     * The columns to select for findById and findAll, in mapRow order.
     */
    @Override
    protected String[] selectColumns() {
        return COLUMNS;
    }

    /**
     * Maps a ResultSet row to a Location object.
     *
     * @param rs The ResultSet to map.
     * @return A Location object representing the current row of the ResultSet.
     */
    @Override
    protected Location mapRow(ResultSet rs) throws SQLException {
        return new Location(
            rs.getInt("id"),
            rs.getString("city"),
            rs.getString("postal_code"),
            rs.getString("district"),
            rs.getString("street_address"),
            rs.getString("country")
        );
    }

    /**
     * Creates a new Location record in the database.
     *
     * @param location The Location object to create.
     * @return true if the creation was successful, false otherwise.
     */
    @Override
    public boolean create(Location location) {
        String sql = "INSERT INTO locations (city, postal_code, district, street_address, country) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, location.getCity());
            stmt.setString(2, location.getPostalCode());
            stmt.setString(3, location.getDistrict());
            stmt.setString(4, location.getStreetAddress());
            stmt.setString(5, location.getCountry());
            boolean success = stmt.executeUpdate() > 0;

            if (success) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        location.setId(keys.getInt(1));
                    }
                }
            }
            return success;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create location", e);
        }
    }

    /**
     * Updates an existing Location record in the database.
     *
     * @param location The Location object with updated values.
     * @return true if the update was successful, false otherwise.
     */
    @Override
    public boolean update(Location location) {
        String sql = "UPDATE locations SET city = ?, postal_code = ?, district = ?, street_address = ?, country = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, location.getCity());
            stmt.setString(2, location.getPostalCode());
            stmt.setString(3, location.getDistrict());
            stmt.setString(4, location.getStreetAddress());
            stmt.setString(5, location.getCountry());
            stmt.setInt(6, location.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update location", e);
        }
    }

    /**
     * Looks for an existing location row matching city, postal_code, street_address and country.
     * District is ignored since it is optional. Returns null if nothing matches.
     */
    // TODO: AssetService will use this inside a findOrCreate so we dont end up with duplicate locations
    public Location findMatch(Location location) {
        String sql = "SELECT id, city, postal_code, district, street_address, country " +
            "FROM locations " +
            "WHERE city = ? AND postal_code = ? AND street_address = ? AND country = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, location.getCity());
            stmt.setString(2, location.getPostalCode());
            stmt.setString(3, location.getStreetAddress());
            stmt.setString(4, location.getCountry());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find matching location", e);
        }
        return null;
    }
}
