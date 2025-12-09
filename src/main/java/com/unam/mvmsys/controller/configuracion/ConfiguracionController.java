package com.unam.mvmsys.controller.configuracion;

import com.unam.mvmsys.entidad.configuracion.CategoriaCliente;
import com.unam.mvmsys.servicio.configuracion.CategoriaClienteService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
public class ConfiguracionController implements Initializable {

    private final CategoriaClienteService categoriaClienteService;

    // === TABS ===
    @FXML private TabPane tabPane;
    @FXML private Tab tabCategorias;

    // === CATEGORÍAS CLIENTE ===
    @FXML private VBox vboxCategorias;
    
    // Lista de categorías
    @FXML private TableView<CategoriaCliente> tblCategorias;
    @FXML private TableColumn<CategoriaCliente, String> colNombreCategoria;
    @FXML private TableColumn<CategoriaCliente, String> colDescripcionCategoria;
    @FXML private TableColumn<CategoriaCliente, String> colIconoCategoria;
    @FXML private TableColumn<CategoriaCliente, String> colColorCategoria;
    @FXML private TableColumn<CategoriaCliente, Boolean> colActivaCategoria;
    @FXML private TableColumn<CategoriaCliente, Void> colAccionesCategoria;
    @FXML private TextField txtBusquedaCategoria;
    
    // Formulario de categoría
    @FXML private VBox vboxFormularioCategoria;
    @FXML private Label lblTituloCategoria;
    @FXML private TextField txtNombreCategoria;
    @FXML private TextArea txtDescripcionCategoria;
    @FXML private TextField txtIconoCategoria;
    @FXML private ColorPicker colorPickerCategoria;
    @FXML private CheckBox chkActivaCategoria;
    @FXML private Button btnGuardarCategoria;
    @FXML private Button btnCancelarCategoria;

    private CategoriaCliente categoriaSeleccionada;
    private final ObservableList<CategoriaCliente> listaCategoriasTabla = FXCollections.observableArrayList();

    public ConfiguracionController(CategoriaClienteService categoriaClienteService) {
        this.categoriaClienteService = categoriaClienteService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTablaCategorias();
        cargarCategorias();
        configurarBusquedaCategoria();
        ocultarFormularioCategoria();
    }

    // ========== CATEGORÍAS CLIENTE ==========

