package app.model;

import app.model.enums.UserStatus;

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
    private UserStatus status;
    private String firstName;
    private String lastName;
    private Integer locationId;

    /**
     * Constructor to load from the DB.
     */
    public User(int id, String username, String email, String passwordHash, LocalDateTime createdTime, UserStatus status) {
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
    public UserStatus getStatus() { return status; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public Integer getLocationId() { return locationId; }

    /**
     * The user's real name ("First Last"), shown only inside a booking between
     * two users. Returns null if the name has not been filled in yet.
     */
    public String getFullName() {
        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) {
            return null;
        }
        return firstName.trim() + " " + lastName.trim();
    }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setStatus(UserStatus status) { this.status = status; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setLocationId(Integer locationId) { this.locationId = locationId; }
}
