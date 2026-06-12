package app.test;

import app.database.Database;
import app.dao.BookingDAO;
import app.dao.UserDAO;
import app.dao.AssetDAO;
import app.dao.LocationDAO;
import app.model.Booking;
import app.model.Rating;
import app.model.User;
import app.model.Asset;
import app.model.Location;
import app.service.RatingService;
import app.util.AuthUtil;

import java.time.LocalDate;

public class RatingserviceTest {

    public static void main(String[] args) {

        // Step 0 — initialize the database
        Database.initialize();

        System.out.println("========================================");
        System.out.println("   RatingService Test");
        System.out.println("========================================\n");

        RatingService ratingService = new RatingService();
        UserDAO userDAO = new UserDAO();
        AssetDAO assetDAO = new AssetDAO();
        LocationDAO locationDAO = new LocationDAO();
        BookingDAO bookingDAO = new BookingDAO();

        // Step 1 — create a user (lender)
        User lender = new User("testLender", "lender@test.com",
                AuthUtil.hashPassword("password123".toCharArray()));
        lender.setStatus("active");
        boolean lenderCreated = userDAO.create(lender);
        if (!lenderCreated) {
            System.out.println("Failed to create lender!");
            return;
        }
        System.out.println("Lender created! ID: " + lender.getId());

        // Step 2 — create a user (renter)
        User renter = new User("testRenter", "renter@test.com",
                AuthUtil.hashPassword("password123".toCharArray()));
        renter.setStatus("active");
        boolean renterCreated = userDAO.create(renter);
        if (!renterCreated) {
            System.out.println("Failed to create renter!");
            return;
        }
        System.out.println("Renter created! ID: " + renter.getId());

        // Step 3 — create a location
        Location location = new Location(
                "Frankfurt",
                "60311",
                "Sachsenhausen",
                "Teststrasse 1",
                "Germany"
        );
        boolean locationCreated = locationDAO.create(location);
        if (!locationCreated) {
            System.out.println("Failed to create location!");
            return;
        }
        System.out.println("Location created! ID: " + location.getId());

        // Step 4 — create an asset
        Asset asset = new Asset(
                lender.getId(),   // owner_id
                1,                // sub_category_id
                "Test Bike",      // model
                "A nice bike",    // description
                "good",           // condition
                location.getId(), // asset_location_id
                20.0              // daily_rate
        );
        boolean assetCreated = assetDAO.create(asset);
        if (!assetCreated) {
            System.out.println("Failed to create asset!");
            return;
        }
        System.out.println("Asset created! ID: " + asset.getId());

        // Step 5 — create a booking
        Booking booking = new Booking(
                asset.getId(),               // asset_id
                renter.getId(),              // renter_id
                LocalDate.now(),             // start_time
                LocalDate.now().plusDays(3), // end_time
                "completed",                 // status
                60.0                         // total_cost
        );
        boolean bookingCreated = bookingDAO.create(booking);
        if (!bookingCreated) {
            System.out.println("Failed to create booking!");
            return;
        }
        System.out.println("Booking created! ID: " + booking.getId());

        // Step 6 — create a rating for that booking
        Rating rating = new Rating(
                booking.getId(),   // bookingId
                renter.getId(),    // reviewerId
                lender.getId(),    // ratedUserId
                5,                 // rating value 1-5
                "Great experience!" // comment
        );

        // Step 7 — test createRating
        Rating result = ratingService.createRating(rating, booking);

        if (result != null) {
            System.out.println("\nPASSED — rating created!");
            System.out.println("Booking ID : " + result.getBookingId());
            System.out.println("Rating     : " + result.getRatingValue() + " stars");
            System.out.println("Comment    : " + result.getComment());
        } else {
            System.out.println("\nFAILED — result was null!");
        }

        System.out.println("\n========================================");
    }
}