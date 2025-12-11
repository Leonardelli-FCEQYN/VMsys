package com.unam.mvmsys.ui;

import com.unam.mvmsys.MvmsysApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFxApplication extends Application {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        applicationContext = new SpringApplicationBuilder(MvmsysApplication.class).run();
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Cargar Main desde carpeta layout
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/layout/main.fxml"));
        loader.setControllerFactory(applicationContext::getBean);
        
        Parent root = loader.load();
        Scene scene = new Scene(root);
        
        // Cargar CSS
        scene.getStylesheets().addAll(
            getClass().getResource("/css/base.css").toExternalForm(),
            getClass().getResource("/css/layout.css").toExternalForm(),
            getClass().getResource("/css/components.css").toExternalForm(),
            getClass().getResource("/css/tables.css").toExternalForm()
        );
        
        stage.setTitle("MVMsys - Gestión Integral");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    @Override
    public void stop() {
        applicationContext.close();
        Platform.exit();
    }
}