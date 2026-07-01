package app.service;

import app.dao.AssetDAO;
import app.dao.BookingDAO;
import app.model.Asset;
import app.model.Booking;
import app.model.enums.BookingStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Bookings Service layer for booking assets.
 * Handles overal booking business logic, cost calculation and more.
 */
public class BookingService {

    private final BookingDAO bookingDAO;
    private final AssetDAO assetDAO;

    public BookingService() {
        this.bookingDAO = new BookingDAO();
        this.assetDAO = new AssetDAO();
    }

    /**
     * Calculates the cost of a booking.
     * The cost is calculated based on the daily rate of the asset and the duration of the booking.
     *
     * @param assetId The ID of the asset being booked.
     * @param startTime The start time of the booking.
     * @param endTime The end time of the booking.
     * @return The total cost of the booking.
     */
    public double calculateCost(int assetId, LocalDateTime startTime, LocalDateTime endTime) {
        Asset asset = assetDAO.findById(assetId);
        if (asset == null) return 0.0;

        double days = Duration.between(startTime, endTime).toMinutes() / (24.0 * 60);
        double billableDays = Math.max(1, Math.ceil(days * 2) / 2.0);
        double rate = asset.getDailyRate();
        int afterDays = asset.getDiscountAfterDays();
        double percentage = asset.getDiscountPercentage();

        if (percentage <= 0 || billableDays <= afterDays) {
            return billableDays * rate;
        }

        double discountedRate = rate * (1 - percentage / 100.0);
        return (afterDays * rate) + ((billableDays - afterDays) * discountedRate);
    }

    /**
     * Creates a new booking.
     * Booking is created with the status Pendning.
     *
     * @param assetId The ID of the asset being booked.
     * @param renterId The ID of the user making the booking.
     * @param startTime The start time of the booking.
     * @param endTime The end time of the booking.
     * @return The created booking with its ID, or null if creation failed.
     */
    public Booking createBooking(int assetId, int renterId, LocalDateTime startTime, LocalDateTime endTime) {
        double totalCost = calculateCost(assetId, startTime, endTime);
        Booking booking = new Booking(assetId, renterId, startTime, endTime, BookingStatus.PENDING, totalCost);
        return bookingDAO.create(booking) ? booking : null;
    }

    /**
     * Confirms a booking by changing its status to Confirmed.
     * Only bookings with status Pending can be confirmed.
     *
     * @param bookingId The ID of the booking to confirm.
     * @return True if the booking was successfully confirmed, false otherwise.
     */
    public boolean confirmBooking(int bookingId) {
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null) return false;
        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingDAO.update(booking);
    }

    /**
     * Cancels a booking by changing its status to Cancelled.
     * Only bookings with status Pending or Confirmed can be cancelled.
     *
     * @param bookingId The ID of the booking to cancel.
     * @return True if the booking was successfully cancelled, false otherwise.
     */
    public boolean cancelBooking(int bookingId) {
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null) return false;
        booking.setStatus(BookingStatus.CANCELLED);
        return bookingDAO.update(booking);
    }

    /**
     * Completes a booking by changing its status to Complete.
     *
     * @param bookingId The ID of the booking to complete.
     * @return True if the booking was successfully completed, false otherwise.
     */
    public boolean completeBooking(int bookingId) {
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null) return false;
        booking.setStatus(BookingStatus.COMPLETED);
        return bookingDAO.update(booking);
    }

    /**
     * Finds all bookings made by a specific renter.
     *
     * @param renterId The ID of the renter whose bookings to find.
     * @return A list of bookings made by the specified renter.
     */
    public List<Booking> findByRenter(int renterId) {
        return bookingDAO.findByRenterId(renterId);
    }

    /**
     * Finds all bookings for a specific asset.
     *
     * @param assetId The ID of the asset whose bookings to find.
     * @return A list of bookings for the specified asset.
     */
    public List<Booking> findByAsset(int assetId) {
        return bookingDAO.findByAssetId(assetId);
    }

    /**
     * Finds all assets that have been booked by a specific owner.
     *
     * @param ownerId The ID of the owner whose booked assets to find.
     * @return A list of assets that have been booked by the specified owner.
     */
    public List<Asset> getBookingsByOwner(int ownerId) {
        return assetDAO.findByOwnerId(ownerId);
    }
}
