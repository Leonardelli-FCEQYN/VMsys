package com.unam.mvmsys.servicio.stock;

import com.unam.mvmsys.entidad.stock.Producto;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductoService {
    
    Producto crearProducto(Producto producto);
    
    Producto actualizarProducto(Producto producto);
    
    void eliminarProducto(UUID id); // Desactivado lógico (Soft Delete)
    
    List<Producto> listarTodos();
    
    List<Producto> listarActivos();
    
    Optional<Producto> buscarPorId(UUID id);
    
    Optional<Producto> buscarPorSku(String sku);
}