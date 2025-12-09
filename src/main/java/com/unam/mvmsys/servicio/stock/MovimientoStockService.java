package com.unam.mvmsys.servicio.stock;

import com.unam.mvmsys.entidad.stock.MovimientoStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MovimientoStockService {
    
    MovimientoStock crearMovimiento(MovimientoStock movimiento);
    
    MovimientoStock actualizarMovimiento(MovimientoStock movimiento);
    
    void eliminarMovimiento(UUID id); // Soft delete
    
    Optional<MovimientoStock> buscarPorId(UUID id);
    
    List<MovimientoStock> listarTodos();
    
    Page<MovimientoStock> listarPaginado(Pageable pageable);
    
    List<MovimientoStock> listarPorTipo(String tipoMovimiento);
    
    List<MovimientoStock> listarPorConcepto(String concepto);
    
    List<MovimientoStock> listarPorReferencia(String referencia);
    
    Page<MovimientoStock> listarPorFechas(LocalDateTime inicio, LocalDateTime fin, Pageable pageable);
}
