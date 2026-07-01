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

/**
 * RegistrationPage is the UI page for user registration.
 * It allows users to create an account by providing relevant information and selecting their role.
 * Is also tied to the login page for quick switching between registration and login.
 */
public class RegistrationPage {

    public VBox build() {

        List<Role> allRoles = ShareS.userService.getAllRoles();
        final int renterID = allRoles.stream().filter(r -> r.getName().equals("renter")).findFirst().get().getId();
        final int lenderId = allRoles.stream().filter(r -> r.getName().equals("lender")).findFirst().get().getId();

        Button back = Ui.button(
                "← Back",
                13,
                "-fx-background-color: transparent;");
        back.setOnAction(event -> ShareS.showStartPage());
        Button login = Ui.button(
                "Already have an account?",
                13,
                "-fx-background-color: transparent;");
        login.setOnAction(event -> ShareS.showLoginPage());

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

        //radio boxes for roles
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
        registerBtn.setOnAction(event -> {
            if (username.getText().isBlank()) {
                error.setText("Username can't be blank.");
                return;
            }

            RadioButton selected = (RadioButton) group.getSelectedToggle();
            if (selected == null) {
                error.setText("Please select a role.");
                return;
            }

            if (email.getText().isBlank()) {
                error.setText("Email can't be blank.");
                return;
            }

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
                ShareS.showStartPage();
            } else {
                error.setText("Registration failed.");
                pw.clear();
            }
        });

        // password hint ✓ ✗
        Label pwAuthLength = Ui.light("✗ At least 8 character", 11);
        Label pwAuthUpper = Ui.light("✗ At least one uppercase letter", 11);
        Label pwAuthLower = Ui.light("✗ At least one lowercase letter", 11);
        Label pwAuthSymbol = Ui.light("✗ At least one symbol", 11);
        Label pwAuthDigit = Ui.light("✗ At least one digit", 11);

        pwAuthLength.setStyle("-fx-text-fill: gray;");
        pwAuthUpper.setStyle("-fx-text-fill: gray;");
        pwAuthLower.setStyle("-fx-text-fill: gray;");
        pwAuthSymbol.setStyle("-fx-text-fill: gray;");
        pwAuthDigit.setStyle("-fx-text-fill: gray;");

        VBox pwAuth = new VBox(2, pwAuthLength, pwAuthUpper, pwAuthLower, pwAuthSymbol, pwAuthDigit);

        // listener for pw textfield live updates hints
        pw.textProperty().addListener((observable, oldValue, newValue) -> {
            updateRule(pwAuthLength, newValue.length() >= 8, "At least 8 character");
            updateRule(pwAuthUpper, newValue.chars().anyMatch(Character::isUpperCase), "At least one uppercase letter");
            updateRule(pwAuthLower, newValue.chars().anyMatch(Character::isLowerCase), "At least one lowercase letter");
            updateRule(pwAuthSymbol, newValue.chars().anyMatch(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c)), "At least one symbol");
            updateRule(pwAuthDigit, newValue.chars().anyMatch(Character::isDigit), "At least one digit");
        });


        //registration tabs
        VBox register = new VBox(
                10,
                Ui.light("Username", 11), username,
                Ui.light("Roles", 11), roles,
                Ui.light("Email address", 11), email,
                Ui.light("Password", 11), pw, pwAuth,
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

    private void updateRule(Label label, boolean isFulfilled, String updatedText) {
        if (isFulfilled) {
            label.setText("✓ " + updatedText);
            label.setStyle("-fx-text-fill: green;");
        } else {
            label.setText("✗ " +  updatedText);
            label.setStyle("-fx-text-fill: gray;");
        }
    }

}
