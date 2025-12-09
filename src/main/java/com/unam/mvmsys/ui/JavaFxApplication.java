package com.unam.mvmsys.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import java.net.URL;

import com.unam.mvmsys.MvmsysApplication;

public class JavaFxApplication extends Application {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        // Iniciamos Spring Boot antes de mostrar la ventana
        applicationContext = new SpringApplicationBuilder(MvmsysApplication.class).run();
    }
    
   @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        loader.setControllerFactory(applicationContext::getBean);
        
        javafx.scene.Parent root = loader.load();
        Scene scene = new Scene(root);
        
        // --- CARGA DE CSS (Ruta Ajustada) ---
        // Buscamos dentro de la carpeta /fxml/
        URL cssUrl = getClass().getResource("/fxml/styles.css");
        
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("⚠️ ADVERTENCIA: No se encontró 'styles.css'.");
            System.err.println("   Verifique que esté en: src/main/resources/fxml/styles.css");
        }
        // ------------------------------------

        stage.setTitle("VMsys");
        stage.setScene(scene);
        stage.setMaximized(true);
        
        // Configurar para cerrar todo el sistema al cerrar la ventana principal
        stage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
        
        stage.show();
    }

    @Override
    public void stop() {
        // Cerramos el contexto de Spring limpiamente al salir
        applicationContext.close();
        Platform.exit();
    }
}