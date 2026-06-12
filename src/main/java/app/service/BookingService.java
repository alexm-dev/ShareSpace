package app.service;

import app.dao.AssetDAO;
import app.dao.BookingDAO;
import app.model.Asset;
import app.model.Booking;
import app.model.enums.BookingStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class BookingService {

    private final BookingDAO bookingDAO;
    private final AssetDAO assetDAO;

    private boolean discountEnabled = false;
    private int discountAfterDays = 3;
    private double discountPercentage = 20.0;

    public BookingService() {
        this.bookingDAO = new BookingDAO();
        this.assetDAO = new AssetDAO();
    }

    public double calculateCost(int assetId, LocalDateTime startTime, LocalDateTime endTime) {
        Asset asset = assetDAO.findById(assetId);
        if (asset == null) return 0.0;

        double days = Duration.between(startTime, endTime).toMinutes() / (24.0 * 60);
        double billableDays = Math.max(1, Math.ceil(days * 2) / 2.0);

        if (!discountEnabled || billableDays <= discountAfterDays) {
            return billableDays * asset.getDailyRate();
        }

        double discountedRate = asset.getDailyRate() * (1 - discountPercentage / 100.0);
        return (discountAfterDays * asset.getDailyRate()) + ((billableDays - discountAfterDays) * discountedRate);
    }

    public Booking createBooking(int assetId, int renterId, LocalDateTime startTime, LocalDateTime endTime) {
        double totalCost = calculateCost(assetId, startTime, endTime);
        Booking booking = new Booking(assetId, renterId, startTime, endTime, BookingStatus.PENDING, totalCost);
        return bookingDAO.create(booking) ? booking : null;
    }

    public boolean confirmBooking(int bookingId) {
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null) return false;
        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingDAO.update(booking);
    }

    public boolean cancelBooking(int bookingId) {
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null) return false;
        booking.setStatus(BookingStatus.CANCELLED);
        return bookingDAO.update(booking);
    }

    public boolean completeBooking(int bookingId) {
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null) return false;
        booking.setStatus(BookingStatus.COMPLETE);
        return bookingDAO.update(booking);
    }

    public List<Booking> findByRenter(int renterId) {
        return bookingDAO.findByRenterId(renterId);
    }

    public List<Booking> findByAsset(int assetId) {
        return bookingDAO.findByAssetId(assetId);
    }

    public List<Asset> getBookingsByOwner(int ownerId) {
        return assetDAO.findByOwnerId(ownerId);
    }

    public void enableDiscount(boolean enabled) {
        this.discountEnabled = enabled;
    }

    public void configureDiscount(int afterDays, double percentage) {
        this.discountAfterDays = afterDays;
        this.discountPercentage = percentage;
    }
}
