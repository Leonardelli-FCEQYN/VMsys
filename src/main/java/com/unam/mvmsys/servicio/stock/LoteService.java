package com.unam.mvmsys.servicio.stock;

import com.unam.mvmsys.entidad.stock.Lote;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoteService {
    
    Lote crearLote(Lote lote);
    
    Lote actualizarLote(Lote lote);
    
    void eliminarLote(UUID id); // Desactivado lógico
    
    List<Lote> listarTodos();
    
    List<Lote> listarActivos();
    
    Optional<Lote> buscarPorId(UUID id);
    
    Optional<Lote> buscarPorCodigo(String codigo);
    
    List<Lote> listarPorProducto(UUID productoId);
}