    private void configurarTablaCategorias() {
        colNombreCategoria.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcionCategoria.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        
        colIconoCategoria.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getIcono() != null ? cellData.getValue().getIcono() : "")
        );
        
        colColorCategoria.setCellValueFactory(cellData -> {
            String hex = cellData.getValue().getColorHex();
            return new SimpleStringProperty(hex != null ? hex : "#CCCCCC");
        });
        
        colColorCategoria.setCellFactory(column -> new TableCell<CategoriaCliente, String>() {
            @Override
            protected void updateItem(String hex, boolean empty) {
                super.updateItem(hex, empty);
                if (empty || hex == null) {
                    setGraphic(null);
                } else {
                    VBox vb = new VBox();
                    vb.setStyle("-fx-background-color: " + hex + "; -fx-border-color: #CCCCCC; -fx-border-width: 1; -fx-padding: 4;");
                    vb.setPrefHeight(30);
                    vb.setPrefWidth(60);
                    setGraphic(vb);
                }
            }
        });
        
        colActivaCategoria.setCellValueFactory(new PropertyValueFactory<>("activa"));
        
        colAccionesCategoria.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("✏");
            private final Button btnToggle = new Button("🔄");
            private final Button btnEliminar = new Button("🗑");
            private final HBox container = new HBox(5, btnEditar, btnToggle, btnEliminar);

            {
                btnEditar.setStyle("-fx-font-size: 13px; -fx-padding: 5px 8px; " +
                        "-fx-background-color: #6B7280; -fx-text-fill: white; " +
                        "-fx-font-weight: 600; -fx-border-radius: 3; -fx-cursor: hand;");

                btnToggle.setStyle("-fx-font-size: 13px; -fx-padding: 5px 8px; " +
                        "-fx-background-color: #7C3AED; -fx-text-fill: white; " +
                        "-fx-font-weight: 600; -fx-border-radius: 3; -fx-cursor: hand;");

                btnEliminar.setStyle("-fx-font-size: 13px; -fx-padding: 5px 8px; " +
                        "-fx-background-color: #991b1b; -fx-text-fill: white; " +
                        "-fx-font-weight: 600; -fx-border-radius: 3; -fx-cursor: hand;");

                container.setStyle("-fx-alignment: CENTER;");

                btnEditar.setOnAction(e -> {
                    CategoriaCliente cat = getTableView().getItems().get(getIndex());
                    cargarEnFormularioCategoria(cat);
                });

                btnToggle.setOnAction(e -> {
                    CategoriaCliente cat = getTableView().getItems().get(getIndex());
                    intentarToggleActiva(cat);
                });

                btnEliminar.setOnAction(e -> {
                    CategoriaCliente cat = getTableView().getItems().get(getIndex());
                    confirmarEliminacionCategoria(cat);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        tblCategorias.setItems(listaCategoriasTabla);
    }

    private void cargarCategorias() {
        listaCategoriasTabla.clear();
        listaCategoriasTabla.addAll(categoriaClienteService.listarTodas());
    }

    private void configurarBusquedaCategoria() {
        if (txtBusquedaCategoria != null) {
            txtBusquedaCategoria.textProperty().addListener((obs, old, val) -> filtrarCategorias(val));
        }
    }

    private void filtrarCategorias(String filtro) {
        String f = filtro == null ? "" : filtro.trim().toLowerCase();
        listaCategoriasTabla.clear();
        
        var todasLasCategorias = categoriaClienteService.listarTodas();
        for (CategoriaCliente cat : todasLasCategorias) {
            if (f.isEmpty() ||
                (cat.getNombre() != null && cat.getNombre().toLowerCase().contains(f)) ||
                (cat.getDescripcion() != null && cat.getDescripcion().toLowerCase().contains(f))) {
                listaCategoriasTabla.add(cat);
            }
        }
    }

    @FXML
    public void accionNuevaCategoria() {
        categoriaSeleccionada = null;
        limpiarFormularioCategoria();
        chkActivaCategoria.setSelected(true);
        mostrarFormularioCategoria("Nueva Categoría de Cliente");
    }

    @FXML
    public void accionGuardarCategoria() {
        try {
            String nombre = txtNombreCategoria.getText().trim();
            String descripcion = txtDescripcionCategoria.getText().trim();
            String icono = txtIconoCategoria.getText().trim();
            String colorHex = colorPickerCategoria.getValue() != null 
                ? String.format("#%02X%02X%02X", 
                    (int)(colorPickerCategoria.getValue().getRed() * 255),
                    (int)(colorPickerCategoria.getValue().getGreen() * 255),
                    (int)(colorPickerCategoria.getValue().getBlue() * 255))
                : "#6366F1";
            boolean activa = chkActivaCategoria.isSelected();

            if (nombre.isBlank()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validación", "El nombre es obligatorio.");
                return;
            }

            if (categoriaSeleccionada == null) {
                categoriaClienteService.crear(nombre, descripcion, icono, colorHex);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Categoría creada correctamente.");
            } else {
                categoriaSeleccionada.setNombre(nombre);
                categoriaSeleccionada.setDescripcion(descripcion);
                categoriaSeleccionada.setIcono(icono);
                categoriaSeleccionada.setColorHex(colorHex);
                categoriaSeleccionada.setActiva(activa);
                categoriaClienteService.actualizar(categoriaSeleccionada);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Categoría actualizada correctamente.");
            }

            cargarCategorias();
            ocultarFormularioCategoria();
        } catch (IllegalArgumentException ex) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", ex.getMessage());
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    public void accionCancelarCategoria() {
        ocultarFormularioCategoria();
    }

    private void cargarEnFormularioCategoria(CategoriaCliente cat) {
        categoriaSeleccionada = cat;
        txtNombreCategoria.setText(cat.getNombre());
        txtDescripcionCategoria.setText(cat.getDescripcion() != null ? cat.getDescripcion() : "");
        txtIconoCategoria.setText(cat.getIcono() != null ? cat.getIcono() : "");
        
        if (cat.getColorHex() != null) {
            try {
                colorPickerCategoria.setValue(Color.web(cat.getColorHex()));
            } catch (Exception e) {
                colorPickerCategoria.setValue(Color.web("#6366F1"));
            }
        }
        
        chkActivaCategoria.setSelected(cat.isActiva());
        mostrarFormularioCategoria("Modificando: " + cat.getNombre());
    }

    private void intentarToggleActiva(CategoriaCliente cat) {
        try {
            categoriaClienteService.toggleActiva(cat.getId());
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", 
                "Categoría " + (cat.isActiva() ? "desactivada" : "activada") + ".");
            cargarCategorias();
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", ex.getMessage());
        }
    }

    private void confirmarEliminacionCategoria(CategoriaCliente cat) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Eliminación");
        confirm.setHeaderText("¿Eliminar la categoría " + cat.getNombre() + "?");
        confirm.setContentText("Esta acción no se puede deshacer.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                categoriaClienteService.eliminar(cat.getId());
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Categoría eliminada.");
                cargarCategorias();
            } catch (IllegalArgumentException ex) {
                mostrarAlerta(Alert.AlertType.WARNING, "No Se Puede Eliminar", ex.getMessage());
            } catch (Exception ex) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", ex.getMessage());
            }
        }
    }

    private void limpiarFormularioCategoria() {
        txtNombreCategoria.clear();
        txtDescripcionCategoria.clear();
        txtIconoCategoria.clear();
        colorPickerCategoria.setValue(Color.web("#6366F1"));
        chkActivaCategoria.setSelected(true);
    }

    private void mostrarFormularioCategoria(String titulo) {
        lblTituloCategoria.setText(titulo);
        vboxFormularioCategoria.setVisible(true);
        vboxFormularioCategoria.setManaged(true);
    }

    private void ocultarFormularioCategoria() {
        vboxFormularioCategoria.setVisible(false);
        vboxFormularioCategoria.setManaged(false);
        limpiarFormularioCategoria();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
