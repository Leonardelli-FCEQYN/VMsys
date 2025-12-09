package com.unam.mvmsys.servicio.stock;

import com.unam.mvmsys.entidad.stock.DetalleMovimiento;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DetalleMovimientoService {
    
    DetalleMovimiento crear(DetalleMovimiento detalle);
    
    DetalleMovimiento actualizar(DetalleMovimiento detalle);
    
    void eliminar(UUID id);
    
    Optional<DetalleMovimiento> buscarPorId(UUID id);
    
    List<DetalleMovimiento> listarPorMovimiento(UUID movimientoId);
    
    List<DetalleMovimiento> listarPorLote(UUID loteId);
}
