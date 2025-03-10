package org.example.jdbcrestaurantecrud.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.jdbcrestaurantecrud.database.DatabaseConnection;
import org.example.jdbcrestaurantecrud.models.DetallePedido;
import org.example.jdbcrestaurantecrud.models.Pedido;
import org.example.jdbcrestaurantecrud.models.Producto;

import java.sql.*;

public class DetallePedidoController {

    @FXML
    private ComboBox<String> comboBoxProducto;
    @FXML
    private TextField tfCantidad;
    @FXML
    private TableView<DetallePedido> tableView;
    @FXML
    private TableColumn<DetallePedido, Integer> tableId, tableIdPedido;
    @FXML
    private TableColumn<DetallePedido, String> tableProducto, tableCantidad, tablePrecio, tableSubtotal;
    @FXML
    private Button btVolver;

    private int pedidoId;
    private ObservableList<DetallePedido> listaDetallesPedidos = FXCollections.observableArrayList();

    public void setPedidoId(int pedidoId) throws SQLException {
        this.pedidoId = pedidoId;
        cargarProductos();
        cargarDetallesPedido();
    }

    @FXML
    public void initialize() {
        tableId.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId_detalle()).asObject());
        tableIdPedido.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getPedido().getId_pedido()).asObject());
        tableProducto.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProducto().getNombre()));
        tableCantidad.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCantidad()).asString());
        tablePrecio.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPrecio()).asString());
        tableSubtotal.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getSubtotal()).asString());

        cargarProductos();
        tableView.setItems(listaDetallesPedidos);
    }

    private void cargarProductos() {
        ObservableList<String> listaProductos = FXCollections.observableArrayList();
        String query = "SELECT nombre FROM Productos";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                listaProductos.add(rs.getString("nombre"));
            }
            comboBoxProducto.setItems(listaProductos);
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los productos.");
            e.printStackTrace();
        }
    }

    private void cargarDetallesPedido(){
        listaDetallesPedidos.clear();

        String query = "SELECT dp.id_detalle, dp.id_pedido, p.id_producto, p.nombre, p.precio, " +
                "dp.cantidad, dp.subtotal " +
                "FROM detalle_pedido dp " +
                "JOIN productos p ON dp.id_producto = p.id_producto " +
                "WHERE dp.id_pedido = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, pedidoId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int idDetalle = rs.getInt("id_detalle");

                int idPedido = rs.getInt("id_pedido");
                Pedido pedido = new Pedido(idPedido);

                int idProducto = rs.getInt("id_producto");
                String nombreProducto = rs.getString("nombre");
                float precioProducto = rs.getFloat("precio");
                Producto producto = new Producto(idProducto, nombreProducto, precioProducto);


                int cantidad = rs.getInt("cantidad");
                float subtotal = rs.getFloat("subtotal");

                DetallePedido detalle = new DetallePedido(idDetalle, pedido, producto, cantidad, precioProducto, subtotal);
                listaDetallesPedidos.add(detalle);
            }

            tableView.setItems(listaDetallesPedidos);

        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo cargar los detalles del pedido.");
            e.printStackTrace();
        }
    }

    @FXML
    private void agregarProducto() {
        if (comboBoxProducto.getValue() == null || tfCantidad.getText().isEmpty()) {
            mostrarAlerta("Campos Vacíos", "Por favor, completa todos los campos.");
            return;
        }

        String productoSeleccionado = comboBoxProducto.getValue();
        int cantidad;
        float precio, subtotal;
        int idProducto;

        try {
            cantidad = Integer.parseInt(tfCantidad.getText());
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "La cantidad no es válida.");
            return;
        }

        String queryProducto = "SELECT id_producto, precio FROM Productos WHERE nombre = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement precioStmt = conn.prepareStatement(queryProducto)) {

            precioStmt.setString(1, productoSeleccionado);
            ResultSet rs = precioStmt.executeQuery();
            if (rs.next()) {
                idProducto = rs.getInt("id_producto");
                precio = rs.getFloat("precio");
            } else {
                mostrarAlerta("Error", "No se encontró el producto seleccionado.");
                return;
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo obtener el precio del producto.");
            e.printStackTrace();
            return;
        }

        subtotal = cantidad * precio;

        String query = "INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, subtotal) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, pedidoId);
            stmt.setInt(2, idProducto);
            stmt.setInt(3, cantidad);
            stmt.setFloat(4, subtotal);
            stmt.executeUpdate();
            actualizarTotalPedido(conn);

            cargarDetallesPedido();
            mostrarAlertaExito("Éxito", "Producto agregado correctamente.");

        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo agregar el producto.");
            e.printStackTrace();
        }
    }

    private void actualizarTotalPedido(Connection conn) {
        String queryTotal = "SELECT SUM(subtotal) AS total FROM detalle_pedido WHERE id_pedido = ?";

        try (PreparedStatement stmt = conn.prepareStatement(queryTotal)) {
            stmt.setInt(1, pedidoId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                float total = rs.getFloat("total");

                // Actualizar el total en la tabla Pedidos
                String updatePedido = "UPDATE Pedidos SET total = ? WHERE id_pedido = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updatePedido)) {
                    updateStmt.setFloat(1, total);
                    updateStmt.setInt(2, pedidoId);
                    updateStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo actualizar el total del pedido.");
            e.printStackTrace();
        }
    }

    @FXML
    private void modificarDetallePedido() {
        DetallePedido detalleSeleccionado = tableView.getSelectionModel().getSelectedItem();

        if (detalleSeleccionado != null) {
            String productoSeleccionado = comboBoxProducto.getValue();
            String cantidadTexto = tfCantidad.getText();

            if (productoSeleccionado == null || productoSeleccionado.trim().isEmpty()) {
                mostrarAlerta("Error", "Debes seleccionar un producto.");
                return;
            }

            int cantidad;
            try {
                cantidad = Integer.parseInt(cantidadTexto);
                if (cantidad <= 0) {
                    mostrarAlerta("Error", "La cantidad debe ser mayor a 0.");
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "La cantidad ingresada no es válida.");
                return;
            }

            System.out.println("Producto seleccionado: " + productoSeleccionado);
            System.out.println("Cantidad ingresada: " + cantidad);

            int idProducto = -1;
            float precio = 0, subtotal;

            String queryProducto = "SELECT id_producto, precio FROM Productos WHERE nombre = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement precioStmt = conn.prepareStatement(queryProducto)) {

                precioStmt.setString(1, productoSeleccionado);
                ResultSet rs = precioStmt.executeQuery();

                if (rs.next()) {
                    idProducto = rs.getInt("id_producto");
                    precio = rs.getFloat("precio");
                } else {
                    mostrarAlerta("Error", "No se encontró el producto en la base de datos.");
                    return;
                }
            } catch (SQLException e) {
                mostrarAlerta("Error", "No se pudo obtener la información del producto.");
                e.printStackTrace();
                return;
            }

            System.out.println("ID del producto seleccionado: " + idProducto);
            System.out.println("Precio del producto seleccionado: " + precio);

            subtotal = cantidad * precio;

            String queryUpdate = "UPDATE detalle_pedido SET id_producto = ?, cantidad = ?, subtotal = ? WHERE id_detalle = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(queryUpdate)) {

                stmt.setInt(1, idProducto);
                stmt.setInt(2, cantidad);
                stmt.setFloat(3, subtotal);
                stmt.setInt(4, detalleSeleccionado.getId_detalle());

                int filasAfectadas = stmt.executeUpdate();

                if (filasAfectadas > 0) {
                    actualizarTotalPedido(conn);
                    cargarDetallesPedido();
                    mostrarAlertaExito("Éxito", "Detalle de pedido modificado correctamente.");
                } else {
                    mostrarAlerta("Error", "No se modificó el detalle del pedido.");
                }
            } catch (SQLException e) {
                mostrarAlerta("Error", "Error al modificar el detalle del pedido.");
                e.printStackTrace();
            }
        } else {
            mostrarAlerta("Error", "Debes seleccionar un detalle de pedido para modificar.");
        }
    }
    @FXML
    private void borrarDetallePedido() {
        // Obtener el elemento seleccionado en la tabla
        DetallePedido detalleSeleccionado = tableView.getSelectionModel().getSelectedItem();

        if (detalleSeleccionado == null) {
            mostrarAlerta("Error", "Debe seleccionar un producto para eliminar.");
            return;
        }

        int idDetalle = detalleSeleccionado.getId_detalle();

        String query = "DELETE FROM detalle_pedido WHERE id_detalle = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idDetalle);
            stmt.executeUpdate();

            actualizarTotalPedido(conn);

            listaDetallesPedidos.remove(detalleSeleccionado);

            mostrarAlertaExito("Éxito", "Producto eliminado correctamente.");
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo eliminar el producto.");
            e.printStackTrace();
        }
    }


    @FXML
    private void volverAPedidos() {
            Stage stageActual = (Stage) btVolver.getScene().getWindow();
            stageActual.close();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    private void mostrarAlertaExito(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
