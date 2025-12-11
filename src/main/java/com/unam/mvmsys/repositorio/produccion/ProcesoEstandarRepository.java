package com.unam.mvmsys.repositorio.produccion;

import com.unam.mvmsys.entidad.produccion.ProcesoEstandar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProcesoEstandarRepository extends JpaRepository<ProcesoEstandar, UUID> {
    
    boolean existsByNombreIgnoreCase(String nombre);
    
    List<ProcesoEstandar> findByActivoTrue();
    
    @Query("SELECT p FROM ProcesoEstandar p WHERE " +
           "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :termino, '%'))) " +
           "AND p.activo = true")
    List<ProcesoEstandar> buscar(String termino);
}