package com.unam.mvmsys.controller.comercial;

import com.unam.mvmsys.entidad.comercial.ClienteProducto;
import com.unam.mvmsys.entidad.configuracion.CategoriaCliente;
import com.unam.mvmsys.entidad.seguridad.Persona;
import com.unam.mvmsys.servicio.comercial.ClienteProductoVinculacionService;
import com.unam.mvmsys.servicio.configuracion.CategoriaClienteService;
import com.unam.mvmsys.servicio.seguridad.PersonaService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
public class ClienteController implements Initializable {

    private final PersonaService personaService;
    private final CategoriaClienteService categoriaClienteService;
    private final ApplicationContext applicationContext;

    // === CONTENEDORES ===
    @FXML private VBox viewLista;
    @FXML private VBox viewFormulario;

    // === LISTA ===
    @FXML private TableView<Persona> tblClientes;
    @FXML private TableColumn<Persona, String> colCuit;
    @FXML private TableColumn<Persona, String> colRazon;
    @FXML private TableColumn<Persona, String> colEmail;
    @FXML private TableColumn<Persona, String> colTelefono;
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
    @FXML private ComboBox<CategoriaCliente> cmbCategoria;
    @FXML private CheckBox chkEsProveedor;
    @FXML private TextField txtBusquedaProducto;
    @FXML private TableView<ClienteProducto> tblProductosVinculados;
    @FXML private TableColumn<ClienteProducto, String> colProducto;
    @FXML private TableColumn<ClienteProducto, String> colCantidad;
    @FXML private TableColumn<ClienteProducto, String> colPrecioEspecial;
    @FXML private TableColumn<ClienteProducto, String> colAccionesProducto;

    // === PAGÍNACÍON ===
    private static final int ITEMS_POR_PAGINA = 10;
    // private static final double ROW_HEIGHT = 32.0; // Ya no se usa - layout automático
    // private static final double HEADER_HEIGHT = 32.0; // Ya no se usa - layout automático
    private int paginaActual = 1;
    private final List<Persona> clientesBase = new ArrayList<>();
    private final List<Persona> clientesFiltrados = new ArrayList<>();
    private final ObservableList<Persona> listaTabla = FXCollections.observableArrayList();
    private final ObservableList<ClienteProducto> productosBaseCliente = FXCollections.observableArrayList();
    private final ObservableList<ClienteProducto> productosFiltradosCliente = FXCollections.observableArrayList();

    private Persona clienteSeleccionado;

    private final ClienteProductoVinculacionService clienteProductoService;

    public ClienteController(PersonaService personaService, CategoriaClienteService categoriaClienteService,
                           ClienteProductoVinculacionService clienteProductoService,
                           ApplicationContext applicationContext) {
        this.personaService = personaService;
        this.categoriaClienteService = categoriaClienteService;
        this.clienteProductoService = clienteProductoService;
        this.applicationContext = applicationContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabla();
        configurarComboCategoria();
        configurarTablaProductos();
        cargarClientes();
        configurarBusqueda();
        actualizarPaginacion();
        mostrarVistaLista();
    }

    private void configurarComboCategoria() {
        cmbCategoria.setItems(FXCollections.observableArrayList(categoriaClienteService.listarActivas()));
        cmbCategoria.setPromptText("Seleccionar categoría...");
        cmbCategoria.setConverter(new javafx.util.StringConverter<CategoriaCliente>() {
            @Override
            public String toString(CategoriaCliente cat) {
                return cat != null ? cat.getDisplay() : "";
            }

            @Override
            public CategoriaCliente fromString(String string) {
                return null;
            }
        });
    }

