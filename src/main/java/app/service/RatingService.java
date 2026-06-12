package app.service;

import app.dao.RatingDAO;
import app.model.Rating;

import java.util.List;

/**
 * Service layer for managing ratings.
 * Handles business logic related to creating and retrieving ratings.
 */
public class RatingService {

    private final RatingDAO ratingDAO;

    public RatingService() {
        this.ratingDAO = new RatingDAO();
    }

    /**
     * Submits a new rating.
     *
     * @param rating The rating to submit.
     * @return The ID of the created rating.
     */
    public int submitRating(Rating rating) {
        ratingDAO.create(rating);
        return rating.getId();
    }

    /**
     * Calculates the average rating for a specific user.
     *
     * @param userId The ID of the user to calculate the average rating for.
     * @return The average rating value, or 0.0 if there are no ratings.
     */
    public double getAverageForUser(int userId) {
        List<Rating> ratings = ratingDAO.findByRatedUserId(userId);
        return ratings.stream().mapToInt(Rating::getRatingValue).average().orElse(0.0);
    }

    /**
     * Calculates the average rating for a specific asset.
     *
     * @param assetId The ID of the asset to calculate the average rating for.
     * @return The average rating value, or 0.0 if there are no ratings.
     */
    public double getAverageForAsset(int assetId) {
        List<Rating> ratings = ratingDAO.findByAssetId(assetId);
        return ratings.stream().mapToInt(Rating::getRatingValue).average().orElse(0.0);
    }

    /**
     * Retrieves all ratings associated with a specific booking.
     *
     * @param bookingId The ID of the booking to find ratings for.
     * @return A list of ratings associated with the booking.
     */
    public List<Rating> findByBooking(int bookingId) {
        return ratingDAO.findByBookingId(bookingId);
    }

    /**
     * Retrieves all ratings submitted by a specific reviewer.
     *
     * @param reviewerId The ID of the reviewer to find ratings for.
     * @return A list of ratings submitted by the reviewer.
     */
    public List<Rating> findByReviewer(int reviewerId) {
        return ratingDAO.findByReviewerId(reviewerId);
    }

    /**
     * Retrieves all ratings received by a specific user.
     *
     * @param userId The ID of the user to find ratings for.
     * @return A list of ratings received by the user.
     */
    public List<Rating> findByRatedUser(int userId) {
        return ratingDAO.findByRatedUserId(userId);
    }
}
