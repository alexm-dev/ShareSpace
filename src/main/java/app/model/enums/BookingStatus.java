package app.model.enums;

/**
 * Status of a booking.
 */
public enum BookingStatus implements DbValued {
    PENDING,
    CONFIRMED,
    COMPLETE,
    CANCELLED
}
