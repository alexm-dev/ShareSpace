package app.service;

import app.dao.AssetDAO;
import app.dao.BookingDAO;
import app.dao.ImageDAO;
import app.dao.LocationDAO;
import app.dao.UserDAO;
import app.model.Asset;
import app.model.Location;
import app.model.User;
import app.model.enums.BookingStatus;
import static app.util.Constants.MAX_IMAGE_BYTES;
import java.util.List;

/**
 * Service layer for managing assets.
 * Handles business logic related to asset creation, updating, deletion, and retrieval.
 */
public class AssetService {

    private final AssetDAO assetDAO;
    private final ImageDAO assetImageDAO;
    private final LocationDAO locationDAO;
    private final BookingDAO bookingDAO;
    private final UserDAO userDAO;

    public AssetService() {
        this.assetDAO = new AssetDAO();
        this.assetImageDAO = new ImageDAO("asset_images", "asset_id");
        this.locationDAO = new LocationDAO();
        this.bookingDAO = new BookingDAO();
        this.userDAO = new UserDAO();
    }

    /**
     * Creates a new asset.
     *
     * @param asset The asset to create.
     * @param location The location associated with the asset. If it already exists, it will be reused.
     * @return The created asset with its ID, or null if creation failed.
     */
    public Asset createAsset(Asset asset, Location location) {
        Location existing = locationDAO.findMatch(location);
        if (existing != null) {
            location = existing;
        } else if (!locationDAO.create(location)) {
            return null;
        }

        asset.setAssetLocationId(location.getId());
        if (!assetDAO.create(asset)) {
            return null;
        }

        // Seed the owner's profile location from their first listing, if they have none yet.
        // After this the owner edits it in profile (or asset) settings, not here.
        User owner = userDAO.findById(asset.getOwnerId());
        if (owner != null && owner.getLocationId() == null) {
            owner.setLocationId(location.getId());
            userDAO.update(owner);
        }

        return asset;
    }

    /**
     * Updates an existing asset.
     *
     * @param asset The asset with updated information. Must have a valid ID.
     * @param requestingUserID The ID of the user requesting the update. Must be the owner of the asset.
     * @return True if the update was successful, false otherwise.
     */
    public boolean updateAsset(Asset asset, int requestingUserID) {
        Asset existing = assetDAO.findById(asset.getId());

        if (existing == null) {
            return false;
        }

        if (existing.getOwnerId() != requestingUserID) {
            return false;
        }

        if (hasActiveBookings(asset.getId())) {
            return false;
        }

        return assetDAO.update(asset);
    }

    /**
     * Deletes an asset by its ID.
     *
     * @param assetId The ID of the asset to delete.
     * @param requestingUserID The ID of the user requesting the deletion. Must be the owner of the asset.
     * @return True if the deletion was successful, false otherwise.
     */
    public boolean deleteAsset(int assetId, int requestingUserID) {
        Asset existing = assetDAO.findById(assetId);

        if (existing == null) {
            return false;
        }

        if (existing.getOwnerId() != requestingUserID) {
            return false;
        }

        if (hasActiveBookings(assetId)) {
            return false;
        }

        return assetDAO.delete(assetId);
    }

    /**
     * Checks if an asset has any active bookings (via booking status).
     * Is used to determine if an asset can be updated or deleted.
     *
     * @param assetId The ID of the asset to check.
     * @return True if there are active bookings, false otherwise.
     */
    public boolean hasActiveBookings(int assetId) {
        return bookingDAO.findByAssetId(assetId).stream()
                .anyMatch(b -> b.getStatus() == BookingStatus.PENDING
                        || b.getStatus() == BookingStatus.CONFIRMED);
    }

    /**
     * Finds an asset by its ID.
     *
     * @param id The ID of the asset to find.
     * @return The asset with the specified ID, or null if not found.
     */
    public Asset findById(int id) {
        return assetDAO.findById(id);
    }

    /**
     * Saves an image for an asset listing.
     * Only the assets owner can save an image.
     *
     * @param assetId the asset id
     * @param data the raw image bytes
     * @param mimeType the image mime type (e.g., "image/png")
     * @param requestingUserID the user requesting the change; must own the asset
     * @return true if the image was saved successfully
     */
    public boolean saveImage(int assetId, byte[] data, String mimeType, int requestingUserID) {
        if (data == null || data.length == 0 || data.length > MAX_IMAGE_BYTES) {
            return false;
        }

        Asset existing = assetDAO.findById(assetId);
        if (existing == null || existing.getOwnerId() != requestingUserID) {
            return false;
        }

        return assetImageDAO.save(assetId, data, mimeType);
    }

    /**
     * Deletes the image for an asset listing.
     * Only the assets owner can delete an image.
     *
     * @param assetId the asset id
     * @param requestingUserID the user requesting the change; must own the asset
     * @return true if the image was deleted successfully
     */
    public boolean deleteImage(int assetId, int requestingUserID) {
        Asset existing = assetDAO.findById(assetId);
        if (existing == null || existing.getOwnerId() != requestingUserID) {
            return false;
        }
        return assetImageDAO.delete(assetId);
    }

    /**
     * Retrieves the image for an asset listing.
     *
     * @param assetId the asset id
     * @return the raw image bytes, or null if not found
     */
    public byte[] getImage(int assetId) {
        return assetImageDAO.find(assetId);
    }

    /**
     * Looks up a location by id.
     *
     * @param locationId the location id (typically from Asset.getAssetLocationId())
     * @return the Location, or null if not found
     */
    public Location findLocationById(int locationId) {
        return locationDAO.findById(locationId);
    }

    /**
     * Finds all assets owned by a specific user.
     *
     * @param ownerId The ID of the owner whose assets to find.
     * @return A list of assets owned by the specified user, or an empty list if none are found.
     */
    public List<Asset> findByOwner(int ownerId) {
        return assetDAO.findByOwnerId(ownerId);
    }

    /**
     * Finds all assets that belong to a specific subcategory.
     *
     * @param subcategoryId The ID of the subcategory to search for.
     * @return A list of assets that belong to the specified subcategory, or an empty list if none are found.
     */
    public List<Asset> findBySubcategory(int subcategoryId) {
        return assetDAO.findBySubCategoryId(subcategoryId);
    }

    /**
     * Returns the location of an asset, shaped for the given viewer.
     *
     * The full address is only revealed to the asset owner
     * or to a renter who has a confirmed or completed booking on it. Anyone
     * else gets an approximate location (city, postal code, district, country)
     * with the street address removed.
     *
     * @param assetId the asset whose location is requested
     * @param viewerId the user viewing the asset
     * @return the location for this viewer, or null if the asset or its location is missing
     */
    public Location getLocationFor(int assetId, int viewerId) {
        Asset asset = assetDAO.findById(assetId);
        if (asset == null) {
            return null;
        }

        Location location = locationDAO.findById(asset.getAssetLocationId());
        if (location == null) {
            return null;
        }

        boolean isOwner = asset.getOwnerId() == viewerId;
        boolean hasBooking = bookingDAO.findByRenterId(viewerId).stream()
                .anyMatch(b -> b.getAssetId() == assetId
                        && (b.getStatus() == BookingStatus.CONFIRMED
                                || b.getStatus() == BookingStatus.COMPLETE));

        return (isOwner || hasBooking) ? location : location.withoutStreet();
    }
}
