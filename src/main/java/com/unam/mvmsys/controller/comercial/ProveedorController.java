package com.unam.mvmsys.controller.comercial;

import com.unam.mvmsys.controller.MainController;
import com.unam.mvmsys.controller.comercial.forms.FormProveedorController; // Importar hijo
import com.unam.mvmsys.entidad.comercial.ProveedorProducto;
import com.unam.mvmsys.entidad.seguridad.Persona;
import com.unam.mvmsys.servicio.comercial.ProveedorProductoService;
import com.unam.mvmsys.servicio.seguridad.PersonaService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.beans.property.SimpleStringProperty;
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
import com.unam.mvmsys.ui.TableActionButtons;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Component
public class ProveedorController implements Initializable {

    public class ProvVinculoRow {
        private final ProveedorProducto vinculo;
        public ProvVinculoRow(ProveedorProducto v) { this.vinculo = v; }
        public String getProducto() { return vinculo.getProducto().getNombre(); }
        public String getPrecioCompra() { return vinculo.getPrecioCompra() != null ? vinculo.getPrecioCompra().toString() : "0.00"; }
        public void setPrecioCompra(String precio) {
            try {
                BigDecimal valor = new BigDecimal(precio.replace(",", "."));
                proveedorProductoService.actualizar(vinculo.getProveedor().getId(), vinculo.getProducto().getId(), valor, vinculo.getObservaciones());
                vinculo.setPrecioCompra(valor);
            } catch (Exception e) {}
        }
        public String getTiempoEntrega() { return vinculo.getObservaciones(); }
        public void setTiempoEntrega(String obs) {
            proveedorProductoService.actualizar(vinculo.getProveedor().getId(), vinculo.getProducto().getId(), vinculo.getPrecioCompra(), obs);
            vinculo.setObservaciones(obs);
        }
    }

    private final PersonaService personaService;
    private final ProveedorProductoService proveedorProductoService;
    private final ApplicationContext applicationContext;

    @Autowired @Lazy private MainController mainController;

    @FXML private VBox viewLista;
    
    // Inyección Hijo
    @FXML private StackPane contenedorFormulario;
    @FXML private FormProveedorController formProveedorController;

    @FXML private TableView<Persona> tblProveedores;
    @FXML private TableColumn<Persona, String> colCuit;
    @FXML private TableColumn<Persona, String> colRazon;
    @FXML private TableColumn<Persona, String> colRubro;
    @FXML private TableColumn<Persona, Integer> colPlazo;
    @FXML private TableColumn<Persona, Void> colAccionesProveedor;
    @FXML private TextField txtBusqueda;
    
    @FXML private TableView<ProvVinculoRow> tblProductosVinculados;
    @FXML private TableColumn<ProvVinculoRow, String> colProducto;
    @FXML private TableColumn<ProvVinculoRow, String> colPrecioCompra;
    @FXML private TableColumn<ProvVinculoRow, Void> colAccionesProducto;

    private Persona proveedorSeleccionado;
    private final ObservableList<Persona> listaProveedores = FXCollections.observableArrayList();
    private FilteredList<Persona> listaProveedoresFiltrada;
    private final ObservableList<ProvVinculoRow> listaProductosVinculados = FXCollections.observableArrayList();

    public ProveedorController(PersonaService personaService,
                               ProveedorProductoService proveedorProductoService, ApplicationContext applicationContext) {
        this.personaService = personaService;
        this.proveedorProductoService = proveedorProductoService;
        this.applicationContext = applicationContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTablas();
        cargarDatos();

        listaProveedoresFiltrada = new FilteredList<>(listaProveedores, p -> true);
        SortedList<Persona> ordenados = new SortedList<>(listaProveedoresFiltrada);
        ordenados.comparatorProperty().bind(tblProveedores.comparatorProperty());
        tblProveedores.setItems(ordenados);
        
        tblProveedores.getSelectionModel().selectedItemProperty().addListener((obs, old, nuevo) -> {
            proveedorSeleccionado = nuevo;
            if (nuevo != null) cargarProductosVinculados();
            else listaProductosVinculados.clear();
        });
        
        txtBusqueda.textProperty().addListener((o, old, val) -> {
            String query = val == null ? "" : val.trim().toLowerCase();
            listaProveedoresFiltrada.setPredicate(p -> coincideBusqueda(p, query));
        });
    }

    // === NAVEGACIÓN PADRE-HIJO ===

    @FXML void accionNuevo(ActionEvent event) {
        viewLista.setVisible(false);
        contenedorFormulario.setVisible(true);
        
        formProveedorController.configurar(null, 
            () -> { // Cancel
                contenedorFormulario.setVisible(false);
                viewLista.setVisible(true);
            }, 
            (guardado) -> { // Success
                contenedorFormulario.setVisible(false);
                viewLista.setVisible(true);
                cargarDatos();
            }
        );
    }

