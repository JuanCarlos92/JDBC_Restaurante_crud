module org.example.jdbcrestaurantecrud {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jasperreports;


    opens org.example.jdbcrestaurantecrud to javafx.fxml;
    exports org.example.jdbcrestaurantecrud;
    exports org.example.jdbcrestaurantecrud.controllers;
    opens org.example.jdbcrestaurantecrud.controllers to javafx.fxml;
    exports org.example.jdbcrestaurantecrud.models;
}