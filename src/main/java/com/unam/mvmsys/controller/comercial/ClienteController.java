package com.unam.mvmsys.controller.comercial;

import com.unam.mvmsys.controller.MainController;
import com.unam.mvmsys.controller.comercial.forms.FormClienteController;
import com.unam.mvmsys.entidad.comercial.ClienteProducto;
import com.unam.mvmsys.entidad.seguridad.Persona;
import com.unam.mvmsys.servicio.comercial.ClienteProductoVinculacionService;
import com.unam.mvmsys.servicio.seguridad.PersonaService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import com.unam.mvmsys.ui.TableActionButtons; // Import Table Action Buttons
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Component
public class ClienteController implements Initializable {

    // --- WRAPPER PARA EDICIÓN EN TABLA ---
    public class VinculoRow {
        private final ClienteProducto vinculo;
        
        public VinculoRow(ClienteProducto vinculo) { this.vinculo = vinculo; }
        
        public String getProducto() { return vinculo.getProducto().getNombre(); }
        public String getCodigo() { return vinculo.getProducto().getCodigoSku(); }
        
        public String getObservacion() { return vinculo.getObservaciones(); }
        public void setObservacion(String obs) { 
            clienteProductoService.actualizar(vinculo.getCliente().getId(), vinculo.getProducto().getId(), obs);
            vinculo.setObservaciones(obs);
        }

        public String getPrecioEspecial() { 
            return String.format("$ %.2f", vinculo.getProducto().getPrecioVentaBase());
        }
    }

    private final PersonaService personaService;
    private final ClienteProductoVinculacionService clienteProductoService;
    private final ApplicationContext applicationContext;

    @Autowired @Lazy private MainController mainController;

    @FXML private VBox viewLista;
    
    // Inyección del controlador hijo
    @FXML private StackPane contenedorFormulario;
    @FXML private FormClienteController formClienteController;

    // Tabla Principal
    @FXML private TableView<Persona> tblClientes;
    @FXML private TableColumn<Persona, String> colRazonSocial;
    @FXML private TableColumn<Persona, String> colCuit;
    @FXML private TableColumn<Persona, String> colEmail;
    @FXML private TableColumn<Persona, String> colTelefono;
    @FXML private TableColumn<Persona, Void> colAccionesCliente;
    @FXML private TextField txtBuscarCliente;

    // Tabla Secundaria
    @FXML private TableView<VinculoRow> tblProductosVinculados;
    @FXML private TableColumn<VinculoRow, String> colProducto;
    @FXML private TableColumn<VinculoRow, String> colPrecioEspecial;
    @FXML private TableColumn<VinculoRow, String> colCantidad;
    @FXML private TableColumn<VinculoRow, Void> colAccionesVinculo;

    private Persona clienteSeleccionado;
    private final ObservableList<Persona> listaClientesMaster = FXCollections.observableArrayList();
    private FilteredList<Persona> listaClientesFiltrada;
    private final ObservableList<VinculoRow> listaProductosVinculados = FXCollections.observableArrayList();

