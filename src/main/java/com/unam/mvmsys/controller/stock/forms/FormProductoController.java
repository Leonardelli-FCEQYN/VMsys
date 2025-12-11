package com.unam.mvmsys.controller.stock.forms;

import com.unam.mvmsys.entidad.configuracion.TipoProducto;
import com.unam.mvmsys.entidad.configuracion.UnidadMedida;
import com.unam.mvmsys.entidad.stock.Producto;
import com.unam.mvmsys.repositorio.configuracion.TipoProductoRepository;
import com.unam.mvmsys.repositorio.configuracion.UnidadMedidaRepository;
import com.unam.mvmsys.servicio.stock.ProductoService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

@Component
public class FormProductoController implements Initializable {

    private final ProductoService productoService;
    private final UnidadMedidaRepository unidadRepo;
    private final TipoProductoRepository tipoRepo;

    @FXML private Label lblTitulo;
    @FXML private TextField txtSku;
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private ComboBox<UnidadMedida> cbUnidad;
    @FXML private ComboBox<TipoProducto> cbTipo;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtCosto;
    @FXML private TextField txtStockMin;

    private Consumer<Producto> onSaveSuccess;
    private Runnable onCancel;
    private Producto productoEdicion;

    public FormProductoController(ProductoService ps, UnidadMedidaRepository umr, TipoProductoRepository tpr) {
        this.productoService = ps; this.unidadRepo = umr; this.tipoRepo = tpr;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarCombos();
        configurarCombosEditables();
    }

    public void configurar(Producto producto, Consumer<Producto> onSave, Runnable onCancel) {
        this.productoEdicion = producto;
        this.onSaveSuccess = onSave;
        this.onCancel = onCancel;
        limpiar();
        if (producto != null) cargarDatos(producto);
        else lblTitulo.setText("Nuevo Producto");
    }

    private void cargarCombos() {
        cbUnidad.getItems().setAll(unidadRepo.findAll());
        cbTipo.getItems().setAll(tipoRepo.findAll());
    }

    private void configurarCombosEditables() {
        cbTipo.setEditable(true);
        cbUnidad.setEditable(true);

        cbTipo.setOnAction(e -> asegurarTipoDesdeEditor());
        cbUnidad.setOnAction(e -> asegurarUnidadDesdeEditor());

        cbTipo.getEditor().focusedProperty().addListener((obs, old, focus) -> { if (!focus) asegurarTipoDesdeEditor(); });
        cbUnidad.getEditor().focusedProperty().addListener((obs, old, focus) -> { if (!focus) asegurarUnidadDesdeEditor(); });
    }

    private void cargarDatos(Producto p) {
        lblTitulo.setText("Editar: " + p.getCodigoSku());
        txtSku.setText(p.getCodigoSku());
        txtSku.setDisable(true);
        txtNombre.setText(p.getNombre());
        txtDescripcion.setText(p.getDescripcion());
        cbUnidad.setValue(p.getUnidadMedida());
        cbTipo.setValue(p.getTipoProducto());
        txtPrecio.setText(p.getPrecioVentaBase().toString());
        txtCosto.setText(p.getCostoReposicion().toString());
        txtStockMin.setText(p.getStockMinimo().toString());
    }

    @FXML
    void accionGuardar() {
        try {
            if (txtSku.getText().isEmpty() || txtNombre.getText().isEmpty() || cbUnidad.getValue() == null) {
                mostrarAlerta("Complete campos obligatorios"); return;
            }
            Producto p = (productoEdicion == null) ? Producto.builder().build() : productoEdicion;
            p.setCodigoSku(txtSku.getText());
            p.setNombre(txtNombre.getText());
            p.setDescripcion(txtDescripcion.getText());
            p.setUnidadMedida(cbUnidad.getValue());
            p.setTipoProducto(cbTipo.getValue());
            p.setPrecioVentaBase(new BigDecimal(txtPrecio.getText().replace(",", ".")));
            p.setCostoReposicion(new BigDecimal(txtCosto.getText().replace(",", ".")));
            p.setStockMinimo(new BigDecimal(txtStockMin.getText()));
            p.setActivo(true);

            Producto guardado = (p.getId() == null) ? productoService.crearProducto(p) : productoService.actualizarProducto(p);
            
            if (onSaveSuccess != null) onSaveSuccess.accept(guardado);
        } catch (Exception e) {
            mostrarAlerta("Error: " + e.getMessage());
        }
    }

    @FXML void accionCancelar() { if (onCancel != null) onCancel.run(); }

    private void limpiar() {
        txtSku.clear(); txtSku.setDisable(false); txtNombre.clear(); txtDescripcion.clear();
        txtPrecio.setText("0.00"); txtCosto.setText("0.00"); txtStockMin.setText("0");
        cbUnidad.setValue(null); cbTipo.setValue(null);
        cbUnidad.getEditor().clear(); cbTipo.getEditor().clear();
    }
    private void mostrarAlerta(String msg) { new Alert(Alert.AlertType.INFORMATION, msg).showAndWait(); }

    @FXML
    void accionNuevoTipo() { asegurarTipoDesdeEditor(); }

    @FXML
    void accionNuevaUnidad() { asegurarUnidadDesdeEditor(); }

    private void asegurarTipoDesdeEditor() {
        String texto = cbTipo.getEditor().getText();
        if (texto == null) return;
        String nombre = texto.trim();
        if (nombre.isEmpty()) return;

        TipoProducto existente = cbTipo.getItems().stream()
                .filter(t -> t.getNombre() != null && t.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElse(null);

        if (existente != null) {
            cbTipo.getSelectionModel().select(existente);
            cbTipo.getEditor().setText(existente.getNombre());
            return;
        }

        TipoProducto nuevo = new TipoProducto();
        nuevo.setNombre(nombre);
        nuevo.setActivo(true);
        tipoRepo.save(nuevo);
        cbTipo.getItems().add(nuevo);
        cbTipo.getSelectionModel().select(nuevo);
    }

    private void asegurarUnidadDesdeEditor() {
        String texto = cbUnidad.getEditor().getText();
        if (texto == null) return;
        String nombre = texto.trim();
        if (nombre.isEmpty()) return;

        UnidadMedida existente = cbUnidad.getItems().stream()
                .filter(u -> u.getNombre() != null && u.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElse(null);

        if (existente != null) {
            cbUnidad.getSelectionModel().select(existente);
            cbUnidad.getEditor().setText(existente.getNombre());
            return;
        }

        UnidadMedida nueva = new UnidadMedida();
        nueva.setNombre(nombre);
        nueva.setCodigo(nombre.substring(0, Math.min(3, nombre.length())).toUpperCase());
        nueva.setPermiteDecimales(false);
        nueva.setActivo(true);
        unidadRepo.save(nueva);
        cbUnidad.getItems().add(nueva);
        cbUnidad.getSelectionModel().select(nueva);
    }
}