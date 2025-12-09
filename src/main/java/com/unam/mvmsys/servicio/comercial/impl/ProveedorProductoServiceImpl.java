package com.unam.mvmsys.servicio.comercial.impl;

import com.unam.mvmsys.entidad.comercial.ProveedorProducto;
import com.unam.mvmsys.entidad.seguridad.Persona;
import com.unam.mvmsys.entidad.stock.Producto;
import com.unam.mvmsys.repositorio.comercial.ProveedorProductoRepository;
import com.unam.mvmsys.servicio.comercial.ProveedorProductoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ProveedorProductoServiceImpl implements ProveedorProductoService {

    private final ProveedorProductoRepository proveedorProductoRepository;

    public ProveedorProductoServiceImpl(ProveedorProductoRepository proveedorProductoRepository) {
        this.proveedorProductoRepository = proveedorProductoRepository;
    }

    @Override
    public ProveedorProducto vincular(Persona proveedor, Producto producto, BigDecimal precioCompra, String observaciones) {
        // Validaciones
        if (proveedor == null || !proveedor.isEsProveedor()) {
            throw new IllegalArgumentException("La persona no es un proveedor válido.");
        }
        if (producto == null || !producto.isActivo()) {
            throw new IllegalArgumentException("El producto no es válido o está inactivo.");
        }
        if (precioCompra == null || precioCompra.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio de compra debe ser mayor o igual a cero.");
        }

        // Verificar si ya existe la vinculación
        Optional<ProveedorProducto> existente = proveedorProductoRepository.findByProveedorAndProducto(
            proveedor.getId(), producto.getId()
        );

        if (existente.isPresent()) {
            // Si existe pero está deshabilitada, reactivarla
            ProveedorProducto pp = existente.get();
            pp.setDisponible(true);
            pp.setPrecioCompra(precioCompra);
            pp.setObservaciones(observaciones);
            return proveedorProductoRepository.save(pp);
        }

        // Crear nueva vinculación
        ProveedorProducto proveedorProducto = ProveedorProducto.builder()
            .proveedor(proveedor)
            .producto(producto)
            .precioCompra(precioCompra)
            .observaciones(observaciones)
            .disponible(true)
            .build();

        return proveedorProductoRepository.save(proveedorProducto);
    }

    @Override
    public void desVincular(UUID proveedorId, UUID productoId) {
        Optional<ProveedorProducto> vinculacion = proveedorProductoRepository.findByProveedorAndProducto(proveedorId, productoId);
        if (vinculacion.isPresent()) {
            ProveedorProducto pp = vinculacion.get();
            pp.setDisponible(false);
            proveedorProductoRepository.save(pp);
        }
    }

    @Override
    public List<ProveedorProducto> obtenerProductosDelProveedor(UUID proveedorId) {
        return proveedorProductoRepository.findProductosActivosByProveedor(proveedorId);
    }

    @Override
    public Optional<ProveedorProducto> obtenerVinculacion(UUID proveedorId, UUID productoId) {
        return proveedorProductoRepository.findByProveedorAndProducto(proveedorId, productoId);
    }

    @Override
    public void actualizar(UUID proveedorId, UUID productoId, BigDecimal precioCompra, String observaciones) {
        Optional<ProveedorProducto> vinculacion = proveedorProductoRepository.findByProveedorAndProducto(proveedorId, productoId);
        if (vinculacion.isPresent()) {
            ProveedorProducto pp = vinculacion.get();
            pp.setPrecioCompra(precioCompra);
            pp.setObservaciones(observaciones);
            proveedorProductoRepository.save(pp);
        } else {
            throw new IllegalArgumentException("No existe vinculación entre el proveedor y el producto.");
        }
    }

    @Override
    public List<ProveedorProducto> obtenerProveedoresDelProducto(UUID productoId) {
        return proveedorProductoRepository.findProveedoresByProducto(productoId);
    }
}
