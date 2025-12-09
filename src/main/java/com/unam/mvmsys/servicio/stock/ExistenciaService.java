package com.unam.mvmsys.servicio.stock;

import com.unam.mvmsys.entidad.stock.Existencia;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExistenciaService {
    
    Existencia crear(Existencia existencia);
    
    Existencia actualizar(Existencia existencia);
    
    void eliminar(UUID id);
    
    Optional<Existencia> buscarPorId(UUID id);
    
    Optional<Existencia> buscarPorLoteYDeposito(UUID loteId, UUID depositoId);
    
    List<Existencia> listarPorLote(UUID loteId);
    
    List<Existencia> listarPorDeposito(UUID depositoId);
    
    /**
     * Incrementar cantidad de existencia (entrada de stock)
     */
    void incrementarStock(UUID loteId, UUID depositoId, BigDecimal cantidad);
    
    /**
     * Decrementar cantidad de existencia (salida de stock)
     */
    void decrementarStock(UUID loteId, UUID depositoId, BigDecimal cantidad);
    
    /**
     * Obtener cantidad total de un lote en un depósito
     */
    BigDecimal obtenerCantidad(UUID loteId, UUID depositoId);
}
