package com.unam.mvmsys.repositorio.produccion;

import com.unam.mvmsys.entidad.produccion.OrdenProduccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrdenProduccionRepository extends JpaRepository<OrdenProduccion, UUID> {
    
    // Validar código único
    boolean existsByCodigo(String codigo);
    
    // Tablero kanban: traer por estado
    List<OrdenProduccion> findByEstado(OrdenProduccion.EstadoOrden estado);
    
    // Todas las activas (no finalizadas ni canceladas)
    @Query("SELECT o FROM OrdenProduccion o WHERE o.estado NOT IN ('FINALIZADA', 'CANCELADA')")
    List<OrdenProduccion> findActivas();
}