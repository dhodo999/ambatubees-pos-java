package com.ambatubees.Controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.ambatubees.Database.Database;

import javafx.fxml.Initializable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Window;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Node;

public class LoginController implements Initializable {

    @FXML
    private PasswordField txtPassword;

    @FXML
    private TextField txtUsername;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    private void actionLogin(ActionEvent event) {
        Window owner = txtUsername.getScene().getWindow();

        if (isInputValid(owner)) {
            String username = txtUsername.getText();
            String password = txtPassword.getText();

            Database database = new Database();
            if (database.validate(username, password)) {
                showInfoBox("Login Successful", null, "Success");
                loadDashboard(event, username);
            } else {
                showInfoBox("Please enter correct Username and Password", null, "Failed");
            }
        }
    }

    private boolean isInputValid(Window owner) {
        if (txtUsername.getText().isEmpty()) {
            showAlert(AlertType.ERROR, owner, "Please enter your Username", "Form Error!");
            return false;
        }
        if (txtPassword.getText().isEmpty()) {
            showAlert(AlertType.ERROR, owner, "Please enter your Password", "Form Error!");
            return false;
        }
        return true;
    }

    private void loadDashboard(ActionEvent event, String username) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ambatubees/fxml/Dashboard.fxml"));
            Parent parentRoot = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Dashboard | POS");
            stage.setScene(new Scene(parentRoot));
            stage.setResizable(false);
            DashboardController controller = loader.getController();
            controller.setUsername(username);
            stage.show();
            ((Node) (event.getSource())).getScene().getWindow().hide();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void showInfoBox(String infoMessage, String headerText, String title) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setContentText(infoMessage);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType alertType, Window owner, String message, String title) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.initOwner(owner);
        alert.show();
    }
}