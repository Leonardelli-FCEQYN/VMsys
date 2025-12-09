package com.unam.mvmsys.servicio.configuracion.impl;

import com.unam.mvmsys.entidad.configuracion.CategoriaCliente;
import com.unam.mvmsys.repositorio.configuracion.CategoriaClienteRepository;
import com.unam.mvmsys.repositorio.seguridad.PersonaRepository;
import com.unam.mvmsys.servicio.configuracion.CategoriaClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoriaClienteServiceImpl implements CategoriaClienteService {

    private final CategoriaClienteRepository categoriaRepo;
    private final PersonaRepository personaRepo;

    @Override
    public CategoriaCliente crear(String nombre, String descripcion, String icono, String colorHex) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío");
        }

        String nombreNormalizado = nombre.trim();

        if (categoriaRepo.existsByNombreIgnoreCaseAndActivaTrue(nombreNormalizado)) {
            throw new IllegalArgumentException("Ya existe una categoría activa con ese nombre");
        }

        CategoriaCliente categoria = CategoriaCliente.builder()
                .nombre(nombreNormalizado)
                .descripcion(descripcion != null ? descripcion.trim() : "")
                .icono(icono != null ? icono.trim() : "")
                .colorHex(colorHex != null && !colorHex.isBlank() ? colorHex : "#6366F1")
                .activa(true)
                .build();

        return categoriaRepo.save(categoria);
    }

    @Override
    public CategoriaCliente actualizar(UUID id, String nombre, String descripcion, String icono, String colorHex) {
        CategoriaCliente categoria = categoriaRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        if (nombre != null && !nombre.isBlank()) {
            String nombreNormalizado = nombre.trim();
            // Verificar que no exista otro con el mismo nombre (excepto ella misma)
            Optional<CategoriaCliente> existente = categoriaRepo.findByNombreIgnoreCase(nombreNormalizado);
            if (existente.isPresent() && !existente.get().getId().equals(id)) {
                throw new IllegalArgumentException("Ya existe otra categoría con ese nombre");
            }
            categoria.setNombre(nombreNormalizado);
        }

        if (descripcion != null) {
            categoria.setDescripcion(descripcion.trim());
        }

        if (icono != null) {
            categoria.setIcono(icono.trim());
        }

        if (colorHex != null && !colorHex.isBlank()) {
            categoria.setColorHex(colorHex);
        }

        return categoriaRepo.save(categoria);
    }

    @Override
    public CategoriaCliente actualizar(CategoriaCliente categoria) {
        if (categoria == null || categoria.getId() == null) {
            throw new IllegalArgumentException("Categoría inválida");
        }

        CategoriaCliente existente = categoriaRepo.findById(categoria.getId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        // Validar nombre único (excepto para la misma entidad)
        if (categoria.getNombre() != null && !categoria.getNombre().isBlank()) {
            String nombreNormalizado = categoria.getNombre().trim();
            Optional<CategoriaCliente> otra = categoriaRepo.findByNombreIgnoreCase(nombreNormalizado);
            if (otra.isPresent() && !otra.get().getId().equals(categoria.getId())) {
                throw new IllegalArgumentException("Ya existe otra categoría con ese nombre");
            }
            existente.setNombre(nombreNormalizado);
        }

        if (categoria.getDescripcion() != null) {
            existente.setDescripcion(categoria.getDescripcion());
        }

        if (categoria.getIcono() != null) {
            existente.setIcono(categoria.getIcono());
        }

        if (categoria.getColorHex() != null) {
            existente.setColorHex(categoria.getColorHex());
        }

        existente.setActiva(categoria.isActiva());

        return categoriaRepo.save(existente);
    }

    @Override
    public CategoriaCliente toggleActiva(UUID id) {
        CategoriaCliente categoria = categoriaRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        categoria.setActiva(!categoria.isActiva());
        return categoriaRepo.save(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoriaCliente> obtenerPorId(UUID id) {
        return categoriaRepo.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoriaCliente> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return Optional.empty();
        }
        return categoriaRepo.findByNombreIgnoreCase(nombre.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaCliente> listarActivas() {
        return categoriaRepo.findByActivaTrueOrderByNombreAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaCliente> listarTodas() {
        return categoriaRepo.findAllByOrderByActivaDescNombreAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaCliente> buscar(String texto) {
        if (texto == null || texto.isBlank()) {
            return listarActivas();
        }
        return categoriaRepo.findByNombreContainingIgnoreCaseOrderByNombreAsc(texto.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existe(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return false;
        }
        return categoriaRepo.existsByNombreIgnoreCaseAndActivaTrue(nombre.trim());
    }

    @Override
    public void eliminar(UUID id) {
        // Verificar que no haya clientes con esta categoría
        CategoriaCliente categoria = categoriaRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        long clientesConCategoria = personaRepo.countByEsClienteTrueAndCategoriaClienteId(id);
        if (clientesConCategoria > 0) {
            throw new IllegalArgumentException(
                "No se puede eliminar: hay " + clientesConCategoria + " cliente(s) con esta categoría. Desactívala en su lugar."
            );
        }

        categoriaRepo.deleteById(id);
    }
}
