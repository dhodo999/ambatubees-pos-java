package com.ambatubees.Controllers;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

import com.ambatubees.Database.Database;
import com.ambatubees.Database.QueryHelper;
import com.ambatubees.Entity.Customer;

import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;

public class CustomerController implements Initializable {

    public static CustomerController instance;

    @FXML
    private Button btnDelete, btnSave, btnUpdate, btnClear;

    @FXML
    private TableColumn<Customer, Integer> colID;

    @FXML
    private TableColumn<Customer, String> colName, colNumber, colAddress;

    @FXML
    private TableView<Customer> tableCustomer;

    @FXML
    private TextField tfCustomerName, tfPhoneNumber, tfAddress;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        new Database();
        addListenerForTable();
        showCustomer();
    }

    @FXML
    void saveCustomer(ActionEvent event) {
        String customerName = tfCustomerName.getText();
        String phoneNumber = tfPhoneNumber.getText();
        String address = tfAddress.getText();

        if (customerName.isEmpty() || phoneNumber.isEmpty() || address.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, null, "Error", "Please fill all fields");
        } else {
            try {
                Connection conn = Database.connect();
                String sql = QueryHelper.INSERT_CUSTOMER;
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, customerName);
                ps.setString(2, phoneNumber);
                ps.setString(3, address);

                int res = ps.executeUpdate();
                if (res > 0) {
                    showAlert(Alert.AlertType.INFORMATION, DashboardController.getPrimaryStage(), "Success",
                            "Customer added successfully");
                    tfCustomerName.clear();
                    tfPhoneNumber.clear();
                    tfAddress.clear();
                    DashboardController.instance.populateCustomerName();
                    showCustomer();
                } else {
                    showAlert(Alert.AlertType.ERROR, DashboardController.getPrimaryStage(), "Error",
                            "Failed to add customer");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, DashboardController.getPrimaryStage(), "Error",
                        "An error occurred while adding the customer");
            }
        }
    }

    @FXML
    void updateCustomer(ActionEvent event) {
        try {
            Customer customer = tableCustomer.getSelectionModel().getSelectedItem();
            String sql = QueryHelper.UPDATE_CUSTOMER;
            executeQuery(sql, tfCustomerName.getText(), tfPhoneNumber.getText(), tfAddress.getText(),
                    String.valueOf(customer.getId()));
            DashboardController.instance.populateCustomerName();
            showCustomer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void deleteCustomer(ActionEvent event) {
        try {
            Customer customer = tableCustomer.getSelectionModel().getSelectedItem();
            if (customer == null) {
                showAlert(AlertType.ERROR, tableCustomer.getScene().getWindow(), "Error", "No customer selected");
                return;
            }

            Alert confirmationAlert = new Alert(AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirmation");
            confirmationAlert.setHeaderText(null);
            confirmationAlert.setContentText("Are you sure you want to delete this customer?");
            Stage stage = (Stage) tableCustomer.getScene().getWindow();
            confirmationAlert.initOwner(stage);

            confirmationAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    if (isCustomerReferencedInOrders(customer.getId())) {
                        showAlert(AlertType.ERROR, stage, "Error",
                                "Cannot delete customer. The customer is referenced in orders.");
                    } else {
                        String sql = QueryHelper.DELETE_CUSTOMER;
                        executeQuery(sql, String.valueOf(customer.getId()));
                        DashboardController.instance.populateCustomerName();
                        showCustomer();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isCustomerReferencedInOrders(int customerId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        boolean isReferenced = false;
        try {
            connection = Database.connect();
            String sql = QueryHelper.COUNT_CUSTOMER;
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, customerId);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                isReferenced = resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return isReferenced;
    }

    @FXML
    void clearField(ActionEvent event) {
        tfCustomerName.clear();
        tfPhoneNumber.clear();
        tfAddress.clear();
        btnClear.setDisable(true);
        btnSave.setDisable(false);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
    }

    private void executeQuery(String sql, String... params) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = Database.connect();
            preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setString(i + 1, params[i]);
            }
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            Database.close(connection, preparedStatement);
        }
    }

    public void showCustomer() {
        ObservableList<Customer> list = getTableList();
        colID.setCellValueFactory(new PropertyValueFactory<Customer, Integer>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<Customer, String>("customerName"));
        colNumber.setCellValueFactory(new PropertyValueFactory<Customer, String>("phoneNumber"));
        colAddress.setCellValueFactory(new PropertyValueFactory<Customer, String>("address"));
        tableCustomer.setItems(list);
    }

    private ObservableList<Customer> getTableList() {
        ObservableList<Customer> customerList = FXCollections.observableArrayList();
        Connection connection = null;
        try {
            connection = Database.connect();
            String sql = QueryHelper.SELECT_CUSTOMER;
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                Customer customer = new Customer(rs.getInt("CustomerID"),
                        rs.getString("CustomerName"),
                        rs.getString("PhoneNumber"),
                        rs.getString("Address"));
                customerList.add(customer);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return customerList;
    }

    private void addListenerForTable() {
        tableCustomer.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                btnSave.setDisable(true);
                btnUpdate.setDisable(false);
                btnDelete.setDisable(false);
                btnClear.setDisable(false);
                tfCustomerName.setText(newSelection.getCustomerName());
                tfPhoneNumber.setText(newSelection.getPhoneNumber());
                tfAddress.setText(newSelection.getAddress());
            } else {
                tfCustomerName.clear();
                tfPhoneNumber.clear();
                tfAddress.clear();
                btnSave.setDisable(false);
                btnUpdate.setDisable(true);
                btnDelete.setDisable(true);
                btnClear.setDisable(true);
            }
        });
    }

    public static void showAlert(Alert.AlertType alertType, Window owner, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(owner);
        alert.showAndWait();
    }
}