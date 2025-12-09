package com.unam.mvmsys.servicio.comercial.impl;

import com.unam.mvmsys.entidad.comercial.CatalogoProveedor;
import com.unam.mvmsys.entidad.seguridad.Persona;
import com.unam.mvmsys.entidad.stock.Producto;
import com.unam.mvmsys.repositorio.comercial.CatalogoProveedorRepository;
import com.unam.mvmsys.repositorio.seguridad.PersonaRepository;
import com.unam.mvmsys.repositorio.stock.ProductoRepository;
import com.unam.mvmsys.servicio.comercial.CatalogoProveedorService;
import lombok.RequiredArgsConstructor;
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
public class CatalogoProveedorServiceImpl implements CatalogoProveedorService {

    private final CatalogoProveedorRepository catalogoRepo;
    private final PersonaRepository personaRepo;
    private final ProductoRepository productoRepo;

    @Override
    public CatalogoProveedor agregarProductoAProveedor(Persona proveedor, Producto producto, BigDecimal precioCompra, int tiempoEntregaDias, BigDecimal cantidadMinima) {
        // Validar que sea proveedor
        if (!proveedor.isEsProveedor()) {
            throw new IllegalArgumentException("La persona debe ser proveedor");
        }

        // Verificar si ya existe
        Optional<CatalogoProveedor> existente = catalogoRepo.findByProveedorAndProducto(proveedor, producto);
        if (existente.isPresent()) {
            throw new IllegalArgumentException("El producto ya está en el catálogo de este proveedor");
        }

        CatalogoProveedor catalogo = CatalogoProveedor.builder()
                .proveedor(proveedor)
                .producto(producto)
                .precioCompra(precioCompra)
                .tiempoEntregaDias(tiempoEntregaDias)
                .cantidadMinima(cantidadMinima != null ? cantidadMinima : BigDecimal.ONE)
                .disponible(true)
                .ultimaActualizacionPrecio(LocalDateTime.now())
                .build();

        return catalogoRepo.save(catalogo);
    }

    @Override
    public CatalogoProveedor actualizarPrecio(UUID catalogoId, BigDecimal nuevoPrecio) {
        CatalogoProveedor catalogo = catalogoRepo.findById(catalogoId)
                .orElseThrow(() -> new IllegalArgumentException("Catálogo no encontrado"));

        catalogo.setPrecioCompra(nuevoPrecio);
        catalogo.setUltimaActualizacionPrecio(LocalDateTime.now());

        return catalogoRepo.save(catalogo);
    }

    @Override
    public CatalogoProveedor toggleDisponibilidad(UUID catalogoId) {
        CatalogoProveedor catalogo = catalogoRepo.findById(catalogoId)
                .orElseThrow(() -> new IllegalArgumentException("Catálogo no encontrado"));

        catalogo.setDisponible(!catalogo.isDisponible());
        return catalogoRepo.save(catalogo);
    }

    @Override
    public CatalogoProveedor toggleFavorito(UUID catalogoId) {
        CatalogoProveedor catalogo = catalogoRepo.findById(catalogoId)
                .orElseThrow(() -> new IllegalArgumentException("Catálogo no encontrado"));

        catalogo.setEsFavorito(!catalogo.isEsFavorito());
        return catalogoRepo.save(catalogo);
    }

    @Override
    public void eliminarProductoDeProveedor(UUID catalogoId) {
        catalogoRepo.deleteById(catalogoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogoProveedor> obtenerProductosDeProveedor(UUID proveedorId) {
        Persona proveedor = personaRepo.findById(proveedorId)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));

        return catalogoRepo.findByProveedorAndDisponibleTrue(proveedor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogoProveedor> obtenerProveedoresPorProducto(UUID productoId) {
        Producto producto = productoRepo.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        return catalogoRepo.findByProductoAndDisponibleTrue(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogoProveedor> compararPrecios(UUID productoId) {
        Producto producto = productoRepo.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        return catalogoRepo.compararPreciosPorProducto(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CatalogoProveedor> buscarPorProveedorYProducto(UUID proveedorId, UUID productoId) {
        Persona proveedor = personaRepo.findById(proveedorId).orElse(null);
        Producto producto = productoRepo.findById(productoId).orElse(null);

        if (proveedor == null || producto == null) {
            return Optional.empty();
        }

        return catalogoRepo.findByProveedorAndProducto(proveedor, producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogoProveedor> obtenerFavoritosDeProveedor(UUID proveedorId) {
        Persona proveedor = personaRepo.findById(proveedorId)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));

        return catalogoRepo.findByProveedorAndEsFavoritoTrue(proveedor);
    }

    @Override
    public void registrarCompra(UUID catalogoId) {
        CatalogoProveedor catalogo = catalogoRepo.findById(catalogoId)
                .orElseThrow(() -> new IllegalArgumentException("Catálogo no encontrado"));

        catalogo.setFechaUltimaCompra(LocalDateTime.now());
        catalogoRepo.save(catalogo);
    }
}
