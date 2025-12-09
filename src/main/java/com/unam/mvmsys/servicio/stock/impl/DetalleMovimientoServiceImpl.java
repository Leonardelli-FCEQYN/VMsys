package com.unam.mvmsys.servicio.stock.impl;

import com.unam.mvmsys.entidad.stock.DetalleMovimiento;
import com.unam.mvmsys.repositorio.stock.DetalleMovimientoRepository;
import com.unam.mvmsys.servicio.stock.DetalleMovimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DetalleMovimientoServiceImpl implements DetalleMovimientoService {

    private final DetalleMovimientoRepository detalleRepo;

    @Override
    @Transactional
    public DetalleMovimiento crear(DetalleMovimiento detalle) {
        if (detalle == null) {
            throw new IllegalArgumentException("El detalle no puede ser nulo");
        }
        
        if (detalle.getMovimiento() == null) {
            throw new IllegalArgumentException("El movimiento es requerido");
        }
        
        if (detalle.getLote() == null) {
            throw new IllegalArgumentException("El lote es requerido");
        }
        
        if (detalle.getCantidad() == null || detalle.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        
        return detalleRepo.save(detalle);
    }

    @Override
    @Transactional
    public DetalleMovimiento actualizar(DetalleMovimiento detalle) {
        if (detalle == null || detalle.getId() == null) {
            throw new IllegalArgumentException("El detalle y su ID no pueden ser nulos");
        }
        
        if (!detalleRepo.existsById(detalle.getId())) {
            throw new IllegalArgumentException("No se puede actualizar. El detalle no existe");
        }
        
        if (detalle.getCantidad() == null || detalle.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        
        return detalleRepo.save(detalle);
    }

    @Override
    @Transactional
    public void eliminar(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        
        if (!detalleRepo.existsById(id)) {
            throw new IllegalArgumentException("Detalle no encontrado");
        }
        
        detalleRepo.deleteById(id);
    }

    @Override
    public Optional<DetalleMovimiento> buscarPorId(UUID id) {
        return detalleRepo.findById(id);
    }

    @Override
    public List<DetalleMovimiento> listarPorMovimiento(UUID movimientoId) {
        if (movimientoId == null) {
            throw new IllegalArgumentException("El ID del movimiento no puede ser nulo");
        }
        return detalleRepo.findByMovimientoId(movimientoId);
    }

    @Override
    public List<DetalleMovimiento> listarPorLote(UUID loteId) {
        if (loteId == null) {
            throw new IllegalArgumentException("El ID del lote no puede ser nulo");
        }
        return detalleRepo.findByLoteId(loteId);
    }
}
