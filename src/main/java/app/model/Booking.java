package app.model;

import java.time.LocalDateTime;
import app.model.enums.BookingStatus;

/**
 * Represents a booking made by a renter for an asset.
 *
 * Each booking has an associated asset, renter, start and end date, status,
 * total cost,
 * and timestamps for when the booking was created and last updated.
 */
public class Booking {
    private int id;
    private int assetId;
    private int renterId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatus status;
    private double totalCost;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    /**
     * Full constructor, used when loading from DB.
     */
    public Booking(int id, int assetId, int renterId, LocalDateTime startTime, LocalDateTime endTime,
            BookingStatus status, double totalCost, LocalDateTime createdTime, LocalDateTime updatedTime) {
        this.id = id;
        this.assetId = assetId;
        this.renterId = renterId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.totalCost = totalCost;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }

    /**
     * Constructor to create a new Booking (id and createdTime are set by the DB).
     */
    public Booking(int assetId, int renterId, LocalDateTime startTime, LocalDateTime endTime,
            BookingStatus status, double totalCost) {
        this.assetId = assetId;
        this.renterId = renterId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.totalCost = totalCost;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getAssetId() {
        return assetId;
    }

    public int getRenterId() {
        return renterId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }
}
