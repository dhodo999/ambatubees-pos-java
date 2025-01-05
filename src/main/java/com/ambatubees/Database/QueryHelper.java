package com.ambatubees.Database;

public class QueryHelper {
    public static final String SELECT_USER = "SELECT * FROM users WHERE Username = ? AND Password = ?";
    public static final String SELECT_CUSTOMER = "SELECT * FROM customers";
    public static final String SELECT_CUSTOMER_NAME = "SELECT CustomerID, CustomerName FROM customers";
    public static final String INSERT_CUSTOMER = "INSERT INTO customers (CustomerName, PhoneNumber, Address) VALUES (?, ?, ?)";
    public static final String UPDATE_CUSTOMER = "UPDATE customers SET CustomerName = ?, PhoneNumber = ?, Address = ? WHERE CustomerID = ?";
    public static final String DELETE_CUSTOMER = "DELETE FROM customers WHERE CustomerID = ?";
    public static final String COUNT_CUSTOMER = "SELECT COUNT(*) FROM orders WHERE CustomerID = ?";
    public static final String SELECT_PRODUCT = "SELECT * FROM products";
    public static final String SELECT_PRODUCT_NAME = "SELECT ProductID, ProductDescription FROM products";
    public static final String SELECT_PRODUCT_PRICE = "SELECT ProductPrice FROM products WHERE ProductID = ?";
    public static final String INSERT_PRODUCT = "INSERT INTO products (ProductDescription, ProductPrice, ProductCategory, Weight, ProductImage, ProductStatus) VALUES (?, ?, ?, ?, ?, ?)";
    public static final String UPDATE_PRODUCT = "UPDATE products SET ProductDescription = ?, ProductPrice = ?, ProductCategory = ?, Weight = ?, ProductImage = ?, ProductStatus = ? WHERE ProductID = ?";
    public static final String DELETE_PRODUCT = "DELETE FROM products WHERE ProductID = ?";
    public static final String COUNT_PRODUCT = "SELECT COUNT(*) FROM order_items WHERE ProductID = ?";
    public static final String SELECT_CATEGORY = "SELECT * FROM categories";
    public static final String INSERT_CATEGORY = "INSERT INTO categories (CategoryName) VALUES (?)";
    public static final String UPDATE_CATEGORY = "UPDATE categories SET CategoryName = ? WHERE CategoryID = ?";
    public static final String DELETE_CATEGORY = "DELETE FROM categories WHERE CategoryID = ?";
    public static final String SELECT_ORDER = "SELECT o.OrderID, o.CustomerID, c.CustomerName, o.OrderDate, o.TotalAmount, o.Status " + "FROM orders o " + "JOIN customers c ON o.CustomerID = c.CustomerID";
    public static final String SELECT_ORDER_ID = "SELECT OrderID FROM orders";
    public static final String SELECT_TOTAL_AMOUNT = "SELECT TotalAmount FROM orders WHERE OrderID = ?";
    public static final String INSERT_ORDER = "INSERT INTO orders (CustomerID, OrderDate, TotalAmount, Status) VALUES (?, ?, ?, ?)";
    public static final String UPDATE_ORDER = "UPDATE orders SET CustomerID = ?, OrderDate = ?, TotalAmount = ?, Status = ? WHERE OrderID = ?";
    public static final String UPDATE_ORDER_STATUS = "UPDATE orders SET Status = ? WHERE OrderID = ?";
    public static final String COUNT_ORDER = "SELECT COUNT(*) FROM order_items WHERE OrderID = ?";
    public static final String SELECT_ORDER_ITEM = "SELECT oi.OrderItemID, oi.OrderID, oi.ProductID, p.ProductDescription, oi.Price, oi.Quantity, oi.SubTotal " + "FROM order_items oi " + "JOIN products p ON oi.ProductID = p.ProductID";
    public static final String INSERT_ORDER_ITEM = "INSERT INTO order_items (OrderID, ProductID, Price, Quantity, SubTotal) VALUES (?, ?, ?, ?, ?)";
    public static final String UPDATE_ORDER_ITEM = "UPDATE order_items SET OrderID = ?, ProductID = ?, Price = ?, Quantity = ?, SubTotal = ? WHERE OrderItemID = ?";
    public static final String DELETE_ORDER_ITEM = "DELETE FROM order_items WHERE OrderItemID = ?";
    public static final String COUNT_TRANSACTION = "SELECT COUNT(*) FROM transactions WHERE OrderID = ?";
    public static final String SELECT_TRANSACTION = "SELECT t.TransactionID, t.OrderID, o.OrderDate, t.PaymentMethod, t.AmountPaid, t.ExcessAmount " + "FROM transactions t " + "JOIN orders o ON t.OrderID = o.OrderID";
    public static final String SELECT_TRANSACTION_ORDER_ID = "SELECT OrderID FROM transactions";
    public static final String INSERT_TRANSACTION = "INSERT INTO transactions (OrderID, TransactionDate, PaymentMethod, AmountPaid, ExcessAmount) VALUES (?, ?, ?, ?, ?)";
    public static final String UPDATE_TRANSACTION = "UPDATE transactions SET OrderID = ?, PaymentMethod = ?, TransactionDate = ?, AmountPaid = ?, ExcessAmount = ? WHERE TransactionID = ?";
}
