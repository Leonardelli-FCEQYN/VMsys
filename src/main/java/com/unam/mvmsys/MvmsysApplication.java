package com.unam.mvmsys;

import com.unam.mvmsys.ui.JavaFxApplication;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MvmsysApplication {

    public static void main(String[] args) {
        // Ahora sí reconocerá la clase
        Application.launch(JavaFxApplication.class, args);
    }
}