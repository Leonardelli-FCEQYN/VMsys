package com.unam.mvmsys.controller.comercial;

import com.unam.mvmsys.entidad.comercial.ClienteProducto;
import com.unam.mvmsys.entidad.comercial.ProveedorProducto;
import com.unam.mvmsys.entidad.seguridad.Persona;
import com.unam.mvmsys.entidad.stock.Producto;
import com.unam.mvmsys.servicio.comercial.ClienteProductoVinculacionService;
import com.unam.mvmsys.servicio.comercial.ProveedorProductoService;
import com.unam.mvmsys.servicio.stock.ProductoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Comparator;

@Component
public class VincularProductoController implements Initializable {

    private final ProductoService productoService;
    private final ProveedorProductoService proveedorProductoService;
    private final ClienteProductoVinculacionService clienteProductoService;

    @FXML private Label lblTitulo;
    @FXML private Label lblSubtitulo;
    @FXML private TextField txtBusqueda;
    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, String> colObservacion;
    @FXML private TableColumn<Producto, Boolean> colSeleccionar;
    
    @FXML private VBox vboxCampo1;
    @FXML private Label lblCampo1;
    @FXML private TextField txtCampo1;
    @FXML private VBox vboxCampo2;
    @FXML private Label lblCampo2;
    @FXML private TextField txtCampo2;
    @FXML private Label lblProductoSeleccionado;

    private Persona entidad;
    private TipoVinculacion tipoVinculacion;
    private final Set<UUID> productosSeleccionados = new HashSet<>();
    private final Map<UUID, String> observacionesPorProducto = new HashMap<>();
    private final Map<UUID, BigDecimal> ultimoPrecioPorProducto = new HashMap<>();

    @Value("${mvmsys.precio-compra.estrategia:ULTIMA_COMPRA}")
    private String estrategiaPrecioConfig;

    private enum EstrategiaPrecioCompra {
        ULTIMA_COMPRA,
        PROMEDIO_PROVEEDORES,
        MAS_FRECUENTE
    }

    public enum TipoVinculacion {
        CLIENTE, PROVEEDOR
    }

    public VincularProductoController(ProductoService productoService,
                                     ProveedorProductoService proveedorProductoService,
                                     ClienteProductoVinculacionService clienteProductoService) {
        this.productoService = productoService;
        this.proveedorProductoService = proveedorProductoService;
        this.clienteProductoService = clienteProductoService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabla();
        txtBusqueda.textProperty().addListener((obs, oldVal, newVal) -> filtrarProductos(newVal));
        
        // Aplicar estilo compacto a las filas de la tabla
        tblProductos.setRowFactory(tv -> {
            TableRow<Producto> row = new TableRow<>();
            row.getStyleClass().add("table-row-cell-compact");
            return row;
        });
    }

    public void configurar(Persona entidad, TipoVinculacion tipo) {
        this.entidad = entidad;
        this.tipoVinculacion = tipo;

        // Resetear estado entre aperturas porque el controlador es singleton
        productosSeleccionados.clear();
        observacionesPorProducto.clear();
        ultimoPrecioPorProducto.clear();
        lblProductoSeleccionado.setText("");
        txtBusqueda.clear();
        txtCampo1.clear();
        txtCampo2.clear();
        vboxCampo1.setVisible(false);
        vboxCampo1.setManaged(false);
        vboxCampo2.setVisible(false);
        vboxCampo2.setManaged(false);

        String nombreEntidad = entidad.getRazonSocial();
        if (tipo == TipoVinculacion.PROVEEDOR) {
            lblTitulo.setText("Vincular Producto/Servicio a Proveedor");
            lblSubtitulo.setText(nombreEntidad);
            lblCampo1.setText("");
            lblCampo2.setText("");
            cargarPreciosHistoricos();
            preSeleccionarProductosVinculados();
        } else {
            lblTitulo.setText("Vincular Producto/Servicio a Cliente");
            lblSubtitulo.setText(nombreEntidad);
            lblCampo1.setText("");
            preSeleccionarProductosVinculados();
        }

        cargarProductos();
    }

