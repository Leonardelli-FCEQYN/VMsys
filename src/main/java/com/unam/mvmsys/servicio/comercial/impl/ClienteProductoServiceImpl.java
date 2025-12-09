package com.unam.mvmsys.servicio.comercial.impl;

import com.unam.mvmsys.entidad.comercial.ClienteProducto;
import com.unam.mvmsys.entidad.seguridad.Persona;
import com.unam.mvmsys.entidad.stock.Producto;
import com.unam.mvmsys.repositorio.comercial.ClienteProductoRepository;
import com.unam.mvmsys.repositorio.seguridad.PersonaRepository;
import com.unam.mvmsys.repositorio.stock.ProductoRepository;
import com.unam.mvmsys.servicio.comercial.ClienteProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ClienteProductoServiceImpl implements ClienteProductoService {

    private final ClienteProductoRepository clienteProductoRepo;
    private final PersonaRepository personaRepo;
    private final ProductoRepository productoRepo;

    @Override
    public ClienteProducto registrarVenta(Persona cliente, Producto producto, BigDecimal cantidad) {
        // Validar que sea cliente
        if (!cliente.isEsCliente()) {
            throw new IllegalArgumentException("La persona debe ser cliente");
        }

        Optional<ClienteProducto> existente = clienteProductoRepo.findByClienteAndProducto(cliente, producto);

        if (existente.isPresent()) {
            // Actualizar estadísticas
            ClienteProducto cp = existente.get();
            cp.setCantidadTotalComprada(cp.getCantidadTotalComprada().add(cantidad));
            cp.setCantidadCompras(cp.getCantidadCompras() + 1);
            cp.setFechaUltimaCompra(LocalDateTime.now());
            cp.setActivo(true);
            return clienteProductoRepo.save(cp);
        } else {
            // Crear nueva relación
            ClienteProducto nuevo = ClienteProducto.builder()
                    .cliente(cliente)
                    .producto(producto)
                    .cantidadTotalComprada(cantidad)
                    .cantidadCompras(1)
                    .fechaPrimeraCompra(LocalDateTime.now())
                    .fechaUltimaCompra(LocalDateTime.now())
                    .activo(true)
                    .build();
            return clienteProductoRepo.save(nuevo);
        }
    }

    @Override
    public ClienteProducto asignarPrecioEspecial(UUID clienteId, UUID productoId, BigDecimal precioEspecial) {
        Persona cliente = personaRepo.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        Producto producto = productoRepo.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        Optional<ClienteProducto> existente = clienteProductoRepo.findByClienteAndProducto(cliente, producto);

        if (existente.isPresent()) {
            ClienteProducto cp = existente.get();
            cp.setPrecioEspecial(precioEspecial);
            return clienteProductoRepo.save(cp);
        } else {
            // Crear relación con precio especial
            ClienteProducto nuevo = ClienteProducto.builder()
                    .cliente(cliente)
                    .producto(producto)
                    .precioEspecial(precioEspecial)
                    .activo(true)
                    .build();
            return clienteProductoRepo.save(nuevo);
        }
    }

    @Override
    public ClienteProducto toggleFavorito(UUID clienteProductoId) {
        ClienteProducto cp = clienteProductoRepo.findById(clienteProductoId)
                .orElseThrow(() -> new IllegalArgumentException("Relación cliente-producto no encontrada"));

        cp.setEsFavorito(!cp.isEsFavorito());
        return clienteProductoRepo.save(cp);
    }

    @Override
    public void desactivar(UUID clienteProductoId) {
        ClienteProducto cp = clienteProductoRepo.findById(clienteProductoId)
                .orElseThrow(() -> new IllegalArgumentException("Relación cliente-producto no encontrada"));

        cp.setActivo(false);
        clienteProductoRepo.save(cp);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteProducto> obtenerProductosDeCliente(UUID clienteId) {
        Persona cliente = personaRepo.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        return clienteProductoRepo.findByClienteAndActivoTrue(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteProducto> obtenerFavoritosDeCliente(UUID clienteId) {
        Persona cliente = personaRepo.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        return clienteProductoRepo.findByClienteAndEsFavoritoTrueAndActivoTrue(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteProducto> obtenerTopProductos(UUID clienteId, int limite) {
        Persona cliente = personaRepo.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        List<ClienteProducto> todos = clienteProductoRepo.findTopProductosPorCliente(cliente);
        return todos.size() > limite ? todos.subList(0, limite) : todos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteProducto> obtenerProductosFrecuentes(UUID clienteId, int limite) {
        Persona cliente = personaRepo.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        List<ClienteProducto> todos = clienteProductoRepo.findTopProductosFrecuentesPorCliente(cliente);
        return todos.size() > limite ? todos.subList(0, limite) : todos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteProducto> obtenerClientesPorProducto(UUID productoId) {
        Producto producto = productoRepo.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        return clienteProductoRepo.findByProductoAndActivoTrue(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteProducto> obtenerClientesInactivosDesde(UUID productoId, LocalDateTime fecha) {
        Producto producto = productoRepo.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        return clienteProductoRepo.findClientesSinComprarProductoDesde(producto, fecha);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteProducto> obtenerProductosConPrecioEspecial(UUID clienteId) {
        Persona cliente = personaRepo.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        return clienteProductoRepo.findProductosConPrecioEspecial(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClienteProducto> buscarPorClienteYProducto(UUID clienteId, UUID productoId) {
        Persona cliente = personaRepo.findById(clienteId).orElse(null);
        Producto producto = productoRepo.findById(productoId).orElse(null);

        if (cliente == null || producto == null) {
            return Optional.empty();
        }

        return clienteProductoRepo.findByClienteAndProducto(cliente, producto);
    }
}
