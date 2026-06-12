package app.service;

import app.dao.RatingDAO;
import app.model.Rating;
import java.util.List;

/**
 * Service for managing ratings.
 * Handles business logic for submitting and retrieving ratings.
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
     * Returns all ratings submitted by a specific reviewer.
     *
     * @param reviewerId the id of the reviewer
     * @return list of ratings, empty if none exist
     */
    public List<Rating> findByReviewr(int reviewerId) {
        return ratingDAO.findByReviewerId(reviewerId);
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
     * Returns all ratings for a specific asset.
     *
     * @param assetId the id of the asset
     * @return list of ratings, empty if none exist
     */
    public List<Rating> findByAsset(int assetId) {
        return ratingDAO.findByAssetId(assetId);
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
}
