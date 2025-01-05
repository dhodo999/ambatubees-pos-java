package com.ambatubees.Entity;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import java.time.LocalDate;

public class Order {
    private final SimpleIntegerProperty id;
    private final SimpleIntegerProperty customerId;
    private final SimpleObjectProperty<LocalDate> orderDate;
    private final SimpleDoubleProperty totalAmount;
    private final SimpleStringProperty customerName;
    private final SimpleStringProperty status;

    public Order(int id, int customerId, LocalDate orderDate, double totalAmount, String customerName, String status) {
        this.id = new SimpleIntegerProperty(id);
        this.customerId = new SimpleIntegerProperty(customerId);
        this.orderDate = new SimpleObjectProperty<>(orderDate);
        this.totalAmount = new SimpleDoubleProperty(totalAmount);
        this.customerName = new SimpleStringProperty(customerName);
        this.status = new SimpleStringProperty(status);
    }

    public Order(int id) {
        this(id, 0, null, 0.0, "", "");
    }

    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public int getCustomerId() {
        return customerId.get();
    }

    public SimpleIntegerProperty customerIdProperty() {
        return customerId;
    }

    public LocalDate getOrderDate() {
        return orderDate.get();
    }

    public SimpleObjectProperty<LocalDate> orderDateProperty() {
        return orderDate;
    }

    public double getTotalAmount() {
        return totalAmount.get();
    }

    public SimpleDoubleProperty totalAmountProperty() {
        return totalAmount;
    }

    public String getCustomerName() {
        return customerName.get();
    }

    public SimpleStringProperty customerNameProperty() {
        return customerName;
    }

    public String getStatus() {
        return status.get();
    }

    public SimpleStringProperty statusProperty() {
        return status;
    }

    @Override
    public String toString() {
        return Integer.toString(id.get());
    }
}