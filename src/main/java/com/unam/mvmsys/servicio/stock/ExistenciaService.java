package com.unam.mvmsys.servicio.stock;

import com.unam.mvmsys.entidad.stock.Existencia;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ExistenciaService {

    /**
     * Guarda o actualiza una existencia física.
     */
    Existencia guardar(Existencia existencia);

    /**
     * Busca todas las existencias físicas de un producto en todos los depósitos,
     * ordenadas por fecha de vencimiento del lote (FIFO).
     * Solo devuelve existencias con cantidad mayor a 0 y lotes en estado 'Disponible'.
     */
    List<Existencia> buscarDisponiblesPorProductoFIFO(UUID productoId);

    /**
     * Calcula la cantidad total física de un producto sumando todos los depósitos.
     */
    BigDecimal consultarStockTotalProducto(UUID productoId);
    
    /**
     * Busca una existencia específica por Lote y Depósito.
     */
    Existencia buscarPorLoteYDeposito(UUID loteId, UUID depositoId);
}