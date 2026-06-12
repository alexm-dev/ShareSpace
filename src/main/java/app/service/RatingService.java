package app.service;

import app.dao.RatingDAO;
import app.model.Rating;

import java.util.List;

public class RatingService {

    private final RatingDAO ratingDAO;

    public RatingService() {
        this.ratingDAO = new RatingDAO();
    }

    public int submitRating(Rating rating) {
        ratingDAO.create(rating);
        return rating.getId();
    }

    public double getAverageForUser(int userId) {
        List<Rating> ratings = ratingDAO.findByRatedUserId(userId);
        return ratings.stream().mapToInt(Rating::getRatingValue).average().orElse(0.0);
    }

    public double getAverageForAsset(int assetId) {
        List<Rating> ratings = ratingDAO.findByAssetId(assetId);
        return ratings.stream().mapToInt(Rating::getRatingValue).average().orElse(0.0);
    }

    public List<Rating> findByBooking(int bookingId) {
        return ratingDAO.findByBookingId(bookingId);
    }

    public List<Rating> findByReviewer(int reviewerId) {
        return ratingDAO.findByReviewerId(reviewerId);
    }

    public List<Rating> findByRatedUser(int userId) {
        return ratingDAO.findByRatedUserId(userId);
    }
}
