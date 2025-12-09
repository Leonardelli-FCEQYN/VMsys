package com.unam.mvmsys.repositorio.comercial;

import com.unam.mvmsys.entidad.comercial.ClienteProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClienteProductoVinculacionRepository extends JpaRepository<ClienteProducto, UUID> {

    @Query("SELECT cp FROM ClienteProducto cp WHERE cp.cliente.id = :clienteId AND cp.activo = true ORDER BY cp.producto.nombre ASC")
    List<ClienteProducto> findProductosActivosByCliente(@Param("clienteId") UUID clienteId);

    @Query("SELECT cp FROM ClienteProducto cp WHERE cp.cliente.id = :clienteId AND cp.producto.id = :productoId")
    Optional<ClienteProducto> findByClienteAndProducto(@Param("clienteId") UUID clienteId, @Param("productoId") UUID productoId);

    @Query("SELECT COUNT(cp) > 0 FROM ClienteProducto cp WHERE cp.cliente.id = :clienteId AND cp.producto.id = :productoId AND cp.activo = true")
    boolean existeVinculacion(@Param("clienteId") UUID clienteId, @Param("productoId") UUID productoId);
}
