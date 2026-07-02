package app.util;

/**
 * Constants used throughout the application.
 */
public class Constants {
    /**
     * Maximum size of an image in bytes (5 MB).
     */
    public static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024;

    /** Global font scale applied to UI text sizes. */
    public static final double FONT_SCALE = 1.2;

    /** Max decode width for full-width banner images, to cap their memory footprint. */
    public static final int BANNER_DECODE_WIDTH = 1720;

    /** Height of the footer band; the banner image is fit into this and the rest is colour-filled. */
    public static final double FOOTER_HEIGHT = 300;

    /** Width of the sliding drawer menu panel. */
    public static final double MENU_WIDTH = 200;
}
