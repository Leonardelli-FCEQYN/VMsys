package com.unam.mvmsys.repositorio.stock;

import com.unam.mvmsys.entidad.stock.Rubro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RubroRepository extends JpaRepository<Rubro, UUID> {
    
    /**
     * Busca un rubro por su nombre exacto (case-insensitive)
     */
    Optional<Rubro> findByNombreIgnoreCase(String nombre);
    
    /**
     * Lista rubros cuyo nombre contenga el texto buscado
     */
    List<Rubro> findByNombreContainingIgnoreCase(String texto);
    
    /**
     * Verifica si existe un rubro con ese nombre
     */
    boolean existsByNombreIgnoreCase(String nombre);
    
    /**
     * Lista todos los rubros ordenados alfabéticamente
     */
    List<Rubro> findAllByOrderByNombreAsc();
}
