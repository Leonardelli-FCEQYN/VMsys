package com.unam.mvmsys.controller.produccion;

import com.unam.mvmsys.entidad.produccion.ProcesoEstandar;
import com.unam.mvmsys.entidad.stock.Producto;
import com.unam.mvmsys.repositorio.produccion.ProcesoEstandarRepository;
import com.unam.mvmsys.repositorio.stock.ProductoRepository;
import com.unam.mvmsys.datatransferobj.EtapaFX;
import com.unam.mvmsys.datatransferobj.InsumoFX;
import com.unam.mvmsys.datatransferobj.RecetaFX;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.util.converter.BigDecimalStringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AdminRecetasController {

    private final ProcesoEstandarRepository procesoRepo;
    private final ProductoRepository productoRepo;

    // -- Componentes FXML --
    @FXML private TextField txtBuscar;
    @FXML private TableView<RecetaFX> tablaRecetas;
    @FXML private TableColumn<RecetaFX, String> colNombreReceta;
    @FXML private TableColumn<RecetaFX, Boolean> colActivoReceta;
    
    @FXML private VBox panelDetalle;
    @FXML private TextField txtNombre;
    @FXML private TextField txtDescripcion;
    @FXML private CheckBox chkActivo;
    
    // Tabla Insumos
    @FXML private TableView<InsumoFX> tablaInsumos;
    @FXML private TableColumn<InsumoFX, Producto> colProductoInsumo;
    @FXML private TableColumn<InsumoFX, BigDecimal> colCantidadInsumo;
    
    // Tabla Etapas
    @FXML private TableView<EtapaFX> tablaEtapas;
    @FXML private TableColumn<EtapaFX, Integer> colOrdenEtapa;
    @FXML private TableColumn<EtapaFX, String> colNombreEtapa;
    @FXML private TableColumn<EtapaFX, Integer> colTiempoEtapa;
    
    @FXML private Label lblEstado;

    // Modelo actual
    private RecetaFX recetaSeleccionada;

    public AdminRecetasController(ProcesoEstandarRepository procesoRepo, ProductoRepository productoRepo) {
        this.procesoRepo = procesoRepo;
        this.productoRepo = productoRepo;
    }

    @FXML
    public void initialize() {
        configurarTablas();
        cargarListaRecetas();
        
        // Listener de selección
        tablaRecetas.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> cargarFormulario(newVal)
        );
        
        // Por defecto ocultar detalle hasta que seleccionen algo
        panelDetalle.setDisable(true);
    }

    private void configurarTablas() {
        // 1. Tabla Principal
        colNombreReceta.setCellValueFactory(cell -> cell.getValue().nombreProperty());
        colActivoReceta.setCellValueFactory(cell -> cell.getValue().activoProperty());

        // 2. Tabla Insumos (Editable)
        colProductoInsumo.setCellValueFactory(cell -> cell.getValue().productoProperty());
        colCantidadInsumo.setCellValueFactory(cell -> cell.getValue().cantidadBaseProperty());
        
        // Configurar edición de cantidad
        colCantidadInsumo.setCellFactory(TextFieldTableCell.forTableColumn(new BigDecimalStringConverter()));
        colCantidadInsumo.setOnEditCommit(e -> e.getRowValue().setCantidadBase(e.getNewValue()));
        
        // Configurar combo de productos (requiere StringConverter para el objeto Producto)
        // Simplificado: aquí deberías cargar la lista de productos disponibles
        // colProductoInsumo.setCellFactory(ComboBoxTableCell.forTableColumn(...)); 
        // *NOTA: Para simplificar ahora, la dejamos solo lectura o requerimos un selector más complejo luego.*

        // 3. Tabla Etapas (Editable)
        colNombreEtapa.setCellValueFactory(cell -> cell.getValue().nombreProperty());
        colNombreEtapa.setCellFactory(TextFieldTableCell.forTableColumn());
        colNombreEtapa.setOnEditCommit(e -> e.getRowValue().setNombre(e.getNewValue()));

        colOrdenEtapa.setCellValueFactory(cell -> cell.getValue().ordenProperty().asObject());
        colOrdenEtapa.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colOrdenEtapa.setOnEditCommit(e -> e.getRowValue().setOrden(e.getNewValue()));
        
        colTiempoEtapa.setCellValueFactory(cell -> cell.getValue().tiempoMinutosProperty().asObject());
        colTiempoEtapa.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colTiempoEtapa.setOnEditCommit(e -> e.getRowValue().setTiempoMinutos(e.getNewValue()));
    }

    private void cargarListaRecetas() {
        var lista = procesoRepo.findAll().stream()
                .map(RecetaFX::new)
                .toList();
        tablaRecetas.setItems(FXCollections.observableArrayList(lista));
    }

    private void cargarFormulario(RecetaFX receta) {
        if (receta == null) {
            panelDetalle.setDisable(true);
            return;
        }
        
        this.recetaSeleccionada = receta;
        panelDetalle.setDisable(false);
        lblEstado.setText("");

        // Bindings bidireccionales (Lo que escribas actualiza el modelo)
        txtNombre.textProperty().bindBidirectional(receta.nombreProperty());
        txtDescripcion.textProperty().bindBidirectional(receta.descripcionProperty());
        chkActivo.selectedProperty().bindBidirectional(receta.activoProperty());

        // Bindings de tablas
        tablaInsumos.setItems(receta.getInsumos());
        tablaEtapas.setItems(receta.getEtapas());
    }

    @FXML
    private void nuevaReceta() {
        RecetaFX nueva = new RecetaFX();
        nueva.setNombre("Nueva Receta");
        nueva.setActivo(true);
        tablaRecetas.getItems().add(nueva);
        tablaRecetas.getSelectionModel().select(nueva);
        txtNombre.requestFocus();
    }
    
    @FXML
    private void eliminarReceta() {
        RecetaFX seleccion = tablaRecetas.getSelectionModel().getSelectedItem();
        if (seleccion == null) return;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Borrar receta " + seleccion.getNombre() + "?");
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (seleccion.getId() != null) {
                try {
                    procesoRepo.deleteById(seleccion.getId());
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "No se puede borrar: " + e.getMessage()).show();
                    return;
                }
            }
            tablaRecetas.getItems().remove(seleccion);
        }
    }

    @FXML
    private void guardarCambios() {
        if (recetaSeleccionada == null) return;
        
        try {
            // Convertir DTO a Entidad JPA
            ProcesoEstandar entidad = recetaSeleccionada.toEntity();
            
            // Guardar en BD
            entidad = procesoRepo.save(entidad);
            
            // Refrescar UI con ID generado si era nueva
            // (Idealmente recargar la lista, pero para simplificar...)
            lblEstado.setText("Guardado exitosamente!");
            cargarListaRecetas(); // Recarga simple
            
        } catch (Exception e) {
            e.printStackTrace(); // Ver en consola por ahora
            new Alert(Alert.AlertType.ERROR, "Error al guardar: " + e.getMessage()).show();
        }
    }

    @FXML
    private void agregarEtapa() {
        if (recetaSeleccionada == null) return;
        EtapaFX etapa = new EtapaFX();
        etapa.setOrden(tablaEtapas.getItems().size() + 1);
        etapa.setNombre("Nuevo Paso");
        recetaSeleccionada.getEtapas().add(etapa);
    }
    
    @FXML
    private void quitarEtapa() {
        var selected = tablaEtapas.getSelectionModel().getSelectedItem();
        if (selected != null) {
            recetaSeleccionada.getEtapas().remove(selected);
        }
    }

    @FXML
    private void agregarInsumo() {
        // Para agregar insumo, necesitamos un selector de productos.
        // Por ahora agregamos uno vacío para probar la tabla, 
        // pero necesitarás un Dialog o ComboBox para elegir el Producto real.
        if (recetaSeleccionada == null) return;
        
        // Simulación: Buscamos el primer producto que haya para no romper
        var prod = productoRepo.findAll().stream().findFirst();
        if (prod.isPresent()) {
            InsumoFX insumo = new InsumoFX();
            insumo.setProducto(prod.get());
            insumo.setCantidadBase(BigDecimal.ONE);
            recetaSeleccionada.getInsumos().add(insumo);
        } else {
            new Alert(Alert.AlertType.WARNING, "No hay productos cargados en el sistema.").show();
        }
    }

    @FXML
    private void quitarInsumo() {
        var selected = tablaInsumos.getSelectionModel().getSelectedItem();
        if (selected != null) {
            recetaSeleccionada.getInsumos().remove(selected);
        }
    }
}