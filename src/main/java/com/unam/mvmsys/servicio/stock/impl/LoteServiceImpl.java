package com.unam.mvmsys.servicio.stock.impl;

import com.unam.mvmsys.entidad.stock.Lote;
import com.unam.mvmsys.repositorio.stock.LoteRepository;
import com.unam.mvmsys.servicio.stock.LoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class LoteServiceImpl implements LoteService {

    private final LoteRepository loteRepository;

    @Override
    @Transactional
    public Lote crearLote(Lote lote) {
        if (lote == null || lote.getCodigo() == null || lote.getCodigo().isBlank()) {
            throw new IllegalArgumentException("El código del lote es obligatorio");
        }

        if (lote.getProducto() == null) {
            throw new IllegalArgumentException("El producto es obligatorio");
        }

        if (lote.getEstado() == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }

        if (loteRepository.findByCodigo(lote.getCodigo()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un lote con el código: " + lote.getCodigo());
        }

        return loteRepository.save(lote);
    }

    @Override
    @Transactional
    public Lote actualizarLote(Lote lote) {
        if (lote == null || lote.getId() == null) {
            throw new IllegalArgumentException("Lote inválido");
        }

        Lote existente = loteRepository.findById(lote.getId())
                .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado"));

        existente.setCodigo(lote.getCodigo());
        existente.setProducto(lote.getProducto());
        existente.setEstado(lote.getEstado());
        existente.setFechaVencimiento(lote.getFechaVencimiento());
        existente.setCostoUnitarioPromedio(lote.getCostoUnitarioPromedio());
        existente.setObservaciones(lote.getObservaciones());

        return loteRepository.save(existente);
    }

    @Override
    @Transactional
    public void eliminarLote(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID de lote no válido");
        }

        Lote lote = loteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado"));

        // Para Lote usamos hard delete si no tiene soft delete configurado
        // Si en futuro se implementa soft delete en Lote, cambiar a: lote.setActivo(false)
        loteRepository.delete(lote);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lote> listarTodos() {
        return loteRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lote> listarActivos() {
        // Si Lote no tiene campo activo, retornamos todos
        // TODO: Implementar si se agrega campo activo a Lote
        return loteRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Lote> buscarPorId(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return loteRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Lote> buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        return loteRepository.findByCodigo(codigo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lote> listarPorProducto(UUID productoId) {
        if (productoId == null) {
            return List.of();
        }
        return StreamSupport.stream(
                loteRepository.findByProductoId(productoId).spliterator(),
                false
        ).toList();
    }
}
