package com.unam.mvmsys.servicio.comercial;

import com.unam.mvmsys.entidad.comercial.CatalogoProveedor;
import com.unam.mvmsys.entidad.seguridad.Persona;
import com.unam.mvmsys.entidad.stock.Producto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CatalogoProveedorService {

    /**
     * Agrega un producto al catálogo de un proveedor
     */
    CatalogoProveedor agregarProductoAProveedor(Persona proveedor, Producto producto, BigDecimal precioCompra, int tiempoEntregaDias, BigDecimal cantidadMinima);

    /**
     * Actualiza el precio de compra de un producto de un proveedor
     */
    CatalogoProveedor actualizarPrecio(UUID catalogoId, BigDecimal nuevoPrecio);

    /**
     * Marca/desmarca un producto como disponible en el catálogo del proveedor
     */
    CatalogoProveedor toggleDisponibilidad(UUID catalogoId);

    /**
     * Marca/desmarca un producto como favorito del proveedor
     */
    CatalogoProveedor toggleFavorito(UUID catalogoId);

    /**
     * Elimina un producto del catálogo del proveedor
     */
    void eliminarProductoDeProveedor(UUID catalogoId);

    /**
     * Obtiene todos los productos disponibles de un proveedor
     */
    List<CatalogoProveedor> obtenerProductosDeProveedor(UUID proveedorId);

    /**
     * Obtiene todos los proveedores que ofrecen un producto
     */
    List<CatalogoProveedor> obtenerProveedoresPorProducto(UUID productoId);

    /**
     * Compara precios entre proveedores para un producto (ordenado por menor precio)
     */
    List<CatalogoProveedor> compararPrecios(UUID productoId);

    /**
     * Busca el catálogo específico de un proveedor-producto
     */
    Optional<CatalogoProveedor> buscarPorProveedorYProducto(UUID proveedorId, UUID productoId);

    /**
     * Obtiene productos favoritos de un proveedor
     */
    List<CatalogoProveedor> obtenerFavoritosDeProveedor(UUID proveedorId);

    /**
     * Registra una compra (actualiza fecha_ultima_compra)
     */
    void registrarCompra(UUID catalogoId);
}
