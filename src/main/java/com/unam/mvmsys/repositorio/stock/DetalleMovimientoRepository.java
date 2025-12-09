package com.unam.mvmsys.repositorio.stock;

import com.unam.mvmsys.entidad.stock.DetalleMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface DetalleMovimientoRepository extends JpaRepository<DetalleMovimiento, UUID> {
    
    List<DetalleMovimiento> findByMovimientoId(UUID movimientoId);
    
    List<DetalleMovimiento> findByLoteId(UUID loteId);
}
