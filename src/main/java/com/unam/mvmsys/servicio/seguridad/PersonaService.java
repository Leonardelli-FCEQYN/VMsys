package com.unam.mvmsys.servicio.seguridad;

import com.unam.mvmsys.entidad.seguridad.Persona;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonaService {
    
    Persona crearPersona(Persona persona);
    
    Persona actualizarPersona(Persona persona);
    
    void eliminarPersona(UUID id); // Soft delete
    
    List<Persona> listarTodas();
    
    List<Persona> listarClientes();
    
    List<Persona> listarProveedores();
    
    Optional<Persona> buscarPorCuit(String cuit);
}