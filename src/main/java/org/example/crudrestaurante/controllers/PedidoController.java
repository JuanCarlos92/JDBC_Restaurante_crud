package org.example.crudrestaurante.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.crudrestaurante.database.DatabaseConnection;
import org.example.crudrestaurante.models.Pedido;
import org.example.crudrestaurante.models.Cliente;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class PedidoController {

    @FXML
    private ComboBox<String> comboBoxClientes;
    @FXML
    private ComboBox<String> comboBoxEstado;
    @FXML
    private TableView<Pedido> tableView;
    @FXML
    private TableColumn<Pedido, Integer> tableId;
    @FXML
    private TableColumn<Pedido, String> tableClienteNombre, tableFecha, tableHora, tableTotal, tableEstado;
    @FXML
    private Button btVolver;

    private final ObservableList<Pedido> listaPedidos = FXCollections.observableArrayList();


    @FXML
    public void initialize() {
        tableId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        tableClienteNombre.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCliente().getNombre()));
        tableFecha.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFechaPedido().toString()));
        tableHora.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getHoraPedido().toString()));
        tableTotal.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getTotal())));
        tableEstado.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEstado()));

        comboBoxEstado.setItems(FXCollections.observableArrayList("Pendiente", "En preparación", "Entregado"));
        cargarClientes();
        cargarPedidos();
        tableView.setItems(listaPedidos);
    }

    private void cargarClientes() {
        ObservableList<String> listaClientes = FXCollections.observableArrayList();
        String query = "SELECT nombre FROM Clientes";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                listaClientes.add(rs.getString("nombre"));
            }

            comboBoxClientes.setItems(listaClientes);

        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo cargar la lista de clientes.");
            e.printStackTrace();
        }
    }

    @FXML
    private void cargarPedidos() {
        listaPedidos.clear();
        String query = "SELECT p.id_pedido, c.id_cliente, c.nombre, p.fecha_pedido, p.hora_pedido, p.total, p.estado " + "FROM Pedidos p " + "JOIN Clientes c ON p.id_cliente = c.id_cliente";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Cliente cliente = new Cliente(rs.getInt("id_cliente"), rs.getString("nombre"), "", "");
                Pedido pedido = new Pedido();
                pedido.setId(rs.getInt("id_pedido"));
                pedido.setCliente(cliente);
                pedido.setFechaPedido(rs.getDate("fecha_pedido").toLocalDate());
                pedido.setHoraPedido(rs.getTime("hora_pedido").toLocalTime());
                pedido.setEstado(rs.getString("estado"));
                pedido.setTotal(rs.getFloat("total"));
                listaPedidos.add(pedido);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo cargar la lista de pedidos.");
            e.printStackTrace();
        }
    }


    @FXML
    private void crearPedido() throws SQLException {
        if (comboBoxClientes.getValue() == null || comboBoxEstado.getValue() == null) {
            mostrarAlerta("Campos Vacíos", "Por favor, completa todos los campos.");
            return;
        }

        String clienteSeleccionado = comboBoxClientes.getValue();
        String estado = comboBoxEstado.getValue();
        int idCliente;

        String queryCliente = "SELECT id_cliente FROM Clientes WHERE nombre = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement precioStmt = conn.prepareStatement(queryCliente)) {

            precioStmt.setString(1, clienteSeleccionado);
            ResultSet rs = precioStmt.executeQuery();
            if (rs.next()) {
                idCliente = rs.getInt("id_cliente");
            } else {
                mostrarAlerta("Error", "No se encontró el cliente seleccionado.");
                return;
            }
        }

        String queryPedido = "INSERT INTO Pedidos (id_cliente, fecha_pedido, hora_pedido, total, estado) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmtPedido = conn.prepareStatement(queryPedido)) {

            stmtPedido.setInt(1, idCliente);
            stmtPedido.setDate(2, Date.valueOf(LocalDate.now()));
            stmtPedido.setTime(3, Time.valueOf(LocalTime.now()));
            stmtPedido.setDouble(4, 0);
            stmtPedido.setString(5, estado);
            stmtPedido.executeUpdate();

            cargarPedidos();
            limpiarCampos();

            mostrarAlertaExito("Éxito", "Pedido creado correctamente.");

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al registrar el pedido.");
            e.printStackTrace();
        }
    }


    @FXML
    private void modificarPedido() {
        Pedido pedidoSeleccionado = tableView.getSelectionModel().getSelectedItem();
        if (pedidoSeleccionado != null) {
            String nuevoEstado = comboBoxEstado.getValue() != null ? comboBoxEstado.getValue() : pedidoSeleccionado.getEstado();

            String query = "UPDATE Pedidos SET estado = ? WHERE id_pedido = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, nuevoEstado);
                stmt.setInt(2, pedidoSeleccionado.getId());
                stmt.executeUpdate();

            cargarPedidos();
            limpiarCampos();
            mostrarAlertaExito("Éxito", "Pedido modificado correctamente.");

            } catch (SQLException e) {
                mostrarAlerta("Error", "No se pudo modificar el pedido.");
                e.printStackTrace();
            }
        } else {
            mostrarAlerta("Error", "Seleccione un pedido para modificar.");
        }
    }

    @FXML
    private void buscarPedido() {
        String clienteSeleccionado = comboBoxClientes.getValue();

        if (clienteSeleccionado != null) {
            String queryCliente = "SELECT id_cliente FROM Clientes WHERE nombre = ?";
            int idCliente;

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmtCliente = conn.prepareStatement(queryCliente)) {

                stmtCliente.setString(1, clienteSeleccionado);
                ResultSet rsCliente = stmtCliente.executeQuery();

                if (rsCliente.next()) {
                    idCliente = rsCliente.getInt("id_cliente");
                } else {
                    mostrarAlerta("Error", "No se encontró el cliente seleccionado.");
                    return;
                }
            } catch (SQLException e) {
                mostrarAlerta("Error", "Error al obtener el ID del cliente.");
                e.printStackTrace();
                return;
            }

            // Ahora que tenemos el ID del cliente, hacemos la búsqueda de pedidos
            String queryPedidos = "SELECT * FROM Pedidos WHERE id_cliente = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmtPedidos = conn.prepareStatement(queryPedidos)) {

                stmtPedidos.setInt(1, idCliente);
                ResultSet rsPedidos = stmtPedidos.executeQuery();
                listaPedidos.clear();

                while (rsPedidos.next()) {
                    Pedido pedido = new Pedido();
                    pedido.setId(rsPedidos.getInt("id_pedido"));
                    pedido.setCliente(new Cliente(idCliente, clienteSeleccionado, "", ""));
                    pedido.setFechaPedido(rsPedidos.getDate("fecha_pedido").toLocalDate());
                    pedido.setHoraPedido(rsPedidos.getTime("hora_pedido").toLocalTime());
                    pedido.setEstado(rsPedidos.getString("estado"));
                    pedido.setTotal(rsPedidos.getFloat("total"));
                    listaPedidos.add(pedido);
                }

                if (listaPedidos.isEmpty()) {
                    mostrarAlerta("Información", "No se encontraron pedidos para este cliente.");
                }

            } catch (SQLException e) {
                mostrarAlerta("Error", "Error al buscar los pedidos.");
                e.printStackTrace();
            }
        } else {
            mostrarAlerta("Error", "Seleccione un cliente para buscar pedidos.");
        }
    }

    @FXML
    private void borrarPedido() {
        Pedido pedidoSeleccionado = tableView.getSelectionModel().getSelectedItem();
        if (pedidoSeleccionado != null) {
            String query = "DELETE FROM Pedidos WHERE id_pedido = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, pedidoSeleccionado.getId());
                stmt.executeUpdate();

            } catch (SQLException e) {
                mostrarAlerta("Error", "No se pudo eliminar el pedido.");
                e.printStackTrace();
                return;
            }

            cargarPedidos();
            limpiarCampos();

            mostrarAlertaExito("Éxito", "Pedido eliminado correctamente.");

        } else {
            mostrarAlerta("Error", "Seleccione un pedido para eliminar.");
        }
    }

    @FXML
    private void seleccionarPedido() {
        Pedido pedidoSeleccionado = tableView.getSelectionModel().getSelectedItem();

        if (pedidoSeleccionado != null) {
            // Abrir la ventana de detalles para agregar productos
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/crudrestaurante/detallePedido-view.fxml"));
                Stage stage = new Stage();
                stage.setScene(new Scene(loader.load()));
                stage.setTitle("Añadir Producto al Pedido");

                // Pasar el ID del pedido seleccionado al controlador de la ventana de detalle
                DetallePedidoController detalleController = loader.getController();
                detalleController.setPedidoId(pedidoSeleccionado.getId());

                stage.show();
            } catch (IOException e) {
                mostrarAlerta("Error", "No se pudo abrir la ventana de detalle del pedido.");
                e.printStackTrace();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            mostrarAlerta("Error", "Selecciona un pedido primero.");
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
        comboBoxClientes.setValue(null);
        comboBoxEstado.setValue(null);
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
