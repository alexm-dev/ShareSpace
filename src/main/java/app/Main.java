package app;

import app.cli.TerminalApp;
import app.database.Database;
import app.service.SessionService;
import app.ui.ShareS;
import app.util.Logger;
import javafx.application.Application;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws Exception {
        if (Arrays.asList(args).contains("--cli")) {
            Database.initialize();
            Logger.info("ShareSpace started (CLI)");
            SessionService session = new SessionService();
            if (session.restoreSession() != null) {
                Logger.info("restored session for: " + session.getActiveUser().getUsername());
            }
            new TerminalApp(session).run();
        } else {
            Application.launch(ShareS.class, args);
        }
    }
}
