package org.example.crudrestaurante.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.crudrestaurante.database.DatabaseConnection;
import org.example.crudrestaurante.models.Producto;

import java.io.IOException;
import java.sql.*;

public class ProductoController {

    @FXML
    private TextField tfNombre, tfCategoria, tfPrecio, tfDisponibilidad;
    @FXML
    private TableView<Producto> tableView;
    @FXML
    private TableColumn<Producto, Integer> tableId;
    @FXML
    private TableColumn<Producto, String> tableNombre, tableCategoria, tablePrecio, tableDisponibilidad;
    @FXML
    private Button btVolver;

    private final ObservableList<Producto> listaProductos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configurar las columnas de la tabla
        tableId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        tableNombre.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNombre()));
        tableCategoria.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCategoria()));
        tablePrecio.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPrecio()).asString());
        tableDisponibilidad.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDisponibilidad()).asString());

        // Cargar los datos desde la base de datos
        cargarProductos();
        tableView.setItems(listaProductos);
    }

    @FXML
    private void cargarProductos() {
        listaProductos.clear();
        String query = "SELECT * FROM Productos";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Producto producto = new Producto();
                producto.setId(rs.getInt("id_producto"));
                producto.setNombre(rs.getString("nombre"));
                producto.setCategoria(rs.getString("categoria"));
                producto.setPrecio(rs.getFloat("precio"));
                producto.setDisponibilidad(rs.getInt("disponibilidad"));
                listaProductos.add(producto);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo cargar la lista de productos.");
            e.printStackTrace();
        }
    }

    @FXML
    private void crearProducto() {
        if (!tfNombre.getText().isEmpty() && !tfCategoria.getText().isEmpty() && !tfPrecio.getText().isEmpty() && !tfDisponibilidad.getText().isEmpty()) {
            String nombre = tfNombre.getText();
            String categoria = tfCategoria.getText();
            float precio = Float.parseFloat(tfPrecio.getText()); // Conversión correcta
            int disponibilidad = Integer.parseInt(tfDisponibilidad.getText()); // Conversión correcta

            String query = "INSERT INTO Productos (nombre, categoria, precio, disponibilidad) VALUES (?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, nombre);
                stmt.setString(2, categoria);
                stmt.setFloat(3, precio);
                stmt.setInt(4, disponibilidad);
                stmt.executeUpdate();

            } catch (SQLException e) {
                mostrarAlerta("Error", "No se pudo crear el producto.");
                e.printStackTrace();
                return;
            }

            cargarProductos();
            limpiarCampos();

            mostrarAlertaExito("Éxito", "Producto creado correctamente.");

        } else {
            mostrarAlerta("Error", "Todos los campos deben estar llenos.");
        }
    }

    @FXML
    private void modificarProducto() {
        Producto productoSeleccionado = tableView.getSelectionModel().getSelectedItem();
        if (productoSeleccionado != null) {
            String nuevoNombre = tfNombre.getText().isEmpty() ? productoSeleccionado.getNombre() : tfNombre.getText();
            String nuevaCategoria = tfCategoria.getText().isEmpty() ? productoSeleccionado.getCategoria() : tfCategoria.getText();
            float nuevoPrecio = tfPrecio.getText().isEmpty() ? productoSeleccionado.getPrecio() : Float.parseFloat(tfPrecio.getText());
            int nuevaDisponibilidad = tfDisponibilidad.getText().isEmpty() ? productoSeleccionado.getDisponibilidad() : Integer.parseInt(tfDisponibilidad.getText());

            String query = "UPDATE Productos SET nombre = ?, categoria = ?, precio = ? , disponibilidad =? WHERE id = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, nuevoNombre);
                stmt.setString(2, nuevaCategoria);
                stmt.setFloat(3, nuevoPrecio);
                stmt.setInt(4, nuevaDisponibilidad);
                stmt.setInt(5, productoSeleccionado.getId());
                stmt.executeUpdate();

            } catch (SQLException e) {
                mostrarAlerta("Error", "No se pudo modificar el producto.");
                e.printStackTrace();
                return;
            }

            cargarProductos();
            limpiarCampos();

            mostrarAlertaExito("Éxito", "Producto modificado correctamente.");

        } else {
            mostrarAlerta("Error", "Seleccione un producto para modificar.");
        }
    }

    @FXML
    private void buscarProducto() {
        String nombreBuscar = tfNombre.getText();
        if (!nombreBuscar.isEmpty()) {
            String query = "SELECT * FROM Productos WHERE nombre = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, nombreBuscar);
                try (ResultSet rs = stmt.executeQuery()) {
                    listaProductos.clear();
                    if (rs.next()) {
                        Producto producto = new Producto();
                        producto.setId(rs.getInt("id_producto"));
                        producto.setNombre(rs.getString("nombre"));
                        producto.setCategoria(rs.getString("categoria"));
                        producto.setPrecio(rs.getFloat("precio"));
                        producto.setDisponibilidad(rs.getInt("disponibilidad"));

                        listaProductos.add(producto);
                    } else {
                        mostrarAlerta("Información", "No se encontró el producto.");
                    }
                }
            } catch (SQLException e) {
                mostrarAlerta("Error", "Error al buscar el producto.");
                e.printStackTrace();
            }
        } else {
            mostrarAlerta("Error", "Ingrese un nombre para buscar.");
        }
    }

    @FXML
    private void borrarProducto() {
        Producto productoSeleccionado = tableView.getSelectionModel().getSelectedItem();
        if (productoSeleccionado != null) {
            String query = "DELETE FROM Productos WHERE id = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, productoSeleccionado.getId());
                stmt.executeUpdate();

            } catch (SQLException e) {
                mostrarAlerta("Error", "No se pudo eliminar el producto.");
                e.printStackTrace();
                return;
            }

            cargarProductos();
            limpiarCampos();

            mostrarAlertaExito("Éxito", "Producto eliminado correctamente.");

        } else {
            mostrarAlerta("Error", "Seleccione un producto para eliminar.");
        }
    }

    @FXML
    private void volverAlMenu() {
        try {
            Stage stageActual = (Stage) btVolver.getScene().getWindow();
            stageActual.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/crudrestaurante/menu-view.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Menú Principal");
            stage.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void limpiarCampos() {
        tfNombre.clear();
        tfCategoria.clear();
        tfPrecio.clear();
        tfDisponibilidad.clear();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarAlertaExito(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

}
