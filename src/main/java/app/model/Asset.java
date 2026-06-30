package app.model;

/**
 * Represents an asset that can be rented on the platform.
 *
 * Each asset has an owner, belongs to a subcategory, and has details like
 * model, description, condition, location, daily rental rate, optional metadata,
 * as well as optional discount information.
 *
 * The Asset class provides constructors for creating new assets and getters/setters
 */
public class Asset {
    private int id;
    private int ownerId;
    private int subCategoryId;
    private String model;
    private String description;
    private String condition;
    private int assetLocationId;
    private double dailyRate;
    private String metadata;
    private int discountAfterDays;
    private double discountPercentage;

    /**
     * Constructor for creating an Asset with a ID (used when retrieving from the database).
     */
    public Asset(int id, int ownerId, int subCategoryId, String model, String description, String condition, int assetLocationId, double dailyRate) {
        this.id = id;
        this.ownerId = ownerId;
        this.subCategoryId = subCategoryId;
        this.model = model;
        this.description = description;
        this.condition = condition;
        this.assetLocationId = assetLocationId;
        this.dailyRate = dailyRate;
    }

    /**
     * Constructor for creating a new Asset without an ID (used when inserting into the database).
     */
    public Asset(int ownerId, int subCategoryId, String model, String description, String condition, int assetLocationId, double dailyRate) {
        this.ownerId = ownerId;
        this.subCategoryId = subCategoryId;
        this.model = model;
        this.description = description;
        this.condition = condition;
        this.assetLocationId = assetLocationId;
        this.dailyRate = dailyRate;
    }

    // Getters
    public int getId() { return id; }
    public int getOwnerId() { return ownerId; }
    public int getSubCategoryId() { return subCategoryId; }
    public String getModel() { return model; }
    public String getDescription() { return description; }
    public String getCondition() { return condition; }
    public int getAssetLocationId() { return assetLocationId; }
    public double getDailyRate() { return dailyRate; }
    public String getMetadata() { return metadata; }
    public int getDiscountAfterDays() { return discountAfterDays; }
    public double getDiscountPercentage() { return discountPercentage; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setModel(String model) { this.model = model; }
    public void setDescription(String description) { this.description = description; }
    public void setCondition(String condition) { this.condition = condition; }
    public void setAssetLocationId(int assetLocationId) { this.assetLocationId = assetLocationId; }
    public void setDailyRate(double dailyRate) { this.dailyRate = dailyRate; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public void setDiscountAfterDays(int discountAfterDays) { this.discountAfterDays = discountAfterDays; }
    public void setDiscountPercentage(double discountPercentage) { this.discountPercentage = discountPercentage; }
}
