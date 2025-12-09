package com.unam.mvmsys.servicio.stock;

import com.unam.mvmsys.entidad.stock.Rubro;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RubroService {
    
    /**
     * Crea un nuevo rubro verificando que no exista duplicado
     * @throws IllegalArgumentException si ya existe un rubro con ese nombre
     */
    Rubro crearRubro(String nombre);
    
    /**
     * Obtiene un rubro por su ID
     */
    Optional<Rubro> obtenerPorId(UUID id);
    
    /**
     * Busca un rubro por su nombre exacto
     */
    Optional<Rubro> buscarPorNombre(String nombre);
    
    /**
     * Lista todos los rubros ordenados alfabéticamente
     */
    List<Rubro> listarTodos();
    
    /**
     * Busca rubros que contengan el texto indicado
     */
    List<Rubro> buscar(String texto);
    
    /**
     * Verifica si existe un rubro con ese nombre
     */
    boolean existe(String nombre);
}
