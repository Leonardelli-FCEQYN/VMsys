package com.unam.mvmsys.controller.produccion.forms;

import com.unam.mvmsys.datatransferobj.EtapaFX;
import com.unam.mvmsys.datatransferobj.InsumoFX;
import com.unam.mvmsys.datatransferobj.RecetaFX;
import com.unam.mvmsys.entidad.produccion.ProcesoEstandar;
import com.unam.mvmsys.entidad.stock.Producto;
import com.unam.mvmsys.repositorio.produccion.ProcesoEstandarRepository;
import com.unam.mvmsys.repositorio.stock.ProductoRepository;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class FormRecetaController {

    private final ProcesoEstandarRepository procesoRepo;
    private final ProductoRepository productoRepo;

    // Entidad DTO
    private RecetaFX receta;
    private Runnable onSaveCallback;

    // UI Header
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private CheckBox chkActivo;

    // UI Tab Insumos
    @FXML private ComboBox<Producto> cmbProducto;
    @FXML private TextField txtCantidadInsumo;
    @FXML private Label lblUnidadMedida;
    @FXML private TableView<InsumoFX> tablaInsumos;
    @FXML private TableColumn<InsumoFX, String> colInsumoProducto; // Mostramos nombre
    @FXML private TableColumn<InsumoFX, BigDecimal> colInsumoCantidad;
    @FXML private TableColumn<InsumoFX, Void> colInsumoAccion;

    // UI Tab Etapas
    @FXML private TextField txtOrdenEtapa;
    @FXML private TextField txtNombreEtapa;
    @FXML private TextField txtTiempoEtapa;
    @FXML private TableView<EtapaFX> tablaEtapas;
    @FXML private TableColumn<EtapaFX, Number> colEtapaOrden;
    @FXML private TableColumn<EtapaFX, String> colEtapaNombre;
    @FXML private TableColumn<EtapaFX, Number> colEtapaTiempo;
    @FXML private TableColumn<EtapaFX, Void> colEtapaAccion;

    public FormRecetaController(ProcesoEstandarRepository procesoRepo, ProductoRepository productoRepo) {
        this.procesoRepo = procesoRepo;
        this.productoRepo = productoRepo;
    }

    @FXML
    public void initialize() {
        configurarTablas();
        cargarProductosCombo();
        
        // Listener para unidad de medida
        cmbProducto.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Asumiendo que producto tiene getUnidadMedida().getCodigo()
                lblUnidadMedida.setText(newVal.getUnidadMedida().getCodigo()); 
            }
        });
    }

    public void setReceta(RecetaFX receta) {
        this.receta = receta;
        
        // Bindings de cabecera
        txtNombre.textProperty().bindBidirectional(receta.nombreProperty());
        txtDescripcion.textProperty().bindBidirectional(receta.descripcionProperty());
        chkActivo.selectedProperty().bindBidirectional(receta.activoProperty());

        // Llenar tablas
        tablaInsumos.setItems(receta.getInsumos());
        tablaEtapas.setItems(receta.getEtapas());
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    private void configurarTablas() {
        // --- INSUMOS ---
        colInsumoProducto.setCellValueFactory(cell -> cell.getValue().productoProperty().asString());
        colInsumoCantidad.setCellValueFactory(cell -> cell.getValue().cantidadBaseProperty());
        
        colInsumoAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button();
            {
                FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
                icon.setSize("14");
                btn.setGraphic(icon);
                btn.getStyleClass().add("action-button");
                btn.setOnAction(e -> receta.getInsumos().remove(getIndex()));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        // --- ETAPAS ---
        colEtapaOrden.setCellValueFactory(cell -> cell.getValue().ordenProperty());
        colEtapaNombre.setCellValueFactory(cell -> cell.getValue().nombreProperty());
        colEtapaTiempo.setCellValueFactory(cell -> cell.getValue().tiempoMinutosProperty());
        
        colEtapaAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button();
            {
                FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
                icon.setSize("14");
                btn.setGraphic(icon);
                btn.getStyleClass().add("action-button");
                btn.setOnAction(e -> receta.getEtapas().remove(getIndex()));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void cargarProductosCombo() {
        List<Producto> productos = productoRepo.findAll(); // Podrías filtrar solo activos o tipo 'Materia Prima'
        cmbProducto.getItems().addAll(productos);
        
        // Converter para mostrar nombre en combo
        cmbProducto.setConverter(new StringConverter<>() {
            @Override
            public String toString(Producto p) { return p == null ? "" : p.getNombre(); }
            @Override
            public Producto fromString(String s) { return null; }
        });
    }

    // --- ACCIONES AGREGAR ---

    @FXML
    private void agregarInsumo() {
        Producto prod = cmbProducto.getValue();
        String cantStr = txtCantidadInsumo.getText();

        if (prod == null || cantStr.isEmpty()) {
            mostrarAlerta("Seleccione producto y cantidad.");
            return;
        }

        try {
            BigDecimal cant = new BigDecimal(cantStr);
            InsumoFX insumo = new InsumoFX();
            insumo.setProducto(prod);
            insumo.setCantidadBase(cant);
            
            receta.getInsumos().add(insumo);
            
            // Limpiar inputs
            cmbProducto.getSelectionModel().clearSelection();
            txtCantidadInsumo.clear();
        } catch (NumberFormatException e) {
            mostrarAlerta("Cantidad inválida.");
        }
    }

    @FXML
    private void agregarEtapa() {
        String ordenStr = txtOrdenEtapa.getText();
        String nombre = txtNombreEtapa.getText();
        String tiempoStr = txtTiempoEtapa.getText();

        if (ordenStr.isEmpty() || nombre.isEmpty()) {
            mostrarAlerta("Orden y Nombre son obligatorios.");
            return;
        }

        try {
            EtapaFX etapa = new EtapaFX();
            etapa.setOrden(Integer.parseInt(ordenStr));
            etapa.setNombre(nombre);
            etapa.setTiempoMinutos(tiempoStr.isEmpty() ? 0 : Integer.parseInt(tiempoStr));
            
            receta.getEtapas().add(etapa);
            
            // Auto-incrementar orden sugerido para el siguiente
            txtOrdenEtapa.setText(String.valueOf(Integer.parseInt(ordenStr) + 1));
            txtNombreEtapa.clear();
            txtTiempoEtapa.clear();
            txtNombreEtapa.requestFocus();
            
        } catch (NumberFormatException e) {
            mostrarAlerta("Orden o Tiempo inválidos.");
        }
    }

    // --- GUARDAR Y SALIR ---

    @FXML
    private void guardar() {
        if (receta.getNombre() == null || receta.getNombre().isEmpty()) {
            mostrarAlerta("El nombre de la receta es obligatorio.");
            return;
        }
        if (receta.getInsumos().isEmpty()) {
            mostrarAlerta("Debe agregar al menos un ingrediente.");
            return;
        }

        try {
            // Convertir DTO -> Entidad
            ProcesoEstandar entidad = receta.toEntity();
            
            // Guardar BD
            procesoRepo.save(entidad);
            
            // Notificar y Cerrar
            if (onSaveCallback != null) onSaveCallback.run();
            cancelar(); // Cierra la ventana
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String mensaje) {
        new Alert(Alert.AlertType.WARNING, mensaje).show();
    }
}