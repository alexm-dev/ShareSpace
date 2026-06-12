package app.dao;

import app.model.Rating;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the Rating entity.
 *
 * Adds finders by booking and by rated user. Update is not supported since
 * ratings are immutable once submitted; the inherited update method throws
 * UnsupportedOperationException.
 *
 * The rated_user_id column is nullable; this DAO uses setObject and
 * getObject to handle SQL NULL correctly for that field.
 */
public class RatingDAO extends BaseDAO<Rating, Integer> {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** The columns to select for findById and findAll, in mapRow order. */
    private static final String[] COLUMNS = {
            "id", "booking_id", "reviewer_id", "rated_user_id",
            "rating", "comment", "created_time"
    };

    /** The name of the database table this DAO manages. */
    @Override
    protected String tableName() { return "ratings"; }

    /** The columns to select for findById and findAll, in mapRow order. */
    @Override
    protected String[] selectColumns() { return COLUMNS; }

    /**
     * Maps a ResultSet row to a Rating object.
     * rated_user_id is read via getObject so a SQL NULL becomes Java null.
     *
     * @param rs The ResultSet to map.
     * @return A Rating object representing the current row.
     */
    @Override
    protected Rating mapRow(ResultSet rs) throws SQLException {
        return new Rating(
                rs.getInt("id"),
                rs.getInt("booking_id"),
                rs.getInt("reviewer_id"),
                (Integer) rs.getObject("rated_user_id"),
                rs.getInt("rating"),
                rs.getString("comment"),
                LocalDateTime.parse(rs.getString("created_time"), FMT)
        );
    }

    /**
     * Creates a new rating in the database.
     *
     * @param rating The rating to create. The generated id will be set back on this object.
     * @return true if the rating was created successfully, false otherwise.
     */
    @Override
    public boolean create(Rating rating) {
        String sql = "INSERT INTO ratings "
                + "(booking_id, reviewer_id, rated_user_id, rating, comment) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, rating.getBookingId());
            stmt.setInt(2, rating.getReviewerId());
            if (rating.getRatedUserId() == null) {
                stmt.setNull(3, Types.INTEGER);
            } else {
                stmt.setInt(3, rating.getRatedUserId());
            }
            stmt.setInt(4, rating.getRatingValue());
            stmt.setString(5, rating.getComment());
            boolean success = stmt.executeUpdate() > 0;
            if (success) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) rating.setId(keys.getInt(1));
                }
            }
            return success;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create rating", e);
        }
    }

    /** Ratings are immutable once submitted -- update is not allowed. */
    @Override
    public boolean update(Rating rating) {
        throw new UnsupportedOperationException(
                "Ratings are immutable once submitted; update is not supported.");
    }

    /**
     * Returns all ratings attached to a given booking.
     *
     * @param bookingId the booking id
     * @return list of ratings, empty if the booking has none
     */
    public List<Rating> findByBookingId(int bookingId) {
        return findByIntColumn("booking_id", bookingId, "Failed to find ratings by booking id");
    }

    /**
     * Returns all ratings whose rated_user_id matches the given user.
     * Ratings where rated_user_id is NULL are not included.
     *
     * @param ratedUserId the user id being rated
     * @return list of ratings, empty if the user has none
     */
    public List<Rating> findByRatedUserId(int ratedUserId) {
        return findByIntColumn("rated_user_id", ratedUserId, "Failed to find ratings by rated user id");
    }

    /**
     * Returns the average rating for a specific asset.
     * Joins ratings with bookings to find all ratings for an asset.
     *
     * @param assetId the asset id
     * @return the average rating as a double, or 0.0 if no ratings exist
     */
    public double findAverageRatingForAsset(int assetId) {
        String sql = "SELECT AVG(r.rating) FROM ratings r "
                + "JOIN bookings b ON r.booking_id = b.id "
                + "WHERE b.asset_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, assetId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find average rating for asset", e);
        }
        return 0.0;
    }

    /**
     * Returns the average rating for a specific user.
     *
     * @param userId the user id
     * @return the average rating as a double, or 0.0 if no ratings exist
     */
    public double findAverageRatingForUser(int userId) {
        String sql = "SELECT AVG(rating) FROM ratings WHERE rated_user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find average rating for user", e);
        }
        return 0.0;
    }

    private List<Rating> findByIntColumn(String column, int value, String errorMessage) {
        List<Rating> list = new ArrayList<>();
        String cols = String.join(", ", COLUMNS);
        String sql  = "SELECT " + cols + " FROM ratings WHERE " + column + " = ?";
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