package com.unam.mvmsys.servicio.comercial;

import com.unam.mvmsys.entidad.comercial.ClienteProducto;
import com.unam.mvmsys.entidad.seguridad.Persona;
import com.unam.mvmsys.entidad.stock.Producto;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteProductoVinculacionService {
    
    ClienteProducto vincular(Persona cliente, Producto producto, String observaciones);
    
    void desVincular(UUID clienteId, UUID productoId);
    
    List<ClienteProducto> obtenerProductosDelCliente(UUID clienteId);
    
    Optional<ClienteProducto> obtenerVinculacion(UUID clienteId, UUID productoId);
    
    void actualizar(UUID clienteId, UUID productoId, String observaciones);
    
    boolean existeVinculacion(UUID clienteId, UUID productoId);
}
