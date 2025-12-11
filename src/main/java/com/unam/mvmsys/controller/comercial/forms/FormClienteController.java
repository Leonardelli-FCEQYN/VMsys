package com.unam.mvmsys.controller.comercial.forms;

import com.unam.mvmsys.entidad.configuracion.CategoriaCliente;
import com.unam.mvmsys.entidad.seguridad.Persona;
import com.unam.mvmsys.servicio.configuracion.CategoriaClienteService;
import com.unam.mvmsys.servicio.seguridad.PersonaService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;
import java.util.function.Consumer;

@Component
public class FormClienteController {
    private final PersonaService personaService;
    private final CategoriaClienteService categoriaService;

    @FXML private Label lblTitulo;
    @FXML private TextField txtRazonSocial, txtCuit, txtEmail, txtTelefono, txtDireccion;
    @FXML private ComboBox<CategoriaCliente> cmbCategoria;
    @FXML private CheckBox chkEsProveedor;

    private Runnable onCancel;
    private Consumer<Persona> onSave;
    private Persona personaEdicion;

    public FormClienteController(PersonaService ps, CategoriaClienteService cs) {
        this.personaService = ps; this.categoriaService = cs;
    }

    public void configurar(Persona p, Runnable cancel, Consumer<Persona> save) {
        this.personaEdicion = p; this.onCancel = cancel; this.onSave = save;
        cmbCategoria.setItems(FXCollections.observableArrayList(categoriaService.listarActivas()));
        limpiar();
        if(p != null) cargarDatos(p); else lblTitulo.setText("Nuevo Cliente");
    }

    private void cargarDatos(Persona p) {
        lblTitulo.setText("Editar Cliente");
        txtRazonSocial.setText(p.getRazonSocial());
        txtCuit.setText(p.getCuitDni());
        txtEmail.setText(p.getEmail());
        txtTelefono.setText(p.getTelefono());
        txtDireccion.setText(p.getDireccionCalle());
        cmbCategoria.setValue(p.getCategoriaCliente());
        chkEsProveedor.setSelected(p.isEsProveedor());
    }

    @FXML void accionGuardar() {
        try {
            if(txtRazonSocial.getText().isEmpty() || txtCuit.getText().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Datos incompletos").showAndWait(); return;
            }
            Persona p = (personaEdicion != null) ? personaEdicion : new Persona();
            p.setRazonSocial(txtRazonSocial.getText());
            p.setCuitDni(txtCuit.getText());
            p.setEmail(txtEmail.getText());
            p.setTelefono(txtTelefono.getText());
            p.setDireccionCalle(txtDireccion.getText());
            p.setEsCliente(true);
            p.setEsProveedor(chkEsProveedor.isSelected());
            p.setCategoriaCliente(cmbCategoria.getValue());
            p.setActivo(true);

            if(p.getId() == null) personaService.crearPersona(p);
            else personaService.actualizarPersona(p);

            if(onSave != null) onSave.accept(p);
        } catch(Exception e) { e.printStackTrace(); }
    }
    @FXML void accionCancelar() { if(onCancel != null) onCancel.run(); }
    private void limpiar() {
        txtRazonSocial.clear();
        txtCuit.clear();
        txtEmail.clear();
        txtTelefono.clear();
        // Evita error si el FXML no tiene el campo txtDireccion
        if (txtDireccion != null) txtDireccion.clear();
        cmbCategoria.setValue(null);
    }
}