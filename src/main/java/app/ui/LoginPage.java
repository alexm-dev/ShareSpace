package app.ui;
import app.util.Palette;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * LoginPage is the UI page for user login.
 * It allows users to log in by providing their email and password.
 */
public class LoginPage {

    public VBox build() {

        Button back = Ui.button("← Back", 13, "-fx-background-color: transparent;");
        back.setOnAction(e -> ShareS.showStartPage());
        Button createA = Ui.button("Create an account", 13, "-fx-background-color: transparent;");
        createA.setOnAction(event -> ShareS.showRegistrationPage());
        HBox bar = new HBox(20, back, Ui.spacer(), createA);
        bar.setAlignment(Pos.CENTER_LEFT);

        TextField email = new TextField();
        email.setPromptText("Email address");
        PasswordField pw = new PasswordField();
        pw.setPromptText("Password");

        Label error = Ui.light("", 12);
        error.setStyle("-fx-text-fill: " + Palette.ERROR_RED + ";");

        Button loginBtn = Ui.button("Log in", 13,
                "-fx-background-color: " + Palette.BUTTON_GREY + "; -fx-text-fill: white;");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> {
            if (ShareS.session.login(email.getText().trim(), pw.getText().toCharArray()) != null) {
                ShareS.showStartPage();
            } else {
                error.setText("Invalid email or password.");
                pw.clear();
            }
        });

        VBox form = new VBox(10,
                Ui.light("Email address", 11), email,
                Ui.light("Password", 11), pw,
                error,
                loginBtn);
        form.setMaxWidth(360);

        VBox center = new VBox(24, Ui.bold("Log in", 56), form);
        center.setAlignment(Pos.CENTER);
        VBox.setVgrow(center, Priority.ALWAYS);

        VBox root = new VBox(0, bar, center);
        root.setFillWidth(true);
        root.setPadding(new Insets(16, 60, 40, 60));
        root.setStyle("-fx-background-color: white;");
        return root;
    }
}
