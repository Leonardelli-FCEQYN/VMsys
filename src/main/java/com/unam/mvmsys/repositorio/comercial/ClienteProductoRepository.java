package com.unam.mvmsys.repositorio.comercial;

import com.unam.mvmsys.entidad.comercial.ClienteProducto;
import com.unam.mvmsys.entidad.seguridad.Persona;
import com.unam.mvmsys.entidad.stock.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClienteProductoRepository extends JpaRepository<ClienteProducto, UUID> {

    /**
     * Lista todos los productos que ha comprado un cliente
     */
    List<ClienteProducto> findByClienteAndActivoTrue(Persona cliente);

    /**
     * Lista productos favoritos de un cliente
     */
    List<ClienteProducto> findByClienteAndEsFavoritoTrueAndActivoTrue(Persona cliente);

    /**
     * Busca la relación específica cliente-producto
     */
    Optional<ClienteProducto> findByClienteAndProducto(Persona cliente, Producto producto);

    /**
     * Verifica si un cliente ya compró un producto
     */
    boolean existsByClienteAndProducto(Persona cliente, Producto producto);

    /**
     * Top N productos más comprados por un cliente (ordenado por cantidad)
     */
    @Query("SELECT cp FROM ClienteProducto cp WHERE cp.cliente = :cliente AND cp.activo = true ORDER BY cp.cantidadTotalComprada DESC")
    List<ClienteProducto> findTopProductosPorCliente(@Param("cliente") Persona cliente);

    /**
     * Top N productos más comprados por un cliente (ordenado por cantidad de compras)
     */
    @Query("SELECT cp FROM ClienteProducto cp WHERE cp.cliente = :cliente AND cp.activo = true ORDER BY cp.cantidadCompras DESC")
    List<ClienteProducto> findTopProductosFrecuentesPorCliente(@Param("cliente") Persona cliente);

    /**
     * Clientes que compraron un producto hace más de X días (para remarketing)
     */
    @Query("SELECT cp FROM ClienteProducto cp WHERE cp.producto = :producto AND cp.fechaUltimaCompra < :fecha AND cp.activo = true")
    List<ClienteProducto> findClientesSinComprarProductoDesde(@Param("producto") Producto producto, @Param("fecha") LocalDateTime fecha);

    /**
     * Lista todos los clientes que compraron un producto específico
     */
    List<ClienteProducto> findByProductoAndActivoTrue(Producto producto);

    /**
     * Productos con precio especial para un cliente
     */
    @Query("SELECT cp FROM ClienteProducto cp WHERE cp.cliente = :cliente AND cp.precioEspecial IS NOT NULL AND cp.activo = true")
    List<ClienteProducto> findProductosConPrecioEspecial(@Param("cliente") Persona cliente);
}
