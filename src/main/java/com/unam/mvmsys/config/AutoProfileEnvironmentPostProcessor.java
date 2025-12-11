package com.unam.mvmsys.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class AutoProfileEnvironmentPostProcessor implements EnvironmentPostProcessor {

    // Configuración de tu servidor remoto (Prod)
    private static final String REMOTE_HOST = "localhost"; // Cambiar por IP real en prod
    private static final int REMOTE_PORT = 9999;
    private static final int TIMEOUT_MS = 2000; // 2 segundos máximo para decidir

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // Si ya se especificó un perfil manualmente (ej. test), no intervenimos
        if (environment.getActiveProfiles().length > 0) {
            return;
        }

        System.out.println("⚡ [Auto-Detect] Verificando conectividad con servidor remoto (" + REMOTE_HOST + ":" + REMOTE_PORT + ")...");

        String selectedProfile;
        if (isPortOpen(REMOTE_HOST, REMOTE_PORT)) {
            selectedProfile = "online";
            System.out.println("✅ [Auto-Detect] Conexión exitosa. Iniciando en modo ONLINE (PostgreSQL).");
        } else {
            selectedProfile = "offline";
            System.out.println("⚠️ [Auto-Detect] Sin conexión al servidor. Iniciando en modo OFFLINE (H2 Local).");
        }

        // Inyectamos el perfil activo dinámicamente
        Map<String, Object> properties = new HashMap<>();
        properties.put("spring.profiles.active", selectedProfile);
        
        environment.getPropertySources().addFirst(new MapPropertySource("autoProfileConfig", properties));
    }

    private boolean isPortOpen(String ip, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), TIMEOUT_MS);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}