package com.ambatubees.Controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

import com.ambatubees.Database.Database;
import com.ambatubees.Database.QueryHelper;
import com.ambatubees.Entity.Product;

import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ProductController implements Initializable {

    public static ProductController instance;

    private Scene fxmlFile;
    private Parent root;
    private Stage window;
    private File file;

    @FXML
    private Button btnBrowse, btnAdd, btnDelete, btnSave, btnUpdate, btnClear;

    @FXML
    private ComboBox<String> cbCategory, cbStatus, cbWeight;

    @FXML
    private TableColumn<Product, Integer> colID;

    @FXML
    private TableColumn<Product, String> colDescription;

    @FXML
    private TableColumn<Product, Double> colPrice;

    @FXML
    private TableColumn<Product, String> colCategory, colStatus;

    @FXML
    private TextField etID, etDescription, etPrice;

    @FXML
    private ImageView ivProduct;

    @FXML
    private TableView<Product> tableProduct;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        new Database();
        showProduct();
        populateCategories();
        addListenerForTable();

        ivProduct.setPreserveRatio(false);
        ivProduct.setSmooth(true);

        cbStatus.getItems().addAll("AVAILABLE", "UNAVAILABLE");
        cbWeight.getItems().addAll("YES", "NO");
    }

    @FXML
    public void handleBrowseImage(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters()
                    .addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            file = fileChooser.showOpenDialog(DashboardController.getPrimaryStage());
            if (file != null) {
                BufferedImage bufferedImage = ImageIO.read(file);
                Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                ivProduct.setImage(image);
                ivProduct.setFitWidth(ivProduct.getFitWidth());
                ivProduct.setFitHeight(ivProduct.getFitHeight());
                ivProduct.setPreserveRatio(false);
                ivProduct.setSmooth(true);
                ivProduct.setCache(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void actionAddCategory(ActionEvent event) {
        try {
            openModalWindow("/com/ambatubees/fxml/Category.fxml", "Manage Category");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void saveProduct(ActionEvent event) {
        try (Connection conn = Database.connect()) {
            String description = etDescription.getText();
            String price = etPrice.getText();
            String category = cbCategory.getSelectionModel().getSelectedItem();
            String isWeight = cbWeight.getSelectionModel().getSelectedItem();
            String status = cbStatus.getSelectionModel().getSelectedItem();

            if (description.isEmpty() || price.isEmpty() || category.isEmpty() || isWeight.isEmpty()
                    || status.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, DashboardController.getPrimaryStage(), "Error",
                        "Please fill all fields");
                return;
            }

            if (file == null) {
                showAlert(Alert.AlertType.ERROR, DashboardController.getPrimaryStage(), "Error",
                        "Please select an image");
                return;
            }

            try (FileInputStream fis = new FileInputStream(file)) {
                int len = (int) file.length();
                PreparedStatement ps = conn.prepareStatement(QueryHelper.INSERT_PRODUCT);
                ps.setString(1, description);
                ps.setString(2, price);
                ps.setString(3, category);
                ps.setString(4, isWeight);
                ps.setBinaryStream(5, fis, len);
                ps.setString(6, status);

                int res = ps.executeUpdate();
                if (res > 0) {
                    showAlert(Alert.AlertType.INFORMATION, DashboardController.getPrimaryStage(), "Success",
                            "Product added successfully");
                    clearFields();
                    showProduct();
                } else {
                    showAlert(Alert.AlertType.ERROR, DashboardController.getPrimaryStage(), "Error",
                            "Failed to add product");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @FXML
    void updateProduct(ActionEvent event) {
        try (Connection conn = Database.connect()) {
            Product product = tableProduct.getSelectionModel().getSelectedItem();
            String description = etDescription.getText();
            String price = etPrice.getText();
            String category = cbCategory.getSelectionModel().getSelectedItem();
            String isWeight = cbWeight.getSelectionModel().getSelectedItem();
            String status = cbStatus.getSelectionModel().getSelectedItem();

            if (description.isEmpty() || price.isEmpty() || category.isEmpty() || isWeight.isEmpty()
                    || status.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, DashboardController.getPrimaryStage(), "Error",
                        "Please fill all fields");
                return;
            }

            String sql = file != null ? QueryHelper.UPDATE_PRODUCT_WITH_IMAGE : QueryHelper.UPDATE_PRODUCT;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, description);
                ps.setString(2, price);
                ps.setString(3, category);
                ps.setString(4, isWeight);
                if (file != null) {
                    FileInputStream fis = new FileInputStream(file);
                    ps.setBinaryStream(5, fis, (int) file.length());
                    ps.setString(6, status);
                    ps.setInt(7, product.getId());
                } else {
                    ps.setString(5, status);
                    ps.setInt(6, product.getId());
                }

                int res = ps.executeUpdate();
                if (res > 0) {
                    showAlert(Alert.AlertType.INFORMATION, DashboardController.getPrimaryStage(), "Success",
                            "Product updated successfully");
                    clearFields();
                    showProduct();
                } else {
                    showAlert(Alert.AlertType.ERROR, DashboardController.getPrimaryStage(), "Error",
                            "Failed to update product");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void deleteProduct(ActionEvent event) {
        try {
            Product product = tableProduct.getSelectionModel().getSelectedItem();
            if (isProductReferenced(product.getId())) {
                showAlert(Alert.AlertType.ERROR, tableProduct.getScene().getWindow(), "Error",
                        "Product is referenced in another table. Cannot delete.");
            } else {
                executeQuery(QueryHelper.DELETE_PRODUCT, String.valueOf(product.getId()));
                showProduct();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void clearField(ActionEvent event) {
        clearFields();
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

    public void showProduct() {
        ObservableList<Product> list = getProductList();
        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("productDescription"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colPrice.setCellFactory(tc -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.format("%,.2f", price));
                }
            }
        });

        tableProduct.setItems(list);
    }

    private ObservableList<Product> getProductList() {
        ObservableList<Product> productList = FXCollections.observableArrayList();
        try (Connection connection = Database.connect();
                Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(QueryHelper.SELECT_PRODUCT)) {
            while (rs.next()) {
                Product product = new Product(rs.getInt("ProductID"),
                        rs.getString("ProductDescription"),
                        rs.getDouble("ProductPrice"),
                        rs.getString("ProductCategory"),
                        rs.getString("Weight"),
                        rs.getBlob("ProductImage"),
                        rs.getString("ProductStatus"));
                productList.add(product);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return productList;
    }

    private void openModalWindow(String resource, String title) throws IOException {
        root = FXMLLoader.load(getClass().getResource(resource));
        fxmlFile = new Scene(root);
        window = new Stage();
        window.setScene(fxmlFile);
        window.setResizable(false);
        window.initModality(Modality.APPLICATION_MODAL);
        window.setAlwaysOnTop(true);
        window.setIconified(false);
        window.setTitle(title);
        window.showAndWait();
    }

    public void populateCategories() {
        ObservableList<String> list = FXCollections.observableArrayList();
        try (Connection connection = Database.connect();
                ResultSet rs = connection.createStatement().executeQuery(QueryHelper.SELECT_CATEGORY)) {
            while (rs.next()) {
                list.add(rs.getString("CategoryName"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        cbCategory.setItems(list);
    }

    private boolean isProductReferenced(int productId) {
        boolean isReferenced = false;
        try (Connection connection = Database.connect();
                PreparedStatement preparedStatement = connection.prepareStatement(QueryHelper.COUNT_PRODUCT)) {
            preparedStatement.setInt(1, productId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    isReferenced = resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isReferenced;
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

    private void addListenerForTable() {
        tableProduct.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                btnSave.setDisable(true);
                btnUpdate.setDisable(false);
                btnDelete.setDisable(false);
                btnClear.setDisable(false);
                etID.setText(String.valueOf(newSelection.getId()));
                etDescription.setText(newSelection.getProductDescription());
                etPrice.setText(String.valueOf(newSelection.getPrice()));
                cbCategory.getSelectionModel().select(newSelection.getCategory());
                cbWeight.getSelectionModel().select(newSelection.getWeight());
                cbStatus.getSelectionModel().select(newSelection.getStatus());

                try {
                    Blob imageBlob = newSelection.getImage();
                    if (imageBlob != null) {
                        InputStream inputStream = imageBlob.getBinaryStream();
                        Image image = new Image(inputStream);
                        ivProduct.setImage(image);
                    } else {
                        ivProduct.setImage(null);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    ivProduct.setImage(null);
                }
            } else {
                clearFields();
            }
        });
    }

    private void clearFields() {
        etID.clear();
        etDescription.clear();
        etPrice.clear();
        cbCategory.valueProperty().set(null);
        cbWeight.valueProperty().set(null);
        cbStatus.valueProperty().set(null);
        ivProduct.setImage(null);
        btnSave.setDisable(false);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        btnClear.setDisable(true);
    }
}
