package com.unam.mvmsys.controller.produccion;

import com.unam.mvmsys.controller.produccion.forms.FormRecetaController;
import com.unam.mvmsys.datatransferobj.RecetaFX;
import com.unam.mvmsys.datatransferobj.EtapaFX;
import com.unam.mvmsys.entidad.produccion.ProcesoEstandar;
import com.unam.mvmsys.repositorio.produccion.ProcesoEstandarRepository;
import com.unam.mvmsys.servicio.produccion.ProcesoEstandarService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import org.springframework.stereotype.Component;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class RecetasController {

    private final ProcesoEstandarService procesoService;
    private final ApplicationContext applicationContext; // Necesario para inyectar el controller del form

    @FXML private TextField txtBuscar;
    @FXML private TableView<RecetaFX> tablaRecetas;
    @FXML private TableColumn<RecetaFX, String> colNombre;
    @FXML private TableColumn<RecetaFX, String> colDescripcion;
    @FXML private TableColumn<RecetaFX, Number> colTiempo;
    @FXML private TableColumn<RecetaFX, Boolean> colActivo;
    @FXML private TableColumn<RecetaFX, Void> colAcciones;

    public RecetasController(ProcesoEstandarService procesoService, ApplicationContext applicationContext) {
        this.procesoService = procesoService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarDatos();
        
        // Filtro de búsqueda
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> filtrar(newVal));
    }

    private void configurarColumnas() {
        colNombre.setCellValueFactory(cell -> cell.getValue().nombreProperty());
        colDescripcion.setCellValueFactory(cell -> cell.getValue().descripcionProperty());
        
        // Calcular tiempo total sumando tiempos de todas las etapas
        colTiempo.setCellValueFactory(cell -> {
            int tiempoTotal = cell.getValue().getEtapas().stream()
                    .mapToInt(EtapaFX::getTiempoMinutos)
                    .sum();
            return new SimpleIntegerProperty(tiempoTotal);
        });
        
        // Columna Estado con Icono
        colActivo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean activo, boolean empty) {
                super.updateItem(activo, empty);
                if (empty || activo == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    FontAwesomeIconView icon = new FontAwesomeIconView(activo ? FontAwesomeIcon.CHECK_CIRCLE : FontAwesomeIcon.TIMES_CIRCLE);
                    icon.setSize("16");
                    icon.getStyleClass().add(activo ? "status-icon-active" : "status-icon-inactive");
                    icon.setStyle(activo ? "-fx-fill: #28a745;" : "-fx-fill: #dc3545;");
                    setGraphic(icon);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                }
            }
        });

        // Columna Acciones (Botones Editar/Eliminar)
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button();
            private final Button btnEliminar = new Button();
            private final HBox container = new HBox(5, btnEditar, btnEliminar);

            {
                FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.EDIT);
                editIcon.setSize("14");
                btnEditar.setGraphic(editIcon);
                btnEditar.getStyleClass().add("action-button");
                btnEditar.setOnAction(event -> editarReceta(getTableView().getItems().get(getIndex())));

                FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
                deleteIcon.setSize("14");
                btnEliminar.setGraphic(deleteIcon);
                btnEliminar.getStyleClass().addAll("action-button", "danger");
                btnEliminar.setOnAction(event -> eliminarReceta(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void cargarDatos() {
        // Cargar desde el servicio con transacción activa
        List<RecetaFX> lista = procesoService.obtenerTodasLasRecetas();
        tablaRecetas.setItems(FXCollections.observableArrayList(lista));
    }

    private void filtrar(String texto) {
        // Implementación simple, idealmente llamar al repo si hay muchos datos
        if (texto == null || texto.isEmpty()) {
            cargarDatos();
        } else {
            var filtrados = tablaRecetas.getItems().stream()
                .filter(r -> r.getNombre().toLowerCase().contains(texto.toLowerCase()) || 
                             r.getDescripcion().toLowerCase().contains(texto.toLowerCase()))
                .toList();
            tablaRecetas.setItems(FXCollections.observableArrayList(filtrados));
        }
    }

    @FXML
    private void nuevaReceta() {
        abrirFormulario(new RecetaFX()); // Nuevo objeto vacío
    }

    private void editarReceta(RecetaFX receta) {
        abrirFormulario(receta); // Objeto existente
    }

    private void abrirFormulario(RecetaFX receta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/forms/form_receta.fxml"));
            
            // Usar ApplicationContext para que Spring inyecte dependencias en el FormController
            loader.setControllerFactory(applicationContext::getBean);
            
            Parent root = loader.load();
            FormRecetaController controller = loader.getController();
            
            controller.setReceta(receta);
            controller.setOnSaveCallback(this::cargarDatos); // Refrescar tabla al guardar

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle(receta.getId() == null ? "Nueva Receta" : "Editar Receta");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al abrir formulario: " + e.getMessage()).show();
        }
    }

    private void eliminarReceta(RecetaFX receta) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Está seguro de eliminar la receta '" + receta.getNombre() + "'?");
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (receta.getId() != null) {
                procesoService.eliminar(receta.getId());
            }
            cargarDatos();
        }
    }
}