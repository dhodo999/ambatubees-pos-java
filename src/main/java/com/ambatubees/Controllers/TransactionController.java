package com.ambatubees.Controllers;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import com.ambatubees.Database.Database;
import com.ambatubees.Database.QueryHelper;
import com.ambatubees.Entity.Transaction;
import com.ambatubees.Entity.Order;

import javafx.fxml.Initializable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Modality;
import javafx.stage.Window;
import javafx.util.converter.LocalDateStringConverter;

public class TransactionController implements Initializable {

    public static TransactionController instance;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @FXML
    private Button btnClear, btnSave, btnUpdate;

    @FXML
    private DatePicker DatePicker;

    @FXML
    private ComboBox<Order> cbOrderID;

    @FXML
    private ComboBox<String> cbPaymentMethod;

    @FXML
    private TableColumn<Transaction, Double> colAmountPaid, colExcessAmount;

    @FXML
    private TableColumn<Transaction, Integer> colID, colOrderID;

    @FXML
    private TableColumn<Transaction, String> colPaymentMethod;

    @FXML
    private TableColumn<Transaction, LocalDate> colTransactionDate;

    @FXML
    private TextField etAmountPaid, etExcessAmount, etID;

    @FXML
    private TableView<Transaction> tableTransaction;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        instance = this;
        new Database();
        initializeUIComponents();
        initializeListeners();
    }

    private void initializeUIComponents() {
        showTransaction();
        populateOrderID();
        cbPaymentMethod.getItems().addAll("Cash", "Credit Card", "Debit Card", "Mobile Payment");
    }

    private void initializeListeners() {
        cbOrderID.setOnAction(event -> fetchAndDisplayExcessAmount());
        etAmountPaid.textProperty().addListener((obs, oldVal, newVal) -> calculateExcessAmount());
        addListenerForTable();
    }

    @FXML
    void saveTransaction(ActionEvent event) {
        try {
            if (!validateForm())
                return;

            Order selectedOrder = cbOrderID.getSelectionModel().getSelectedItem();
            String orderId = String.valueOf(selectedOrder.getId());
            LocalDate orderDate = DatePicker.getValue();
            double amountPaid = Double.parseDouble(etAmountPaid.getText());
            String paymentMethod = cbPaymentMethod.getSelectionModel().getSelectedItem();
            double totalAmount = getOrderTotalAmount(Integer.parseInt(orderId));
            double excessAmount = amountPaid - totalAmount;

            String formattedOrderDate = orderDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            executeQuery(QueryHelper.INSERT_TRANSACTION, orderId, formattedOrderDate, paymentMethod, amountPaid,
                    excessAmount);
            executeQuery(QueryHelper.UPDATE_ORDER_STATUS, "Success", selectedOrder.getId());

            showAlert(Alert.AlertType.INFORMATION, DashboardController.getPrimaryStage(), "Success",
                    "Transaction saved successfully");
            refreshUI(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void updateTransaction(ActionEvent event) {
        try {
            if (!validateForm())
                return;

            Transaction selectedTransaction = tableTransaction.getSelectionModel().getSelectedItem();
            Order selectedOrder = cbOrderID.getSelectionModel().getSelectedItem();
            LocalDate orderDate = DatePicker.getValue();
            double amountPaid = Double.parseDouble(etAmountPaid.getText());
            String paymentMethod = cbPaymentMethod.getSelectionModel().getSelectedItem();
            double totalAmount = getOrderTotalAmount(selectedOrder.getId());
            double excessAmount = amountPaid - totalAmount;

            String formattedOrderDate = orderDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            executeQuery(QueryHelper.UPDATE_TRANSACTION, selectedOrder.getId(), paymentMethod, formattedOrderDate,
                    amountPaid, excessAmount, selectedTransaction.getId());

            showAlert(Alert.AlertType.INFORMATION, DashboardController.getPrimaryStage(), "Success",
                    "Transaction updated successfully");
            refreshUI(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void clearField(ActionEvent event) {
        etID.clear();
        cbOrderID.getSelectionModel().clearSelection();
        DatePicker.getEditor().clear();
        cbPaymentMethod.getSelectionModel().clearSelection();
        etAmountPaid.clear();
        etExcessAmount.clear();
        btnSave.setDisable(false);
        btnUpdate.setDisable(true);
        btnClear.setDisable(true);
    }

    private void showTransaction() {
        ObservableList<Transaction> transactionList = getTransactionList();
        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colOrderID.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colTransactionDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colPaymentMethod.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        colAmountPaid.setCellValueFactory(new PropertyValueFactory<>("amountPaid"));
        colExcessAmount.setCellValueFactory(new PropertyValueFactory<>("excessAmount"));

        colAmountPaid.setCellFactory(tc -> new TextFieldTableCell<>(new javafx.util.converter.DoubleStringConverter()) {
            @Override
            public void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.2f", item));
                }
            }
        });

        colExcessAmount
                .setCellFactory(tc -> new TextFieldTableCell<>(new javafx.util.converter.DoubleStringConverter()) {
                    @Override
                    public void updateItem(Double item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(String.format("%,.2f", item));
                        }
                    }
                });

        colTransactionDate
                .setCellFactory(column -> new TextFieldTableCell<>(new LocalDateStringConverter(DATE_FORMATTER, null)) {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : DATE_FORMATTER.format(item));
                    }
                });
        tableTransaction.setItems(transactionList);
    }

    private ObservableList<Transaction> getTransactionList() {
        ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
        try (Connection connection = Database.connect();
                Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(QueryHelper.SELECT_TRANSACTION)) {

            while (rs.next()) {
                Transaction transaction = new Transaction(rs.getInt("TransactionID"),
                        rs.getInt("OrderID"),
                        rs.getDate("OrderDate").toLocalDate(),
                        rs.getDouble("AmountPaid"),
                        rs.getString("PaymentMethod"),
                        rs.getDouble("ExcessAmount"));
                transactionList.add(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactionList;
    }

    private void populateOrderID() {
        ObservableList<Order> orderList = FXCollections.observableArrayList();
        try (Connection connection = Database.connect();
                PreparedStatement preparedStatement = connection.prepareStatement(QueryHelper.SELECT_ORDER);
                ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                Order order = new Order(resultSet.getInt("OrderID"),
                        resultSet.getInt("CustomerID"),
                        resultSet.getDate("OrderDate").toLocalDate(),
                        resultSet.getDouble("TotalAmount"),
                        resultSet.getString("CustomerName"),
                        resultSet.getString("Status"));
                orderList.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        cbOrderID.setItems(orderList);
    }

    private double getOrderTotalAmount(int orderId) {
        double totalAmount = 0;
        try (Connection connection = Database.connect();
                PreparedStatement preparedStatement = connection.prepareStatement(QueryHelper.SELECT_TOTAL_AMOUNT)) {

            preparedStatement.setInt(1, orderId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    totalAmount = resultSet.getDouble("TotalAmount");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalAmount;
    }

    private void calculateExcessAmount() {
        try {
            Order selectedOrder = cbOrderID.getSelectionModel().getSelectedItem();
            if (selectedOrder == null) {
                etExcessAmount.setText("");
                return;
            }

            double totalAmount = getOrderTotalAmount(selectedOrder.getId());
            String amountPaidStr = etAmountPaid.getText();

            if (amountPaidStr.isEmpty()) {
                etExcessAmount.setText("");
                return;
            }

            double amountPaid = Double.parseDouble(amountPaidStr);
            double excessAmount = amountPaid - totalAmount;
            etExcessAmount.setText(String.valueOf(excessAmount));
        } catch (NumberFormatException e) {
            etExcessAmount.setText("");
        }
    }

    private void fetchAndDisplayExcessAmount() {
        try (Connection connection = Database.connect();
                PreparedStatement preparedStatement = connection.prepareStatement(QueryHelper.SELECT_TOTAL_AMOUNT)) {

            Order selectedOrder = cbOrderID.getSelectionModel().getSelectedItem();
            if (selectedOrder != null) {
                preparedStatement.setInt(1, selectedOrder.getId());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        double totalAmount = resultSet.getDouble("TotalAmount");
                        etExcessAmount.setText(String.valueOf(totalAmount));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void executeQuery(String sql, Object... params) {
        try (Connection connection = Database.connect();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof String) {
                    preparedStatement.setString(i + 1, (String) params[i]);
                } else if (params[i] instanceof Integer) {
                    preparedStatement.setInt(i + 1, (Integer) params[i]);
                } else if (params[i] instanceof Double) {
                    preparedStatement.setDouble(i + 1, (Double) params[i]);
                } else if (params[i] instanceof LocalDate) {
                    preparedStatement.setDate(i + 1, Date.valueOf((LocalDate) params[i]));
                }
            }
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void addListenerForTable() {
        tableTransaction.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateFields(newSelection);
            } else {
                clearField(null);
            }
        });
    }

    private void populateFields(Transaction transaction) {
        btnSave.setDisable(true);
        btnUpdate.setDisable(false);
        btnClear.setDisable(false);
        etID.setText(String.valueOf(transaction.getId()));
        cbOrderID.getItems().stream()
                .filter(order -> order.getId() == transaction.getOrderId())
                .findFirst()
                .ifPresent(order -> cbOrderID.getSelectionModel().select(order));
        DatePicker.setValue(transaction.getOrderDate());
        cbPaymentMethod.setValue(transaction.getPaymentMethod());
        etAmountPaid.setText(String.valueOf(transaction.getAmountPaid()));
        etExcessAmount.setText(String.valueOf(transaction.getExcessAmount()));
    }

    private boolean validateForm() {
        Order selectedOrder = cbOrderID.getSelectionModel().getSelectedItem();
        LocalDate orderDate = DatePicker.getValue();
        String amountPaidStr = etAmountPaid.getText();
        String paymentMethod = cbPaymentMethod.getSelectionModel().getSelectedItem();

        if (selectedOrder == null || orderDate == null || amountPaidStr.isEmpty() || paymentMethod == null
                || paymentMethod.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, btnSave.getScene().getWindow(), "Form Error!",
                    "Please fill in all fields");
            return false;
        }
        return true;
    }

    private void refreshUI(ActionEvent event) {
        showTransaction();
        clearField(event);
        DashboardController.instance.showOrder();
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
