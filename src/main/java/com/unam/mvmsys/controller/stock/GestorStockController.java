package com.unam.mvmsys.controller.stock;

import com.unam.mvmsys.controller.MainController;
import com.unam.mvmsys.controller.comercial.VincularProductoController;
import com.unam.mvmsys.controller.stock.forms.FormProductoController; // Importar hijo
import com.unam.mvmsys.entidad.configuracion.UnidadMedida;
import com.unam.mvmsys.entidad.stock.Producto;
import com.unam.mvmsys.repositorio.configuracion.UnidadMedidaRepository;
import com.unam.mvmsys.servicio.stock.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class GestorStockController implements Initializable {

    private final ProductoService productoService;
    private final UnidadMedidaRepository unidadRepo;
    
    @Autowired @Lazy private MainController mainController;
    @Autowired private ApplicationContext context;

    private boolean modoVinculacion = false;
    private Object entidadVinculacion;

    @FXML private VBox viewLista;
    
    // Contenedor e Inyección del Hijo
    @FXML private StackPane contenedorFormulario;
    @FXML private FormProductoController formProductoController; 

    @FXML private TableView<Producto> tblProductosStock;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombreProducto;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, String> colUnitario;
    @FXML private TableColumn<Producto, Void> colStockActual;
    @FXML private TableColumn<Producto, String> colStockMinimo;
    @FXML private TableColumn<Producto, String> colCostoUnitario;
    @FXML private TableColumn<Producto, String> colPrecioVenta;
    @FXML private TableColumn<Producto, Void> colAccionesProducto;
    
    @FXML private TextField txtBusquedaProducto;
    @FXML private HBox paginationBoxProductos;

    private final ObservableList<Producto> listaProductosMaster = FXCollections.observableArrayList();
    private FilteredList<Producto> productosFiltrados;

    public GestorStockController(ProductoService productoService, UnidadMedidaRepository unidadRepo) {
        this.productoService = productoService;
        this.unidadRepo = unidadRepo;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabla();
        cargarProductos();
        configurarBusquedaEnVivo();
        mostrarVistaLista();
    }

    // === LÓGICA DE NAVEGACIÓN Y COMPOSICIÓN ===

    @FXML
    public void accionNuevo() {
        viewLista.setVisible(false);
        contenedorFormulario.setVisible(true);
        
        // DELEGAMOS AL HIJO
        formProductoController.configurar(null, 
            (prodGuardado) -> { // On Success
                if (modoVinculacion) volverAVincular(prodGuardado);
                else {
                    mostrarVistaLista();
                    cargarProductos();
                }
            },
            () -> { // On Cancel
                if (modoVinculacion) volverAVincular(null);
                else mostrarVistaLista();
            }
        );
    }

    public void setModoVinculacion(Object entidad) {
        this.modoVinculacion = true;
        this.entidadVinculacion = entidad;
        accionNuevo();
    }

    private void volverAVincular(Producto p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/comercial/vincularProducto.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent vista = loader.load();
            
            VincularProductoController controller = loader.getController();
            
            if (entidadVinculacion instanceof com.unam.mvmsys.entidad.seguridad.Persona) {
                com.unam.mvmsys.entidad.seguridad.Persona per = (com.unam.mvmsys.entidad.seguridad.Persona) entidadVinculacion;
                if (per.isEsCliente()) controller.setCliente(per);
                else controller.setProveedor(per);
            }
            
            if (p != null) controller.preseleccionarProducto(p);
            
            mainController.cambiarVistaCentro(vista);
        } catch(IOException e) { e.printStackTrace(); }
    }

    private void mostrarVistaLista() {
        contenedorFormulario.setVisible(false);
        viewLista.setVisible(true);
    }

    // === TABLA Y DATOS ===

    private void cargarProductos() {
        try {
            listaProductosMaster.setAll(productoService.listarTodos());
        } catch (Exception ex) {
            listaProductosMaster.setAll(productoService.listarActivos());
        }
    }

    private void configurarTabla() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoSku"));
        colNombreProducto.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getRubro() != null ? cell.getValue().getRubro().getNombre() : "-"));
        
        colUnitario.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getUnidadMedida() != null ? cell.getValue().getUnidadMedida().getCodigo() : "u"));

        colPrecioVenta.setCellValueFactory(cell -> new SimpleStringProperty(
            "$ " + cell.getValue().getPrecioVentaBase().toString()));
            
        colCostoUnitario.setCellValueFactory(cell -> new SimpleStringProperty(
            "$ " + cell.getValue().getCostoReposicion().toString()));

        // Asegurar que la columna esté presente (previene NPE si la inyección falló)
        if (colAccionesProducto == null && tblProductosStock != null) {
            TableColumn<Producto, Void> fallbackCol = new TableColumn<>("ACCIONES");
            fallbackCol.setId("colAccionesProducto");
            fallbackCol.setPrefWidth(120);
            fallbackCol.setStyle("-fx-alignment: CENTER;");
            tblProductosStock.getColumns().add(fallbackCol);
            colAccionesProducto = fallbackCol;
        }

        // COLUMNA ACCIONES
        colAccionesProducto.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = TableActionButtons.iconButton("✎", () -> editarProducto(getTableView().getItems().get(getIndex())));
            private final Button btnEstado = TableActionButtons.iconButton("", () -> cambiarEstado(getTableView().getItems().get(getIndex())), "danger");
            private final HBox box = new HBox(6, btnEditar, btnEstado);

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Producto p = getTableView().getItems().get(getIndex());
                    btnEstado.setText(p.isActivo() ? "🗑" : "♻");
                    if (p.isActivo()) {
                        TableActionButtons.setDanger(btnEstado);
                    } else {
                        TableActionButtons.setSuccess(btnEstado);
                    }
                    setGraphic(box);
                }
            }
        });

        productosFiltrados = new FilteredList<>(listaProductosMaster, p -> true);
        SortedList<Producto> ordenados = new SortedList<>(productosFiltrados);
        ordenados.comparatorProperty().bind(tblProductosStock.comparatorProperty());
        tblProductosStock.setItems(ordenados);

        // Suavizar visual: opacar filas inactivas
        tblProductosStock.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Producto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    setOpacity(item.isActivo() ? 1.0 : 0.55);
                }
            }
        });
    }

    private void configurarBusquedaEnVivo() {
        if (txtBusquedaProducto == null) return;
        txtBusquedaProducto.textProperty().addListener((obs, old, val) -> {
            String query = val == null ? "" : val.trim().toLowerCase();
            productosFiltrados.setPredicate(p -> coincideBusqueda(p, query));
        });
    }

    private boolean coincideBusqueda(Producto p, String query) {
        if (query == null || query.isEmpty()) return true;
        String sku = safe(p.getCodigoSku());
        String nombre = safe(p.getNombre());
        String desc = safe(p.getDescripcion());
        String rubro = p.getRubro() != null ? safe(p.getRubro().getNombre()) : "";
        String tipo = p.getTipoProducto() != null ? safe(p.getTipoProducto().getNombre()) : "";
        String unidad = p.getUnidadMedida() != null ? (safe(p.getUnidadMedida().getCodigo()) + " " + safe(p.getUnidadMedida().getNombre())) : "";
        String precio = p.getPrecioVentaBase() != null ? p.getPrecioVentaBase().toPlainString() : "";
        String costo = p.getCostoReposicion() != null ? p.getCostoReposicion().toPlainString() : "";
        String stockMin = p.getStockMinimo() != null ? p.getStockMinimo().toPlainString() : "";
        String estado = p.isActivo() ? "activo" : "inactivo";

        return sku.contains(query) || nombre.contains(query) || desc.contains(query) || rubro.contains(query)
                || tipo.contains(query) || unidad.toLowerCase().contains(query)
                || precio.contains(query) || costo.contains(query) || stockMin.contains(query)
                || estado.contains(query);
    }

    private String safe(String s) { return s == null ? "" : s.toLowerCase(); }
    
    private void editarProducto(Producto p) {
        viewLista.setVisible(false);
        contenedorFormulario.setVisible(true);
        formProductoController.configurar(p, 
            (prodGuardado) -> { // On Success
                mostrarVistaLista();
                cargarProductos();
            },
            () -> mostrarVistaLista() // On Cancel
        );
    }

    private void cargarCombos() { /* Delegado al hijo */ }

    private void cambiarEstado(Producto p) {
        if (p == null || p.getId() == null) return;
        p.setActivo(!p.isActivo());
        productoService.actualizarProducto(p);
        tblProductosStock.refresh();
    }
}