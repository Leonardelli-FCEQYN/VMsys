package com.unam.mvmsys.servicio.stock.impl;

import com.unam.mvmsys.entidad.stock.MovimientoStock;
import com.unam.mvmsys.repositorio.stock.MovimientoStockRepository;
import com.unam.mvmsys.servicio.stock.MovimientoStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovimientoStockServiceImpl implements MovimientoStockService {

    private final MovimientoStockRepository movimientoRepo;

    @Override
    @Transactional
    public MovimientoStock crearMovimiento(MovimientoStock movimiento) {
        // Validaciones básicas
        if (movimiento == null) {
            throw new IllegalArgumentException("El movimiento no puede ser nulo");
        }
        
        if (movimiento.getTipoMovimiento() == null || movimiento.getTipoMovimiento().isEmpty()) {
            throw new IllegalArgumentException("El tipo de movimiento es requerido");
        }
        
        if (movimiento.getConcepto() == null || movimiento.getConcepto().isEmpty()) {
            throw new IllegalArgumentException("El concepto es requerido");
        }
        
        if (movimiento.getFecha() == null) {
            movimiento.setFecha(LocalDateTime.now());
        }
        
        return movimientoRepo.save(movimiento);
    }

    @Override
    @Transactional
    public MovimientoStock actualizarMovimiento(MovimientoStock movimiento) {
        if (movimiento == null || movimiento.getId() == null) {
            throw new IllegalArgumentException("El movimiento y su ID no pueden ser nulos");
        }
        
        if (!movimientoRepo.existsById(movimiento.getId())) {
            throw new IllegalArgumentException("No se puede actualizar. El movimiento no existe");
        }
        
        return movimientoRepo.save(movimiento);
    }

    @Override
    @Transactional
    public void eliminarMovimiento(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        
        MovimientoStock movimiento = movimientoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Movimiento no encontrado"));
        
        // Soft delete
        movimiento.setActivo(false);
        movimientoRepo.save(movimiento);
    }

    @Override
    public Optional<MovimientoStock> buscarPorId(UUID id) {
        return movimientoRepo.findById(id);
    }

    @Override
    public List<MovimientoStock> listarTodos() {
        return movimientoRepo.findAll();
    }

    @Override
    public Page<MovimientoStock> listarPaginado(Pageable pageable) {
        return movimientoRepo.findByActivoTrue(pageable);
    }

    @Override
    public List<MovimientoStock> listarPorTipo(String tipoMovimiento) {
        if (tipoMovimiento == null || tipoMovimiento.isEmpty()) {
            throw new IllegalArgumentException("El tipo de movimiento no puede estar vacío");
        }
        return movimientoRepo.findByTipoMovimiento(tipoMovimiento);
    }

    @Override
    public List<MovimientoStock> listarPorConcepto(String concepto) {
        if (concepto == null || concepto.isEmpty()) {
            throw new IllegalArgumentException("El concepto no puede estar vacío");
        }
        return movimientoRepo.findByConcepto(concepto);
    }

    @Override
    public List<MovimientoStock> listarPorReferencia(String referencia) {
        if (referencia == null || referencia.isEmpty()) {
            throw new IllegalArgumentException("La referencia no puede estar vacía");
        }
        return movimientoRepo.findByReferenciaComprobante(referencia);
    }

    @Override
    public Page<MovimientoStock> listarPorFechas(LocalDateTime inicio, LocalDateTime fin, Pageable pageable) {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son requeridas");
        }
        
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha final");
        }
        
        return movimientoRepo.findByFechaBetween(inicio, fin, pageable);
    }
}
