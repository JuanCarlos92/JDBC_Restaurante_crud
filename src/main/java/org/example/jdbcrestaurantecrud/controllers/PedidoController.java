package org.example.jdbcrestaurantecrud.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;
import org.example.jdbcrestaurantecrud.database.DatabaseConnection;
import org.example.jdbcrestaurantecrud.models.Pedido;
import org.example.jdbcrestaurantecrud.models.Cliente;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

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
    private final ObservableList<Pedido> listaPedidosPreparacion = FXCollections.observableArrayList();


    @FXML
    public void initialize() {
        tableId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId_pedido()).asObject());
        tableClienteNombre.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCliente().getNombre()));
        tableFecha.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFecha_pedido().toString()));
        tableHora.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getHora_pedido().toString()));
        tableTotal.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getTotal())));
        tableEstado.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEstado()));

        comboBoxEstado.setItems(FXCollections.observableArrayList("Pendiente", "En preparación", "Entregado"));
        cargarClientes();
        cargarPedidos();
        cargarPedidosPreparacion();
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
                pedido.setId_pedido(rs.getInt("id_pedido"));
                pedido.setCliente(cliente);
                pedido.setFecha_pedido(rs.getDate("fecha_pedido"));
                pedido.setHora_pedido(rs.getTime("hora_pedido"));
                pedido.setEstado(rs.getString("estado"));
                pedido.setTotal(rs.getFloat("total"));
                listaPedidos.add(pedido);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo cargar la lista de pedidos.");
            e.printStackTrace();
        }
    }

    private void cargarPedidosPreparacion() {
        listaPedidosPreparacion.clear();
        String query = "SELECT * FROM Pedidos WHERE estado ='en preparación'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Pedido pedido = new Pedido();
                pedido.setId_pedido(rs.getInt("id_pedido"));
                pedido.setId_cliente(rs.getInt("id_cliente"));
                pedido.setFecha_pedido(rs.getDate("fecha_pedido"));
                pedido.setHora_pedido(rs.getTime("hora_pedido"));
                pedido.setEstado(rs.getString("estado"));
                pedido.setTotal(rs.getFloat("total"));
                listaPedidosPreparacion.add(pedido);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo cargar la lista de pedidos en preparacion.");
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
                stmt.setInt(2, pedidoSeleccionado.getId_pedido());
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
                    pedido.setId_pedido(rsPedidos.getInt("id_pedido"));
                    pedido.setCliente(new Cliente(idCliente, clienteSeleccionado, "", ""));
                    pedido.setFecha_pedido(rsPedidos.getDate("fecha_pedido"));
                    pedido.setHora_pedido(rsPedidos.getTime("hora_pedido"));
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

                stmt.setInt(1, pedidoSeleccionado.getId_pedido());
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/jdbcrestaurantecrud/detallePedido-view.fxml"));
                Stage stage = new Stage();
                stage.setScene(new Scene(loader.load()));
                stage.setTitle("Añadir Producto al Pedido");

                // Pasar el ID del pedido seleccionado al controlador de la ventana de detalle
                DetallePedidoController detalleController = loader.getController();
                detalleController.setPedidoId(pedidoSeleccionado.getId_pedido());

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
    private void generarInforme() throws JRException {
        String jrxmlFile = "src/main/jasperreports/InformePedido.jrxml";
        String pdfFile = "src/main/jasperreports/reportesgenerados/InformePedido.pdf";

        JasperReport report = JasperCompileManager.compileReport(jrxmlFile);

        // Cargar datos desde la lista de clientes
        JRDataSource datasource = new JRBeanCollectionDataSource(listaPedidosPreparacion);

        // Llenar el informe con datos
        JasperPrint jasperPrint = JasperFillManager.fillReport(report, null, datasource);

        JasperViewer viewer = new JasperViewer(jasperPrint, false);
        viewer.setVisible(true);
        JasperExportManager.exportReportToPdfFile(jasperPrint, pdfFile);

        mostrarAlertaExito("Éxito", "Informe de productos generado correctamente en: " + pdfFile);
    }

    @FXML
    private void generarInformeTicketPedido() throws JRException {
        Pedido pedidoSeleccionado = tableView.getSelectionModel().getSelectedItem();

        if (pedidoSeleccionado == null) {
            mostrarAlerta("Error", "Seleccione un pedido para generar el informe.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String jasperFile = "src/main/jasperreports/Ticket.jasper";
            String pdfFile = "src/main/jasperreports/reportesgenerados/InformeTicketPedido.pdf";
            JasperReport jasperReport = (JasperReport) JRLoader.loadObjectFromFile(jasperFile);

            // Parámetros para el informe
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("id_pedido", pedidoSeleccionado.getId_pedido());

            // Llenar el informe con datos de la base de datos
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);

            // Mostrar el informe
            JasperViewer.viewReport(jasperPrint, false);
            JasperExportManager.exportReportToPdfFile(jasperPrint, pdfFile);

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo generar el informe.");
            e.printStackTrace();
        }
        
    }

    @FXML
    private void volverAlMenu() {
        try {
            Stage stageActual = (Stage) btVolver.getScene().getWindow();
            stageActual.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/jdbcrestaurantecrud/menu-view.fxml"));
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