    public ClienteController(PersonaService personaService,
                             ClienteProductoVinculacionService clienteProductoService,
                             ApplicationContext applicationContext) {
        this.personaService = personaService;
        this.clienteProductoService = clienteProductoService;
        this.applicationContext = applicationContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTablas();
        cargarDatosIniciales();

        // Selección de cliente
        tblClientes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            clienteSeleccionado = newVal;
            if (newVal != null) cargarProductosVinculados();
            else listaProductosVinculados.clear();
        });

        // Búsqueda en tiempo real
        txtBuscarCliente.textProperty().addListener((observable, oldValue, newValue) -> {
            String query = newValue == null ? "" : newValue.trim().toLowerCase();
            listaClientesFiltrada.setPredicate(persona -> coincideBusqueda(persona, query));
        });
    }

    // === NAVEGACIÓN PADRE-HIJO ===

    @FXML void accionNuevo(ActionEvent event) {
        viewLista.setVisible(false);
        contenedorFormulario.setVisible(true);
        
        // CORRECCIÓN: El método se llama configurar, no iniciarFormulario
        formClienteController.configurar(null, 
            () -> { // On Cancel
                contenedorFormulario.setVisible(false);
                viewLista.setVisible(true);
            }, 
            (clienteGuardado) -> { // On Success
                contenedorFormulario.setVisible(false);
                viewLista.setVisible(true);
                cargarDatosIniciales();
            }
        );
    }

    @FXML void accionAgregarProducto(ActionEvent event) {
        if (clienteSeleccionado == null) {
            mostrarAlerta("Seleccione un cliente para vincular.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/comercial/vincularProducto.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent vista = loader.load();
            VincularProductoController controller = loader.getController();
            controller.setCliente(clienteSeleccionado);
            if (mainController != null) mainController.cambiarVistaCentro(vista);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void configurarTablas() {
        // --- TABLA CLIENTES ---
        colRazonSocial.setCellValueFactory(new PropertyValueFactory<>("razonSocial"));
        colCuit.setCellValueFactory(new PropertyValueFactory<>("cuitDni"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        colAccionesCliente.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = TableActionButtons.iconButton("✎", () -> editarCliente(getTableView().getItems().get(getIndex())));
            private final Button btnEstado = TableActionButtons.iconButton("", () -> cambiarEstado(getTableView().getItems().get(getIndex())), "danger");
            private final HBox pane = new HBox(5, btnEditar, btnEstado);

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Persona p = getTableView().getItems().get(getIndex());
                    btnEstado.setText(p.isActivo() ? "🗑" : "♻");
                    if (p.isActivo()) {
                        TableActionButtons.setDanger(btnEstado);
                    } else {
                        TableActionButtons.setSuccess(btnEstado);
                    }
                    setGraphic(pane);
                }
            }
        });

        // --- TABLA VINCULADOS ---
        tblProductosVinculados.setEditable(true);
        colProducto.setCellValueFactory(new PropertyValueFactory<>("producto"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("observacion"));
        colCantidad.setCellFactory(TextFieldTableCell.forTableColumn());
        colCantidad.setOnEditCommit(e -> e.getRowValue().setObservacion(e.getNewValue()));
        colPrecioEspecial.setCellValueFactory(new PropertyValueFactory<>("precioEspecial"));

        colAccionesVinculo.setCellFactory(param -> new TableCell<>() {
            private final Button btnEliminar = TableActionButtons.iconButton("✖", () -> desvincular(getTableView().getItems().get(getIndex())), "danger");
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnEliminar);
            }
        });

        // Configurar lista filtrada
        listaClientesFiltrada = new FilteredList<>(listaClientesMaster, p -> true);
        SortedList<Persona> sortedData = new SortedList<>(listaClientesFiltrada);
        sortedData.comparatorProperty().bind(tblClientes.comparatorProperty());

        tblClientes.setItems(sortedData);
        tblProductosVinculados.setItems(listaProductosVinculados);
    }

    private void editarCliente(Persona p) {
        viewLista.setVisible(false);
        contenedorFormulario.setVisible(true);
        formClienteController.configurar(p,
            () -> { // Cancel
                contenedorFormulario.setVisible(false);
                viewLista.setVisible(true);
            },
            (clienteGuardado) -> { // Save
                contenedorFormulario.setVisible(false);
                viewLista.setVisible(true);
                cargarDatosIniciales();
            }
        );
    }

    private void cambiarEstado(Persona p) {
        String accion = p.isActivo() ? "DESACTIVAR" : "ACTIVAR";
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea " + accion + " al cliente?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                p.setActivo(!p.isActivo());
                personaService.actualizarPersona(p);
                tblClientes.refresh();
            }
        });
    }

    private void desvincular(VinculoRow row) {
        // Lógica de desvinculación (si tienes el servicio)
        // clienteProductoService.eliminar(row.vinculo.getId());
        listaProductosVinculados.remove(row);
    }

    private void cargarDatosIniciales() {
        listaClientesMaster.setAll(personaService.listarClientes());
    }

    private boolean coincideBusqueda(Persona persona, String query) {
        if (query == null || query.isEmpty()) return true;
        String razon = safe(persona.getRazonSocial());
        String cuit = safe(persona.getCuitDni());
        String email = safe(persona.getEmail());
        String tel = safe(persona.getTelefono());
        String calle = safe(persona.getDireccionCalle());
        String numero = safe(persona.getDireccionNumero());
        String localidad = persona.getLocalidad() != null ? safe(persona.getLocalidad().getNombre()) : "";
        String categoria = persona.getCategoriaCliente() != null ? safe(persona.getCategoriaCliente().getNombre()) : "";
        String rubro = persona.getRubro() != null ? safe(persona.getRubro().getNombre()) : "";

        return razon.contains(query) || cuit.contains(query) || email.contains(query) || tel.contains(query)
                || calle.contains(query) || numero.contains(query) || localidad.contains(query)
                || categoria.contains(query) || rubro.contains(query);
    }

    private String safe(String val) { return val == null ? "" : val.toLowerCase(); }

    private void cargarProductosVinculados() {
        if (clienteSeleccionado != null) {
            try {
                List<ClienteProducto> productos = clienteProductoService.obtenerProductosDelCliente(clienteSeleccionado.getId());
                List<VinculoRow> rows = productos.stream().map(VinculoRow::new).collect(Collectors.toList());
                listaProductosVinculados.setAll(rows);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void mostrarAlerta(String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}