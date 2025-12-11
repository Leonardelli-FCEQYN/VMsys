package com.unam.mvmsys.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class MainController implements Initializable {

    private final ApplicationContext springContext;
    private static MainController instance;

    @FXML private BorderPane rootPane;
    @FXML private Button btnStock;
    @FXML private Button btnClientes;
    @FXML private Button btnProveedores;
    @FXML private Button btnVentas;
    @FXML private Button btnRecetas;
    @FXML private Button btnConfiguracion;
    @FXML private TextField searchField;
    @FXML private ImageView logoImage;
    private Button botonActivo;

    public MainController(ApplicationContext springContext) {
        this.springContext = springContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        try {
            logoImage.setImage(new Image(getClass().getResourceAsStream("/imagenes/Logo VMsys.png")));
        } catch (Exception ignored) {}

        configurarBusquedaLateral();
    }

    public static MainController getInstance() { return instance; }

    public void cambiarVistaCentro(Node nodoVista) {
        if (rootPane != null) rootPane.setCenter(nodoVista);
    }

    public void navegarA(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(springContext::getBean);
            Parent vista = loader.load();
            cambiarVistaCentro(vista);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("ERROR CRÍTICO: No se encuentra " + fxmlPath);
        }
    }

    // RUTAS CORREGIDAS
    @FXML public void irAStock(ActionEvent event) {
        resaltarBoton((Button)event.getSource());
        navegarA("/fxml/stock/gestorStock.fxml");
    }

    @FXML public void irAClientes(ActionEvent event) {
        resaltarBoton((Button)event.getSource());
        navegarA("/fxml/comercial/clientes.fxml");
    }

    @FXML public void irAProveedores(ActionEvent event) {
        resaltarBoton((Button)event.getSource());
        navegarA("/fxml/comercial/proveedores.fxml");
    }

    @FXML public void irAVentas(ActionEvent event) { resaltarBoton((Button)event.getSource()); }
    
    @FXML public void irARecetas(ActionEvent event) {
        resaltarBoton((Button)event.getSource());
        navegarA("/fxml/produccion/admin_recetas.fxml");
    }
    
    @FXML public void irAConfiguracion(ActionEvent event) { resaltarBoton((Button)event.getSource()); }

    private void resaltarBoton(Button boton) {
        if (botonActivo != null) botonActivo.getStyleClass().remove("nav-button-active");
        botonActivo = boton;
        if (botonActivo != null) botonActivo.getStyleClass().add("nav-button-active");
    }

    private void configurarBusquedaLateral() {
        if (searchField == null) return;
        List<Button> navButtons = List.of(btnStock, btnClientes, btnProveedores, btnVentas, btnRecetas, btnConfiguracion);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String q = newVal == null ? "" : newVal.trim().toLowerCase();
            navButtons.forEach(btn -> {
                if (btn == null) return;
                boolean show = q.isEmpty() || btn.getText().toLowerCase().contains(q);
                btn.setVisible(show);
                btn.setManaged(show);
            });
        });
    }
}