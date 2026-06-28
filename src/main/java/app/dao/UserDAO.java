package app.dao;

import app.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Data Access Object for the User entity.
 * Adds lookups by email and username on top of the inherited CRUD operations.
 */
public class UserDAO extends BaseDAO<User, Integer> {

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** The columns to select for findById and findAll, in mapRow order. */
    private static final String[] COLUMNS =
        { "id", "username", "email", "password_hash", "created_time", "status", "location_id" };

    /** The name of the database table this DAO manages. */
    @Override
    protected String tableName() { return "users"; }

    /** The columns to select for findById and findAll, in mapRow order. */
    @Override
    protected String[] selectColumns() { return COLUMNS; }

    /**
     * Maps a ResultSet row to a User object.
     *
     * @param rs The ResultSet to map.
     * @return A User object representing the current row of the ResultSet.
     */
    @Override
    protected User mapRow(ResultSet rs) throws SQLException {
        User user = new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("password_hash"),
            LocalDateTime.parse(rs.getString("created_time"), FMT),
            rs.getString("status")
        );
        int locationId = rs.getInt("location_id");
        if (!rs.wasNull()) {
            user.setLocationId(locationId);
        }
        return user;
    }

    /**
     * Creates a new user in the database.
     *
     * @param user The user to create. The generated id will be set back on this object.
     * @return true if the user was created successfully, false otherwise.
     */
    @Override
    public boolean create(User user) {
        // TODO: UserService will hash the plaintext password before calling this, never insert raw passwords here
        String sql = "INSERT INTO users (username, email, password_hash, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getStatus());
            boolean success = stmt.executeUpdate() > 0;
            if (success) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) user.setId(keys.getInt(1));
                }
            }
            return success;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user", e);
        }
    }

    /**
     * Updates an existing user in the database.
     *
     * @param user The user to update. Must have its id set.
     * @return true if the user was updated successfully, false if not found.
     */
    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET username = ?, email = ?, password_hash = ?, status = ?, location_id = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getStatus());
            if (user.getLocationId() != null) {
                stmt.setInt(5, user.getLocationId());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            stmt.setInt(6, user.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }

    /**
     * Finds a user by email.
     */
    public User findByEmail(String email) {
        String sql = "SELECT id, username, email, password_hash, created_time, status, location_id FROM users WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by email", e);
        }
        return null;
    }

    /**
     * Finds a user by their username.
     *
     * @param username The username to search for.
     * @return The user with the given username, or null if not found.
     */
    public User findByUsername(String username) {
        String sql = "SELECT id, username, email, password_hash, created_time, status, location_id FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by username", e);
        }
        return null;
    }
}