    private void configurarTabla() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoSku"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getRubro() != null ? 
                cellData.getValue().getRubro().getNombre() : ""));

        // Observaciones por producto (columna editable)
        colObservacion.setCellFactory(col -> new TableCell<>() {
            private final TextField input = new TextField();

            {
                input.setPromptText("Observación...");
                input.setOnKeyReleased(event -> guardarObservacion());
                input.focusedProperty().addListener((obs, old, focused) -> {
                    if (!focused) {
                        guardarObservacion();
                    }
                });
            }

            private void guardarObservacion() {
                if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                    Producto producto = getTableView().getItems().get(getIndex());
                    observacionesPorProducto.put(producto.getId(), input.getText());
                }
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Producto producto = getTableView().getItems().get(getIndex());
                    input.setText(observacionesPorProducto.getOrDefault(producto.getId(), ""));
                    setGraphic(input);
                }
            }
        });
        
        // Casillero de selección múltiple estilo borde rojo y fondo blanco
        colSeleccionar.setCellFactory(col -> new TableCell<>() {
            private final CheckBox check = new CheckBox();

            {
                check.setText("");
                check.setStyle("-fx-padding: 6 10; -fx-background-color: white; -fx-border-color: #9CA3AF; " +
                    "-fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand; -fx-mark-color: #16A34A;");
                check.setOnAction(event -> {
                    Producto producto = getTableView().getItems().get(getIndex());
                    if (check.isSelected()) {
                        productosSeleccionados.add(producto.getId());
                    } else {
                        productosSeleccionados.remove(producto.getId());
                        observacionesPorProducto.remove(producto.getId());
                    }
                    actualizarEtiquetaSeleccion();
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Producto producto = getTableView().getItems().get(getIndex());
                    check.setSelected(productosSeleccionados.contains(producto.getId()));
                    setGraphic(check);
                }
            }
        });
    }

    private void cargarProductos() {
        try {
            List<Producto> productos = productoService.listarActivos();
            ObservableList<Producto> observableProductos = FXCollections.observableArrayList(productos);
            tblProductos.setItems(observableProductos);
            tblProductos.setFixedCellSize(36.0);
            
            // Placeholder personalizado
            Label placeholder = new Label("No hay productos disponibles para vincular.");
            placeholder.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px;");
            tblProductos.setPlaceholder(placeholder);
            
            // ajustarAlturaTabla(); // Comentado: dejamos que el layout se ajuste solo
        } catch (Exception e) {
            mostrarError("Error al cargar productos", "No se pudieron cargar los productos: " + e.getMessage());
        }
    }

    private void filtrarProductos(String filtro) {
        if (filtro == null || filtro.isEmpty()) {
            cargarProductos();
            return;
        }

        try {
            List<Producto> productos = productoService.listarActivos();
            String filtroLower = filtro.toLowerCase();
            ObservableList<Producto> filtrados = FXCollections.observableArrayList(
                productos.stream()
                    .filter(p -> p.getCodigoSku().toLowerCase().contains(filtroLower) ||
                            p.getNombre().toLowerCase().contains(filtroLower))
                    .toList()
            );
            tblProductos.setItems(filtrados);
            tblProductos.setFixedCellSize(36.0);
            // ajustarAlturaTabla(); // Comentado: dejamos que el layout se ajuste solo
        } catch (Exception e) {
            mostrarError("Error al filtrar", "No se pudieron filtrar los productos: " + e.getMessage());
        }
    }

    /**
     * Ajusta la altura de la tabla para que se adapte al contenido sin scrollbar.
     * Altura de encabezado: 25px
     * Altura de cada fila: 36px (con buen aire)
     */
    // Método comentado: Dejamos que el layout se ajuste automáticamente
    /*
    private void ajustarAlturaTabla() {
        // Header + (filas * 24px) + buffer
        double alturaHeader = 25;
        double alturaFila = 24;
        double alturaTotal = alturaHeader + (tblProductos.getItems().size() * alturaFila) + 4.0;
        
        // Límite máximo para que no se vea ridículo (máx 10 filas visible)
        double alturaMaxima = alturaHeader + (10 * alturaFila) + 9.0;
        double alturaFinal = Math.min(alturaTotal, alturaMaxima);
        
        tblProductos.setPrefHeight(alturaFinal);
        tblProductos.setMinHeight(alturaFinal);
        tblProductos.setMaxHeight(alturaFinal);
        tblProductos.refresh();
        tblProductos.setMinHeight(alturaFinal);
        tblProductos.setMaxHeight(alturaFinal);
    }
    */

    private void actualizarEtiquetaSeleccion() {
        int total = productosSeleccionados.size();
        if (total == 0) {
            lblProductoSeleccionado.setText("");
            return;
        }
        lblProductoSeleccionado.setText("Seleccionados: " + total + " producto(s)");
        lblProductoSeleccionado.setStyle("-fx-font-size: 12px; -fx-text-fill: #059669; -fx-font-weight: 600;");
    }

    @FXML
    public void accionBuscar() {
        String busqueda = txtBusqueda.getText();
        filtrarProductos(busqueda);
    }

    private void seleccionarProducto(Producto producto) {
        // Ya no se selecciona individualmente; se mantiene para compatibilidad si se requiere
    }

    @FXML
    public void accionVincular() {
        if (productosSeleccionados.isEmpty()) {
            mostrarError("Sin selección", "Selecciona al menos un producto/servicio para vincular.");
            return;
        }

        try {
            if (tipoVinculacion == TipoVinculacion.PROVEEDOR) {
                for (UUID productoId : productosSeleccionados) {
                    productoService.buscarPorId(productoId).ifPresent(prod -> {
                        BigDecimal precioCompra = ultimoPrecioPorProducto.get(productoId);
                        if (precioCompra == null) {
                            precioCompra = obtenerPrecioParaProveedor(prod);
                            ultimoPrecioPorProducto.put(productoId, precioCompra);
                        }
                        String observaciones = observacionesPorProducto.getOrDefault(productoId, "").trim();
                        ProveedorProducto vinculo = proveedorProductoService.vincular(entidad, prod, precioCompra, observaciones);
                        ultimoPrecioPorProducto.put(productoId, vinculo.getPrecioCompra());
                    });
                }
                mostrarExito("Productos vinculados", "Los productos seleccionados fueron vinculados al proveedor.");
                cerrarVentana();
            } else {
                for (UUID productoId : productosSeleccionados) {
                    productoService.buscarPorId(productoId).ifPresent(prod -> {
                        String observaciones = observacionesPorProducto.getOrDefault(productoId, "").trim();
                        clienteProductoService.vincular(entidad, prod, observaciones);
                    });
                }
                mostrarExito("Productos vinculados", "Los productos seleccionados fueron vinculados al cliente.");
                cerrarVentana();
            }
        } catch (NumberFormatException e) {
            mostrarError("Precio inválido", "El precio debe ser un número válido.");
        } catch (IllegalArgumentException e) {
            mostrarError("Error de validación", e.getMessage());
        } catch (Exception e) {
            mostrarError("Error al vincular", "No se pudo vincular el producto: " + e.getMessage());
        }
    }

    @FXML
    public void accionCancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) lblTitulo.getScene().getWindow();
        stage.close();
    }

    private void mostrarExito(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void cargarPreciosHistoricos() {
        try {
            ultimoPrecioPorProducto.clear();
            proveedorProductoService.obtenerProductosDelProveedor(entidad.getId()).forEach(pp ->
                ultimoPrecioPorProducto.put(pp.getProducto().getId(), pp.getPrecioCompra())
            );
        } catch (Exception e) {
            // No bloquear por errores al obtener precios históricos
        }
    }

    /**
     * Pre-selecciona los productos ya vinculados para que aparezcan tildados
     */
    private void preSeleccionarProductosVinculados() {
        try {
            if (tipoVinculacion == TipoVinculacion.PROVEEDOR) {
                proveedorProductoService.obtenerProductosDelProveedor(entidad.getId()).forEach(pp -> {
                    productosSeleccionados.add(pp.getProducto().getId());
                    if (pp.getObservaciones() != null && !pp.getObservaciones().isBlank()) {
                        observacionesPorProducto.put(pp.getProducto().getId(), pp.getObservaciones());
                    }
                });
            } else {
                clienteProductoService.obtenerProductosDelCliente(entidad.getId()).forEach(cp -> {
                    productosSeleccionados.add(cp.getProducto().getId());
                    if (cp.getObservaciones() != null && !cp.getObservaciones().isBlank()) {
                        observacionesPorProducto.put(cp.getProducto().getId(), cp.getObservaciones());
                    }
                });
            }
            actualizarEtiquetaSeleccion();
        } catch (Exception e) {
            // No bloquear si falla la pre-carga
        }
    }

    private BigDecimal obtenerPrecioParaProveedor(Producto producto) {
        EstrategiaPrecioCompra estrategia = parseEstrategia();
        List<ProveedorProducto> proveedores = proveedorProductoService.obtenerProveedoresDelProducto(producto.getId());
        if (proveedores == null || proveedores.isEmpty()) {
            return fallbackCosto(producto);
        }

        return switch (estrategia) {
            case ULTIMA_COMPRA -> proveedores.stream()
                .max(Comparator.comparing(ProveedorProducto::getFechaAlta))
                .map(ProveedorProducto::getPrecioCompra)
                .orElse(fallbackCosto(producto));
            case PROMEDIO_PROVEEDORES -> {
                BigDecimal suma = proveedores.stream()
                    .map(ProveedorProducto::getPrecioCompra)
                    .filter(p -> p != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                long count = proveedores.stream().map(ProveedorProducto::getPrecioCompra).filter(p -> p != null).count();
                yield count > 0 ? suma.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : fallbackCosto(producto);
            }
            case MAS_FRECUENTE -> proveedores.stream()
                .max(Comparator.comparing(ProveedorProducto::getFechaAlta))
                .map(ProveedorProducto::getPrecioCompra)
                .orElse(fallbackCosto(producto));
        };
    }

    private EstrategiaPrecioCompra parseEstrategia() {
        try {
            return EstrategiaPrecioCompra.valueOf(estrategiaPrecioConfig.toUpperCase());
        } catch (Exception ex) {
            return EstrategiaPrecioCompra.ULTIMA_COMPRA;
        }
    }

    private BigDecimal fallbackCosto(Producto producto) {
        BigDecimal costo = producto.getCostoReposicion();
        if (costo != null && costo.compareTo(BigDecimal.ZERO) > 0) {
            return costo;
        }
        BigDecimal base = producto.getPrecioVentaBase();
        return base != null ? base : BigDecimal.ZERO;
    }
}
