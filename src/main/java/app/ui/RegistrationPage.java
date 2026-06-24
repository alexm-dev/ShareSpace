package app.ui;

import app.model.Role;
import app.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class RegistrationPage {

    public VBox build() {

        List<Role> allRoles = ShareS.userService.getAllRoles();
        final int renterID = allRoles.stream().filter(r -> r.getName().equals("renter")).findFirst().get().getId();
        final int lenderId =  allRoles.stream().filter(r -> r.getName().equals("lender")).findFirst().get().getId();

        Button back = Ui.button(
                "← Back",
                13,
                "-fx-background-color: transparent;");
        back.setOnAction(e -> ShareS.showStartPage());
        Button login = Ui.button(
                "Already have an account?",
                13,
                "-fx-background-color: transparent;");
        login.setOnAction(e -> ShareS.showLoginPage());

        //horizontal tabs at the top
        HBox htabs = new HBox(
                20,
                back,
                Ui.spacer(),
                login);
        htabs.setAlignment(Pos.CENTER_LEFT);


        TextField username = new TextField();
        username.setPromptText("Username");
        TextField email = new TextField();
        email.setPromptText("Email address");
        PasswordField pw = new PasswordField();
        pw.setPromptText("Password");

        //radioboxes for roles
        ToggleGroup group = new ToggleGroup();
        RadioButton roleRenter = new RadioButton("Renter");
        RadioButton roleLender = new RadioButton("Lender");
        RadioButton roleBoth = new RadioButton("Both");

        roleRenter.setToggleGroup(group);
        roleLender.setToggleGroup(group);
        roleBoth.setToggleGroup(group);

        HBox roles = new HBox(
                12,
                roleRenter,
                roleLender,
                roleBoth
        );

        Label error = Ui.light("", 12);
        error.setStyle("-fx-text-fill: #e53935;");

        Button registerBtn = Ui.button(
                "Register",
                13,
                "-fx-background-color: #bdbdbd; -fx-text-fill: white;");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setOnAction(e -> {
            if (username.getText().isBlank()) {error.setText("Username can't be blank."); return;}

            RadioButton selected = (RadioButton) group.getSelectedToggle();
            if (selected == null) {error.setText("Please select a role."); return;}

            if (email.getText().isBlank()) {error.setText("Email can't be blank."); return;}

            if (pw.getText().isBlank()) {
                error.setText("Password can't be blank.");
                return;
            }


            User user = ShareS.userService.register(username.getText().trim(), email.getText().trim(), pw.getText().toCharArray());
            if (user != null) {
                ShareS.session.loginAfterRegister(user);
                switch (selected.getText()) {
                    case "Renter":
                        ShareS.userService.assignRoleToUser(user.getId(), renterID);
                        break;
                    case "Lender":
                        ShareS.userService.assignRoleToUser(user.getId(), lenderId);
                        break;
                    case "Both":
                        ShareS.userService.assignRoleToUser(user.getId(), renterID);
                        ShareS.userService.assignRoleToUser(user.getId(), lenderId);
                        break;
                }
                ShareS.showProfileSettingsPage();
            } else {
                error.setText("Registration failed.");
                pw.clear();
            }
        });


        //registration tabs
        VBox register = new VBox(
                10,
                Ui.light("Username", 11), username,
                Ui.light("Roles", 11), roles,
                Ui.light("Email address", 11), email,
                Ui.light("Password", 11), pw,
                error,
                registerBtn);
        register.setMaxWidth(360);

        //bold + registration tabs
        VBox createAcc = new VBox(
                24,
                Ui.bold("Create an account", 56),
                register);
        createAcc.setAlignment(Pos.CENTER);
        VBox.setVgrow(createAcc, Priority.ALWAYS);

        //combined layouts
        VBox root = new VBox(
                0,
                htabs,
                createAcc);
        root.setFillWidth(true);
        root.setPadding(new Insets(16, 60, 40, 60));
        root.setStyle("-fx-background-color: white;");
        return root;
    }
}
