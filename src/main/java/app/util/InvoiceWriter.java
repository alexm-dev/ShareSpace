package app.util;

import app.model.Asset;
import app.model.Booking;
import app.model.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility to render and write a booking invoice to a plain text file.
 * The invoices are written to the "invoices" directory in the current working directory.
 */
public final class InvoiceWriter {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String DIR = "invoices";
    private static final String NL = System.lineSeparator();
    private static final String LINE = "================================================================";
    private static final String DASH = "----------------------------------------------------------------";

    private InvoiceWriter() {
    }

    /**
     * Writes the booking invoice to a text file.
     *
     * @param booking the booking for which the invoice is generated
     * @param asset the asset being booked
     * @param renter the user who is renting the asset
     * @param owner the user who owns the asset
     * @param categoryPath the category path of the asset
     * @return the file containing the invoice
     */
    public static File write(Booking booking, Asset asset, User renter, User owner, String categoryPath)
            throws IOException {
        new File(DIR).mkdirs();
        File file = new File(DIR, "invoice-" + booking.getId() + ".txt");
        Files.writeString(file.toPath(), render(booking, asset, renter, owner, categoryPath));
        return file;
    }

    private static String render(Booking b, Asset asset, User renter, User owner, String categoryPath) {
        StringBuilder sb = new StringBuilder();
        line(sb, LINE);
        line(sb, "                  SHARESPACE  -  RENTAL INVOICE");
        line(sb, LINE);
        line(sb, String.format("Invoice #: %04d", b.getId()));
        line(sb, "Issued:    " + LocalDate.now().format(DATE));
        line(sb, "");
        line(sb, DASH);

        line(sb, "LENDER (owner)");
        party(sb, owner);
        line(sb, "");
        line(sb, "RENTER");
        party(sb, renter);
        line(sb, DASH);

        line(sb, "ITEM");
        line(sb, "  Model:    " + (asset != null ? asset.getModel() : "#" + b.getAssetId()));
        if (categoryPath != null && !categoryPath.isBlank()) {
            line(sb, "  Category: " + categoryPath);
        }
        if (asset != null) {
            line(sb, String.format("  Rate:     €%.0f / day", asset.getDailyRate()));
        }
        line(sb, "");

        long days = Math.max(1, ChronoUnit.DAYS.between(
                b.getStartTime().toLocalDate(), b.getEndTime().toLocalDate()));
        line(sb, "RENTAL PERIOD");
        line(sb, "  From:     " + b.getStartTime().format(DATE));
        line(sb, "  To:       " + b.getEndTime().format(DATE));
        line(sb, "  Days:     " + days);
        line(sb, "");
        line(sb, DASH);

        line(sb, "  Status:   " + b.getStatus().getDbValue());
        line(sb, String.format("  TOTAL:    €%.2f", b.getTotalCost()));
        line(sb, LINE);
        line(sb, "   Thank you for using ShareSpace. This document summarises");
        line(sb, "        the booking and is not a legal tax invoice.");
        line(sb, LINE);
        return sb.toString();
    }

    private static void party(StringBuilder sb, User u) {
        if (u == null) {
            line(sb, "  (unknown)");
            return;
        }
        line(sb, "  Name:     " + (u.getFullName() != null ? u.getFullName() : "-"));
        line(sb, "  Username: @" + u.getUsername());
        line(sb, "  Email:    " + u.getEmail());
    }

    private static void line(StringBuilder sb, String text) {
        sb.append(text).append(NL);
    }
}
