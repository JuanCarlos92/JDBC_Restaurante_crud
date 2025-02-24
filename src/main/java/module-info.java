module org.example.crudrestaurante {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens org.example.crudrestaurante to javafx.fxml;
    exports org.example.crudrestaurante;
    exports org.example.crudrestaurante.controllers;
    opens org.example.crudrestaurante.controllers to javafx.fxml;
}