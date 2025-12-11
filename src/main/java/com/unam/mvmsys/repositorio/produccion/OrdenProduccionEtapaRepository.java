package com.unam.mvmsys.repositorio.produccion;

import com.unam.mvmsys.entidad.produccion.OrdenProduccionEtapa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrdenProduccionEtapaRepository extends JpaRepository<OrdenProduccionEtapa, UUID> {
    
    // Ver carga de trabajo de un operario
    List<OrdenProduccionEtapa> findByOperarioIdAndEstado(UUID operarioId, OrdenProduccionEtapa.EstadoEtapa estado);
}