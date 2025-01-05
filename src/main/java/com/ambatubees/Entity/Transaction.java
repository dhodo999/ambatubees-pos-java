package com.ambatubees.Entity;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import java.time.LocalDate;

public class Transaction {
    private final SimpleIntegerProperty id;
    private final SimpleIntegerProperty orderId;
    private final SimpleObjectProperty<LocalDate> orderDate;
    private final SimpleDoubleProperty amountPaid;
    private final SimpleStringProperty paymentMethod;
    private final SimpleDoubleProperty excessAmount;

    public Transaction(int id, int orderId, LocalDate orderDate, double amountPaid, String paymentMethod, double excessAmount) {
        this.id = new SimpleIntegerProperty(id);
        this.orderId = new SimpleIntegerProperty(orderId);
        this.orderDate = new SimpleObjectProperty<>(orderDate);
        this.amountPaid = new SimpleDoubleProperty(amountPaid);
        this.paymentMethod = new SimpleStringProperty(paymentMethod);
        this.excessAmount = new SimpleDoubleProperty(excessAmount);
    }

    public Transaction(int id) {
        this(id, 0, null, 0.0, "", 0.0);
    }

    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public int getOrderId() {
        return orderId.get();
    }

    public SimpleIntegerProperty orderIdProperty() {
        return orderId;
    }

    public LocalDate getOrderDate() {
        return orderDate.get();
    }

    public SimpleObjectProperty<LocalDate> orderDateProperty() {
        return orderDate;
    }

    public double getAmountPaid() {
        return amountPaid.get();
    }

    public SimpleDoubleProperty amountPaidProperty() {
        return amountPaid;
    }

    public String getPaymentMethod() {
        return paymentMethod.get();
    }

    public SimpleStringProperty paymentMethodProperty() {
        return paymentMethod;
    }

    public double getExcessAmount() {
        return excessAmount.get();
    }

    public SimpleDoubleProperty excessAmountProperty() {
        return excessAmount;
    }

    public void setId(int id) {
        this.id.set(id);
    }
}
