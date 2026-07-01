package app.model.enums;

/**
 * Inteface for enums that have a database value representation.
 * Is used to provide a consistent way to retrieve the database value of a enum.
 */
public interface DbValued {

    /** Provided by every enum */
    String name();

    /** The value written to / read from the database. */
    default String getDbValue() {
        return name().toLowerCase();
    }
}
