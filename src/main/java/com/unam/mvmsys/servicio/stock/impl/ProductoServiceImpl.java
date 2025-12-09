package com.unam.mvmsys.servicio.stock.impl;

import com.unam.mvmsys.entidad.stock.Producto;
import com.unam.mvmsys.repositorio.stock.ProductoRepository;
import com.unam.mvmsys.servicio.stock.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // Por defecto solo lectura (optimización)
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepo;

    @Override
    @Transactional // Escritura habilitada
    public Producto crearProducto(Producto producto) {
        // 1. Validar unicidad de SKU
        if (productoRepo.existsByCodigoSku(producto.getCodigoSku())) {
            throw new IllegalArgumentException("Ya existe un producto con el SKU: " + producto.getCodigoSku());
        }
        
        // 2. Validaciones de negocio extras (ej: precio no negativo)
        if (producto.getPrecioVentaBase().doubleValue() < 0) {
            throw new IllegalArgumentException("El precio base no puede ser negativo");
        }

        return productoRepo.save(producto);
    }

    @Override
    @Transactional
    public Producto actualizarProducto(Producto producto) {
        // Validar que el producto y su ID no sean nulos
        if (producto == null || producto.getId() == null) {
            throw new IllegalArgumentException("El producto y su ID no pueden ser nulos.");
        }
        
        // Verificar que exista antes de actualizar
        @SuppressWarnings("null")
        boolean exists = productoRepo.existsById(producto.getId());
        if (!exists) {
            throw new IllegalArgumentException("No se puede actualizar. El producto no existe.");
        }
        return productoRepo.save(producto);
    }

    @Override
    @Transactional
    public void eliminarProducto(UUID id) {
        // Validar que el ID no sea nulo
        if (id == null) {
            throw new IllegalArgumentException("El ID del producto no puede ser nulo.");
        }
        
        // Soft Delete: No borramos físico, solo marcamos activo = false
        productoRepo.findById(id).ifPresent(p -> {
            p.setActivo(false);
            productoRepo.save(p);
        });
    }

    @Override
    public List<Producto> listarTodos() {
        return productoRepo.findAll();
    }

    @Override
    public List<Producto> listarActivos() {
        // Filtrar en memoria o crear método en repo 'findByActivoTrue'
        // Por simplicidad inicial filtramos aquí stream, pero lo ideal es repositorio
        return productoRepo.findAll().stream()
                .filter(Producto::isActivo)
                .toList();
    }

    @Override
    public Optional<Producto> buscarPorId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo.");
        }
        return productoRepo.findById(id);
    }

    @Override
    public Optional<Producto> buscarPorSku(String sku) {
        return productoRepo.findByCodigoSku(sku);
    }
}