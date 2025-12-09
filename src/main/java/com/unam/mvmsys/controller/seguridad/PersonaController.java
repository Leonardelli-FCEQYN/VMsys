package com.unam.mvmsys.controller.seguridad;

import com.unam.mvmsys.entidad.seguridad.Persona;
import com.unam.mvmsys.servicio.seguridad.PersonaService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

@Component
public class PersonaController implements Initializable {

    private final PersonaService personaService;

    // === CONTENEDORES DE VISTAS ===
    @FXML private VBox viewLista;
    @FXML private VBox viewFormulario;

    // === VISTA 1: LISTA ===
    @FXML private TableView<Persona> tblPersonas;
    @FXML private TableColumn<Persona, String> colCuit;
    @FXML private TableColumn<Persona, String> colRazonSocial;
    @FXML private TableColumn<Persona, String> colEmail;
    @FXML private TableColumn<Persona, String> colRol;
    @FXML private TableColumn<Persona, Void> colAcciones;
    @FXML private TextField txtBusqueda;
    @FXML private HBox paginationBox;

    // === VISTA 2: FORMULARIO ===
    @FXML private Label lblTituloFormulario;
    @FXML private TextField txtCuit;
    @FXML private TextField txtRazonSocial;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDireccion;
    @FXML private CheckBox chkCliente;
    @FXML private CheckBox chkProveedor;

    // === PAGÍNACÍON ===
    private static final int ITEMS_POR_PAGINA = 10;
    // private static final double ROW_HEIGHT = 32.0; // Ya no se usa - layout automático
    // private static final double HEADER_HEIGHT = 32.0; // Ya no se usa - layout automático
    private int paginaActual = 1;
    private List<Persona> todasLasPersonas = new ArrayList<>();
    private final ObservableList<Persona> listaPersonas = FXCollections.observableArrayList();
    
    private Persona personaSeleccionada = null;

    public PersonaController(PersonaService personaService) {
        this.personaService = personaService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabla();
        cargarPersonas();
        actualizarPaginacion();
        mostrarVistaLista(); 
    }

    // =======================================================
    // LOGICA DE NAVEGACIÓN
    // =======================================================

    private void mostrarVistaLista() {
        viewLista.setVisible(true);
        viewFormulario.setVisible(false);
        limpiarFormulario(); 
    }

    private void mostrarVistaFormulario(String titulo) {
        lblTituloFormulario.setText(titulo);
        viewLista.setVisible(false);
        viewFormulario.setVisible(true);
    }

    @FXML
    public void accionCancelar() {
        mostrarVistaLista();
    }

    // =======================================================
    // ACCIONES (Solo Nuevo, el resto va en tabla)
    // =======================================================

    @FXML
    public void accionNuevo() {
        personaSeleccionada = null;
        limpiarFormulario();
        txtCuit.setDisable(false); 
        mostrarVistaFormulario("Nueva Persona");
    }

    // =======================================================
    // LOGICA DE GUARDADO
    // =======================================================

