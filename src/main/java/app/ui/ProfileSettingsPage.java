package app.ui;

import app.model.Role;
import app.model.User;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class ProfileSettingsPage {

    public VBox build() {

        Region header = Ui.header(
                new String[]{"CATALOG", "BOOKINGS", "PROFILE"},
                new Runnable[]{ShareS::showCatalogPage, ShareS::showBookingPage, ShareS::showProfilePage},
                ShareS::showStartPage);

        User user = ShareS.session.getActiveUser();

        HBox title = new HBox(
                16,
                Ui.bold("SETTINGS", 28),
                Ui.spacer(),
                Ui.light("PROFILE SETTINGS", 11));
        title.setAlignment(Pos.BOTTOM_LEFT);

        //username email password management
        TextField username = new TextField();
        username.setPromptText("New username");
        username.setMaxWidth(300);
        Label usernameError = Ui.light("", 12);

        TextField email = new TextField();
        email.setPromptText("New email");
        email.setMaxWidth(300);
        Label emailError = Ui.light("", 12);

        PasswordField pw = new PasswordField();
        pw.setPromptText("New password");
        pw.setMaxWidth(300);
        Label pwError = Ui.light("", 12);


        //role management
        List<Role> allRoles = ShareS.userService.getAllRoles();
        final int renterID = allRoles.stream().filter(r -> r.getName().equals("renter")).findFirst().get().getId();
        final int lenderId =  allRoles.stream().filter(r -> r.getName().equals("lender")).findFirst().get().getId();
        final boolean hasRenter = ShareS.userService.hasRole(user.getId(), "renter");
        final boolean hasLender = ShareS.userService.hasRole(user.getId(), "lender");
        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll(
                "RENTER",
                "LENDER",
                "BOTH"
        );
        roleBox.setValue(null);
        if (hasRenter && hasLender) {roleBox.setValue("BOTH");
        } else if (hasRenter) {roleBox.setValue("RENTER");
        } else if (hasLender) {roleBox.setValue("LENDER");}
        Label placeholder = Ui.light("", 12);

        //delete account
        TextField delete = new TextField();
        delete.setPromptText("Delete account");
        delete.setMaxWidth(300);
        String deleteString = "DELETE";
        Label deleteInfo = Ui.light("To delete your account write 'DELETE'" , 11);
        deleteInfo.setStyle("-fx-text-fill: #e53935;");
        Label deleteError = Ui.light("", 12);


        //save button with logic
        Button save = Ui.button(
                "Save settings",
                13,
                "-fx-background-color: #bdbdbd; -fx-text-fill: white;");
        save.setMaxWidth(300);
        save.setOnAction(e -> {
            if (!username.getText().isBlank()) {
                if (ShareS.userService.updateUsername(user.getId(), username.getText().trim())) {
                    usernameError.setText("Your username has been updated.");
                    usernameError.setStyle("-fx-text-fill: green;");
                } else {
                    usernameError.setText("Username already exists.");
                    usernameError.setStyle("-fx-text-fill: #e53935;");
                }
            }

            if (!email.getText().isBlank()) {
                if (ShareS.userService.updateEmail(user.getId(), email.getText().trim())) {
                    emailError.setText("Your email has been updated.");
                    emailError.setStyle("-fx-text-fill: green;");
                } else {
                    emailError.setText("Email already exists.");
                    emailError.setStyle("-fx-text-fill: #e53935;");
                }
            }

            if (!pw.getText().isBlank()) {
                if (ShareS.userService.updatePassword(user.getId(), pw.getText().toCharArray())) {
                    pwError.setText("Your password has been updated.");
                    pwError.setStyle("-fx-text-fill: green;");
                    pw.clear();
                } else {
                    pwError.setText("password error?"); //Does an invalid password exist?
                    pwError.setStyle("-fx-text-fill: #e53935;");
                }
            }

           switch (roleBox.getValue()) {
                case "RENTER":
                    if (!hasRenter) {ShareS.userService.assignRoleToUser(user.getId(), renterID);}
                    if (hasLender) {ShareS.userService.removeRoleFromUser(user.getId(), lenderId);}
                    break;
                case "LENDER":
                    if (hasRenter) {ShareS.userService.removeRoleFromUser(user.getId(), renterID);}
                    if (!hasLender) {ShareS.userService.assignRoleToUser(user.getId(), lenderId);}
                    break;
                case "BOTH":
                    if (!hasRenter) {ShareS.userService.assignRoleToUser(user.getId(), renterID);}
                    if (!hasLender) {ShareS.userService.assignRoleToUser(user.getId(), lenderId);}
                    break;
           }

            if (!delete.getText().isBlank()) {
                String enteredText = delete.getText(); //no trim() case sensitive!
                if (deleteString.equals(enteredText)) {
                    ShareS.userService.deleteAccount(user.getId());
                    //maybe a pop-up window to ask 'are you sure you want to delete your account'
                    //session.logout() or session clear?
                    //show homepage ?
                }
            } else {
                deleteError.setText("Input does not match.");
                deleteError.setStyle("-fx-text-fill: #e53935;");
            }
        });


        //combined settings for page
        VBox settings = new VBox(
                10,
                Ui.light("Username", 11), username, usernameError,
                Ui.light("Email address", 11), email, emailError,
                Ui.light("Password", 11), pw, pwError,
                Ui.light("Roles", 11), roleBox, placeholder,
                Ui.light("Delete account", 11), deleteInfo, delete, deleteError,
                save);
        settings.setStyle("-fx-background-color: white;");

        return Ui.page(header, title, settings, Ui.footer());
    }
}
