package com.unam.mvmsys.controller.comercial;

import com.unam.mvmsys.entidad.comercial.ProveedorProducto;
import com.unam.mvmsys.entidad.seguridad.Persona;
import com.unam.mvmsys.entidad.stock.Rubro;
import com.unam.mvmsys.servicio.comercial.ProveedorProductoService;
import com.unam.mvmsys.servicio.seguridad.PersonaService;
import com.unam.mvmsys.servicio.stock.RubroService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
public class ProveedorController implements Initializable {

    private final PersonaService personaService;
    private final RubroService rubroService;
    private final ApplicationContext applicationContext;

    // === CONTENEDORES ===
    @FXML private VBox viewLista;
    @FXML private VBox viewFormulario;

    // === LISTA ===
    @FXML private TableView<Persona> tblProveedores;
    @FXML private TableColumn<Persona, String> colCuit;
    @FXML private TableColumn<Persona, String> colRazon;
    @FXML private TableColumn<Persona, String> colRubro;
    @FXML private TableColumn<Persona, String> colPlazo;
    @FXML private TableColumn<Persona, String> colBonif;
    @FXML private TableColumn<Persona, String> colInteres;
    @FXML private TableColumn<Persona, Void> colAcciones;
    @FXML private TextField txtBusqueda;
    @FXML private HBox paginationBox;

    // === FORMULARIO ===
    @FXML private Label lblTituloFormulario;
    @FXML private TextField txtCuit;
    @FXML private TextField txtRazonSocial;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDireccion;
    @FXML private ComboBox<Rubro> cmbRubro;
    @FXML private TextField txtPlazoPago;
    @FXML private TextField txtBonificacion;
    @FXML private TextField txtInteres;
    @FXML private CheckBox chkEsCliente;
    @FXML private TextField txtBusquedaProducto;
    @FXML private TableView<ProveedorProducto> tblProductosVinculados;
    @FXML private TableColumn<ProveedorProducto, String> colProducto;
    @FXML private TableColumn<ProveedorProducto, String> colPrecioCompra;
    @FXML private TableColumn<ProveedorProducto, String> colTiempoEntrega;
    @FXML private TableColumn<ProveedorProducto, String> colAccionesProducto;

    // === PAGÍNACÍON ===
    private static final int ITEMS_POR_PAGINA = 10;
    // private static final double ROW_HEIGHT = 32.0; // Ya no se usa - layout automático
    // private static final double HEADER_HEIGHT = 32.0; // Ya no se usa - layout automático
    private int paginaActual = 1;
    private final List<Persona> proveedoresBase = new ArrayList<>();
    private final List<Persona> proveedoresFiltrados = new ArrayList<>();
    private final ObservableList<Persona> listaTabla = FXCollections.observableArrayList();
    private final ObservableList<ProveedorProducto> productosBaseProveedor = FXCollections.observableArrayList();
    private final ObservableList<ProveedorProducto> productosFiltradosProveedor = FXCollections.observableArrayList();

    private Persona proveedorSeleccionado;
    private final ObservableList<Rubro> rubrosDisponibles = FXCollections.observableArrayList();

    private final ProveedorProductoService proveedorProductoService;

    public ProveedorController(PersonaService personaService, RubroService rubroService,
                             ProveedorProductoService proveedorProductoService,
                             ApplicationContext applicationContext) {
        this.personaService = personaService;
        this.rubroService = rubroService;
        this.proveedorProductoService = proveedorProductoService;
        this.applicationContext = applicationContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabla();
        configurarComboRubro();
        configurarTablaProductos();
        cargarProveedores();
        configurarBusqueda();
        actualizarPaginacion();
        mostrarVistaLista();
    }

