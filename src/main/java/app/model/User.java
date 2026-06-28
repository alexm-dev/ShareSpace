package app.model;

import java.time.LocalDateTime;

/**
 * User model representing the User table in the database.
 */
public class User {
    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private LocalDateTime createdTime;
    private String status;
    private Integer locationId;

    /**
     * Constructor to load from the DB.
     */
    public User(int id, String username, String email, String passwordHash, LocalDateTime createdTime, String status) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdTime = createdTime;
        this.status = status;
    }

    /**
     * Constructor to create a new User (id and createdTime are set by the DB).
     */
    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public String getStatus() { return status; }
    public Integer getLocationId() { return locationId; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setStatus(String status) { this.status = status; }
    public void setLocationId(Integer locationId) { this.locationId = locationId; }
}
