package com.unam.mvmsys.servicio.comercial;

import com.unam.mvmsys.entidad.comercial.ClienteProducto;
import com.unam.mvmsys.entidad.seguridad.Persona;
import com.unam.mvmsys.entidad.stock.Producto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteProductoService {

    /**
     * Registra una venta a cliente (crea relación o actualiza estadísticas)
     */
    ClienteProducto registrarVenta(Persona cliente, Producto producto, BigDecimal cantidad);

    /**
     * Asigna un precio especial a un producto para un cliente específico
     */
    ClienteProducto asignarPrecioEspecial(UUID clienteId, UUID productoId, BigDecimal precioEspecial);

    /**
     * Marca/desmarca un producto como favorito del cliente
     */
    ClienteProducto toggleFavorito(UUID clienteProductoId);

    /**
     * Desactiva la relación cliente-producto
     */
    void desactivar(UUID clienteProductoId);

    /**
     * Obtiene todos los productos que ha comprado un cliente
     */
    List<ClienteProducto> obtenerProductosDeCliente(UUID clienteId);

    /**
     * Obtiene los productos favoritos de un cliente
     */
    List<ClienteProducto> obtenerFavoritosDeCliente(UUID clienteId);

    /**
     * Top N productos más comprados por un cliente (por cantidad)
     */
    List<ClienteProducto> obtenerTopProductos(UUID clienteId, int limite);

    /**
     * Top N productos más frecuentes de un cliente (por número de compras)
     */
    List<ClienteProducto> obtenerProductosFrecuentes(UUID clienteId, int limite);

    /**
     * Obtiene todos los clientes que compraron un producto
     */
    List<ClienteProducto> obtenerClientesPorProducto(UUID productoId);

    /**
     * Clientes que no compran un producto hace más de X días (remarketing)
     */
    List<ClienteProducto> obtenerClientesInactivosDesde(UUID productoId, LocalDateTime fecha);

    /**
     * Obtiene productos con precio especial de un cliente
     */
    List<ClienteProducto> obtenerProductosConPrecioEspecial(UUID clienteId);

    /**
     * Busca la relación específica cliente-producto
     */
    Optional<ClienteProducto> buscarPorClienteYProducto(UUID clienteId, UUID productoId);
}