    private void configurarTabla() {
        colCuit.setCellValueFactory(new PropertyValueFactory<>("cuitDni"));
        colRazon.setCellValueFactory(new PropertyValueFactory<>("razonSocial"));
        colRubro.setCellValueFactory(p -> new SimpleStringProperty(
            p.getValue().getRubro() != null ? p.getValue().getRubro().getNombre() : "-"
        ));
        colPlazo.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getPlazoPagoDias() + " días"));
        colBonif.setCellValueFactory(p -> new SimpleStringProperty(formatearPorcentaje(p.getValue().getBonificacionPorcentaje())));
        colInteres.setCellValueFactory(p -> new SimpleStringProperty(formatearPorcentaje(p.getValue().getInteresMoraPorcentaje())));

        colAcciones.setCellFactory(param -> new TableCell<>() {
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
                    Persona p = getTableView().getItems().get(getIndex());
                    cargarEnFormulario(p);
                });

                btnEliminar.setOnAction(e -> {
                    Persona p = getTableView().getItems().get(getIndex());
                    confirmarEliminacion(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        tblProveedores.setItems(listaTabla);
        tblProveedores.setFixedCellSize(36.0);
        
        // Placeholder personalizado
        Label placeholder = new Label("No hay proveedores registrados o coincidentes.");
        placeholder.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 14px;");
        tblProveedores.setPlaceholder(placeholder);
    }

    private void configurarComboRubro() {
        // Configurar StringConverter para mostrar nombre del rubro
        cmbRubro.setConverter(new StringConverter<Rubro>() {
            @Override
            public String toString(Rubro rubro) {
                return rubro != null ? rubro.getNombre() : "";
            }

            @Override
            public Rubro fromString(String string) {
                // Permite entrada manual para crear nuevo rubro
                return null;
            }
        });

        // Hacer el ComboBox editable para permitir crear nuevos rubros
        cmbRubro.setEditable(true);
        cmbRubro.setPromptText("Seleccionar o escribir nuevo rubro...");

        // Cargar rubros existentes
        cargarRubros();
    }

    private void configurarTablaProductos() {
        colProducto.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getProducto().getNombre()
        ));
        colPrecioCompra.setCellValueFactory(data -> new SimpleStringProperty(
            formatearMoneda(data.getValue().getPrecioCompra())
        ));
        colTiempoEntrega.setText("Observación");
        colTiempoEntrega.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getObservaciones() != null ? data.getValue().getObservaciones() : "-"
        ));

        colAccionesProducto.setCellFactory(col -> new TableCell<>() {
            private final Button btnEliminar = new Button("🗑");

            {
                btnEliminar.setStyle("-fx-font-size: 13px; -fx-padding: 5px 8px; " +
                        "-fx-background-color: #991b1b; -fx-text-fill: white; " +
                        "-fx-font-weight: 600; -fx-border-radius: 3; -fx-cursor: hand;");
                btnEliminar.setOnAction(e -> {
                    ProveedorProducto pp = getTableView().getItems().get(getIndex());
                    confirmarDesvincular(pp);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnEliminar);
            }
        });

        tblProductosVinculados.setItems(productosFiltradosProveedor);

        if (txtBusquedaProducto != null) {
            txtBusquedaProducto.textProperty().addListener((obs, old, val) -> filtrarProductos(val));
        }
    }

    private void cargarRubros() {
        rubrosDisponibles.clear();
        rubrosDisponibles.addAll(rubroService.listarTodos());
        cmbRubro.setItems(rubrosDisponibles);
    }

    private void configurarBusqueda() {
        if (txtBusqueda != null) {
            txtBusqueda.textProperty().addListener((obs, old, val) -> aplicarFiltro(val));
        }
    }

    @FXML
    public void accionNuevo() {
        proveedorSeleccionado = null;
        limpiarFormulario();
        txtCuit.setDisable(false);
        mostrarVistaFormulario("Nuevo Proveedor");
    }

    @FXML
    public void accionGuardar() {
        try {
            if (txtCuit.getText().isBlank() || txtRazonSocial.getText().isBlank()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validación", "CUIT y Razón Social son obligatorios.");
                return;
            }

            int plazo = parseEntero(txtPlazoPago.getText());
            BigDecimal bonif = parseDecimal(txtBonificacion.getText());
            BigDecimal interes = parseDecimal(txtInteres.getText());

            // Obtener o crear rubro
            Rubro rubroSeleccionado = obtenerOCrearRubro();

            if (proveedorSeleccionado == null) {
                Persona nuevo = Persona.builder()
                        .cuitDni(txtCuit.getText())
                        .razonSocial(txtRazonSocial.getText())
                        .email(txtEmail.getText())
                        .telefono(txtTelefono.getText())
                        .direccionCalle(txtDireccion.getText())
                        .rubro(rubroSeleccionado)
                        .plazoPagoDias(plazo)
                        .bonificacionPorcentaje(bonif)
                        .interesMoraPorcentaje(interes)
                        .esProveedor(true)
                        .esCliente(chkEsCliente.isSelected())
                        .esEmpleado(false)
                        .build();
                personaService.crearPersona(nuevo);
            } else {
                proveedorSeleccionado.setRazonSocial(txtRazonSocial.getText());
                proveedorSeleccionado.setEmail(txtEmail.getText());
                proveedorSeleccionado.setTelefono(txtTelefono.getText());
                proveedorSeleccionado.setDireccionCalle(txtDireccion.getText());
                proveedorSeleccionado.setRubro(rubroSeleccionado);
                proveedorSeleccionado.setPlazoPagoDias(plazo);
                proveedorSeleccionado.setBonificacionPorcentaje(bonif);
                proveedorSeleccionado.setInteresMoraPorcentaje(interes);
                proveedorSeleccionado.setEsProveedor(true);
                proveedorSeleccionado.setEsCliente(chkEsCliente.isSelected());
                personaService.actualizarPersona(proveedorSeleccionado);
            }

            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Proveedor guardado correctamente.");
            cargarRubros(); // Recargar rubros por si se creó uno nuevo
            cargarProveedores();
            mostrarVistaLista();
        } catch (IllegalArgumentException ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", ex.getMessage());
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo guardar el proveedor.");
            ex.printStackTrace();
        }
    }

    private void cargarProveedores() {
        proveedoresBase.clear();
        proveedoresBase.addAll(personaService.listarProveedores());
        aplicarFiltro(txtBusqueda == null ? "" : txtBusqueda.getText());
        paginaActual = 1;
        actualizarPaginacion();
    }

    private void aplicarFiltro(String filtro) {
        String f = filtro == null ? "" : filtro.trim().toLowerCase();
        proveedoresFiltrados.clear();
        for (Persona p : proveedoresBase) {
            String rubroNombre = p.getRubro() != null ? p.getRubro().getNombre() : "";
            if (f.isEmpty() ||
                contiene(p.getCuitDni(), f) ||
                contiene(p.getRazonSocial(), f) ||
                contiene(rubroNombre, f) ||
                contiene(p.getEmail(), f)) {
                proveedoresFiltrados.add(p);
            }
        }
        paginaActual = 1;
        actualizarPaginacion();
    }

    private boolean contiene(String valor, String filtro) {
        return valor != null && valor.toLowerCase().contains(filtro);
    }

    private void actualizarPaginacion() {
        paginationBox.getChildren().clear();

        if (proveedoresFiltrados.isEmpty()) {
            listaTabla.clear();
            return;
        }

        int totalPaginas = (int) Math.ceil((double) proveedoresFiltrados.size() / ITEMS_POR_PAGINA);
        if (totalPaginas <= 1) {
            mostrarPaginaActual();
            return;
        }

        // Botones de navegación (Primera y Anterior)
        Button btnPrimera = crearBotonNavegacion("❮❮", e -> irAPagina(1, totalPaginas));
        Button btnAnterior = crearBotonNavegacion("❮", e -> irAPagina(paginaActual - 1, totalPaginas));
        paginationBox.getChildren().addAll(btnPrimera, btnAnterior);

        int inicio = Math.max(1, paginaActual - 1);
        int fin = Math.min(totalPaginas, paginaActual + 1);

        if (inicio > 1) {
            paginationBox.getChildren().add(crearBotonPaginacion("1", e -> irAPagina(1, totalPaginas), false));
            if (inicio > 2) {
                Label puntos = new Label("···");
                puntos.getStyleClass().add("pagination-dots");
                paginationBox.getChildren().add(puntos);
            }
        }

        for (int i = inicio; i <= fin; i++) {
            final int pagina = i;
            boolean esActual = (i == paginaActual);
            Button btn = crearBotonPaginacion(String.valueOf(i), e -> irAPagina(pagina, totalPaginas), esActual);
            paginationBox.getChildren().add(btn);
        }

        if (fin < totalPaginas) {
            if (fin < totalPaginas - 1) {
                Label puntos = new Label("···");
                puntos.getStyleClass().add("pagination-dots");
                paginationBox.getChildren().add(puntos);
            }
            paginationBox.getChildren().add(crearBotonPaginacion(String.valueOf(totalPaginas), e -> irAPagina(totalPaginas, totalPaginas), false));
        }

        // Botones de navegación (Siguiente y Última)
        Button btnSiguiente = crearBotonNavegacion("❯", e -> irAPagina(paginaActual + 1, totalPaginas));
        Button btnUltima = crearBotonNavegacion("❯❯", e -> irAPagina(totalPaginas, totalPaginas));
        paginationBox.getChildren().addAll(btnSiguiente, btnUltima);

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
        listaTabla.clear();
        int inicio = (paginaActual - 1) * ITEMS_POR_PAGINA;
        int fin = Math.min(inicio + ITEMS_POR_PAGINA, proveedoresFiltrados.size());
        listaTabla.addAll(proveedoresFiltrados.subList(inicio, fin));
        // ajustarAlturaTabla(tblProveedores, listaTabla.size()); // Comentado: dejamos que el layout se ajuste solo
    }

    // Método comentado: Dejamos que el layout se ajuste automáticamente
    /*
    private void ajustarAlturaTabla(TableView<?> tabla, int filas) {
        tabla.setFixedCellSize(ROW_HEIGHT);
        // Agregar +4px de buffer para evitar scroll fantasma por redondeo
        double altura = HEADER_HEIGHT + Math.max(1, filas) * ROW_HEIGHT + 4.0;
        tabla.setPrefHeight(altura);
        tabla.setMinHeight(altura);
        tabla.setMaxHeight(altura);
        tabla.refresh();
    }
    */

    private void cargarEnFormulario(Persona p) {
        proveedorSeleccionado = p;
        txtCuit.setText(p.getCuitDni());
        txtCuit.setDisable(true);
        txtRazonSocial.setText(p.getRazonSocial());
        txtEmail.setText(p.getEmail());
        txtTelefono.setText(p.getTelefono());
        txtDireccion.setText(p.getDireccionCalle());
        cmbRubro.setValue(p.getRubro());
        txtPlazoPago.setText(String.valueOf(p.getPlazoPagoDias()));
        txtBonificacion.setText(p.getBonificacionPorcentaje() == null ? "0" : p.getBonificacionPorcentaje().toPlainString());
        txtInteres.setText(p.getInteresMoraPorcentaje() == null ? "0" : p.getInteresMoraPorcentaje().toPlainString());
        chkEsCliente.setSelected(p.isEsCliente());
        cargarProductosVinculados();
        mostrarVistaFormulario("Modificando: " + p.getRazonSocial());
    }

    private void confirmarEliminacion(Persona p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Baja");
        confirm.setHeaderText("¿Dar de baja a " + p.getRazonSocial() + "?");
        confirm.setContentText("El registro quedará inactivo.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                personaService.eliminarPersona(p.getId());
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Proveedor dado de baja.");
                cargarProveedores();
            } catch (Exception e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
    }

    private void limpiarFormulario() {
        txtCuit.clear();
        txtRazonSocial.clear();
        txtEmail.clear();
        txtTelefono.clear();
        txtDireccion.clear();
        cmbRubro.setValue(null);
        cmbRubro.getEditor().clear();
        txtPlazoPago.setText("0");
        txtBonificacion.setText("0");
        txtInteres.setText("0");
        chkEsCliente.setSelected(false);
        productosBaseProveedor.clear();
        productosFiltradosProveedor.clear();
    }

    @FXML
    public void mostrarVistaLista() {
        viewLista.setVisible(true);
        viewFormulario.setVisible(false);
        limpiarFormulario();
    }

    private void mostrarVistaFormulario(String titulo) {
        lblTituloFormulario.setText(titulo);
        viewLista.setVisible(false);
        viewFormulario.setVisible(true);
    }

    private int parseEntero(String valor) {
        try {
            return valor == null || valor.isBlank() ? 0 : Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El plazo de pago debe ser un número entero.");
        }
    }

    private BigDecimal parseDecimal(String valor) {
        try {
            return valor == null || valor.isBlank()
                    ? BigDecimal.ZERO
                    : new BigDecimal(valor.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Los porcentajes deben ser numéricos.");
        }
    }

    private String formatearPorcentaje(BigDecimal valor) {
        BigDecimal v = valor == null ? BigDecimal.ZERO : valor;
        return String.format("%.2f%%", v);
    }

    @FXML
    public void accionVincularProductos() {
        if (proveedorSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleccionar Proveedor", 
                "Debe seleccionar un proveedor primero.");
            return;
        }
        
        // Por ahora, mostrar un mensaje indicando que esta funcionalidad se implementará
        mostrarAlerta(Alert.AlertType.INFORMATION, "Vincular Productos", 
            "Funcionalidad en desarrollo.\nProveedor: " + proveedorSeleccionado.getRazonSocial());
    }

    @FXML
    public void accionAgregarProducto() {
        if (proveedorSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleccionar Proveedor", 
                "Debe guardar el proveedor primero para vincular productos/servicios.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/vincularProducto.fxml")
            );
            loader.setControllerFactory(applicationContext::getBean);
            
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Vincular Producto/Servicio");
            stage.setScene(scene);
            
            // Configurar el controlador con los datos del proveedor
            VincularProductoController controller = loader.getController();
            controller.configurar(proveedorSeleccionado, VincularProductoController.TipoVinculacion.PROVEEDOR);
            
            stage.showAndWait();
            
            cargarProductosVinculados();
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo abrir la ventana de vinculación.");
            ex.printStackTrace();
        }
    }

    private void filtrarProductos(String filtro) {
        String f = filtro == null ? "" : filtro.trim().toLowerCase();
        productosFiltradosProveedor.clear();
        for (ProveedorProducto pp : productosBaseProveedor) {
            String nombre = pp.getProducto().getNombre() != null ? pp.getProducto().getNombre().toLowerCase() : "";
            String codigo = pp.getProducto().getCodigoSku() != null ? pp.getProducto().getCodigoSku().toLowerCase() : "";
            if (f.isEmpty() || nombre.contains(f) || codigo.contains(f)) {
                productosFiltradosProveedor.add(pp);
            }
        }
        // ajustarAlturaTabla(tblProductosVinculados, productosFiltradosProveedor.size()); // Comentado
    }

    private void cargarProductosVinculados() {
        if (proveedorSeleccionado == null) {
            productosBaseProveedor.clear();
            productosFiltradosProveedor.clear();
            return;
        }
        productosBaseProveedor.setAll(
            proveedorProductoService.obtenerProductosDelProveedor(proveedorSeleccionado.getId())
        );
        filtrarProductos(txtBusquedaProducto == null ? "" : txtBusquedaProducto.getText());
    }

    private String formatearMoneda(BigDecimal valor) {
        BigDecimal v = valor == null ? BigDecimal.ZERO : valor;
        return String.format("$%.2f", v);
    }

    private void confirmarDesvincular(ProveedorProducto pp) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Quitar producto");
        confirm.setHeaderText("¿Quitar " + pp.getProducto().getNombre() + " del proveedor?");
        confirm.setContentText("La relación quedará inactiva.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            proveedorProductoService.desVincular(pp.getProveedor().getId(), pp.getProducto().getId());
            cargarProductosVinculados();
        }
    }

    private Rubro obtenerOCrearRubro() {
        Rubro rubroSeleccionado = cmbRubro.getValue();
        
        // Si se seleccionó un rubro de la lista, usarlo directamente
        if (rubroSeleccionado != null) {
            return rubroSeleccionado;
        }
        
        // Si el usuario escribió texto en el ComboBox editable
        String textoRubro = cmbRubro.getEditor().getText();
        if (textoRubro != null && !textoRubro.isBlank()) {
            // Verificar si ya existe
            Optional<Rubro> existente = rubroService.buscarPorNombre(textoRubro);
            if (existente.isPresent()) {
                return existente.get();
            }
            
            // Crear nuevo rubro
            try {
                Rubro nuevoRubro = rubroService.crearRubro(textoRubro);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Rubro creado", 
                    "Se creó el nuevo rubro: " + nuevoRubro.getNombre());
                return nuevoRubro;
            } catch (IllegalArgumentException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
                throw e;
            }
        }
        
        // Si no hay rubro, retornar null (es opcional)
        return null;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
