package app.service;

import app.dao.UserDAO;
import app.dao.UserRoleDAO;
import app.dao.RoleDAO;
import app.dao.LocationDAO;
import app.dao.ImageDAO;
import app.dao.AssetDAO;
import app.dao.BookingDAO;
import app.dao.RatingDAO;
import app.model.User;
import app.model.UserRole;
import app.model.Role;
import app.model.Location;
import app.model.Asset;
import app.model.Booking;
import app.model.Rating;
import app.model.enums.UserStatus;
import app.model.enums.BookingStatus;
import app.util.AuthUtil;
import static app.util.Constants.MAX_IMAGE_BYTES;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles user registration, profile management, password updates and role assignment.
 * All persistence goes through UserDAO and UserRoleDAO, no user state is held in memory here.
 */
public class UserService {

    private final UserDAO userDAO;
    private final UserRoleDAO userRoleDAO;
    private final RoleDAO roleDAO;
    private final LocationDAO locationDAO;
    private final ImageDAO userImageDAO;
    private final AssetDAO assetDAO;
    private final BookingDAO bookingDAO;
    private final RatingDAO ratingDAO;

    public UserService() {
        this.userDAO = new UserDAO();
        this.userRoleDAO = new UserRoleDAO();
        this.roleDAO = new RoleDAO();
        this.locationDAO = new LocationDAO();
        this.userImageDAO = new ImageDAO("user_images", "user_id");
        this.assetDAO = new AssetDAO();
        this.bookingDAO = new BookingDAO();
        this.ratingDAO = new RatingDAO();
    }

    /**
     * Registers a new user. Hashes the plain-text password before storing.
     * Returns null if the username or email is already taken.
     *
     * @param username the desired username
     * @param email the user's email address
     * @param plainPassword the plain-text password (hashed internally)
     * @return the created User with its generated id, or null if registration failed
     */
    public User register(String username, String email, char[] plainPassword) {
        if (!AuthUtil.isValidEmail(email)){
            return null;
        }

        if (!AuthUtil.isValidPassword(plainPassword)) {
            return null;
        }

        if (userDAO.findByEmail(email) != null) {
            return null;
        }

        if (userDAO.findByUsername(username) != null) {
            return null;
        }

        User user = new User(username, email, AuthUtil.hashPassword(plainPassword));
        user.setStatus(UserStatus.ACTIVE);
        return userDAO.create(user) ? user : null;
    }

    /**
     * Looks up a user by their id.
     *
     * @param id the user id
     * @return the User, or null if not found
     */
    public User findById(int id) {
        return userDAO.findById(id);
    }

    /**
     * Looks up a user by their email address.
     *
     * @param email the email to search for
     * @return the User, or null if not found
     */
    public User findByEmail(String email) {
        return userDAO.findByEmail(email);
    }

    /**
     * Changes a users email address. Rejects the change if the new email
     * is already taken by another user.
     *
     * @param userId the user id
     * @param newEmail the new email address
     * @return true if updated, false if the user was not found or the email is taken
     */
    public boolean updateEmail(int userId, String newEmail) {
        if (!AuthUtil.isValidEmail(newEmail)) { 
            return false;
        }

        User existing = userDAO.findByEmail(newEmail);
        if (existing != null && existing.getId() != userId) { 
            return false;
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            return false;
        }

        user.setEmail(newEmail);
        return userDAO.update(user);
    }

    /**
     * Changes a users username. Rejects the change if the new username
     * is already taken by another user.
     *
     * @param userId the user id
     * @param newUsername the new username
     * @return true if updated, false if the user was not found or the username is taken
     */
    public boolean updateUsername(int userId, String newUsername) {
        User existing = userDAO.findByUsername(newUsername);
        if (existing != null && existing.getId() != userId) { 
            return false;
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            return false;
        }

        user.setUsername(newUsername);
        return userDAO.update(user);
    }

    /**
     * Updates a user's status (eg. active, inactive, suspended).
     *
     * @param userId the user id
     * @param newStatus the new status
     * @return true if updated, false if the user was not found
     */
    public boolean updateStatus(int userId, UserStatus newStatus) {
        User user = userDAO.findById(userId);
        if (user == null) {
            return false;
        }

        user.setStatus(newStatus);
        return userDAO.update(user);
    }

    /**
     * Replaces the stored password hash for a user.
     *
     * @param userId the id of the user
     * @param plainPassword the new plain-text password (hashed internally)
     * @return true if updated, false if the user was not found
     */
    public boolean updatePassword(int userId, char[] plainPassword) {
        if (!AuthUtil.isValidPassword(plainPassword)) { 
            return false;
        }
        User user = userDAO.findById(userId);
        if (user == null) {
            return false;
        }

        user.setPasswordHash(AuthUtil.hashPassword(plainPassword));
        return userDAO.update(user);
    }


    /**
     * Updates a users first and last name.
     *
     * @param userId the user id
     * @param firstName the new first name
     * @param lastName the new last name
     * @return true if updated, false if the user was not found
     */
    public boolean updateName(int userId, String firstName, String lastName) {
        User user = userDAO.findById(userId);
        if (user == null) {
            return false;
        }
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return userDAO.update(user);
    }


