package com.unam.mvmsys.servicio.comercial.impl;

import com.unam.mvmsys.entidad.comercial.ClienteProducto;
import com.unam.mvmsys.entidad.seguridad.Persona;
import com.unam.mvmsys.entidad.stock.Producto;
import com.unam.mvmsys.repositorio.comercial.ClienteProductoVinculacionRepository;
import com.unam.mvmsys.servicio.comercial.ClienteProductoVinculacionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ClienteProductoVinculacionServiceImpl implements ClienteProductoVinculacionService {

    private final ClienteProductoVinculacionRepository clienteProductoRepository;

    public ClienteProductoVinculacionServiceImpl(ClienteProductoVinculacionRepository clienteProductoRepository) {
        this.clienteProductoRepository = clienteProductoRepository;
    }

    @Override
    public ClienteProducto vincular(Persona cliente, Producto producto, String observaciones) {
        // Validaciones
        if (cliente == null || !cliente.isEsCliente()) {
            throw new IllegalArgumentException("La persona no es un cliente válido.");
        }
        if (producto == null || !producto.isActivo()) {
            throw new IllegalArgumentException("El producto no es válido o está inactivo.");
        }

        // Verificar si ya existe la vinculación
        Optional<ClienteProducto> existente = clienteProductoRepository.findByClienteAndProducto(
            cliente.getId(), producto.getId()
        );

        if (existente.isPresent()) {
            // Si existe pero está inactiva, reactivarla
            ClienteProducto cp = existente.get();
            cp.setActivo(true);
            cp.setObservaciones(observaciones);
            return clienteProductoRepository.save(cp);
        }

        // Crear nueva vinculación
        ClienteProducto clienteProducto = ClienteProducto.builder()
            .cliente(cliente)
            .producto(producto)
            .observaciones(observaciones)
            .activo(true)
            .build();

        return clienteProductoRepository.save(clienteProducto);
    }

    @Override
    public void desVincular(UUID clienteId, UUID productoId) {
        Optional<ClienteProducto> vinculacion = clienteProductoRepository.findByClienteAndProducto(clienteId, productoId);
        if (vinculacion.isPresent()) {
            ClienteProducto cp = vinculacion.get();
            cp.setActivo(false);
            clienteProductoRepository.save(cp);
        }
    }

    @Override
    public List<ClienteProducto> obtenerProductosDelCliente(UUID clienteId) {
        return clienteProductoRepository.findProductosActivosByCliente(clienteId);
    }

    @Override
    public Optional<ClienteProducto> obtenerVinculacion(UUID clienteId, UUID productoId) {
        return clienteProductoRepository.findByClienteAndProducto(clienteId, productoId);
    }

    @Override
    public void actualizar(UUID clienteId, UUID productoId, String observaciones) {
        Optional<ClienteProducto> vinculacion = clienteProductoRepository.findByClienteAndProducto(clienteId, productoId);
        if (vinculacion.isPresent()) {
            ClienteProducto cp = vinculacion.get();
            cp.setObservaciones(observaciones);
            clienteProductoRepository.save(cp);
        } else {
            throw new IllegalArgumentException("No existe vinculación entre el cliente y el producto.");
        }
    }

    @Override
    public boolean existeVinculacion(UUID clienteId, UUID productoId) {
        return clienteProductoRepository.existeVinculacion(clienteId, productoId);
    }
}
