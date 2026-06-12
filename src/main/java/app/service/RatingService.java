package app.service;

import app.dao.BookingDAO;
import app.dao.RatingDAO;
import app.model.Booking;
import app.model.Rating;
import app.util.Logger;
import java.util.List;

/**
 * Service for managing ratings.
 * Handles business logic for submitting and retrieving ratings.
 *
 * Note: Ratings are immutable once submitted.
 * Update and delete operations are not supported by design.
 * This ensures trust and fairness in the ShareSpace community —
 */
public class RatingService {

    private final RatingDAO ratingDAO;
    private final BookingDAO bookingDAO;

    public RatingService() {
        this.ratingDAO = new RatingDAO();
        this.bookingDAO = new BookingDAO();
    }

    /**
     * Creates a new rating.
     * First checks if the booking exists, then creates the rating.
     *
     * @param rating  the rating to create
     * @param booking the booking to check
     * @return the created rating, or null if booking not found
     */
    public Rating createRating(Rating rating, Booking booking) {

        // check if the booking exists
        Booking existingBooking = bookingDAO.findById(booking.getId());
        if (existingBooking == null) {
            Logger.warn("Booking not found");
            return null;
        }

        // create the rating
        boolean success = ratingDAO.create(rating);
        if (success) {
            Logger.info("Rating created for booking " + booking.getId());
            return rating;
        } else {
            Logger.warn("Failed to create rating");
            return null;
        }
    }

    /**
     * Returns all ratings received by a specific user.
     *
     * @param ratedUserId the id of the rated user
     * @return list of ratings, empty if none exist
     */
    public List<Rating> findByRatedUser(int ratedUserId) {
        return ratingDAO.findByRatedUserId(ratedUserId);
    }

    /**
     * Returns all ratings for a specific booking.
     *
     * @param bookingId the id of the booking
     * @return list of ratings, empty if none exist
     */
    public List<Rating> findByBooking(int bookingId) {
        return ratingDAO.findByBookingId(bookingId);
    }

    /**
     * Returns the average rating for a specific asset.
     *
     * @param assetId the id of the asset
     * @return the average rating as a double
     */
    public double getAverageForAsset(int assetId) {
        return ratingDAO.findAverageRatingForAsset(assetId);
    }

    /**
     * Returns the average rating for a specific user.
     *
     * @param userId the id of the user
     * @return the average rating as a double
     */
    public double getAverageForUser(int userId) {
        return ratingDAO.findAverageRatingForUser(userId);
    }

    // Note: updateRating() is not implemented.
    // Ratings are immutable once submitted — a user cannot change
    // their review after it has been posted.

    // Note: deleteRating() is not implemented.
    // Ratings are immutable once submitted — a user cannot delete
    // their review after it has been posted.
}