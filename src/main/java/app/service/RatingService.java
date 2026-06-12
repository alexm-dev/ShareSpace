package app.service;

import app.dao.RatingDAO;
import app.model.Rating;
import java.util.List;

/**
 * Service for managing ratings.
 * Handles business logic for submitting and retrieving ratings.
 *
 * Note: Ratings are immutable once submitted.
 * Update and delete operations are not supported by design.
 * This ensures trust and fairness in the ShareSpace community.
 */
public class RatingService {

    private final RatingDAO ratingDAO;

    public RatingService() {
        this.ratingDAO = new RatingDAO();
    }

    /**
     * Creates a new rating.
     * First checks if the booking exists, then creates the rating.
     *
     * @param rating the rating to create
     * @return the created rating
     */
    public Rating submitRating(Rating rating) {
        ratingDAO.create(rating);
        return rating;
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
    // Ratings are immutable once submitted; a user cannot change
    // their review after it has been posted.

    // Note: deleteRating() is not implemented.
    // Ratings are immutable once submitted; a user cannot delete
    // their review after it has been posted.
}
