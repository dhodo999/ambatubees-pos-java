package com.ambatubees.Entity;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Category {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty categoryName;

    public Category(int id, String categoryName) {
        this.id = new SimpleIntegerProperty(id);
        this.categoryName = new SimpleStringProperty(categoryName);
    }

    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public String getCategoryName() {
        return categoryName.get();
    }

    public SimpleStringProperty categoryNameProperty() {
        return categoryName;
    }
}
