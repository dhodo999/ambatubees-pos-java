package com.ambatubees.Entity;

import java.sql.Blob;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Product {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty productDescription;
    private final SimpleDoubleProperty price;
    private final SimpleStringProperty category;
    private final SimpleStringProperty weight;
    private Blob image;
    private final SimpleStringProperty status;

    public Product(int id, String productDescription, Double price, String category, String weight, Blob image, String status) {
        this.id = new SimpleIntegerProperty(id);
        this.productDescription = new SimpleStringProperty(productDescription);
        this.price = new SimpleDoubleProperty(price);
        this.category = new SimpleStringProperty(category);
        this.weight = new SimpleStringProperty(weight);
        this.image = image;
        this.status = new SimpleStringProperty(status);
    }

    public Product(int id, String productDescription) {
        this(id, productDescription, 0.0, "", "", null, "");
    }

    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public String getProductDescription() {
        return productDescription.get();
    }

    public SimpleStringProperty productDescriptionProperty() {
        return productDescription;
    }

    public Double getPrice() {
        return price.get();
    }

    public SimpleDoubleProperty priceProperty() {
        return price;
    }

    public String getCategory() {
        return category.get();
    }

    public SimpleStringProperty categoryProperty() {
        return category;
    }

    public String getWeight() {
        return weight.get();
    }

    public SimpleStringProperty weightProperty() {
        return weight;
    }

    public Blob getImage() {
        return image;
    }

    public String getStatus() {
        return status.get();
    }

    public SimpleStringProperty statusProperty() {
        return status;
    }

    @Override
    public String toString() {
        return productDescription.get();
    }
}