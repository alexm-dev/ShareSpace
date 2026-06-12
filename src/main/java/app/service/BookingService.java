package app.service;

import app.model.Booking;
import app.dao.BookingDAO;
import app.model.Asset;
import app.dao.AssetDAO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;

public class BookingService {

    private final BookingDAO bookingDAO;
    private final AssetDAO assetDAO;

    private boolean discountEnabled = false;
    private int discountAfterDays = 3;
    private double discountPercentange = 20.0;

    public BookingService() {
        this.bookingDAO = new BookingDAO();
        this.assetDAO = new AssetDAO();
    }

    public double calculateCost(int assetId, LocalDateTime startTime, LocalDateTime endTime) {
        double dailyRate = assetDAO.findById(assetId).getDailyRate();

        double days = Duration.between(startTime, endTime).toMinutes() / (24.0 * 60);

        double billableDays = Math.ceil(days * 2) / 2.0;

        if (!discountEnabled || billableDays <= discountAfterDays) {
            return billableDays * dailyRate;
        }

        double discountedRate = dailyRate * (1 - discountPercentange / 100.0);

        return (discountAfterDays * dailyRate) + ((billableDays - discountAfterDays) * discountedRate);
    }

    public Booking createBooking(int assetId, int renterId, LocalDateTime startTime, LocalDateTime endTime) {
        double totalCost = calculateCost(assetId, startTime, endTime);
        var status = bookingDAO.findByStatus(status)
        Booking booking = new Booking(assetId, renterId, startTime, endTime, status, totalCost);
    }

    public void ennableDiscount(boolean enabled) {
        this.discountEnabled = enabled;
    }

    public void configureDiscount(int afterDays, double percentage) {
        this.discountAfterDays = afterDays;
        this.discountPercentange = percentage;
    }

    public Asset getBookedAsset(int assetId) {
        return assetDAO.findById(assetId);
    }

    public List<Asset> getBookingsByOwner(int ownerId) {
        return assetDAO.findByOwnerId(ownerId);
    }

}
