package com.unam.mvmsys.controller.comercial.forms;

import com.unam.mvmsys.entidad.seguridad.Persona;
import com.unam.mvmsys.entidad.stock.Rubro;
import com.unam.mvmsys.servicio.seguridad.PersonaService;
import com.unam.mvmsys.servicio.stock.RubroService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.function.Consumer;

@Component
public class FormProveedorController {

    private final PersonaService personaService;
    private final RubroService rubroService;

    @FXML private Label lblTitulo;
    @FXML private TextField txtRazonSocial;
    @FXML private TextField txtCuit;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtPlazoPago;
    @FXML private ComboBox<Rubro> cmbRubro;

    private Runnable onCancel;
    private Consumer<Persona> onSave;
    private Persona personaEdicion;

    public FormProveedorController(PersonaService ps, RubroService rs) {
        this.personaService = ps;
        this.rubroService = rs;
    }

    public void configurar(Persona p, Runnable cancel, Consumer<Persona> save) {
        this.personaEdicion = p;
        this.onCancel = cancel;
        this.onSave = save;
        
        cargarCombos();
        limpiar();

        if (p != null) {
            lblTitulo.setText("Editar Proveedor");
            cargarDatos(p);
        } else {
            lblTitulo.setText("Nuevo Proveedor");
        }
    }

    private void cargarCombos() {
        cmbRubro.setItems(FXCollections.observableArrayList(rubroService.listarTodos()));
    }

    private void cargarDatos(Persona p) {
        txtRazonSocial.setText(p.getRazonSocial());
        txtCuit.setText(p.getCuitDni());
        txtEmail.setText(p.getEmail());
        txtTelefono.setText(p.getTelefono());
        txtDireccion.setText(p.getDireccionCalle());
        txtPlazoPago.setText(String.valueOf(p.getPlazoPagoDias()));
        cmbRubro.setValue(p.getRubro());
    }

    @FXML
    void accionGuardar() {
        try {
            if (txtRazonSocial.getText().isEmpty() || txtCuit.getText().isEmpty() || cmbRubro.getValue() == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Razón Social, CUIT y Rubro son obligatorios.");
                return;
            }

            Persona p = (personaEdicion != null) ? personaEdicion : new Persona();
            p.setRazonSocial(txtRazonSocial.getText());
            p.setCuitDni(txtCuit.getText());
            p.setEmail(txtEmail.getText());
            p.setTelefono(txtTelefono.getText());
            p.setDireccionCalle(txtDireccion.getText());
            p.setEsProveedor(true);
            p.setRubro(cmbRubro.getValue());
            p.setActivo(true);
            
            try {
                int plazo = Integer.parseInt(txtPlazoPago.getText().isEmpty() ? "0" : txtPlazoPago.getText());
                p.setPlazoPagoDias(plazo);
            } catch (NumberFormatException e) {
                mostrarAlerta(Alert.AlertType.WARNING, "El plazo de pago debe ser un número.");
                return;
            }

            if (p.getId() == null) personaService.crearPersona(p);
            else personaService.actualizarPersona(p);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Proveedor guardado correctamente.");

            if (onSave != null) onSave.accept(p);

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error: " + e.getMessage());
        }
    }

    @FXML
    void accionCancelar() {
        if (onCancel != null) onCancel.run();
    }

    private void limpiar() {
        txtRazonSocial.clear(); txtCuit.clear(); txtEmail.clear(); 
        txtTelefono.clear(); txtDireccion.clear(); txtPlazoPago.clear();
        cmbRubro.setValue(null);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String msg) {
        Alert a = new Alert(tipo);
        a.setContentText(msg);
        a.showAndWait();
    }
}