    /**
     * Checks if a user has a first name and last name set.
     *
     * @param userId the user id
     * @return true if the user has a name set, false if not or if the user does not exist
     */
    public boolean hasName(int userId) {
        User user = userDAO.findById(userId);
        return user != null && user.getFullName() != null;
    }

    /**
     * Returns the users saved location, if any.
     *
     * @param userId the user id
     * @return the Location, or null if the user has no location or does not exist
     */
    public Location getLocation(int userId) {
        User user = userDAO.findById(userId);
        if (user == null || user.getLocationId() == null) {
            return null;
        }
        return locationDAO.findById(user.getLocationId());
    }

    /**
     * Checks if a user has a location set.
     *
     * @param userId the user id
     * @return true if the user has a location set
     */
    public boolean hasLocation(int userId) {
        User user = userDAO.findById(userId);
        return user != null && user.getLocationId() != null;
    }

    /**
     * Updates the users location.
     *
     * @param userId the user id
     * @param location the location to save
     * @return true if the user's location was updated, false if the user was not found
     */
    public boolean updateLocation(int userId, Location location) {
        User user = userDAO.findById(userId);
        if (user == null) {
            return false;
        }

        Location existing = locationDAO.findMatch(location);
        if (existing != null) {
            location = existing;
        } else if (!locationDAO.create(location)) {
            return false;
        }

        user.setLocationId(location.getId());
        return userDAO.update(user);
    }

    /**
     * Saves a users profile image. Overwrites any existing image.
     *
     * @param userId the user id
     * @param data the raw image bytes
     * @param mimeType the image MIME type (eg. "image/png")
     * @return true if saved, false if the user was not found or the image is invalid
     */
    public boolean saveProfileImage(int userId, byte[] data, String mimeType) {
        if (data == null || data.length == 0 || data.length > MAX_IMAGE_BYTES) {
            return false;
        }
        if (userDAO.findById(userId) == null) {
            return false;
        }
        return userImageDAO.save(userId, data, mimeType);
    }

    /**
     * Retrieves a users profile image.
     *
     * @param userId the user id
     * @return the raw image bytes, or null if no image is found
     */
    public byte[] getProfileImage(int userId) {
        return userImageDAO.find(userId);
    }

    /**
     * Removes a users profile image.
     *
     * @param userId the user id
     * @return true if an image was removed
     */
    public boolean deleteProfileImage(int userId) {
        return userImageDAO.delete(userId);
    }

    /**
     * Deletes a user account together with its history.
     *
     * @param userId the id of the user to delete
     * @return true if deleted, false if not found or blocked by an active booking
     */
    public boolean deleteAccount(int userId) {
        List<Booking> bookings = new ArrayList<>(bookingDAO.findByRenterId(userId));
        for (Asset asset : assetDAO.findByOwnerId(userId)) {
            bookings.addAll(bookingDAO.findByAssetId(asset.getId()));
        }

        boolean hasActiveBooking = bookings.stream().anyMatch(b ->
                b.getStatus() == BookingStatus.PENDING || b.getStatus() == BookingStatus.CONFIRMED);
        if (hasActiveBooking) {
            return false;
        }

        // clear dependents in FK-safe order: ratings -> bookings -> user
        for (Booking booking : bookings) {
            for (Rating rating : ratingDAO.findByBookingId(booking.getId())) {
                ratingDAO.delete(rating.getId());
            }
            bookingDAO.delete(booking.getId());
        }

        for (Rating rating : ratingDAO.findByReviewerId(userId)) {
            ratingDAO.delete(rating.getId());
        }

        for (Rating rating : ratingDAO.findByRatedUserId(userId)) {
            ratingDAO.delete(rating.getId());
        }

        return userDAO.delete(userId);
    }

    /**
     * Assigns a role to a user (eg. lender or renter).
     *
     * @param userId the user id
     * @param roleId the role id to assign
     * @return true if the role was assigned
     */
    public boolean assignRoleToUser(int userId, int roleId) {
        return userRoleDAO.create(new UserRole(userId, roleId));
    }

    /**
     * Removes a role from a user.
     *
     * @param userId the user id
     * @param roleId the role id to remove
     * @return true if removed, false if the role wasn't assigned
     */
    public boolean removeRoleFromUser(int userId, int roleId) {
        return userRoleDAO.delete(userId, roleId);
    }

    /**
     * Check if a user has a specific role.
     *
     * @param userId the user id
     * @param roleName the name of the role to check (eg. "lender")
     * @return true if the user has the role, false otherwise
     */
    public boolean hasRole(int userId, String roleName) {
        Role role = roleDAO.findByName(roleName);
        if (role == null) {
            return false;
        }
        return userRoleDAO.findByUserId(userId)
            .stream()
            .anyMatch(ur -> ur.getRoleId() == role.getId());
    }

    /**
     * Returns a list of all available roles in the system.
     *
     * @return a list of Roles, or an empty list if none exist
     */
    public List<Role> getAllRoles() {
        return roleDAO.findAll();
    }

    /**
     * Returns a list of Role objects assigned to the user.
     *
     * @param userId the user id
     * @return a list of Roles, or an empty list
     */
    public List<Role> getRolesForUser(int userId) {
        return userRoleDAO.findByUserId(userId).stream()
            .map(ur -> roleDAO.findById(ur.getRoleId()))
            .filter(r -> r != null)
            .collect(Collectors.toList());
    }
}
