package app.dao;

import app.model.Booking;

import app.model.enums.BookingStatus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the Booking entity.
 *
 * Adds finders by asset, by renter, and by status on top of the inherited
 * CRUD operations. Only the mutable fields (status, total_cost, updated_time)
 * are written by update; the time window, asset, and renter cannot change
 * after creation.
 */
public class BookingDAO extends BaseDAO<Booking, Integer> {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** The columns to select for findById and findAll, in mapRow order. */
    private static final String[] COLUMNS = {
            "id", "asset_id", "renter_id", "start_time", "end_time",
            "status", "total_cost", "created_time", "updated_time"
    };

    /** The name of the database table this DAO manages. */
    @Override
    protected String tableName() {
        return "bookings";
    }

    /** The columns to select for findById and findAll, in mapRow order. */
    @Override
    protected String[] selectColumns() {
        return COLUMNS;
    }

    /**
     * Maps a ResultSet row to a Booking object.
     *
     * @param rs The ResultSet to map.
     * @return A Booking object representing the current row.
     */
    @Override
    protected Booking mapRow(ResultSet rs) throws SQLException {
        String updatedStr = rs.getString("updated_time");
        return new Booking(
                rs.getInt("id"),
                rs.getInt("asset_id"),
                rs.getInt("renter_id"),
                LocalDateTime.parse(rs.getString("start_time"), DT_FMT),
                LocalDateTime.parse(rs.getString("end_time"), DT_FMT),
                BookingStatus.valueOf(rs.getString("status").toUpperCase()),
                rs.getDouble("total_cost"),
                LocalDateTime.parse(rs.getString("created_time"), DT_FMT),
                updatedStr != null ? LocalDateTime.parse(updatedStr, DT_FMT) : null);
    }

    /**
     * Creates a new booking in the database.
     *
     * @param booking The booking to create. The generated id will be set back on
     *                this object.
     * @return true if the booking was created successfully, false otherwise.
     */
    @Override
    public boolean create(Booking booking) {
        String sql = "INSERT INTO bookings "
                + "(asset_id, renter_id, start_time, end_time, status, total_cost) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, booking.getAssetId());
            stmt.setInt(2, booking.getRenterId());
            stmt.setString(3, booking.getStartTime().format(DT_FMT));
            stmt.setString(4, booking.getEndTime().format(DT_FMT));
            stmt.setString(5, booking.getStatus().getDbValue());
            stmt.setDouble(6, booking.getTotalCost());
            boolean success = stmt.executeUpdate() > 0;
            if (success) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next())
                        booking.setId(keys.getInt(1));
                }
            }
            return success;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create booking", e);
        }
    }

    /**
     * Updates status and total_cost. The time window, asset and renter
     * are immutable after creation. updated_time is set automatically by the DB.
     *
     * @param booking Must have its id set.
     * @return true if a row was updated, false if not found.
     */
    // TODO: BookingService should guard the status transitions (pending ->
    // confirmed -> completed / cancelled).
    // Right now this happily sets any status string, which we dont want once the UI
    // is wired in.
    @Override
    public boolean update(Booking booking) {
        String sql = "UPDATE bookings SET status = ?, total_cost = ?, updated_time = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, booking.getStatus().getDbValue());
            stmt.setDouble(2, booking.getTotalCost());
            stmt.setInt(3, booking.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update booking", e);
        }
    }

    /**
     * Returns all bookings for a given asset.
     *
     * @param assetId the asset id
     * @return list of bookings, empty if the asset has none
     */
    public List<Booking> findByAssetId(int assetId) {
        return findByColumn("asset_id = ?", assetId, "Failed to find bookings by asset id");
    }

    /**
     * Returns all bookings made by a given renter.
     *
     * @param renterId the renter (user) id
     * @return list of bookings, empty if the renter has none
     */
    public List<Booking> findByRenterId(int renterId) {
        return findByColumn("renter_id = ?", renterId, "Failed to find bookings by renter id");
    }

    /**
     * Returns all bookings in a given status (pending, confirmed, completed,
     * cancelled).
     *
     * @param status the booking status
     * @return list of bookings in that status, empty if none match
     */
    public List<Booking> findByStatus(String status) {
        List<Booking> list = new ArrayList<>();
        String cols = String.join(", ", COLUMNS);
        String sql = "SELECT " + cols + " FROM bookings WHERE status = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find bookings by status", e);
        }
        return list;
    }

    private List<Booking> findByColumn(String wherePredicate, int value, String errorMessage) {
        List<Booking> list = new ArrayList<>();
        String cols = String.join(", ", COLUMNS);
        String sql = "SELECT " + cols + " FROM bookings WHERE " + wherePredicate;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(errorMessage, e);
        }
        return list;
    }
}
