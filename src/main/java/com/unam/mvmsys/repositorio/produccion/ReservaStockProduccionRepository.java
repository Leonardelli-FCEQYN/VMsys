package com.unam.mvmsys.repositorio.produccion;

import com.unam.mvmsys.entidad.produccion.ReservaStockProduccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReservaStockProduccionRepository extends JpaRepository<ReservaStockProduccion, UUID> {
    
    // Qué stock está "retenido" por órdenes activas
    List<ReservaStockProduccion> findByLoteIdAndActivoTrue(UUID loteId);
}