    @FXML void accionAgregarProducto(ActionEvent event) {
        if (proveedorSeleccionado == null) {
            mostrarAlerta("Seleccione un proveedor.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/comercial/vincularProducto.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent vista = loader.load();
            VincularProductoController controller = loader.getController();
            controller.setProveedor(proveedorSeleccionado);
            if (mainController != null) mainController.cambiarVistaCentro(vista);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void configurarTablas() {
        colCuit.setCellValueFactory(new PropertyValueFactory<>("cuitDni"));
        colRazon.setCellValueFactory(new PropertyValueFactory<>("razonSocial"));
        colRubro.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getRubro() != null ? cell.getValue().getRubro().getNombre() : "-"));
        colPlazo.setCellValueFactory(new PropertyValueFactory<>("plazoPagoDias"));

        // Acciones Proveedor
        colAccionesProveedor.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = TableActionButtons.iconButton("✎", () -> editarProveedor(getTableView().getItems().get(getIndex())));
            private final Button btnEstado = TableActionButtons.iconButton("", () -> cambiarEstado(getTableView().getItems().get(getIndex())), "danger");
            private final HBox pane = new HBox(5, btnEditar, btnEstado);
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Persona p = getTableView().getItems().get(getIndex());
                btnEstado.setText(p.isActivo() ? "🗑" : "♻");
                if (p.isActivo()) {
                    TableActionButtons.setDanger(btnEstado);
                } else {
                    TableActionButtons.setSuccess(btnEstado);
                }
                setGraphic(pane);
            }
        });
        
        tblProductosVinculados.setEditable(true);
        colProducto.setCellValueFactory(new PropertyValueFactory<>("producto"));
        
        colPrecioCompra.setCellValueFactory(new PropertyValueFactory<>("precioCompra"));
        colPrecioCompra.setCellFactory(TextFieldTableCell.forTableColumn());
        colPrecioCompra.setOnEditCommit(e -> e.getRowValue().setPrecioCompra(e.getNewValue()));

        // Acciones vínculo: desvincular
        colAccionesProducto.setCellFactory(param -> new TableCell<>() {
            private final Button btnEliminar = TableActionButtons.iconButton("✖", () -> listaProductosVinculados.remove(getTableView().getItems().get(getIndex())), "danger");
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnEliminar);
            }
        });
        
        tblProductosVinculados.setItems(listaProductosVinculados);
    }

    private void editarProveedor(Persona p) {
        viewLista.setVisible(false);
        contenedorFormulario.setVisible(true);
        formProveedorController.configurar(p,
            () -> { contenedorFormulario.setVisible(false); viewLista.setVisible(true); },
            (guardado) -> { contenedorFormulario.setVisible(false); viewLista.setVisible(true); cargarDatos(); }
        );
    }

    private void cambiarEstado(Persona p) {
        p.setActivo(!p.isActivo());
        personaService.actualizarPersona(p);
        tblProveedores.refresh();
    }
    
    private void cargarDatos() {
        listaProveedores.setAll(personaService.listarProveedores());
    }
    
    private void cargarProductosVinculados() {
        if (proveedorSeleccionado != null) {
            try {
                List<ProveedorProducto> lista = proveedorProductoService.obtenerProductosDelProveedor(proveedorSeleccionado.getId());
                List<ProvVinculoRow> rows = lista.stream().map(ProvVinculoRow::new).collect(Collectors.toList());
                listaProductosVinculados.setAll(rows);
            } catch (Exception e) { listaProductosVinculados.clear(); }
        }
    }

    private void mostrarAlerta(String contenido) { new Alert(Alert.AlertType.INFORMATION, contenido).showAndWait(); }

    private boolean coincideBusqueda(Persona p, String query) {
        if (query == null || query.isEmpty()) return true;
        String razon = safe(p.getRazonSocial());
        String cuit = safe(p.getCuitDni());
        String rubro = p.getRubro() != null ? safe(p.getRubro().getNombre()) : "";
        String email = safe(p.getEmail());
        String tel = safe(p.getTelefono());
        String calle = safe(p.getDireccionCalle());
        String numero = safe(p.getDireccionNumero());
        String localidad = p.getLocalidad() != null ? safe(p.getLocalidad().getNombre()) : "";
        String plazo = String.valueOf(p.getPlazoPagoDias());

        return razon.contains(query) || cuit.contains(query) || rubro.contains(query) || email.contains(query)
                || tel.contains(query) || calle.contains(query) || numero.contains(query)
                || localidad.contains(query) || plazo.contains(query);
    }

    private String safe(String val) { return val == null ? "" : val.toLowerCase(); }
}