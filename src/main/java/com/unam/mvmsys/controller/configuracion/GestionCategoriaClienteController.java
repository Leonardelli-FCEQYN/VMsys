package com.unam.mvmsys.controller.configuracion;

import com.unam.mvmsys.entidad.configuracion.CategoriaCliente;
import com.unam.mvmsys.servicio.configuracion.CategoriaClienteService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
public class GestionCategoriaClienteController implements Initializable {

    private final CategoriaClienteService categoriaClienteService;

    @FXML private TextField txtNuevaCategoria;
    @FXML private TableView<CategoriaCliente> tblCategorias;
    @FXML private TableColumn<CategoriaCliente, String> colNombre;
    @FXML private TableColumn<CategoriaCliente, String> colDescripcion;
    @FXML private TableColumn<CategoriaCliente, String> colEstado;
    @FXML private TableColumn<CategoriaCliente, Void> colAcciones;

    public GestionCategoriaClienteController(CategoriaClienteService categoriaClienteService) {
        this.categoriaClienteService = categoriaClienteService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabla();
        cargarCategorias();
    }

    private void configurarTabla() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colEstado.setCellValueFactory(p -> new SimpleStringProperty(
            p.getValue().isActiva() ? "✓ Activa" : "✗ Inactiva"
        ));

        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("✏");
            private final Button btnToggle = new Button();
            private final HBox container = new HBox(8, btnEditar, btnToggle);

            {
                btnEditar.setStyle("-fx-font-size: 16px; -fx-padding: 8px 12px; " +
                        "-fx-background-color: #6B7280; -fx-text-fill: white; " +
                        "-fx-font-weight: 600; -fx-border-radius: 4; -fx-cursor: hand;");
                
                container.setStyle("-fx-alignment: CENTER;");

                btnEditar.setOnAction(e -> {
                    CategoriaCliente cat = getTableView().getItems().get(getIndex());
                    editarCategoria(cat);
                });

                btnToggle.setOnAction(e -> {
                    CategoriaCliente cat = getTableView().getItems().get(getIndex());
                    toggleActiva(cat);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    CategoriaCliente cat = getTableView().getItems().get(getIndex());
                    if (cat.isActiva()) {
                        btnToggle.setText("✕");
                        btnToggle.setStyle("-fx-font-size: 16px; -fx-padding: 8px 12px; " +
                                "-fx-background-color: #991b1b; -fx-text-fill: white; " +
                                "-fx-font-weight: 600; -fx-border-radius: 4; -fx-cursor: hand;");
                    } else {
                        btnToggle.setText("✓");
                        btnToggle.setStyle("-fx-font-size: 16px; -fx-padding: 8px 12px; " +
                                "-fx-background-color: #10B981; -fx-text-fill: white; " +
                                "-fx-font-weight: 600; -fx-border-radius: 4; -fx-cursor: hand;");
                    }
                    setGraphic(container);
                }
            }
        });

        tblCategorias.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_NEXT_COLUMN);
    }

    private void cargarCategorias() {
        tblCategorias.setItems(FXCollections.observableArrayList(
            categoriaClienteService.listarTodas()
        ));
    }

    @FXML
    public void accionCrearCategoria() {
        String nombre = txtNuevaCategoria.getText().trim();
        
        if (nombre.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Ingrese el nombre de la categoría.");
            return;
        }

        try {
            categoriaClienteService.crear(nombre, "", "", "#6366F1");
            txtNuevaCategoria.clear();
            cargarCategorias();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Categoría creada correctamente.");
        } catch (IllegalArgumentException ex) {
            mostrarAlerta(Alert.AlertType.WARNING, "Error", ex.getMessage());
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo crear la categoría.");
            ex.printStackTrace();
        }
    }

    private void editarCategoria(CategoriaCliente categoria) {
        TextInputDialog dialog = new TextInputDialog(categoria.getNombre());
        dialog.setTitle("Editar Categoría");
        dialog.setHeaderText("Editar: " + categoria.getNombre());
        dialog.setContentText("Nombre:");
        
        Optional<String> resultado = dialog.showAndWait();
        if (resultado.isPresent() && !resultado.get().trim().isEmpty()) {
            try {
                categoria.setNombre(resultado.get().trim());
                categoriaClienteService.actualizar(categoria);
                cargarCategorias();
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Categoría actualizada correctamente.");
            } catch (Exception ex) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", ex.getMessage());
            }
        }
    }

    private void toggleActiva(CategoriaCliente categoria) {
        String accion = categoria.isActiva() ? "desactivar" : "activar";
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar " + accion.toUpperCase());
        confirm.setHeaderText("¿" + accion + " categoría: " + categoria.getNombre() + "?");
        confirm.setContentText("Esta acción se puede revertir en cualquier momento.");
        
        Optional<ButtonType> resultado = confirm.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                categoriaClienteService.toggleActiva(categoria.getId());
                cargarCategorias();
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", 
                    "Categoría " + accion + "da correctamente.");
            } catch (Exception ex) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", ex.getMessage());
            }
        }
    }

    @FXML
    public void accionCerrar() {
        Stage stage = (Stage) tblCategorias.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
