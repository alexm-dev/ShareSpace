package com.sharespace;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class LoginPage {

    public VBox build() {

        Button back = Ui.button("← Back", 13, "-fx-background-color: transparent;");
        back.setOnAction(e -> ShareS.showStartPage());
        HBox bar = new HBox(20, back, Ui.spacer(),
                Ui.button("Create an account", 13, "-fx-background-color: transparent;"));
        bar.setAlignment(Pos.CENTER_LEFT);

        TextField email = new TextField();
        email.setPromptText("Email address");
        PasswordField pw = new PasswordField();
        pw.setPromptText("Password");

        Button loginBtn = Ui.button("Log in", 13,
                "-fx-background-color: #bdbdbd; -fx-text-fill: white;");
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        VBox form = new VBox(10,
                Ui.light("Email address", 11), email,
                Ui.light("Password", 11), pw,
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