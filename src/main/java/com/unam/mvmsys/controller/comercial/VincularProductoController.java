package com.unam.mvmsys.controller.comercial;

import com.unam.mvmsys.controller.MainController;
import com.unam.mvmsys.controller.stock.GestorStockController;
import com.unam.mvmsys.entidad.comercial.ProveedorProducto;
import com.unam.mvmsys.entidad.seguridad.Persona;
import com.unam.mvmsys.entidad.stock.Producto;
import com.unam.mvmsys.servicio.comercial.ClienteProductoVinculacionService;
import com.unam.mvmsys.servicio.comercial.ProveedorProductoService;
import com.unam.mvmsys.servicio.stock.ProductoService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class VincularProductoController implements Initializable {

    public static class ProductoItem {
        private final Producto producto;
        private final BooleanProperty seleccionado;
        private final StringProperty observacion;
        private final StringProperty precioStr;

        public ProductoItem(Producto producto, boolean seleccionadoInicial, BigDecimal precioInicial) {
            this.producto = producto;
            this.seleccionado = new SimpleBooleanProperty(seleccionadoInicial);
            this.observacion = new SimpleStringProperty("");
            this.precioStr = new SimpleStringProperty(precioInicial != null ? precioInicial.toString() : "0.00");
        }
        public BooleanProperty seleccionadoProperty() { return seleccionado; }
        public boolean isSeleccionado() { return seleccionado.get(); }
        public void setSeleccionado(boolean s) { seleccionado.set(s); }
        public StringProperty observacionProperty() { return observacion; }
        public String getObservacion() { return observacion.get(); }
        public void setObservacion(String o) { observacion.set(o); }
        public StringProperty precioStrProperty() { return precioStr; }
        public String getPrecioStr() { return precioStr.get(); }
        public void setPrecioStr(String p) { precioStr.set(p); }
        public Producto getProducto() { return producto; }
        public String getCodigoSku() { return producto.getCodigoSku(); }
        public String getNombre() { return producto.getNombre(); }
        public String getCategoria() { return producto.getTipoProducto() != null ? producto.getTipoProducto().getNombre() : "-"; }
    }

    private final ProductoService productoService;
    private final ClienteProductoVinculacionService clienteVinculacionService;
    private final ProveedorProductoService proveedorProductoService;
    private final ApplicationContext context;
    private MainController mainController;

    @FXML private Label lblTitulo;
    @FXML private Label lblSubtitulo;
    @FXML private TextField txtBusqueda;
    @FXML private TableView<ProductoItem> tblProductos;
    @FXML private TableColumn<ProductoItem, Boolean> colSeleccionar;
    @FXML private TableColumn<ProductoItem, String> colCodigo;
    @FXML private TableColumn<ProductoItem, String> colNombre;
    @FXML private TableColumn<ProductoItem, String> colCategoria;
    @FXML private TableColumn<ProductoItem, String> colPrecio;
    @FXML private TableColumn<ProductoItem, String> colObservacion;

    private Persona clienteActual;
    private Persona proveedorActual;
    private ObservableList<ProductoItem> listaItems = FXCollections.observableArrayList();
    private FilteredList<ProductoItem> listaFiltrada;
    private static final PseudoClass HIGHLIGHTED = PseudoClass.getPseudoClass("highlighted");

    public VincularProductoController(ProductoService ps, ClienteProductoVinculacionService cvs,
                                      ProveedorProductoService pps, ApplicationContext ctx) {
        this.productoService = ps;
        this.clienteVinculacionService = cvs;
        this.proveedorProductoService = pps;
        this.context = ctx;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabla();
        configurarBusquedaEnVivo();
    }

    public void setMainController(MainController mc) { this.mainController = mc; }

    @FXML
    void accionNuevoProducto(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/stock/gestorStock.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent vista = loader.load();
            GestorStockController controller = loader.getController();
            Object entidad = clienteActual != null ? clienteActual : proveedorActual;
            controller.setModoVinculacion(entidad);
            if (mainController == null) mainController = MainController.getInstance();
            mainController.cambiarVistaCentro(vista);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    void accionCancelar(ActionEvent event) {
        volverAtras();
    }

    private void volverAtras() {
        if (mainController == null) mainController = MainController.getInstance();
        if (clienteActual != null) mainController.navegarA("/fxml/comercial/clientes.fxml");
        else if (proveedorActual != null) mainController.navegarA("/fxml/comercial/proveedores.fxml");
        else mainController.navegarA("/fxml/stock/gestorStock.fxml");
    }

    private void configurarTabla() {
        tblProductos.setEditable(true);
        listaFiltrada = new FilteredList<>(listaItems, p -> true);
        SortedList<ProductoItem> ordenados = new SortedList<>(listaFiltrada);
        ordenados.comparatorProperty().bind(tblProductos.comparatorProperty());
        tblProductos.setItems(ordenados);
        colSeleccionar.setCellValueFactory(cellData -> cellData.getValue().seleccionadoProperty());
        colSeleccionar.setCellFactory(CheckBoxTableCell.forTableColumn(colSeleccionar));
        colSeleccionar.setEditable(true);
        
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoSku"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        
        colPrecio.setCellValueFactory(cellData -> cellData.getValue().precioStrProperty());
        colPrecio.setCellFactory(TextFieldTableCell.forTableColumn());
        colPrecio.setOnEditCommit(e -> { e.getRowValue().setPrecioStr(e.getNewValue()); e.getRowValue().setSeleccionado(true); });
        
        colObservacion.setCellValueFactory(cellData -> cellData.getValue().observacionProperty());
        colObservacion.setCellFactory(TextFieldTableCell.forTableColumn());
        colObservacion.setOnEditCommit(e -> { e.getRowValue().setObservacion(e.getNewValue()); e.getRowValue().setSeleccionado(true); });

        // Highlight Logic
        tblProductos.setRowFactory(tv -> {
            TableRow<ProductoItem> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem != null) {
                    row.pseudoClassStateChanged(HIGHLIGHTED, newItem.isSeleccionado());
                    newItem.seleccionadoProperty().addListener((o, ov, nv) -> row.pseudoClassStateChanged(HIGHLIGHTED, nv));
                } else {
                    row.pseudoClassStateChanged(HIGHLIGHTED, false);
                }
            });
            return row;
        });
    }

    private void configurarBusquedaEnVivo() {
        if (txtBusqueda == null) return;
        txtBusqueda.textProperty().addListener((obs, old, val) -> {
            String query = val == null ? "" : val.trim().toLowerCase();
            listaFiltrada.setPredicate(item -> {
                if (query.isEmpty()) return true;
                return safe(item.getNombre()).contains(query)
                        || safe(item.getCodigoSku()).contains(query)
                        || safe(item.getCategoria()).contains(query);
            });
        });
    }
    
    public void setCliente(Persona c) { 
        this.clienteActual = c; 
        this.proveedorActual = null; 
        if(lblTitulo != null) {
            lblTitulo.setText("Vincular a Cliente");
            lblSubtitulo.setText("Cliente: " + c.getRazonSocial());
        }
        if(colPrecio != null) colPrecio.setText("PRECIO VENTA ($) ✎");
        cargarYMarcarProductos(); 
    }

    public void setProveedor(Persona p) {
        this.proveedorActual = p;
        this.clienteActual = null;
        if (lblTitulo != null) {
            lblTitulo.setText("Vincular a Proveedor");
            lblSubtitulo.setText("Proveedor: " + p.getRazonSocial());
        }
        if (colPrecio != null) colPrecio.setText("COSTO COMPRA ($) ✎");
        cargarYMarcarProductos();
    }
    
    public void preseleccionarProducto(Producto p) {
        cargarYMarcarProductos();
        for(ProductoItem item : listaItems) {
            if(item.getProducto().getId().equals(p.getId())) {
                item.setSeleccionado(true);
                tblProductos.scrollTo(item);
                tblProductos.getSelectionModel().select(item);
                break;
            }
        }
    }
    
    private void cargarYMarcarProductos() {
        List<Producto> todos = productoService.listarActivos();
        List<ProductoItem> items = new ArrayList<>();
        
        Map<UUID, ProveedorProducto> vinculosProveedor = new HashMap<>();
        Map<UUID, String> vinculosCliente = new HashMap<>();

        if (proveedorActual != null) {
            try {
                List<ProveedorProducto> ppList = proveedorProductoService.obtenerProductosDelProveedor(proveedorActual.getId());
                for(ProveedorProducto pp : ppList) vinculosProveedor.put(pp.getProducto().getId(), pp);
            } catch (Exception ignored) {}
        } else if (clienteActual != null) {
            try {
                clienteVinculacionService.obtenerProductosDelCliente(clienteActual.getId())
                        .forEach(cp -> vinculosCliente.put(cp.getProducto().getId(), cp.getObservaciones()));
            } catch (Exception ignored) {}
        }

        for (Producto p : todos) {
            boolean seleccionado = false;
            BigDecimal precio = BigDecimal.ZERO;
            String obs = "";

            if (proveedorActual != null) {
                if (vinculosProveedor.containsKey(p.getId())) {
                    ProveedorProducto pp = vinculosProveedor.get(p.getId());
                    seleccionado = true;
                    precio = pp.getPrecioCompra();
                    obs = pp.getObservaciones();
                } else {
                    precio = p.getCostoReposicion() != null ? p.getCostoReposicion() : BigDecimal.ZERO;
                }
            } else {
                if (vinculosCliente.containsKey(p.getId())) {
                    seleccionado = true;
                    precio = p.getPrecioVentaBase();
                    obs = vinculosCliente.get(p.getId());
                } else {
                    precio = p.getPrecioVentaBase();
                }
            }
            
            ProductoItem item = new ProductoItem(p, seleccionado, precio);
            item.setObservacion(obs);
            items.add(item);
        }
        
        listaItems.setAll(items);
    }
    
    @FXML
    void accionBuscar(ActionEvent event) {
        // Mantener acción accesible; la búsqueda ya es en vivo
        if (listaFiltrada != null) listaFiltrada.setPredicate(listaFiltrada.getPredicate());
    }

    @FXML
    void accionVincular(ActionEvent event) {
        List<ProductoItem> seleccionados = listaItems.stream()
                .filter(ProductoItem::isSeleccionado)
                .collect(Collectors.toList());

        if (seleccionados.isEmpty()) {
            mostrarAlerta("No ha seleccionado ningún producto.");
            return;
        }

        int count = 0;
        try {
            for (ProductoItem item : seleccionados) {
                String precioStr = item.getPrecioStr();
                if(precioStr == null) precioStr = "0";
                BigDecimal precio = new BigDecimal(precioStr.replace(",", "."));
                String obs = item.getObservacion();

                if (clienteActual != null) {
                    if (!clienteVinculacionService.existeVinculacion(clienteActual.getId(), item.getProducto().getId())) {
                        clienteVinculacionService.vincular(clienteActual, item.getProducto(), obs);
                        count++;
                    }
                } else if (proveedorActual != null) {
                    proveedorProductoService.vincular(proveedorActual, item.getProducto(), precio, obs);
                    count++;
                }
            }
            
            mostrarAlerta("Se procesaron " + count + " productos correctamente.");
            volverAtras();
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error al procesar: " + e.getMessage());
        }
    }
    
    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private String safe(String val) { return val == null ? "" : val.toLowerCase(); }
}