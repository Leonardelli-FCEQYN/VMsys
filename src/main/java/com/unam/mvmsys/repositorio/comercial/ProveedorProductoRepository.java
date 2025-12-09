package com.unam.mvmsys.repositorio.comercial;

import com.unam.mvmsys.entidad.comercial.ProveedorProducto;
import com.unam.mvmsys.entidad.seguridad.Persona;
import com.unam.mvmsys.entidad.stock.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProveedorProductoRepository extends JpaRepository<ProveedorProducto, UUID> {

    @Query("SELECT pp FROM ProveedorProducto pp WHERE pp.proveedor.id = :proveedorId AND pp.disponible = true")
    List<ProveedorProducto> findProductosByProveedor(@Param("proveedorId") UUID proveedorId);

    @Query("SELECT pp FROM ProveedorProducto pp WHERE pp.proveedor.id = :proveedorId AND pp.producto.id = :productoId")
    Optional<ProveedorProducto> findByProveedorAndProducto(@Param("proveedorId") UUID proveedorId, @Param("productoId") UUID productoId);

    @Query("SELECT pp FROM ProveedorProducto pp WHERE pp.proveedor.id = :proveedorId AND pp.disponible = true ORDER BY pp.producto.nombre ASC")
    List<ProveedorProducto> findProductosActivosByProveedor(@Param("proveedorId") UUID proveedorId);

    @Query("SELECT pp FROM ProveedorProducto pp WHERE pp.producto.id = :productoId AND pp.disponible = true")
    List<ProveedorProducto> findProveedoresByProducto(@Param("productoId") UUID productoId);
}
