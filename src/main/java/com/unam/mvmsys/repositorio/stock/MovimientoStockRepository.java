package com.unam.mvmsys.repositorio.stock;

import com.unam.mvmsys.entidad.stock.MovimientoStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, UUID> {
    
    List<MovimientoStock> findByTipoMovimiento(String tipoMovimiento);
    
    List<MovimientoStock> findByConcepto(String concepto);
    
    List<MovimientoStock> findByDepositoOrigenId(UUID depositoId);
    
    List<MovimientoStock> findByDepositoDestinoId(UUID depositoId);
    
    Page<MovimientoStock> findByActivoTrue(Pageable pageable);
    
    Page<MovimientoStock> findByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
    
    List<MovimientoStock> findByReferenciaComprobante(String referencia);
}
