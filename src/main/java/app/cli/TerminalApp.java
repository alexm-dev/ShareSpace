package app.cli;

import app.model.*;
import app.service.*;
import app.util.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Temporary CLI testbed for ShareSpace before the JavaFX UI is built.
 *
 * Mirrors the planned UI for basic functionallity of sharespace.
 * Will be removed once JavaFX UI is ready.
 */
public class TerminalApp {

    private final Scanner scanner = new Scanner(System.in);
    private final UserService userService = new UserService();
    private final AssetService assetService = new AssetService();
    private final CatalogService catalogService = new CatalogService();
    private final RatingService ratingService = new RatingService();
    private final SessionService session;
    private final AdminMenu adminMenu;

    public TerminalApp(SessionService session) {
        this.session = session;
        this.adminMenu = new AdminMenu(scanner);
    }

    /**
     * Run the Termainl UI.
     * Loops until user exits, handles all exceptions to avoid crashing.
     */
    public void run() {
        System.out.println("ShareSpace CLI");
        if (session.isLoggedIn()) {
            System.out.println("welcome back " + session.getActiveUser().getUsername());
        }
        while (true) {
            try {
                if (!session.isLoggedIn()) guestMenu();
                else loggedInMenu();
            } catch (Exception e) {
                Logger.error("menu error", e);
            }
        }
    }

    private void guestMenu() {
        System.out.println();
        System.out.println("[guest menu]");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Browse assets");
        System.out.println("4. Admin / Debug");
        System.out.println("9. Toggle debug logs (" + (Logger.isDebug() ? "ON" : "OFF") + ")");
        System.out.println("0. Exit");

        switch (prompt()) {
            case "1" -> register();
            case "2" -> login();
            case "3" -> browseAssets();
            case "4" -> adminMenu.run();
            case "9" -> toggleDebug();
            case "0" -> exit();
            default -> System.out.println("unknown option");
        }
    }

    private void loggedInMenu() {
        User me = session.getActiveUser();
        int userId = me.getId();
        boolean isLender = userService.hasRole(userId, "lender");
        boolean isRenter = userService.hasRole(userId, "renter");

        System.out.println();
        System.out.println("[" + me.getUsername() + "]");
        System.out.println("1. View profile");
        System.out.println("2. Change username");
        System.out.println("3. Change email");
        System.out.println("4. Change password");
        System.out.println("5. Manage roles");
        System.out.println("6. Delete account");
        if (isLender) System.out.println("7. My listings");
        if (isRenter) System.out.println("8. My bookings (not yet)");
        System.out.println("r. Ratings");
        System.out.println("9. Toggle debug logs (" + (Logger.isDebug() ? "ON" : "OFF") + ")");
        System.out.println("a. Admin / Debug");
        System.out.println("b. Browse assets");
        System.out.println("l. Logout");
        System.out.println("0. Exit");

        switch (prompt().toLowerCase()) {
            case "1" -> viewProfile();
            case "2" -> changeUsername();
            case "3" -> changeEmail();
            case "4" -> changePassword();
            case "5" -> manageRoles();
            case "6" -> deleteAccount();
            case "7" -> {
                if (isLender) myListingsMenu();
                else System.out.println("unknown option");
            }
            case "8" -> {
                if (isRenter) System.out.println("BookingService not implemented yet.");
                else System.out.println("unknown option");
            }
            case "r" -> ratingsMenu();
            case "9" -> toggleDebug();
            case "a" -> adminMenu.run();
            case "b" -> browseAssets();
            case "l" -> logout();
            case "0" -> exit();
            default -> System.out.println("unknown option");
        }
    }

    private void register() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        if (!AuthUtil.isValidEmail(email)) {
            System.out.println("invalid email format");
            return;
        }
        char[] password = readPassword("Password: ");
        if (!AuthUtil.isValidPassword(password)) {
            System.out.println(AuthUtil.PASSWORD_RULES);
            return;
        }

