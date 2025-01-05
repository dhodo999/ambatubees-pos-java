package com.ambatubees.Controllers;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

import com.ambatubees.Entity.OrderItem;
import com.ambatubees.Entity.Product;
import com.ambatubees.Entity.Order;
import com.ambatubees.Database.Database;
import com.ambatubees.Database.QueryHelper;

import javafx.fxml.Initializable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Window;

public class OrderItemController implements Initializable {

    public static OrderItemController instance;

    @FXML
    private Button btnDelete, btnSave, btnUpdate, btnClear;

    @FXML
    private ComboBox<Order> cbOrderID;

    @FXML
    private ComboBox<Product> cbProductName;

    @FXML
    private TableColumn<OrderItem, Integer> colID, colOrderID, colQuantity;

    @FXML
    private TableColumn<OrderItem, Double> colPrice, colSubTotal;

    @FXML
    private TableColumn<OrderItem, String> colProductName;

    @FXML
    private TextField etID, etPrice, etQuantity, etSubTotal;

    @FXML
    private TableView<OrderItem> tableOrderItem;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        new Database();
        initializeUIComponents();
    }

    private void initializeUIComponents() {
        showOrderItem();
        addListenerForTable();
        populateOrderID();
        populateProductName();
        addProductSelectionListener();
        addQuantityListener();
    }

    @FXML
    void saveOrderItem(ActionEvent event) {
        if (areFieldsValid()) {
            try (Connection conn = Database.connect()) {
                String orderId = String.valueOf(cbOrderID.getSelectionModel().getSelectedItem().getId());
                String productId = String.valueOf(cbProductName.getSelectionModel().getSelectedItem().getId());
                int quantity = Integer.parseInt(etQuantity.getText());
                double price = fetchProductPrice(conn, productId);
                double subTotal = price * quantity;

                try (PreparedStatement ps = conn.prepareStatement(QueryHelper.INSERT_ORDER_ITEM)) {
                    ps.setString(1, orderId);
                    ps.setString(2, productId);
                    ps.setDouble(3, price);
                    ps.setInt(4, quantity);
                    ps.setDouble(5, subTotal);

                    if (ps.executeUpdate() > 0) {
                        showAlert(Alert.AlertType.INFORMATION, DashboardController.getPrimaryStage(), "Success",
                                "Order Item added successfully");
                        refreshUI(orderId, event);
                    } else {
                        showAlert(Alert.AlertType.ERROR, DashboardController.getPrimaryStage(), "Error",
                                "Failed to add Order Item");
                    }
                }
            } catch (SQLException e) {
                handleSQLException(e, "An error occurred while adding Order Item");
            }
        }
    }

    @FXML
    void updateOrderItem(ActionEvent event) {
        try {
            OrderItem orderItem = tableOrderItem.getSelectionModel().getSelectedItem();
            String orderId = String.valueOf(cbOrderID.getSelectionModel().getSelectedItem().getId());
            String productId = String.valueOf(cbProductName.getSelectionModel().getSelectedItem().getId());
            String quantity = etQuantity.getText();
            String sql = QueryHelper.UPDATE_ORDER_ITEM;

            executeQuery(sql, orderId, productId, etPrice.getText(), quantity, etSubTotal.getText(), String.valueOf(orderItem.getId()));
            refreshUI(orderId, event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void deleteOrderItem(ActionEvent event) {
        try {
            OrderItem orderItem = tableOrderItem.getSelectionModel().getSelectedItem();
            String sql = QueryHelper.DELETE_ORDER_ITEM;

            executeQuery(sql, String.valueOf(orderItem.getId()));
            refreshUI(String.valueOf(orderItem.getOrderId()), event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void clearField(ActionEvent event) {
        clearFields();
        toggleButtons(true, false, false, false);
    }

    private void executeQuery(String sql, String... params) {
        try (Connection connection = Database.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setString(i + 1, params[i]);
            }
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            handleSQLException(ex, "Error executing query");
        }
    }

    private void showOrderItem() {
        ObservableList<OrderItem> orderItems = getOrderItem();
        colID.setCellValueFactory(new PropertyValueFactory<>("Id"));
        colOrderID.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colProductName.setCellValueFactory(new PropertyValueFactory<>("description"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colSubTotal.setCellValueFactory(new PropertyValueFactory<>("subTotal"));
        tableOrderItem.setItems(orderItems);
    }

    private ObservableList<OrderItem> getOrderItem() {
        ObservableList<OrderItem> orderItems = FXCollections.observableArrayList();
        try (Connection connection = Database.connect();
             Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(QueryHelper.SELECT_ORDER_ITEM)) {
            while (rs.next()) {
                orderItems.add(new OrderItem(rs.getInt("OrderItemID"), rs.getInt("OrderID"), rs.getInt("ProductID"),
                        rs.getString("ProductDescription"), rs.getDouble("Price"), rs.getInt("Quantity"), rs.getDouble("SubTotal")));
            }
        } catch (SQLException e) {
            handleSQLException(e, "Error fetching order items");
        }
        return orderItems;
    }

    private void addProductSelectionListener() {
        cbProductName.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            if (newValue != null) {
                fetchAndDisplayProductPrice(newValue.getId());
            }
        });
    }

    private void fetchAndDisplayProductPrice(int productId) {
        try (Connection connection = Database.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(QueryHelper.SELECT_PRODUCT_PRICE)) {
            preparedStatement.setInt(1, productId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    etPrice.setText(String.valueOf(resultSet.getDouble("ProductPrice")));
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, "Error fetching product price");
        }
    }

    private void addQuantityListener() {
        etQuantity.textProperty().addListener((observable, oldValue, newValue) -> calculateSubTotal());
    }

    private void calculateSubTotal() {
        try {
            double price = Double.parseDouble(etPrice.getText());
            int quantity = Integer.parseInt(etQuantity.getText());
            etSubTotal.setText(String.valueOf(price * quantity));
        } catch (NumberFormatException e) {
            etSubTotal.clear();
        }
    }

    private void updateOrderTotalAmount(String orderId) {
        try (Connection connection = Database.connect();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT SUM(SubTotal) AS TotalAmount FROM order_items WHERE OrderID = ?")) {
            preparedStatement.setString(1, orderId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    double totalAmount = resultSet.getDouble("TotalAmount");
                    try (PreparedStatement updateStmt = connection.prepareStatement("UPDATE orders SET TotalAmount = ? WHERE OrderID = ?")) {
                        updateStmt.setDouble(1, totalAmount);
                        updateStmt.setString(2, orderId);
                        updateStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, "Error updating order total amount");
        }
    }

    public void populateOrderID() {
        ObservableList<Order> orderList = FXCollections.observableArrayList();
        try (Connection connection = Database.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(QueryHelper.SELECT_ORDER_ID);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                orderList.add(new Order(resultSet.getInt("OrderID")));
            }
        } catch (SQLException e) {
            handleSQLException(e, "Error populating order IDs");
        }
        cbOrderID.setItems(orderList);
    }

    private void populateProductName() {
        ObservableList<Product> productList = FXCollections.observableArrayList();
        try (Connection connection = Database.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(QueryHelper.SELECT_PRODUCT_NAME);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                productList.add(new Product(resultSet.getInt("ProductID"), resultSet.getString("ProductDescription")));
            }
        } catch (SQLException e) {
            handleSQLException(e, "Error populating product names");
        }
        cbProductName.setItems(productList);
    }

    private void addListenerForTable() {
        tableOrderItem.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateFields(newSelection);
                toggleButtons(false, true, true, true);
            } else {
                clearFields();
                toggleButtons(true, false, false, false);
            }
        });
    }

    private void populateFields(OrderItem orderItem) {
        etID.setText(String.valueOf(orderItem.getId()));
        cbOrderID.getItems().stream().filter(order -> order.getId() == orderItem.getOrderId()).findFirst().ifPresent(order -> cbOrderID.getSelectionModel().select(order));
        cbProductName.getItems().stream().filter(product -> product.getProductDescription().equals(orderItem.getDescription())).findFirst().ifPresent(product -> cbProductName.getSelectionModel().select(product));
        etPrice.setText(String.valueOf(orderItem.getPrice()));
        etQuantity.setText(String.valueOf(orderItem.getQuantity()));
        etSubTotal.setText(String.valueOf(orderItem.getSubTotal()));
    }

    private void clearFields() {
        etID.clear();
        cbOrderID.getSelectionModel().clearSelection();
        cbProductName.getSelectionModel().clearSelection();
        etPrice.clear();
        etQuantity.clear();
        etSubTotal.clear();
    }

    private void toggleButtons(boolean save, boolean update, boolean delete, boolean clear) {
        btnSave.setDisable(!save);
        btnUpdate.setDisable(!update);
        btnDelete.setDisable(!delete);
        btnClear.setDisable(!clear);
    }

    private boolean areFieldsValid() {
        if (cbOrderID.getSelectionModel().isEmpty() || cbProductName.getSelectionModel().isEmpty() || etQuantity.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, DashboardController.getPrimaryStage(), "Error", "Please fill all the fields");
            return false;
        }
        return true;
    }

    private double fetchProductPrice(Connection conn, String productId) throws SQLException {
        try (PreparedStatement priceStmt = conn.prepareStatement(QueryHelper.SELECT_PRODUCT_PRICE)) {
            priceStmt.setString(1, productId);
            try (ResultSet rs = priceStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("ProductPrice");
                }
            }
        }
        return 0;
    }

    private void refreshUI(String orderId, ActionEvent event) {
        showOrderItem();
        clearField(event);
        updateOrderTotalAmount(orderId);
        DashboardController.instance.showOrder();
    }

    private void handleSQLException(SQLException e, String message) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, DashboardController.getPrimaryStage(), "Error", message);
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