    @FXML
    public void accionGuardar() {
        try {
            // Validaciones
            if (txtCuit.getText().isEmpty() || txtRazonSocial.getText().isEmpty()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validación", "CUIT y Razón Social son obligatorios.");
                return;
            }
            if (!chkCliente.isSelected() && !chkProveedor.isSelected()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Seleccione al menos un Rol.");
                return;
            }

            if (!validarCuitDni(txtCuit.getText())) {
                mostrarAlerta(Alert.AlertType.WARNING, "Formato Incorrecto", "El CUIT/DNI no es válido.");
                return;
            }

            if (!txtTelefono.getText().isEmpty() && !validarTelefono(txtTelefono.getText())) {
                mostrarAlerta(Alert.AlertType.WARNING, "Formato Incorrecto", "Teléfono inválido (use números, espacios, +, -).");
                return;
            }

            if (personaSeleccionada == null) {
                // CREAR
                Persona nueva = Persona.builder()
                        .cuitDni(txtCuit.getText())
                        .razonSocial(txtRazonSocial.getText())
                        .email(txtEmail.getText())
                        .telefono(txtTelefono.getText())
                        .direccionCalle(txtDireccion.getText())
                        .esCliente(chkCliente.isSelected())
                        .esProveedor(chkProveedor.isSelected())
                        .esEmpleado(false)
                        .build();
                personaService.crearPersona(nueva);
            } else {
                // ACTUALIZAR
                personaSeleccionada.setRazonSocial(txtRazonSocial.getText());
                personaSeleccionada.setEmail(txtEmail.getText());
                personaSeleccionada.setTelefono(txtTelefono.getText());
                personaSeleccionada.setDireccionCalle(txtDireccion.getText());
                personaSeleccionada.setEsCliente(chkCliente.isSelected());
                personaSeleccionada.setEsProveedor(chkProveedor.isSelected());
                personaService.actualizarPersona(personaSeleccionada);
            }

            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Datos guardados correctamente.");
            cargarPersonas();
            mostrarVistaLista(); 

        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Negocio", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error Crítico", e.getMessage());
            e.printStackTrace();
        }
    }

    // =======================================================
    // CONFIGURACIÓN DE TABLA (Con Botones Internos)
    // =======================================================

    private void configurarTabla() {
        colCuit.setCellValueFactory(new PropertyValueFactory<>("cuitDni"));
        colRazonSocial.setCellValueFactory(new PropertyValueFactory<>("razonSocial"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        // Mostrar teléfono en lugar del rol para mejor visualización
        colRol.setCellValueFactory(cell -> {
            Persona p = cell.getValue();
            String telefono = p.getTelefono() != null ? p.getTelefono() : "-";
            return new SimpleStringProperty(telefono);
        });

        // === COLUMNA DE ACCIONES ===
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
                    cargarPersonaEnFormulario(p);
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

        tblPersonas.setItems(listaPersonas);
        
        // Placeholder personalizado
        Label placeholder = new Label("No hay personas registradas o coincidentes.");
        placeholder.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 14px;");
        tblPersonas.setPlaceholder(placeholder);
    }

    // Métodos auxiliares para las acciones de la tabla
    private void cargarPersonaEnFormulario(Persona p) {
        this.personaSeleccionada = p;
        txtCuit.setText(p.getCuitDni());
        txtCuit.setDisable(true); // Bloquear CUIT
        txtRazonSocial.setText(p.getRazonSocial());
        txtEmail.setText(p.getEmail());
        txtTelefono.setText(p.getTelefono());
        txtDireccion.setText(p.getDireccionCalle());
        chkCliente.setSelected(p.isEsCliente());
        chkProveedor.setSelected(p.isEsProveedor());

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
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Persona dada de baja.");
                cargarPersonas();
            } catch (Exception e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
    }

    // =======================================================
    // UTILIDADES
    // =======================================================

    private void cargarPersonas() {
        todasLasPersonas.clear();
        todasLasPersonas.addAll(personaService.listarTodas());
        paginaActual = 1;
        actualizarPaginacion();
    }

    private void actualizarPaginacion() {
        paginationBox.getChildren().clear();
        
        if (todasLasPersonas.isEmpty()) {
            listaPersonas.clear();
            return;
        }
        
        int totalPaginas = (int) Math.ceil((double) todasLasPersonas.size() / ITEMS_POR_PAGINA);
        
        // Solo mostrar paginación si hay más de una página
        if (totalPaginas <= 1) {
            mostrarPaginaActual();
            return;
        }
        
        // Botón Primera Página
        Button btnPrimera = crearBotonPaginacion("<<", e -> irAPagina(1, totalPaginas));
        paginationBox.getChildren().add(btnPrimera);
        
        // Botón Página Anterior
        Button btnAnterior = crearBotonPaginacion("<", e -> irAPagina(paginaActual - 1, totalPaginas));
        paginationBox.getChildren().add(btnAnterior);
        
        // Números de página
        int inicio = Math.max(1, paginaActual - 1);
        int fin = Math.min(totalPaginas, paginaActual + 1);
        
        if (inicio > 1) {
            Button btn1 = crearBotonPaginacion("1", e -> irAPagina(1, totalPaginas));
            paginationBox.getChildren().add(btn1);
            
            if (inicio > 2) {
                Label puntos = new Label("...");
                puntos.setStyle("-fx-text-fill: #9CA3AF; -fx-padding: 8px 4px;");
                paginationBox.getChildren().add(puntos);
            }
        }
        
        for (int i = inicio; i <= fin; i++) {
            final int pagina = i;
            Button btn = crearBotonPaginacion(String.valueOf(i), e -> irAPagina(pagina, totalPaginas));
            if (i == paginaActual) {
                btn.setStyle("-fx-font-size: 14px; -fx-padding: 8px 12px; " +
                        "-fx-border-color: #DC2626; -fx-border-width: 2; -fx-border-radius: 4; " +
                        "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; " +
                        "-fx-font-weight: bold; -fx-cursor: hand;");
            }
            paginationBox.getChildren().add(btn);
        }
        
        if (fin < totalPaginas) {
            if (fin < totalPaginas - 1) {
                Label puntos = new Label("...");
                puntos.setStyle("-fx-text-fill: #9CA3AF; -fx-padding: 8px 4px;");
                paginationBox.getChildren().add(puntos);
            }
            
            Button btnUltima = crearBotonPaginacion(String.valueOf(totalPaginas), e -> irAPagina(totalPaginas, totalPaginas));
            paginationBox.getChildren().add(btnUltima);
        }
        
        // Botón Página Siguiente
        Button btnSiguiente = crearBotonPaginacion(">", e -> irAPagina(paginaActual + 1, totalPaginas));
        paginationBox.getChildren().add(btnSiguiente);
        
        // Botón Última Página
        Button btnUltimaPag = crearBotonPaginacion(">>", e -> irAPagina(totalPaginas, totalPaginas));
        paginationBox.getChildren().add(btnUltimaPag);
        
        mostrarPaginaActual();
    }
    
    private Button crearBotonPaginacion(String texto, javafx.event.EventHandler<javafx.event.ActionEvent> accion) {
        Button btn = new Button(texto);
        btn.setStyle("-fx-font-size: 12px; -fx-padding: 8px 10px; " +
                "-fx-border-color: #D1D5DB; -fx-border-width: 1; -fx-border-radius: 4; " +
                "-fx-background-color: #FFFFFF; -fx-text-fill: #374151; -fx-cursor: hand;");
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
        listaPersonas.clear();
        int inicio = (paginaActual - 1) * ITEMS_POR_PAGINA;
        int fin = Math.min(inicio + ITEMS_POR_PAGINA, todasLasPersonas.size());
        listaPersonas.addAll(todasLasPersonas.subList(inicio, fin));

        // ajustarAlturaTabla(tblPersonas, listaPersonas.size()); // Comentado: dejamos que el layout se ajuste solo
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

    private void limpiarFormulario() {
        txtCuit.clear();
        txtRazonSocial.clear();
        txtEmail.clear();
        txtTelefono.clear();
        txtDireccion.clear();
        chkCliente.setSelected(false);
        chkProveedor.setSelected(false);
    }

    private boolean validarTelefono(String telefono) {
        String regex = "^[0-9+\\-\\s()]+$";
        return Pattern.matches(regex, telefono);
    }

    private boolean validarCuitDni(String cuit) {
        String regex = "^\\d{7,8}$|^\\d{11}$|^\\d{2}-\\d{8}-\\d{1}$";
        return Pattern.matches(regex, cuit);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}