        Logger.debug("register attempt: " + email);
        User user = userService.register(username, email, password);
        if (user == null) {
            System.out.println("registration failed - username or email taken");
            return;
        }
        System.out.println("registered: " + user.getUsername() + " (id=" + user.getId() + ")");
        Logger.info("user registered: id=" + user.getId());
        promptInitialRoles(user.getId());
        session.loginAfterRegister(user);
        System.out.println("logged in as " + user.getUsername());
    }

    private void promptInitialRoles(int userId) {
        System.out.println();
        System.out.println("How will you use ShareSpace?");
        System.out.println("1. Lend items only");
        System.out.println("2. Rent items only");
        System.out.println("3. Both (recommended)");

        String choice = prompt();
        boolean wantLender = choice.equals("1") || choice.equals("3");
        boolean wantRenter = choice.equals("2") || choice.equals("3");

        if (!wantLender && !wantRenter) {
            System.out.println("no role assigned - you can add one later via 'Manage roles'");
            return;
        }

        for (Role r : userService.getAllRoles()) {
            if (wantLender && r.getName().equalsIgnoreCase("lender")) {
                userService.assignRoleToUser(userId, r.getId());
            }
            if (wantRenter && r.getName().equalsIgnoreCase("renter")) {
                userService.assignRoleToUser(userId, r.getId());
            }
        }
        System.out.println("role(s) assigned");
    }

    private void login() {
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        char[] password = readPassword("Password: ");

        Logger.debug("login attempt: " + email);
        User user = session.login(email, password);
        if (user == null) {
            System.out.println("invalid email or password");
            Logger.warn("failed login: " + email);
            return;
        }
        System.out.println("welcome " + user.getUsername());
        Logger.info("user logged in: id=" + user.getId());
    }

    private void logout() {
        int id = session.getActiveUser().getId();
        session.logout();
        System.out.println("logged out");
        Logger.info("user logged out: id=" + id);
    }

    private void viewProfile() {
        session.refreshActiveUser();
        User me = session.getActiveUser();

        System.out.println("id: " + me.getId());
        System.out.println("username: " + me.getUsername());
        System.out.println("email: " + me.getEmail());
        System.out.println("status: " + me.getStatus());
        System.out.println("joined: " + me.getCreatedTime());
    }

    private void changeUsername() {
        System.out.print("New username: ");
        String newName = scanner.nextLine().trim();

        int id = session.getActiveUser().getId();
        if (userService.updateUsername(id, newName)) {
            session.refreshActiveUser();
            System.out.println("ok");
            Logger.info("username changed: id=" + id);
        } else {
            System.out.println("failed - username taken");
        }
    }

    private void changeEmail() {
        System.out.print("New email: ");
        String newEmail = scanner.nextLine().trim();

        if (!AuthUtil.isValidEmail(newEmail)) {
            System.out.println("invalid email format");
            return;
        }

        int id = session.getActiveUser().getId();
        if (userService.updateEmail(id, newEmail)) {
            session.refreshActiveUser();
            System.out.println("ok");
            Logger.info("email changed: id=" + id);
        } else {
            System.out.println("failed - email taken");
        }
    }

    private void changePassword() {
        char[] oldPwd = readPassword("Current password: ");
        if (!session.verifyActivePassword(oldPwd)) {
            System.out.println("wrong password");
            Logger.warn("password change failed: id=" + session.getActiveUser().getId());
            return;
        }

        char[] newPwd = readPassword("New password: ");
        if (!AuthUtil.isValidPassword(newPwd)) {
            System.out.println(AuthUtil.PASSWORD_RULES);
            return;
        }

        int id = session.getActiveUser().getId();
        userService.updatePassword(id, newPwd);
        session.refreshActiveUser();
        System.out.println("password updated");
        Logger.info("password changed: id=" + id);
    }

    private void deleteAccount() {
        System.out.print("Type DELETE to confirm: ");
        if (!"DELETE".equals(scanner.nextLine().trim())) {
            System.out.println("cancelled");
            return;
        }
        int id = session.getActiveUser().getId();
        userService.deleteAccount(id);
        session.logout();
        System.out.println("account deleted");
        Logger.info("account deleted: id=" + id);
    }

    private void browseAssets() {
        List<Category> categories = catalogService.getAllCategories();
        if (categories.isEmpty()) {
            System.out.println("no categories yet (admin needs to seed them)");
            return;
        }
        System.out.println();
        System.out.println("[categories]");
        for (Category c : categories) {
            System.out.println(c.getId() + ". " + c.getName()
                    + (c.getDescription() != null ? " - " + c.getDescription() : ""));
        }
        Integer categoryId = promptInt("Pick category id (0 = back): ");
        if (categoryId == null || categoryId == 0) return;

        browseSubCategories(categoryId);
    }

    private void browseSubCategories(int categoryId) {
        List<SubCategory> subs = catalogService.getSubCategoriesByCategoryId(categoryId);
        if (subs.isEmpty()) {
            System.out.println("no sub-categories in this category");
            return;
        }

        System.out.println();
        System.out.println("[sub-categories]");
        for (SubCategory s : subs) {
            System.out.println(s.getId() + ". " + s.getName());
        }

        Integer subId = promptInt("Pick sub-category id (0 = back): ");
        if (subId == null || subId == 0) return;

        showAssetsInSubCategory(subId);
    }

    private void showAssetsInSubCategory(int subCategoryId) {
        List<Asset> assets = catalogService.getAssetsBySubCategory(subCategoryId);
        if (assets.isEmpty()) {
            System.out.println("no listings in this sub-category");
            return;
        }
        System.out.println();
        System.out.println("[listings]");
        for (Asset a : assets) {
            System.out.println("id=" + a.getId()
                    + " | " + a.getModel()
                    + " | " + a.getCondition()
                    + " | " + a.getDailyRate() + "/day"
                    + " | owner=" + a.getOwnerId());
            printAssetDetails(a);
        }
    }

    private void printAssetDetails(Asset a) {
        Location loc = assetService.findLocationById(a.getAssetLocationId());
        if (loc != null) {
            System.out.println("  location: " + formatLocation(loc));
        }
        if (a.getDescription() != null && !a.getDescription().isBlank()) {
            System.out.println("  " + a.getDescription());
        }
        Map<String, String> meta = MetadataUtil.parse(a.getMetadata());
        for (Map.Entry<String, String> e : meta.entrySet()) {
            System.out.println("  " + e.getKey() + ": " + e.getValue());
        }
    }

    private String formatLocation(Location l) {
        StringBuilder sb = new StringBuilder();
        sb.append(l.getStreetAddress()).append(", ");
        sb.append(l.getPostalCode()).append(" ").append(l.getCity());
        if (l.getDistrict() != null && !l.getDistrict().isBlank()) {
            sb.append(" (").append(l.getDistrict()).append(")");
        }
        sb.append(", ").append(l.getCountry());
        return sb.toString();
    }

    private void myListingsMenu() {
        while (true) {
            System.out.println();
            System.out.println("[my listings]");
            System.out.println("1. View my listings");
            System.out.println("2. Add new listing");
            System.out.println("3. Update a listing");
            System.out.println("4. Delete a listing");
            System.out.println("0. Back");

            switch (prompt()) {
                case "1" -> listMyAssets();
                case "2" -> createListing();
                case "3" -> updateListing();
                case "4" -> deleteListing();
                case "0" -> { return; }
                default -> System.out.println("unknown option");
            }
        }
    }

    private void listMyAssets() {
        int ownerId = session.getActiveUser().getId();
        List<Asset> mine = assetService.findByOwner(ownerId);
        if (mine.isEmpty()) {
            System.out.println("no listings yet");
            return;
        }
        for (Asset a : mine) {
            System.out.println("id=" + a.getId()
                    + " | " + a.getModel()
                    + " | " + a.getCondition()
                    + " | " + a.getDailyRate() + "/day"
                    + " | subCat=" + a.getSubCategoryId());
            printAssetDetails(a);
        }
    }

    private void manageRoles() {
        int userId = session.getActiveUser().getId();
        List<Role> roles = userService.getRolesForUser(userId);
        List<Role> allRoles = userService.getAllRoles();

        System.out.println();
        System.out.println("[manage roles]");
        System.out.print("current: ");
        System.out.println(roles.isEmpty() ? "none"
                : roles.stream().map(Role::getName).collect(Collectors.joining(", ")));

        System.out.println("available:");
        for (Role r : allRoles) {
            boolean has = roles.stream().anyMatch(ur -> ur.getId() == r.getId());
            System.out.println("  " + r.getId() + ". " + r.getName() + (has ? " [assigned]" : ""));
        }

        System.out.println("a. Add role");
        System.out.println("r. Remove role");
        System.out.println("0. Back");

        switch (prompt().toLowerCase()) {
            case "a" -> {
                Integer roleId = promptInt("Role id to add: ");
                if (roleId == null) return;
                if (userService.assignRoleToUser(userId, roleId)) {
                    System.out.println("role assigned");
                } else {
                    System.out.println("failed - already assigned or role not found");
                }
            }
            case "r" -> {
                Integer roleId = promptInt("Role id to remove: ");
                if (roleId == null) return;
                if (userService.removeRoleFromUser(userId, roleId)) {
                    System.out.println("role removed");
                } else {
                    System.out.println("failed - role not assigned");
                }
            }
            case "0" -> {}
            default -> System.out.println("unknown option");
        }
    }

    private void createListing() {
        if (!userService.hasRole(session.getActiveUser().getId(), "lender")) {
            System.out.println("you need the lender role to create listings (manage roles -> add lender)");
            return;
        }
        System.out.println("(use Admin / Debug to look up sub-category ids)");
        Integer subCategoryId = promptInt("Sub-category id: ");

        if (subCategoryId == null) {
            return;
        }

        SubCategory sub = catalogService.getSubCategoryById(subCategoryId);
        if (sub == null) {
            System.out.println("sub-category not found");
            return;
        }
        System.out.print("Model: ");
        String model = scanner.nextLine().trim();

        System.out.print("Description: ");
        String description = scanner.nextLine().trim();

        System.out.print("Condition: ");
        String condition = scanner.nextLine().trim();

        Double dailyRate = promptDouble("Daily rate: ");
        if (dailyRate == null) {
            return;
        }

        String metadata = promptMetadata(sub.getName(), null);

        System.out.print("City: ");
        String city = scanner.nextLine().trim();

        System.out.print("Postal code: ");
        String postalCode = scanner.nextLine().trim();

        System.out.print("District (blank = none): ");
        String district = scanner.nextLine().trim();
        if (district.isEmpty()) district = null;

        System.out.print("Street address: ");
        String streetAddress = scanner.nextLine().trim();

        System.out.print("Country: ");
        String country = scanner.nextLine().trim();

        int ownerId = session.getActiveUser().getId();
        Asset asset = new Asset(ownerId, subCategoryId, model, description, condition, 0, dailyRate);
        asset.setMetadata(metadata);
        Location loc = new Location(city, postalCode, district, streetAddress, country);

        Asset created = assetService.createAsset(asset, loc);
        if (created == null) {
            System.out.println("failed to create listing");
            return;
        }
        System.out.println("created listing id=" + created.getId());
        Logger.info("asset created: id=" + created.getId() + " owner=" + ownerId);
    }

    private void updateListing() {
        Integer id = promptInt("Asset id to update: ");
        if (id == null) return;
        Asset existing = assetService.findById(id);
        if (existing == null) {
            System.out.println("not found");
            return;
        }
        int me = session.getActiveUser().getId();
        if (existing.getOwnerId() != me) {
            System.out.println("not your listing");
            return;
        }

        existing.setModel(promptOrKeep("Model", existing.getModel()));
        existing.setDescription(promptOrKeep("Description", existing.getDescription()));
        existing.setCondition(promptOrKeep("Condition", existing.getCondition()));

        System.out.print("Daily rate [" + existing.getDailyRate() + "]: ");
        String rate = scanner.nextLine().trim();
        if (!rate.isEmpty()) {
            try { existing.setDailyRate(Double.parseDouble(rate)); }
            catch (NumberFormatException e) { System.out.println("bad number, kept old"); }
        }

        SubCategory sub = catalogService.getSubCategoryById(existing.getSubCategoryId());
        if (sub != null) {
            Map<String, String> current = MetadataUtil.parse(existing.getMetadata());
            existing.setMetadata(promptMetadataUpdate(sub.getName(), current));
        }

        if (assetService.updateAsset(existing, me)) {
            System.out.println("ok");
            Logger.info("asset updated: id=" + id);
        } else {
            System.out.println("update failed");
        }
    }

    private String promptMetadata(String subCategoryName, Map<String, String> existing) {
        List<String> keys = MetadataSchema.keysFor(subCategoryName);
        if (keys.isEmpty()) {
            System.out.println("(no metadata schema for " + subCategoryName + ", skipping)");
            return existing == null ? null : MetadataUtil.serialize(existing);
        }
        Map<String, String> map = new LinkedHashMap<>();
        if (existing != null) map.putAll(existing);

        System.out.println("metadata for " + subCategoryName + " (blank = keep / skip):");
        for (String key : keys) {
            String current = map.get(key);
            System.out.print("  " + key + (current != null ? " [" + current + "]" : "") + ": ");
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) map.put(key, value);
        }
        return MetadataUtil.serialize(map);
    }

    private String promptMetadataUpdate(String subCategoryName, Map<String, String> current) {
        List<String> keys = MetadataSchema.keysFor(subCategoryName);
        if (keys.isEmpty()) {
            System.out.println("(no metadata schema for " + subCategoryName + ", skipping)");
            return MetadataUtil.serialize(current);
        }

        Map<String, String> updated = new LinkedHashMap<>(current);

        System.out.println("current metadata (" + subCategoryName + "):");
        for (String key : keys) {
            System.out.println("  " + key + ": " + updated.getOrDefault(key, "(not set)"));
        }
        System.out.println("field to update (blank = done):");

        while (true) {
            System.out.print("  field: ");
            String field = scanner.nextLine().trim();
            if (field.isEmpty()) break;
            if (!keys.contains(field)) {
                System.out.println("  unknown field. valid: " + String.join(", ", keys));
                continue;
            }
            System.out.print("  " + field + " [" + updated.getOrDefault(field, "") + "]: ");
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                updated.put(field, value);
                System.out.println("  updated");
            }
        }
        return MetadataUtil.serialize(updated);
    }

    private void deleteListing() {
        Integer id = promptInt("Asset id to delete: ");
        if (id == null) return;
        System.out.print("Type DELETE to confirm: ");

        if (!"DELETE".equals(scanner.nextLine().trim())) {
            System.out.println("cancelled");
            return;
        }

        int me = session.getActiveUser().getId();
        if (assetService.deleteAsset(id, me)) {
            System.out.println("deleted");
            Logger.info("asset deleted: id=" + id);
        } else {
            System.out.println("not found or not your listing");
        }
    }

    // ratings menu and methods
    private void ratingsMenu() {
        while (true) {
            System.out.println();
            System.out.println("[ratings menu]");
            System.out.println("1. Submit a rating");
            System.out.println("2. View my received ratings");
            System.out.println("3. View average rating for a user");
            System.out.println("4. View average rating for an asset");
            System.out.println("0. Back");

            switch (prompt()) {
                case "1" -> submitRating();
                case "2" -> viewMyRatings();
                case "3" -> viewAverageForUser();
                case "4" -> viewAverageForAsset();
                case "0" -> { return; }
                default -> System.out.println("unknown option");
            }
        }
    }

    private void submitRating() {
        Integer bookingId = promptInt("Booking id to rate: ");
        if (bookingId == null) return;

        app.dao.BookingDAO bookingDAO = new app.dao.BookingDAO();
        Booking booking = bookingDAO.findById(bookingId);

        if (booking == null) {
            System.out.println("booking not found");
            return;
        }

        if (!booking.getStatus().equals("completed")) {
            System.out.println("booking is not completed yet — status: " + booking.getStatus());
            return;
        }

        int myId = session.getActiveUser().getId();
        if (booking.getRenterId() != myId) {
            System.out.println("this is not your booking");
            return;
        }

        Integer ratingValue = promptInt("Rating (1-5): ");
        if (ratingValue == null || ratingValue < 1 || ratingValue > 5) {
            System.out.println("invalid rating — must be between 1 and 5");
            return;
        }

        System.out.print("Comment (blank = none): ");
        String comment = scanner.nextLine().trim();
        if (comment.isEmpty()) comment = null;

        app.dao.AssetDAO assetDAO = new app.dao.AssetDAO();
        Asset asset = assetDAO.findById(booking.getAssetId());
        Integer ratedUserId = asset != null ? asset.getOwnerId() : null;

        Rating rating = new Rating(
                bookingId,
                myId,
                ratedUserId,
                ratingValue,
                comment
        );

        Rating result = ratingService.createRating(rating, booking);
        if (result != null) {
            System.out.println("rating submitted!");
        } else {
            System.out.println("failed to submit rating");
        }
    }

    private void viewMyRatings() {
        int myId = session.getActiveUser().getId();
        List<Rating> ratings = ratingService.findByRatedUser(myId);

        if (ratings.isEmpty()) {
            System.out.println("no ratings received yet");
            return;
        }

        System.out.println();
        System.out.println("[my received ratings]");
        for (Rating r : ratings) {
            System.out.println("booking=" + r.getBookingId()
                    + " | " + r.getRatingValue() + " stars"
                    + " | " + (r.getComment() != null ? r.getComment() : "no comment")
                    + " | from userId=" + r.getReviewerId());
        }
    }

    private void viewAverageForUser() {
        Integer userId = promptInt("User id: ");
        if (userId == null) return;

        double avg = ratingService.getAverageForUser(userId);
        if (avg == 0.0) {
            System.out.println("no ratings yet for user id=" + userId);
        } else {
            System.out.println("average rating for user id=" + userId + " : " + avg + " stars");
        }
    }

    private void viewAverageForAsset() {
        Integer assetId = promptInt("Asset id: ");
        if (assetId == null) return;

        double avg = ratingService.getAverageForAsset(assetId);
        if (avg == 0.0) {
            System.out.println("no ratings yet for asset id=" + assetId);
        } else {
            System.out.println("average rating for asset id=" + assetId + " : " + avg + " stars");
        }
    }

    // Prompt helpers

    private String promptOrKeep(String label, String current) {
        System.out.print(label + " [" + current + "]: ");
        String input = scanner.nextLine();
        return input.isEmpty() ? current : input;
    }

    private Integer promptInt(String label) {
        System.out.print(label);

        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("bad number");
            return null;
        }
    }

    private Double promptDouble(String label) {
        System.out.print(label);

        try {
            return Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("bad number");
            return null;
        }
    }

    private void toggleDebug() {
        Logger.setDebug(!Logger.isDebug());
        System.out.println("debug logs " + (Logger.isDebug() ? "ON" : "OFF"));
    }

    private void exit() {
        System.out.println("bye");
        System.exit(0);
    }

    private String prompt() {
        System.out.print("> ");
        return scanner.nextLine().trim();
    }

    private char[] readPassword(String label) {
        System.out.print(label);
        if (System.console() != null) {
            return System.console().readPassword();
        }
        Logger.debug("no Console, password input is visible");
        return scanner.nextLine().toCharArray();
    }
}