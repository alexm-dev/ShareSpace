package app;

import app.ui.ShareS;
import javafx.application.Application;

/**
 * Main entry point for ShareSpace.
 * Launches the JavaFX application.
 */
public class Main {

    /**
     * The main method that launches the ShareSpace application.
     *
     * @param args command-line arguments
     * @throws Exception if an error occurs during application launch
     */
    public static void main(String[] args) throws Exception {
        Application.launch(ShareS.class, args);
    }
}
