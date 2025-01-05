module com.ambatubees {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires transitive javafx.graphics;
    requires javafx.base;
    requires java.desktop;
    requires javafx.swing;

    opens com.ambatubees to javafx.fxml;
    opens com.ambatubees.Controllers to javafx.fxml;
    opens com.ambatubees.Entity to javafx.base;
    exports com.ambatubees;
}