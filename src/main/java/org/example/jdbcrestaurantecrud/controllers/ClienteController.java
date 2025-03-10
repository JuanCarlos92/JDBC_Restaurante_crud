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
import net.sf.jasperreports.view.JasperViewer;
import org.example.jdbcrestaurantecrud.database.DatabaseConnection;
import org.example.jdbcrestaurantecrud.models.Cliente;

import java.io.IOException;
import java.sql.*;

public class ClienteController {

    @FXML
    private TextField tfNombre, tfTelefono, tfDireccion;
    @FXML
    private TableView<Cliente> tableView;
    @FXML
    private TableColumn<Cliente, Integer> tableId;
    @FXML
    private TableColumn<Cliente, String> tableNombre, tableTelefono, tableDireccion;
    @FXML
    private Button btVolver;

    private final ObservableList<Cliente> listaClientes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configurar las columnas de la tabla
        tableId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId_cliente()).asObject());
        tableNombre.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNombre()));
        tableTelefono.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTelefono()));
        tableDireccion.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDireccion()));

        // Cargar los datos desde la base de datos
        cargarClientes();
        tableView.setItems(listaClientes);
    }
    @FXML
    private void cargarClientes() {
        listaClientes.clear();
        String query = "SELECT * FROM Clientes";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setId_cliente(rs.getInt("id_cliente"));
                cliente.setNombre(rs.getString("nombre"));
                cliente.setTelefono(rs.getString("telefono"));
                cliente.setDireccion(rs.getString("direccion"));
                listaClientes.add(cliente);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo cargar la lista de clientes.");
            e.printStackTrace();
        }
    }
    @FXML
    private ObservableList<Cliente> cargar1Clientes() {
        listaClientes.clear();
        String query = "SELECT * FROM Clientes";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setId_cliente(rs.getInt("id_cliente"));
                cliente.setNombre(rs.getString("nombre"));
                cliente.setTelefono(rs.getString("telefono"));
                cliente.setDireccion(rs.getString("direccion"));
                listaClientes.add(cliente);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo cargar la lista de clientes.");
            e.printStackTrace();
        }
        return listaClientes;
    }

    @FXML
    private void crearCliente() {
        if (!tfNombre.getText().isEmpty() && !tfTelefono.getText().isEmpty() && !tfDireccion.getText().isEmpty()) {
            String query = "INSERT INTO Clientes (nombre, telefono, direccion) VALUES (?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, tfNombre.getText());
                stmt.setString(2, tfTelefono.getText());
                stmt.setString(3, tfDireccion.getText());
                stmt.executeUpdate();

            } catch (SQLException e) {
                mostrarAlerta("Error", "No se pudo crear el cliente.");
                e.printStackTrace();
                return;
            }

            cargarClientes();
            limpiarCampos();

            mostrarAlertaExito("Éxito", "Cliente creado correctamente.");

        } else {
            mostrarAlerta("Error", "Todos los campos deben estar llenos.");
        }
    }

    @FXML
    private void modificarCliente() {
        Cliente clienteSeleccionado = tableView.getSelectionModel().getSelectedItem();
        if (clienteSeleccionado != null) {
            String nuevoNombre = tfNombre.getText().isEmpty() ? clienteSeleccionado.getNombre() : tfNombre.getText();
            String nuevoTelefono = tfTelefono.getText().isEmpty() ? clienteSeleccionado.getTelefono() : tfTelefono.getText();
            String nuevaDireccion = tfDireccion.getText().isEmpty() ? clienteSeleccionado.getDireccion() : tfDireccion.getText();

            String query = "UPDATE Clientes SET nombre = ?, telefono = ?, direccion = ? WHERE id_cliente = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, nuevoNombre);
                stmt.setString(2, nuevoTelefono);
                stmt.setString(3, nuevaDireccion);
                stmt.setInt(4, clienteSeleccionado.getId_cliente());
                stmt.executeUpdate();

            } catch (SQLException e) {
                mostrarAlerta("Error", "No se pudo modificar el cliente.");
                e.printStackTrace();
                return;
            }

            cargarClientes();
            limpiarCampos();

            mostrarAlertaExito("Éxito", "Cliente modificado correctamente.");

        } else {
            mostrarAlerta("Error", "Seleccione un cliente para modificar.");
        }
    }


    @FXML
    private void buscarCliente() {
        String nombreBuscar = tfNombre.getText();
        if (!nombreBuscar.isEmpty()) {
            String query = "SELECT * FROM Clientes WHERE nombre = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, nombreBuscar);
                try (ResultSet rs = stmt.executeQuery()) {
                    listaClientes.clear(); // Limpiar la tabla antes de agregar el cliente encontrado
                    if (rs.next()) {
                        Cliente cliente = new Cliente();
                        cliente.setId_cliente(rs.getInt("id_cliente"));
                        cliente.setNombre(rs.getString("nombre"));
                        cliente.setTelefono(rs.getString("telefono"));
                        cliente.setDireccion(rs.getString("direccion"));

                        listaClientes.add(cliente);
                    } else {
                        mostrarAlerta("Información", "No se encontró el cliente.");
                    }
                }
            } catch (SQLException e) {
                mostrarAlerta("Error", "Error al buscar el cliente.");
                e.printStackTrace();
            }
        } else {
            mostrarAlerta("Error", "Ingrese un nombre para buscar.");
        }
    }

    @FXML
    private void borrarCliente() {
        Cliente clienteSeleccionado = tableView.getSelectionModel().getSelectedItem();
        if (clienteSeleccionado != null) {
            String query = "DELETE FROM Clientes WHERE id = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, clienteSeleccionado.getId_cliente());
                stmt.executeUpdate();

            } catch (SQLException e) {
                mostrarAlerta("Error", "No se pudo eliminar el cliente.");
                e.printStackTrace();
                return;
            }

            cargarClientes();
            limpiarCampos();

            mostrarAlertaExito("Éxito", "Cliente eliminado correctamente.");

        } else {
            mostrarAlerta("Error", "Seleccione un cliente para eliminar.");
        }
    }
    @FXML
    private void generarInforme() throws JRException {
        String jrxmlFile = "src/main/jasperreports/InformeCliente.jrxml";
        String pdfFile = "src/main/jasperreports/reportesgenerados/InformeCliente.pdf";

        JasperReport report = JasperCompileManager.compileReport(jrxmlFile);

        // Cargar datos desde la lista de clientes
        JRDataSource datasource = new JRBeanCollectionDataSource(listaClientes);

        // Llenar el informe con datos
        JasperPrint jasperPrint = JasperFillManager.fillReport(report, null,datasource);

        JasperViewer viewer = new JasperViewer(jasperPrint, false);
        viewer.setVisible(true);
        JasperExportManager.exportReportToPdfFile(jasperPrint,pdfFile);

        mostrarAlertaExito("Éxito", "Informe de clientes generado correctamente en: " + pdfFile);
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
        tfNombre.clear();
        tfTelefono.clear();
        tfDireccion.clear();
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
