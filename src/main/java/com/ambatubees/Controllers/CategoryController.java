package com.ambatubees.Controllers;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

import com.ambatubees.Database.Database;
import com.ambatubees.Database.QueryHelper;
import com.ambatubees.Entity.Category;

import javafx.fxml.Initializable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Window;

public class CategoryController implements Initializable {

    public static CategoryController instance;

    @FXML
    private Button btnDelete, btnSave, btnUpdate;

    @FXML
    private TableColumn<Category, Integer> colID;

    @FXML
    private TableColumn<Category, String> colName;

    @FXML
    private TableView<Category> tableCategory;

    @FXML
    private TextField tfCategoryName;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        new Database();
        addListenerForTable();
        showCategory();
    }

    @FXML
    private void saveCategory(ActionEvent event) {
        String categoryName = tfCategoryName.getText();
        if (categoryName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, DashboardController.getPrimaryStage(), "Error",
                    "Please fill the category name");
            return;
        }
        executeUpdate(QueryHelper.INSERT_CATEGORY, categoryName);
        refreshCategoryData();
    }

    @FXML
    private void updateCategory(ActionEvent event) {
        Category category = tableCategory.getSelectionModel().getSelectedItem();
        if (category == null)
            return;
        executeUpdate(QueryHelper.UPDATE_CATEGORY, tfCategoryName.getText(), String.valueOf(category.getId()));
        refreshCategoryData();
    }

    @FXML
    private void deleteCategory(ActionEvent event) {
        Category category = tableCategory.getSelectionModel().getSelectedItem();
        if (category == null)
            return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this category?");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(DashboardController.getPrimaryStage());

        if (alert.showAndWait().get() == ButtonType.OK) {
            executeUpdate(QueryHelper.DELETE_CATEGORY, String.valueOf(category.getId()));
            refreshCategoryData();
        }
    }

    private void executeUpdate(String sql, String... params) {
        try (Connection conn = Database.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            int res = ps.executeUpdate();
            handleSaveResult(res);
        } catch (SQLException e) {
            handleSQLException(e, "Error executing query");
        }
    }

    private void showCategory() {
        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("CategoryName"));
        tableCategory.setItems(getCategoryList());
    }

    private ObservableList<Category> getCategoryList() {
        ObservableList<Category> categoryList = FXCollections.observableArrayList();
        try (Connection conn = Database.connect();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(QueryHelper.SELECT_CATEGORY)) {
            while (rs.next()) {
                categoryList.add(new Category(rs.getInt("CategoryID"), rs.getString("CategoryName")));
            }
        } catch (SQLException e) {
            handleSQLException(e, "Error fetching category list");
        }
        return categoryList;
    }

    private void addListenerForTable() {
        tableCategory.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                btnSave.setDisable(true);
                btnUpdate.setDisable(false);
                btnDelete.setDisable(false);
                tfCategoryName.setText(newSelection.getCategoryName());
            } else {
                resetForm();
            }
        });
    }

    private void resetForm() {
        tfCategoryName.clear();
        btnSave.setDisable(false);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
    }

    private void handleSaveResult(int res) {
        if (res > 0) {
            showAlert(Alert.AlertType.INFORMATION, DashboardController.getPrimaryStage(), "Success",
                    "Category added successfully");
            tfCategoryName.clear();
            ProductController.instance.populateCategories();
            showCategory();
        } else {
            showAlert(Alert.AlertType.ERROR, DashboardController.getPrimaryStage(), "Error", "Failed to add category");
        }
    }

    private void handleSQLException(SQLException e, String message) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, DashboardController.getPrimaryStage(), "Error", message);
    }

    private void refreshCategoryData() {
        showCategory();
        tfCategoryName.clear();
        ProductController.instance.populateCategories();
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
