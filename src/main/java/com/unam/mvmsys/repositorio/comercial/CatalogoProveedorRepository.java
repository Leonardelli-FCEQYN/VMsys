package com.unam.mvmsys.repositorio.comercial;

import com.unam.mvmsys.entidad.comercial.CatalogoProveedor;
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
public interface CatalogoProveedorRepository extends JpaRepository<CatalogoProveedor, UUID> {

    /**
     * Lista todos los productos disponibles de un proveedor
     */
    List<CatalogoProveedor> findByProveedorAndDisponibleTrue(Persona proveedor);

    /**
     * Lista todos los productos de un proveedor (incluye no disponibles)
     */
    List<CatalogoProveedor> findByProveedor(Persona proveedor);

    /**
     * Busca todos los proveedores que ofrecen un producto específico
     */
    List<CatalogoProveedor> findByProductoAndDisponibleTrue(Producto producto);

    /**
     * Busca la relación específica proveedor-producto
     */
    Optional<CatalogoProveedor> findByProveedorAndProducto(Persona proveedor, Producto producto);

    /**
     * Verifica si un proveedor ofrece un producto
     */
    boolean existsByProveedorAndProducto(Persona proveedor, Producto producto);

    /**
     * Lista productos favoritos de un proveedor
     */
    List<CatalogoProveedor> findByProveedorAndEsFavoritoTrue(Persona proveedor);

    /**
     * Compara precios de un producto entre todos los proveedores (ordenado por precio menor)
     */
    @Query("SELECT cp FROM CatalogoProveedor cp WHERE cp.producto = :producto AND cp.disponible = true ORDER BY cp.precioCompra ASC")
    List<CatalogoProveedor> compararPreciosPorProducto(@Param("producto") Producto producto);

    /**
     * Busca proveedores por producto y rubro (útil para filtrar por categoría)
     */
    @Query("SELECT cp FROM CatalogoProveedor cp WHERE cp.producto.rubro.id = :rubroId AND cp.disponible = true")
    List<CatalogoProveedor> findByProductoRubroId(@Param("rubroId") UUID rubroId);
}
