package com.unam.mvmsys.controller.stock;

import com.unam.mvmsys.entidad.configuracion.TipoProducto;
import com.unam.mvmsys.entidad.configuracion.UnidadMedida;
import com.unam.mvmsys.entidad.configuracion.Estado;
import com.unam.mvmsys.entidad.stock.*;
import com.unam.mvmsys.repositorio.configuracion.TipoProductoRepository;
import com.unam.mvmsys.repositorio.configuracion.UnidadMedidaRepository;
import com.unam.mvmsys.repositorio.configuracion.EstadoRepository;
import com.unam.mvmsys.servicio.stock.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class GestorStockController implements Initializable {

    private final ProductoService productoService;
    private final UnidadMedidaRepository unidadRepo;
    private final TipoProductoRepository tipoRepo;
    private final EstadoRepository estadoRepo;
    private final MovimientoStockService movimientoService;
    private final DetalleMovimientoService detalleService;
    private final ExistenciaService existenciaService;
    private final DepositoService depositoService;
    private final LoteService loteService;

    // === CONTENEDORES DE VISTAS ===
    @FXML private VBox viewLista;
    @FXML private VBox viewFormulario;

    // === VISTA LISTA: TABLA DE INVENTARIO ===
    @FXML private TableView<Producto> tblProductosStock;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombreProducto;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, String> colUnitario;
    @FXML private TableColumn<Producto, Void> colStockActual; // Con lápiz para ajuste
    @FXML private TableColumn<Producto, String> colStockMinimo;
    @FXML private TableColumn<Producto, String> colCostoUnitario;
    @FXML private TableColumn<Producto, String> colPrecioVenta;
    @FXML private TableColumn<Producto, Void> colAccionesProducto;
    @FXML private TextField txtBusquedaProducto;
    @FXML private HBox paginationBoxProductos;

    // === VISTA FORMULARIO: ALTA/MODIFICACIÓN PRODUCTO ===
    @FXML private Label lblTituloFormulario;
    @FXML private TextField txtSku;
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private ComboBox<UnidadMedida> cbUnidad;
    @FXML private ComboBox<TipoProducto> cbTipo;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtCosto;
    @FXML private TextField txtStockMin;

    // === PAGINACIÓN ===
    private static final int ITEMS_POR_PAGINA = 10;
    private int paginaActual = 1;
    private List<Producto> todosLosProductos = new ArrayList<>();
    private final ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    
    private Producto productoSeleccionado = null;

    public GestorStockController(ProductoService productoService,
                                  UnidadMedidaRepository unidadRepo,
                                  TipoProductoRepository tipoRepo,
                                  EstadoRepository estadoRepo,
                                  MovimientoStockService movimientoService,
                                  DetalleMovimientoService detalleService,
                                  ExistenciaService existenciaService,
                                  DepositoService depositoService,
                                  LoteService loteService) {
        this.productoService = productoService;
        this.unidadRepo = unidadRepo;
        this.tipoRepo = tipoRepo;
        this.estadoRepo = estadoRepo;
        this.movimientoService = movimientoService;
        this.detalleService = detalleService;
        this.existenciaService = existenciaService;
        this.depositoService = depositoService;
        this.loteService = loteService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabla();
        cargarCombos();
        cargarProductos();
        actualizarPaginacion();
        mostrarVistaLista();
    }

    // =======================================================
    // LÓGICA DE NAVEGACIÓN
    // =======================================================

    private void mostrarVistaLista() {
        if (viewLista != null) {
            viewLista.setVisible(true);
            viewLista.setManaged(true);
        }
        if (viewFormulario != null) {
            viewFormulario.setVisible(false);
            viewFormulario.setManaged(false);
        }
        limpiarFormulario();
    }

    private void mostrarVistaFormulario(String titulo) {
        if (lblTituloFormulario != null) {
            lblTituloFormulario.setText(titulo);
        }
        if (viewLista != null) {
            viewLista.setVisible(false);
            viewLista.setManaged(false);
        }
        if (viewFormulario != null) {
            viewFormulario.setVisible(true);
            viewFormulario.setManaged(true);
        }
    }

    @FXML
    public void accionCancelar() {
        mostrarVistaLista();
        cargarProductos();
    }

    // =======================================================
    // ACCIONES DE LA LISTA
    // =======================================================

    @FXML
    public void accionNuevo() {
        productoSeleccionado = null;
        limpiarFormulario();
        if (txtSku != null) txtSku.setDisable(false);
        mostrarVistaFormulario("Nuevo Producto");
    }

    // =======================================================
    // ACCIÓN GUARDAR
    // =======================================================

    @FXML
    public void accionGuardar() {
        try {
            // Validaciones
            if (txtSku.getText().isEmpty() || txtNombre.getText().isEmpty() || 
                cbUnidad.getValue() == null || cbTipo.getValue() == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Datos Incompletos", 
                    "SKU, Nombre, Unidad y Tipo son obligatorios.");
                return;
            }

            BigDecimal precio = parseMoneda(txtPrecio.getText());
            BigDecimal costo = parseMoneda(txtCosto.getText());
            BigDecimal stockMin = parseMoneda(txtStockMin.getText());

            if (productoSeleccionado == null) {
                // CREAR
                Producto nuevo = Producto.builder()
                        .codigoSku(txtSku.getText())
                        .nombre(txtNombre.getText())
                        .descripcion(txtDescripcion.getText())
                        .unidadMedida(cbUnidad.getValue())
                        .tipoProducto(cbTipo.getValue())
                        .precioVentaBase(precio)
                        .costoReposicion(costo)
                        .stockMinimo(stockMin)
                        .build();
                productoService.crearProducto(nuevo);
            } else {
                // ACTUALIZAR
                productoSeleccionado.setNombre(txtNombre.getText());
                productoSeleccionado.setDescripcion(txtDescripcion.getText());
                productoSeleccionado.setUnidadMedida(cbUnidad.getValue());
                productoSeleccionado.setTipoProducto(cbTipo.getValue());
                productoSeleccionado.setPrecioVentaBase(precio);
                productoSeleccionado.setCostoReposicion(costo);
                productoSeleccionado.setStockMinimo(stockMin);
                productoService.actualizarProducto(productoSeleccionado);
            }

            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto guardado correctamente.");
            cargarProductos();
            mostrarVistaLista();

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Formato Numérico", "Verifique precios y stock.");
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Regla de Negocio", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error Crítico", e.getMessage());
            e.printStackTrace();
        }
    }

    // =======================================================
    // CONFIGURACIÓN TABLA Y BOTONES INTERNOS
    // =======================================================

    private void configurarTabla() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoSku"));
        colNombreProducto.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getRubro() != null ? cell.getValue().getRubro().getNombre() : "N/A"
        ));
        colUnitario.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getUnidadMedida() != null ? cell.getValue().getUnidadMedida().getCodigo() : "un"
        ));

        // === COLUMNA STOCK ACTUAL con lápiz para ajuste manual ===
        colStockActual.setCellFactory(col -> new TableCell<Producto, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Producto producto = getTableView().getItems().get(getIndex());
                    
                    HBox hbox = new HBox(8);
                    hbox.setAlignment(javafx.geometry.Pos.CENTER);
                    
                    // Obtener stock actual
                    BigDecimal stockActual = obtenerStockActual(producto);
                    String unidad = producto.getUnidadMedida() != null ? 
                        producto.getUnidadMedida().getCodigo() : "un";
                    
                    // Badge de estado
                    Label lblEstado = new Label();
                    if (stockActual.compareTo(BigDecimal.ZERO) == 0) {
                        lblEstado.setText("⚠️");
                        lblEstado.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 14px;");
                    } else if (stockActual.compareTo(producto.getStockMinimo()) < 0) {
                        lblEstado.setText("⚠️");
                        lblEstado.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 14px;");
                    } else {
                        lblEstado.setText("✓");
                        lblEstado.setStyle("-fx-text-fill: #10b981; -fx-font-size: 14px;");
                    }
                    
                    Label lblCantidad = new Label(String.format("%.2f %s", stockActual, unidad));
                    lblCantidad.setStyle("-fx-font-weight: 600; -fx-font-size: 11px;");
                    
                    Button btnEditar = new Button("✎");
                    btnEditar.setStyle("-fx-font-size: 13px; -fx-padding: 5px 8px; " +
                            "-fx-background-color: #6B7280; -fx-text-fill: white; " +
                            "-fx-font-weight: 600; -fx-border-radius: 3; -fx-cursor: hand;");
                    btnEditar.setOnAction(e -> abrirDialogoAjustarStock(producto));
                    
                    hbox.getChildren().addAll(lblEstado, lblCantidad, btnEditar);
                    setGraphic(hbox);
                }
            }
        });

        colStockMinimo.setCellValueFactory(cell -> new SimpleStringProperty(
            String.format("%.2f", cell.getValue().getStockMinimo())
        ));
        colCostoUnitario.setCellValueFactory(cell -> new SimpleStringProperty(
            String.format("$%.2f", cell.getValue().getCostoReposicion())
        ));
        colPrecioVenta.setCellValueFactory(cell -> new SimpleStringProperty(
            String.format("$%.2f", cell.getValue().getPrecioVentaBase())
        ));

        // === COLUMNA DE ACCIONES (Editar y Eliminar producto) ===
        colAccionesProducto.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("✎");
            private final Button btnEliminar = new Button("🗑");
            private final HBox container = new HBox(8, btnEditar, btnEliminar);

            {
                btnEditar.setStyle("-fx-font-size: 13px; -fx-padding: 5px 8px; " +
                        "-fx-background-color: #6B7280; -fx-text-fill: white; " +
                        "-fx-font-weight: 600; -fx-border-radius: 3; -fx-cursor: hand;");
                btnEliminar.setStyle("-fx-font-size: 13px; -fx-padding: 5px 8px; " +
                        "-fx-background-color: #991b1b; -fx-text-fill: white; " +
                        "-fx-font-weight: 600; -fx-border-radius: 3; -fx-cursor: hand;");
                container.setStyle("-fx-alignment: CENTER;");

                btnEditar.setOnAction(e -> {
                    Producto p = getTableView().getItems().get(getIndex());
                    cargarProductoEnFormulario(p);
                });

                btnEliminar.setOnAction(e -> {
                    Producto p = getTableView().getItems().get(getIndex());
                    confirmarEliminacion(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        tblProductosStock.setItems(listaProductos);
        tblProductosStock.setFixedCellSize(36.0);
        
        Label placeholder = new Label("No hay productos registrados o coincidentes.");
        placeholder.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 14px;");
        tblProductosStock.setPlaceholder(placeholder);
    }

    // Métodos auxiliares para las acciones de la tabla
    private void cargarProductoEnFormulario(Producto p) {
        this.productoSeleccionado = p;
        txtSku.setText(p.getCodigoSku());
        txtSku.setDisable(true); // Bloquear SKU al editar
        txtNombre.setText(p.getNombre());
        txtDescripcion.setText(p.getDescripcion());
        cbUnidad.setValue(p.getUnidadMedida());
        cbTipo.setValue(p.getTipoProducto());
        txtPrecio.setText(p.getPrecioVentaBase().toString());
        txtCosto.setText(p.getCostoReposicion().toString());
        txtStockMin.setText(p.getStockMinimo().toString());
        
        mostrarVistaFormulario("Modificando: " + p.getCodigoSku());
    }

    private void confirmarEliminacion(Producto p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Baja");
        confirm.setHeaderText("¿Eliminar " + p.getNombre() + "?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                productoService.eliminarProducto(p.getId());
                cargarProductos();
            } catch (Exception e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
    }

    // =======================================================
    // UTILIDADES
    // =======================================================

    private void cargarCombos() {
        cbUnidad.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(UnidadMedida u) { return u == null ? "" : u.getNombre(); }
            @Override public UnidadMedida fromString(String s) { return null; }
        });
        cbTipo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(TipoProducto t) { return t == null ? "" : t.getNombre(); }
            @Override public TipoProducto fromString(String s) { return null; }
        });
        cbUnidad.getItems().setAll(unidadRepo.findAll());
        cbTipo.getItems().setAll(tipoRepo.findAll());
    }

    private void cargarProductos() {
        todosLosProductos.clear();
        todosLosProductos.addAll(productoService.listarActivos());
        paginaActual = 1;
        actualizarPaginacion();
    }

    private void actualizarPaginacion() {
        paginationBoxProductos.getChildren().clear();
        
        if (todosLosProductos.isEmpty()) {
            listaProductos.clear();
            return;
        }
        
        int totalPaginas = (int) Math.ceil((double) todosLosProductos.size() / ITEMS_POR_PAGINA);
        
        // Solo mostrar paginación si hay más de una página
        if (totalPaginas <= 1) {
            mostrarPaginaActual();
            return;
        }
        
        // Botones de navegación (Primera y Anterior)
        Button btnPrimera = crearBotonNavegacion("❮❮", e -> irAPagina(1, totalPaginas));
        Button btnAnterior = crearBotonNavegacion("❮", e -> irAPagina(paginaActual - 1, totalPaginas));
        paginationBoxProductos.getChildren().addAll(btnPrimera, btnAnterior);
        
        // Números de página
        int inicio = Math.max(1, paginaActual - 1);
        int fin = Math.min(totalPaginas, paginaActual + 1);
        
        if (inicio > 1) {
            Button btn1 = crearBotonPaginacion("1", e -> irAPagina(1, totalPaginas), false);
            paginationBoxProductos.getChildren().add(btn1);
            
            if (inicio > 2) {
                Label puntos = new Label("···");
                puntos.getStyleClass().add("pagination-dots");
                paginationBoxProductos.getChildren().add(puntos);
            }
        }
        
        for (int i = inicio; i <= fin; i++) {
            final int pagina = i;
            boolean esActual = (i == paginaActual);
            Button btn = crearBotonPaginacion(String.valueOf(i), e -> irAPagina(pagina, totalPaginas), esActual);
            paginationBoxProductos.getChildren().add(btn);
        }
        
        if (fin < totalPaginas) {
            if (fin < totalPaginas - 1) {
                Label puntos = new Label("···");
                puntos.getStyleClass().add("pagination-dots");
                paginationBoxProductos.getChildren().add(puntos);
            }
            
            Button btnUltima = crearBotonPaginacion(String.valueOf(totalPaginas), e -> irAPagina(totalPaginas, totalPaginas), false);
            paginationBoxProductos.getChildren().add(btnUltima);
        }
        
        // Botones de navegación (Siguiente y Última)
        Button btnSiguiente = crearBotonNavegacion("❯", e -> irAPagina(paginaActual + 1, totalPaginas));
        Button btnUltimaPag = crearBotonNavegacion("❯❯", e -> irAPagina(totalPaginas, totalPaginas));
        paginationBoxProductos.getChildren().addAll(btnSiguiente, btnUltimaPag);
        
        mostrarPaginaActual();
    }
    
    private Button crearBotonPaginacion(String texto, javafx.event.EventHandler<javafx.event.ActionEvent> accion, boolean esActivo) {
        Button btn = new Button(texto);
        btn.getStyleClass().add(esActivo ? "btn-pagination-active" : "btn-pagination");
        btn.setPrefSize(32, 32);
        btn.setMinSize(32, 32);
        btn.setPadding(Insets.EMPTY);
        btn.setOnAction(accion);
        return btn;
    }
    
    private Button crearBotonNavegacion(String texto, javafx.event.EventHandler<javafx.event.ActionEvent> accion) {
        Button btn = new Button(texto);
        btn.getStyleClass().add("btn-pagination-nav");
        btn.setPrefSize(32, 32);
        btn.setMinSize(32, 32);
        btn.setPadding(Insets.EMPTY);
        btn.setOnAction(accion);
        return btn;
    }
    
    private void irAPagina(int pagina, int totalPaginas) {
        if (pagina >= 1 && pagina <= totalPaginas) {
            paginaActual = pagina;
            actualizarPaginacion();
        }
    }
    
    private void mostrarPaginaActual() {
        listaProductos.clear();
        int inicio = (paginaActual - 1) * ITEMS_POR_PAGINA;
        int fin = Math.min(inicio + ITEMS_POR_PAGINA, todosLosProductos.size());
        listaProductos.addAll(todosLosProductos.subList(inicio, fin));
    }

    private void limpiarFormulario() {
        if (txtSku != null) txtSku.clear();
        if (txtNombre != null) txtNombre.clear();
        if (txtDescripcion != null) txtDescripcion.clear();
        if (txtPrecio != null) txtPrecio.setText("0.00");
        if (txtCosto != null) txtCosto.setText("0.00");
        if (txtStockMin != null) txtStockMin.setText("0");
        if (cbUnidad != null) cbUnidad.getSelectionModel().clearSelection();
        if (cbTipo != null) cbTipo.getSelectionModel().clearSelection();
    }

    private BigDecimal parseMoneda(String texto) {
        if (texto == null || texto.trim().isEmpty()) return BigDecimal.ZERO;
        return new BigDecimal(texto.replace(",", "."));
    }

    // =======================================================
    // STOCK: OBTENER STOCK ACTUAL Y AJUSTES MANUALES
    // =======================================================

    private BigDecimal obtenerStockActual(Producto producto) {
        try {
            // Obtener todos los lotes del producto
            List<Lote> lotes = loteService.listarPorProducto(producto.getId());
            BigDecimal total = BigDecimal.ZERO;
            
            // Sumar existencias de todos los lotes en todos los depósitos
            for (Lote lote : lotes) {
                List<Existencia> existencias = existenciaService.listarPorLote(lote.getId());
                for (Existencia existencia : existencias) {
                    total = total.add(existencia.getCantidad());
                }
            }
            
            return total;
        } catch (Exception e) {
            System.err.println("Error obteniendo stock: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private void abrirDialogoAjustarStock(Producto producto) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ajuste Manual de Stock");
        
        // Contenedor principal con estilo
        VBox mainContainer = new VBox(20);
        mainContainer.setStyle("-fx-background-color: #F9FAFB; -fx-padding: 25;");
        mainContainer.setPrefWidth(550);

        // Header
        VBox header = new VBox(5);
        Label lblTitulo = new Label("Ajuste Manual de Stock");
        lblTitulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #991b1b;");
        
        Label lblProducto = new Label("Producto: " + producto.getNombre());
        lblProducto.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151; -fx-font-weight: 600;");
        
        header.getChildren().addAll(lblTitulo, lblProducto);

        // Card con información actual
        VBox cardActual = new VBox(10);
        cardActual.setStyle("-fx-background-color: white; -fx-padding: 15; " +
                "-fx-background-radius: 6; -fx-border-color: #E5E7EB; " +
                "-fx-border-width: 1; -fx-border-radius: 6;");
        
        BigDecimal stockActual = obtenerStockActual(producto);
        String unidad = producto.getUnidadMedida() != null ? producto.getUnidadMedida().getCodigo() : "un";
        
        Label lblStockActual = new Label("Stock Actual:");
        lblStockActual.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        
        Label lblStockValor = new Label(String.format("%.2f %s", stockActual, unidad));
        lblStockValor.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        
        cardActual.getChildren().addAll(lblStockActual, lblStockValor);

        // Formulario
        VBox formulario = new VBox(15);
        formulario.setStyle("-fx-background-color: white; -fx-padding: 20; " +
                "-fx-background-radius: 6; -fx-border-color: #E5E7EB; " +
                "-fx-border-width: 1; -fx-border-radius: 6;");

        // Campo Cantidad
        VBox vboxCantidad = new VBox(8);
        Label lblCantidad = new Label("Cantidad a Ajustar *");
        lblCantidad.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #374151;");
        
        TextField txtCantidad = new TextField();
        txtCantidad.setPromptText("Ej: 10 (entrada) o -5 (salida)");
        txtCantidad.setPrefHeight(40);
        txtCantidad.setStyle("-fx-font-size: 13px; -fx-background-radius: 4; -fx-border-radius: 4;");
        
        Label lblHint = new Label("💡 Use números positivos para agregar stock y negativos para restar");
        lblHint.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
        
        vboxCantidad.getChildren().addAll(lblCantidad, txtCantidad, lblHint);

        // Campo Motivo
        VBox vboxMotivo = new VBox(8);
        Label lblMotivo = new Label("Motivo del Ajuste *");
        lblMotivo.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #374151;");
        
        ComboBox<String> cbMotivo = new ComboBox<>();
        cbMotivo.setItems(FXCollections.observableArrayList(
                "Ajuste por inventario físico",
                "Merma o pérdida",
                "Devolución de cliente",
                "Corrección de error de carga",
                "Producto vencido o dañado",
                "Otros"
        ));
        cbMotivo.setPromptText("Seleccione un motivo...");
        cbMotivo.setPrefHeight(40);
        cbMotivo.setMaxWidth(Double.MAX_VALUE);
        cbMotivo.setStyle("-fx-font-size: 13px;");
        
        vboxMotivo.getChildren().addAll(lblMotivo, cbMotivo);

        // Campo Depósito (solo si hay depósitos creados)
        VBox vboxDeposito = new VBox(8);
        List<Deposito> depositos = depositoService.listarActivos();
        ComboBox<Deposito> cbDeposito = new ComboBox<>();
        
        if (!depositos.isEmpty()) {
            Label lblDeposito = new Label("Depósito *");
            lblDeposito.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #374151;");
            
            cbDeposito.setItems(FXCollections.observableArrayList(depositos));
            cbDeposito.setConverter(new javafx.util.StringConverter<>() {
                @Override public String toString(Deposito d) { return d == null ? "" : d.getNombre(); }
                @Override public Deposito fromString(String s) { return null; }
            });
            cbDeposito.setPrefHeight(40);
            cbDeposito.setMaxWidth(Double.MAX_VALUE);
            cbDeposito.setStyle("-fx-font-size: 13px;");
            cbDeposito.getSelectionModel().select(0);
            
            vboxDeposito.getChildren().addAll(lblDeposito, cbDeposito);
        }

        // Campo Observaciones
        VBox vboxObservaciones = new VBox(8);
        Label lblObservaciones = new Label("Observaciones / Justificación");
        lblObservaciones.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #374151;");
        
        TextArea txtObservaciones = new TextArea();
        txtObservaciones.setPromptText("Describa el motivo detallado del ajuste...");
        txtObservaciones.setPrefRowCount(3);
        txtObservaciones.setWrapText(true);
        txtObservaciones.setStyle("-fx-font-size: 13px; -fx-background-radius: 4; -fx-border-radius: 4;");
        
        vboxObservaciones.getChildren().addAll(lblObservaciones, txtObservaciones);

        // Agregar campos al formulario
        formulario.getChildren().addAll(vboxCantidad, vboxMotivo);
        if (!depositos.isEmpty()) {
            formulario.getChildren().add(vboxDeposito);
        }
        formulario.getChildren().add(vboxObservaciones);

        // Agregar todo al contenedor principal
        mainContainer.getChildren().addAll(header, cardActual, formulario);

        dialog.getDialogPane().setContent(mainContainer);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Personalizar botones
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Registrar Ajuste");
        okButton.setStyle("-fx-background-color: #991b1b; -fx-text-fill: white; " +
                "-fx-font-weight: 600; -fx-font-size: 13px; -fx-padding: 10 20; " +
                "-fx-background-radius: 4;");
        
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setText("Cancelar");
        cancelButton.setStyle("-fx-background-color: white; -fx-text-fill: #374151; " +
                "-fx-font-weight: 600; -fx-font-size: 13px; -fx-padding: 10 20; " +
                "-fx-border-color: #D1D5DB; -fx-border-width: 1.5; " +
                "-fx-background-radius: 4; -fx-border-radius: 4;");

        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                String cantidadStr = txtCantidad.getText().trim();
                String motivo = cbMotivo.getValue();
                String observaciones = txtObservaciones.getText();

                // Validaciones
                if (cantidadStr.isEmpty()) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Campo Requerido", 
                        "Debe ingresar una cantidad para el ajuste");
                    event.consume();
                    return;
                }

                if (motivo == null || motivo.isEmpty()) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Campo Requerido", 
                        "Debe seleccionar un motivo para el ajuste");
                    event.consume();
                    return;
                }

                Deposito depositoSeleccionado = cbDeposito.getValue();
                if (!depositos.isEmpty() && depositoSeleccionado == null) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Campo Requerido", 
                        "Debe seleccionar un depósito");
                    event.consume();
                    return;
                }

                BigDecimal cantidad = new BigDecimal(cantidadStr);
                BigDecimal nuevoStock = stockActual.add(cantidad);

                if (nuevoStock.compareTo(BigDecimal.ZERO) < 0) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Stock Insuficiente", 
                        "El ajuste resultaría en stock negativo.\n" +
                        "Stock actual: " + String.format("%.2f", stockActual) + "\n" +
                        "Ajuste solicitado: " + cantidad + "\n" +
                        "Stock resultante: " + String.format("%.2f", nuevoStock));
                    event.consume();
                    return;
                }

                // REGISTRAR EL MOVIMIENTO EN LA BASE DE DATOS
                registrarMovimientoAjuste(producto, cantidad, motivo, observaciones, depositoSeleccionado);

                mostrarAlerta(Alert.AlertType.INFORMATION, "✓ Ajuste Registrado", 
                    "El ajuste de stock se registró correctamente.\n\n" +
                    "📦 Producto: " + producto.getNombre() + "\n" +
                    "📊 Stock anterior: " + String.format("%.2f %s", stockActual, unidad) + "\n" +
                    "📈 Ajuste: " + (cantidad.compareTo(BigDecimal.ZERO) > 0 ? "+" : "") + 
                        String.format("%.2f", cantidad) + "\n" +
                    "📊 Stock nuevo: " + String.format("%.2f %s", nuevoStock, unidad));

                cargarProductos(); // Recargar tabla

            } catch (NumberFormatException ex) {
                mostrarAlerta(Alert.AlertType.ERROR, "Formato Inválido", 
                    "La cantidad ingresada no es un número válido.\n" +
                    "Ingrese solo números, use punto (.) para decimales.");
                event.consume();
            } catch (Exception ex) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error al Registrar", 
                    "Ocurrió un error al registrar el ajuste:\n" + ex.getMessage());
                event.consume();
                ex.printStackTrace();
            }
        });

        dialog.showAndWait();
    }

    private void registrarMovimientoAjuste(Producto producto, BigDecimal cantidad, 
                                           String motivo, String observaciones, Deposito deposito) {
        try {
            // Si no hay depósito, obtener o crear uno por defecto
            if (deposito == null) {
                List<Deposito> depositosActivos = depositoService.listarActivos();
                if (depositosActivos.isEmpty()) {
                    // Crear depósito por defecto
                    deposito = Deposito.builder()
                            .nombre("Principal")
                            .direccion("Depósito principal")
                            .esPropio(true)
                            .activo(true)
                            .build();
                    deposito = depositoService.crearDeposito(deposito);
                } else {
                    deposito = depositosActivos.get(0);
                }
            }
            
            // Crear MovimientoStock
            MovimientoStock movimiento = MovimientoStock.builder()
                    .fecha(LocalDateTime.now())
                    .tipoMovimiento("AJUSTE")
                    .concepto(motivo)
                    .depositoOrigen(deposito)
                    .depositoDestino(deposito)
                    .referenciaComprobante("AJUSTE-" + System.currentTimeMillis())
                    .observaciones(observaciones)
                    .build();
            
            movimiento = movimientoService.crearMovimiento(movimiento);

            // Buscar o crear lote genérico
            String codigoLote = "LOTE-" + producto.getCodigoSku();
            Lote lote = loteService.buscarPorCodigo(codigoLote)
                    .orElseGet(() -> {
                        // Buscar estado "Disponible" o el primero disponible
                        Estado estadoDisponible = estadoRepo.findAll().stream()
                                .filter(e -> e.getNombre().equalsIgnoreCase("Disponible") || 
                                           e.getNombre().equalsIgnoreCase("Activo"))
                                .findFirst()
                                .orElseGet(() -> estadoRepo.findAll().stream()
                                        .findFirst()
                                        .orElseThrow(() -> new RuntimeException("No hay estados configurados en el sistema")));
                        
                        Lote nuevoLote = Lote.builder()
                                .codigo(codigoLote)
                                .producto(producto)
                                .estado(estadoDisponible)
                                .build();
                        return loteService.crearLote(nuevoLote);
                    });

            // Crear DetalleMovimiento
            DetalleMovimiento detalle = DetalleMovimiento.builder()
                    .movimiento(movimiento)
                    .lote(lote)
                    .cantidad(cantidad.abs())
                    .build();
            detalleService.crear(detalle);

            // Actualizar o crear Existencia
            Optional<Existencia> existenciaOpt = existenciaService.buscarPorLoteYDeposito(lote.getId(), deposito.getId());
            if (existenciaOpt.isPresent()) {
                Existencia existencia = existenciaOpt.get();
                BigDecimal nuevaCantidad = existencia.getCantidad().add(cantidad);
                existencia.setCantidad(nuevaCantidad);
                existenciaService.actualizar(existencia);
            } else {
                // Crear nueva existencia
                Existencia nuevaExistencia = Existencia.builder()
                        .lote(lote)
                        .deposito(deposito)
                        .cantidad(cantidad)
                        .build();
                existenciaService.crear(nuevaExistencia);
            }

            System.out.println("Ajuste registrado: " + producto.getNombre() + " - Cantidad: " + cantidad);

        } catch (Exception e) {
            throw new RuntimeException("Error al registrar ajuste: " + e.getMessage(), e);
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
