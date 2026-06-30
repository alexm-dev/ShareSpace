package app.model.enums;

/**
 * Status of a user.
 */
public enum UserStatus implements DbValued {
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    DELETED
}