    private void configurarTablaProductos() {
        colProducto.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getProducto().getNombre()
        ));
        colCantidad.setText("Observación");
        colCantidad.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getObservaciones() != null ? data.getValue().getObservaciones() : "-"
        ));
        colPrecioEspecial.setText("Última compra");
        colPrecioEspecial.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getFechaUltimaCompra() != null
                ? data.getValue().getFechaUltimaCompra().toLocalDate().toString()
                : "-"
        ));

        colAccionesProducto.setCellFactory(col -> new TableCell<>() {
            private final Button btnEliminar = new Button("🗑");

            {
                btnEliminar.setStyle("-fx-font-size: 13px; -fx-padding: 5px 8px; " +
                        "-fx-background-color: #991b1b; -fx-text-fill: white; " +
                        "-fx-font-weight: 600; -fx-border-radius: 3; -fx-cursor: hand;");
                btnEliminar.setOnAction(e -> {
                    ClienteProducto cp = getTableView().getItems().get(getIndex());
                    confirmarDesvincular(cp);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnEliminar);
            }
        });

        tblProductosVinculados.setItems(productosFiltradosCliente);

        if (txtBusquedaProducto != null) {
            txtBusquedaProducto.textProperty().addListener((obs, old, val) -> filtrarProductos(val));
        }
    }

    private void configurarTabla() {
        colCuit.setCellValueFactory(new PropertyValueFactory<>("cuitDni"));
        colRazon.setCellValueFactory(new PropertyValueFactory<>("razonSocial"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));

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

        tblClientes.setItems(listaTabla);
        tblClientes.setFixedCellSize(36.0);
        
        // Placeholder personalizado
        Label placeholder = new Label("No hay clientes registrados o coincidentes.");
        placeholder.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 14px;");
        tblClientes.setPlaceholder(placeholder);
    }

    private void configurarBusqueda() {
        if (txtBusqueda != null) {
            txtBusqueda.textProperty().addListener((obs, old, val) -> aplicarFiltro(val));
        }
    }

    @FXML
    public void accionNuevo() {
        clienteSeleccionado = null;
        limpiarFormulario();
        txtCuit.setDisable(false);
        mostrarVistaFormulario("Nuevo Cliente");
    }

    @FXML
    public void accionGuardar() {
        try {
            if (txtCuit.getText().isBlank() || txtRazonSocial.getText().isBlank()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validación", "CUIT y Razón Social son obligatorios.");
                return;
            }

            if (clienteSeleccionado == null) {
                Persona nuevo = Persona.builder()
                        .cuitDni(txtCuit.getText())
                        .razonSocial(txtRazonSocial.getText())
                        .email(txtEmail.getText())
                        .telefono(txtTelefono.getText())
                        .direccionCalle(txtDireccion.getText())
                        .categoriaCliente(cmbCategoria.getValue())
                        .esCliente(true)
                        .esProveedor(chkEsProveedor.isSelected())
                        .esEmpleado(false)
                        .build();
                personaService.crearPersona(nuevo);
            } else {
                clienteSeleccionado.setRazonSocial(txtRazonSocial.getText());
                clienteSeleccionado.setEmail(txtEmail.getText());
                clienteSeleccionado.setTelefono(txtTelefono.getText());
                clienteSeleccionado.setDireccionCalle(txtDireccion.getText());
                clienteSeleccionado.setCategoriaCliente(cmbCategoria.getValue());
                clienteSeleccionado.setEsCliente(true);
                clienteSeleccionado.setEsProveedor(chkEsProveedor.isSelected());
                personaService.actualizarPersona(clienteSeleccionado);
            }

            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Cliente guardado correctamente.");
            cargarClientes();
            mostrarVistaLista();
        } catch (IllegalArgumentException ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", ex.getMessage());
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo guardar el cliente.");
            ex.printStackTrace();
        }
    }

    private void cargarClientes() {
        clientesBase.clear();
        clientesBase.addAll(personaService.listarClientes());
        aplicarFiltro(txtBusqueda == null ? "" : txtBusqueda.getText());
        paginaActual = 1;
        actualizarPaginacion();
    }

    private void aplicarFiltro(String filtro) {
        String f = filtro == null ? "" : filtro.trim().toLowerCase();
        clientesFiltrados.clear();
        for (Persona p : clientesBase) {
            if (f.isEmpty() ||
                contiene(p.getCuitDni(), f) ||
                contiene(p.getRazonSocial(), f) ||
                contiene(p.getEmail(), f)) {
                clientesFiltrados.add(p);
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

        if (clientesFiltrados.isEmpty()) {
            listaTabla.clear();
            return;
        }

        int totalPaginas = (int) Math.ceil((double) clientesFiltrados.size() / ITEMS_POR_PAGINA);
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
        int fin = Math.min(inicio + ITEMS_POR_PAGINA, clientesFiltrados.size());
        listaTabla.addAll(clientesFiltrados.subList(inicio, fin));
        // ajustarAlturaTabla(tblClientes, listaTabla.size()); // Comentado: dejamos que el layout se ajuste solo
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
        clienteSeleccionado = p;
        txtCuit.setText(p.getCuitDni());
        txtCuit.setDisable(true);
        txtRazonSocial.setText(p.getRazonSocial());
        txtEmail.setText(p.getEmail());
        txtTelefono.setText(p.getTelefono());
        txtDireccion.setText(p.getDireccionCalle());
        cmbCategoria.setValue(p.getCategoriaCliente());
        chkEsProveedor.setSelected(p.isEsProveedor());
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
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Cliente dado de baja.");
                cargarClientes();
            } catch (Exception e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
    }

    @FXML
    public void accionGestionarCategoria() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/gestionCategoriaCliente.fxml")
            );
            loader.setControllerFactory(applicationContext::getBean);
            
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Gestionar Categorías de Cliente");
            stage.setScene(scene);
            stage.setWidth(700);
            stage.setHeight(500);
            stage.showAndWait();
            
            // Recargar ComboBox después de cerrar
            cmbCategoria.setItems(FXCollections.observableArrayList(categoriaClienteService.listarActivas()));
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo abrir la ventana de gestión de categorías.");
            ex.printStackTrace();
        }
    }

    @FXML
    public void accionAgregarProducto() {
        if (clienteSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleccionar Cliente", 
                "Debe guardar el cliente primero para vincular productos/servicios.");
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
            
            // Configurar el controlador con los datos del cliente
            VincularProductoController controller = loader.getController();
            controller.configurar(clienteSeleccionado, VincularProductoController.TipoVinculacion.CLIENTE);
            
            stage.showAndWait();
            
            cargarProductosVinculados();
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo abrir la ventana de vinculación.");
            ex.printStackTrace();
        }
    }

    private void filtrarProductos(String filtro) {
        String f = filtro == null ? "" : filtro.trim().toLowerCase();
        productosFiltradosCliente.clear();
        for (ClienteProducto cp : productosBaseCliente) {
            String nombre = cp.getProducto().getNombre() != null ? cp.getProducto().getNombre().toLowerCase() : "";
            String codigo = cp.getProducto().getCodigoSku() != null ? cp.getProducto().getCodigoSku().toLowerCase() : "";
            if (f.isEmpty() || nombre.contains(f) || codigo.contains(f)) {
                productosFiltradosCliente.add(cp);
            }
        }
        // ajustarAlturaTabla(tblProductosVinculados, productosFiltradosCliente.size()); // Comentado
    }

    private void cargarProductosVinculados() {
        if (clienteSeleccionado == null) {
            productosBaseCliente.clear();
            productosFiltradosCliente.clear();
            return;
        }
        productosBaseCliente.setAll(
            clienteProductoService.obtenerProductosDelCliente(clienteSeleccionado.getId())
        );
        filtrarProductos(txtBusquedaProducto == null ? "" : txtBusquedaProducto.getText());
    }

    private void confirmarDesvincular(ClienteProducto cp) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Quitar producto");
        confirm.setHeaderText("¿Quitar " + cp.getProducto().getNombre() + " del cliente?");
        confirm.setContentText("La relación quedará inactiva.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            clienteProductoService.desVincular(cp.getCliente().getId(), cp.getProducto().getId());
            cargarProductosVinculados();
        }
    }

    private void limpiarFormulario() {
        txtCuit.clear();
        txtRazonSocial.clear();
        txtEmail.clear();
        txtTelefono.clear();
        txtDireccion.clear();
        cmbCategoria.setValue(null);
        chkEsProveedor.setSelected(false);
        productosBaseCliente.clear();
        productosFiltradosCliente.clear();
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

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
