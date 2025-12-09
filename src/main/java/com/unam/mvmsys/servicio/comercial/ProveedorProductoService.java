package com.unam.mvmsys.servicio.comercial;

import com.unam.mvmsys.entidad.comercial.ProveedorProducto;
import com.unam.mvmsys.entidad.seguridad.Persona;
import com.unam.mvmsys.entidad.stock.Producto;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProveedorProductoService {
    
    ProveedorProducto vincular(Persona proveedor, Producto producto, BigDecimal precioCompra, String observaciones);
    
    void desVincular(UUID proveedorId, UUID productoId);
    
    List<ProveedorProducto> obtenerProductosDelProveedor(UUID proveedorId);
    
    Optional<ProveedorProducto> obtenerVinculacion(UUID proveedorId, UUID productoId);
    
    void actualizar(UUID proveedorId, UUID productoId, BigDecimal precioCompra, String observaciones);
    
    List<ProveedorProducto> obtenerProveedoresDelProducto(UUID productoId);
}
