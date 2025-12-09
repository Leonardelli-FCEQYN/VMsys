package com.unam.mvmsys.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Component
public class MainController implements Initializable {

    private final ApplicationContext springContext;

    @FXML private StackPane contentArea;
    @FXML private Button btnClientes;
    @FXML private Button btnProveedores;
    @FXML private Button btnStock;
    @FXML private Button btnVentas;
    @FXML private Button btnConfiguracion;
    @FXML private TextField searchField;
    @FXML private ImageView logoImage;

    private Button botonActivo;

    public MainController(ApplicationContext springContext) {
        this.springContext = springContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Cargar logo
        try {
            Image logo = new Image(getClass().getResourceAsStream("/imagenes/Logo VMsys.png"));
            logoImage.setImage(logo);
        } catch (Exception e) {
            System.err.println("No se pudo cargar el logo: " + e.getMessage());
        }
        
        // Cargar pantalla de inicio por defecto (Stock e Inventario)
        irAStock();
        
        // Configurar búsqueda de menús
        if (searchField != null) {
            searchField.setOnKeyReleased(event -> buscarMenu(searchField.getText()));
        }
    }

    @FXML
    public void irAClientes() {
        resaltarBoton(btnClientes);
        cargarVista("/fxml/clientes.fxml");
    }

    @FXML
    public void irAProveedores() {
        resaltarBoton(btnProveedores);
        cargarVista("/fxml/proveedores.fxml");
    }

    @FXML
    public void irAStock() {
        resaltarBoton(btnStock);
        cargarVista("/fxml/gestorStockSimple.fxml");
    }

    @FXML
    public void irAVentas() {
        resaltarBoton(btnVentas);
        // Futuro módulo
        System.out.println("Módulo Ventas en construcción...");
    }

    @FXML
    public void irAConfiguracion() {
        resaltarBoton(btnConfiguracion);
        cargarVista("/fxml/configuracion.fxml");
    }

    private void resaltarBoton(javafx.scene.control.Button boton) {
        // Quitar estilo del botón anterior
        if (botonActivo != null) {
            botonActivo.setStyle("");
        }
        
        // Aplicar estilo al nuevo botón activo
        boton.setStyle("-fx-background-color: #E5E7EB; -fx-text-fill: #DC2626; -fx-font-weight: bold;");
        botonActivo = boton;
    }

    private void cargarVista(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            
            // ¡CRUCIAL! Usamos el contexto de Spring para crear los controladores de las sub-vistas
            loader.setControllerFactory(springContext::getBean);
            
            Parent vista = loader.load();
            
            // Limpiamos el centro y ponemos la nueva vista
            contentArea.getChildren().clear();
            contentArea.getChildren().add(vista);
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error cargando vista: " + fxmlPath);
        }
    }

    /**
     * Busca en los menús disponibles según el texto ingresado.
     * Filtra los botones del menú que coincidan con el texto.
     */
    private void buscarMenu(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            // Si está vacío, mostrar todos los botones
            btnStock.setManaged(true);
            btnStock.setVisible(true);
            btnClientes.setManaged(true);
            btnClientes.setVisible(true);
            btnProveedores.setManaged(true);
            btnProveedores.setVisible(true);
            btnVentas.setManaged(true);
            btnVentas.setVisible(true);
            btnConfiguracion.setManaged(true);
            btnConfiguracion.setVisible(true);
            return;
        }
        
        String busqueda = texto.toLowerCase().trim();
        
        // Definir los menús disponibles: texto a mostrar, palabra clave de búsqueda, botón
        String[][] menus = {
            {"Stock e Inventario", "stock producto inventario", "btn-stock"},
            {"Clientes", "cliente", "btn-cliente"},
            {"Proveedores", "proveedor", "btn-proveedor"},
            {"Ventas", "venta", "btn-venta"},
            {"Configuración", "config", "btn-config"}
        };
        
        // Filtrar y mostrar/ocultar según coincidencia
        boolean stockCoincide = "stock".contains(busqueda) || "inventario".contains(busqueda) || "producto".contains(busqueda) || "📦".contains(busqueda);
        boolean clienteCoincide = "cliente".contains(busqueda) || "clientes".contains(busqueda) || "👥".contains(busqueda);
        boolean proveedorCoincide = "proveedor".contains(busqueda) || "proveedores".contains(busqueda) || "📑".contains(busqueda);
        boolean ventaCoincide = "venta".contains(busqueda) || "ventas".contains(busqueda) || "💰".contains(busqueda);
        boolean configCoincide = "config".contains(busqueda) || "configuración".contains(busqueda) || "⚙️".contains(busqueda);
        
        btnStock.setVisible(stockCoincide);
        btnStock.setManaged(stockCoincide);
        
        btnClientes.setVisible(clienteCoincide);
        btnClientes.setManaged(clienteCoincide);
        
        btnProveedores.setVisible(proveedorCoincide);
        btnProveedores.setManaged(proveedorCoincide);
        
        btnVentas.setVisible(ventaCoincide);
        btnVentas.setManaged(ventaCoincide);
        
        btnConfiguracion.setVisible(configCoincide);
        btnConfiguracion.setManaged(configCoincide);
    }
}