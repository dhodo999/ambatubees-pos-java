package com.ambatubees.Controllers;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

import com.ambatubees.Entity.Order;
import com.ambatubees.Entity.Customer;
import com.ambatubees.Database.Database;
import com.ambatubees.Database.QueryHelper;

import javafx.fxml.Initializable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.converter.LocalDateStringConverter;

public class DashboardController implements Initializable {

    public static DashboardController instance;
    private static Stage primaryStage;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private Scene fxmlFile;
    private Parent root;
    private Stage window;

    @FXML
    private Label lbUsername;

    @FXML
    private DatePicker DatePicker;

    @FXML
    private Button btnAddOrder, btnCancelOrder, btnUpdateOrder, btnClear, btnOrderItems, btnPayment, btnManageCustomer, btnManageProduct;

    @FXML
    private ComboBox<Customer> cbCustomerName;

    @FXML
    private TableColumn<Order, Integer> colCustomerID, colID;
    @FXML
    private TableColumn<Order, String> colCustomerName, colStatus;
    @FXML
    private TableColumn<Order, LocalDate> colDate;
    @FXML
    private TableColumn<Order, Double> colTotal;

    @FXML
    private TableView<Order> tableOrder;

    @FXML
    private TextField etID, etTotalAmount;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        new Database();
        showOrder();
        populateCustomerName();
        addListenerForTable();
    }

    @FXML
    void handleKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case F1: handleOrder(null); break;
            case F2: openModalWindow("/com/ambatubees/fxml/OrderItem.fxml", "Manage Order Items"); break;
            case F3: cancelOrder(null); break;
            case F4: openModalWindow("/com/ambatubees/fxml/Product.fxml", "Manage Product"); break;
            case F5: openModalWindow("/com/ambatubees/fxml/Customer.fxml", "Manage Customer"); break;
            case F6: openModalWindow("/com/ambatubees/fxml/Transaction.fxml", "Payment"); break;
            case F7: logoutConfirmation(event); break;
            default: System.out.println("Unhandled key: " + event.getCode()); break;
        }
    }

    @FXML
    void handleOrder(ActionEvent event) {
        Customer selectedCustomer = cbCustomerName.getSelectionModel().getSelectedItem();
        LocalDate orderDate = DatePicker.getValue();

        if (selectedCustomer == null || orderDate == null) {
            showAlert(Alert.AlertType.ERROR, window, "Error", "Please fill all the fields");
        } else {
            addOrder(selectedCustomer, orderDate);
        }
    }

    @FXML
    void updateOrder(ActionEvent event) {
        try {
            Order order = tableOrder.getSelectionModel().getSelectedItem();
            Customer selectedCustomer = cbCustomerName.getSelectionModel().getSelectedItem();
            LocalDate orderDate = DatePicker.getValue();
            String customerId = String.valueOf(selectedCustomer.getId());
            String formattedOrderDate = orderDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String totalAmount = etTotalAmount.getText();
            String sql = QueryHelper.UPDATE_ORDER;
            executeQuery(sql, customerId, formattedOrderDate, totalAmount, String.valueOf(order.getId()));
            showOrder();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void cancelOrder(ActionEvent event) {
        try {
            Order order = tableOrder.getSelectionModel().getSelectedItem();
            String sql = QueryHelper.UPDATE_ORDER_STATUS;
            executeQuery(sql, "Canceled", String.valueOf(order.getId()));
            if (OrderItemController.instance != null) {
                OrderItemController.instance.populateOrderID();
            }
            showOrder();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleOrderItems(ActionEvent event) {
        openModalWindow("/com/ambatubees/fxml/OrderItem.fxml", "Manage Order Items");
    }

    @FXML
    void handlePayment(ActionEvent event) {
        openModalWindow("/com/ambatubees/fxml/Transaction.fxml", "Payment");
    }

    @FXML
    void clearField(ActionEvent event) {
        etID.clear();
        cbCustomerName.getSelectionModel().clearSelection();
        DatePicker.getEditor().clear();
        etTotalAmount.clear();
        btnAddOrder.setDisable(false);
        btnUpdateOrder.setDisable(true);
        btnCancelOrder.setDisable(true);
    }

    @FXML
    private void manageCustomer(ActionEvent event) {
        openModalWindow("/com/ambatubees/fxml/Customer.fxml", "Manage Customer");
    }

    @FXML
    void manageProduct(ActionEvent event) {
        openModalWindow("/com/ambatubees/fxml/Product.fxml", "Manage Product");
    }

    @FXML
    void logout(ActionEvent event) {
        logoutConfirmation(event);
    }

    private void logoutConfirmation(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to logout?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/com/ambatubees/fxml/Login.fxml"));
                Scene loginScene = new Scene(root);
                Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                primaryStage.setScene(loginScene);
                primaryStage.setTitle("Login");
                primaryStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void logoutConfirmation(KeyEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to logout?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/com/ambatubees/fxml/Login.fxml"));
                Scene loginScene = new Scene(root);
                Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                primaryStage.setScene(loginScene);
                primaryStage.setTitle("Login");
                primaryStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public void setUsername(String username) {
        lbUsername.setText(username);
    }

    public void showOrder() {
        ObservableList<Order> list = getOrderList();
        colCustomerID.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colCustomerName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colDate.setCellFactory(column -> new TextFieldTableCell<>(new LocalDateStringConverter(DATE_FORMATTER, null)) {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : DATE_FORMATTER.format(item));
            }
        });
        tableOrder.setItems(list);
    }

    private ObservableList<Order> getOrderList() {
        ObservableList<Order> orderList = FXCollections.observableArrayList();
        try (Connection connection = Database.connect();
             Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(QueryHelper.SELECT_ORDER)) {

            while (rs.next()) {
                Order order = new Order(rs.getInt("OrderID"),
                        rs.getInt("CustomerID"),
                        rs.getDate("OrderDate").toLocalDate(),
                        rs.getDouble("TotalAmount"),
                        rs.getString("CustomerName"),
                        rs.getString("Status"));
                orderList.add(order);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return orderList;
    }

    public void populateCustomerName() {
        ObservableList<Customer> customerList = FXCollections.observableArrayList();
        try (Connection connection = Database.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(QueryHelper.SELECT_CUSTOMER_NAME);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int customerId = resultSet.getInt("CustomerID");
                String customerName = resultSet.getString("CustomerName");
                customerList.add(new Customer(customerId, customerName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        cbCustomerName.setItems(customerList);
    }

    private void executeQuery(String sql, String... params) {
        try (Connection connection = Database.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                preparedStatement.setString(i + 1, params[i]);
            }
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void openModalWindow(String resource, String title) {
        try {
            root = FXMLLoader.load(getClass().getResource(resource));
            fxmlFile = new Scene(root);
            window = new Stage();
            window.setScene(fxmlFile);
            window.setResizable(false);
            window.initModality(Modality.APPLICATION_MODAL);
            window.setAlwaysOnTop(true);
            window.setIconified(false);
            window.setTitle(title);
            setPrimaryStage(window);
            window.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addListenerForTable() {
        tableOrder.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateFields(newSelection);
            } else {
                clearFields();
            }
        });
    }

    private void populateFields(Order order) {
        btnAddOrder.setDisable(true);
        btnUpdateOrder.setDisable(false);
        btnCancelOrder.setDisable(false);
        etID.setText(String.valueOf(order.getId()));
        cbCustomerName.getItems().stream()
                .filter(customer -> customer.getCustomerName().equals(order.getCustomerName()))
                .findFirst()
                .ifPresent(customer -> cbCustomerName.getSelectionModel().select(customer));
        DatePicker.setValue(order.getOrderDate());
        DatePicker.getEditor().setText(DATE_FORMATTER.format(order.getOrderDate()));
        etTotalAmount.setText(String.valueOf(order.getTotalAmount()));
    }

    private void clearFields() {
        etID.clear();
        cbCustomerName.getSelectionModel().clearSelection();
        DatePicker.getEditor().clear();
        etTotalAmount.clear();
        btnAddOrder.setDisable(false);
        btnUpdateOrder.setDisable(true);
        btnCancelOrder.setDisable(true);
    }

    private void addOrder(Customer selectedCustomer, LocalDate orderDate) {
        try (Connection conn = Database.connect()) {
            String customerId = String.valueOf(selectedCustomer.getId());
            String formattedOrderDate = orderDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String totalAmount = "0";
            String status = "Pending";
            String sql = QueryHelper.INSERT_ORDER;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, customerId);
                ps.setString(2, formattedOrderDate);
                ps.setString(3, totalAmount);
                ps.setString(4, status);

                int res = ps.executeUpdate();
                if (res > 0) {
                    showAlert(Alert.AlertType.INFORMATION, window, "Success", "Order added successfully");
                    showOrder();
                    clearField(null);
                } else {
                    showAlert(Alert.AlertType.ERROR, window, "Error", "Failed to add order");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, window, "Error", "An error occurred while adding the order");
        }
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
