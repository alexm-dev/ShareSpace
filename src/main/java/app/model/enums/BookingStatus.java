package app.model.enums;

public enum BookingStatus {
    PENDING("pending"),
    CONFIRMED("confirmed"),
    COMPLETE("complete"),
    CANCELLED("cancelled");

    private final String dbValue;

    BookingStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }
}
