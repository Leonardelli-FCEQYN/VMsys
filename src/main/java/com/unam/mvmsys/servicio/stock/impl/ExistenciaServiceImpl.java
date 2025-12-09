package com.unam.mvmsys.servicio.stock.impl;

import com.unam.mvmsys.entidad.stock.Existencia;
import com.unam.mvmsys.repositorio.stock.ExistenciaRepository;
import com.unam.mvmsys.servicio.stock.ExistenciaService;
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
public class ExistenciaServiceImpl implements ExistenciaService {

    private final ExistenciaRepository existenciaRepo;

    @Override
    @Transactional
    public Existencia crear(Existencia existencia) {
        if (existencia == null) {
            throw new IllegalArgumentException("La existencia no puede ser nula");
        }
        
        if (existencia.getLote() == null) {
            throw new IllegalArgumentException("El lote es requerido");
        }
        
        if (existencia.getDeposito() == null) {
            throw new IllegalArgumentException("El depósito es requerido");
        }
        
        if (existencia.getCantidad() == null || existencia.getCantidad().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        
        // Validar que no exista ya una combinación lote-depósito
        Optional<Existencia> existente = existenciaRepo.findByLoteIdAndDepositoId(
            existencia.getLote().getId(),
            existencia.getDeposito().getId()
        );
        
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Ya existe stock para este lote en este depósito");
        }
        
        return existenciaRepo.save(existencia);
    }

    @Override
    @Transactional
    public Existencia actualizar(Existencia existencia) {
        if (existencia == null || existencia.getId() == null) {
            throw new IllegalArgumentException("La existencia y su ID no pueden ser nulos");
        }
        
        if (!existenciaRepo.existsById(existencia.getId())) {
            throw new IllegalArgumentException("No se puede actualizar. La existencia no existe");
        }
        
        if (existencia.getCantidad() == null || existencia.getCantidad().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        
        return existenciaRepo.save(existencia);
    }

    @Override
    @Transactional
    public void eliminar(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        
        if (!existenciaRepo.existsById(id)) {
            throw new IllegalArgumentException("Existencia no encontrada");
        }
        
        existenciaRepo.deleteById(id);
    }

    @Override
    public Optional<Existencia> buscarPorId(UUID id) {
        return existenciaRepo.findById(id);
    }

    @Override
    public Optional<Existencia> buscarPorLoteYDeposito(UUID loteId, UUID depositoId) {
        if (loteId == null || depositoId == null) {
            throw new IllegalArgumentException("Los IDs de lote y depósito no pueden ser nulos");
        }
        return existenciaRepo.findByLoteIdAndDepositoId(loteId, depositoId);
    }

    @Override
    public List<Existencia> listarPorLote(UUID loteId) {
        if (loteId == null) {
            throw new IllegalArgumentException("El ID del lote no puede ser nulo");
        }
        return existenciaRepo.findByLoteId(loteId);
    }

    @Override
    public List<Existencia> listarPorDeposito(UUID depositoId) {
        if (depositoId == null) {
            throw new IllegalArgumentException("El ID del depósito no puede ser nulo");
        }
        return existenciaRepo.findByDepositoId(depositoId);
    }

    @Override
    @Transactional
    public void incrementarStock(UUID loteId, UUID depositoId, BigDecimal cantidad) {
        if (loteId == null || depositoId == null) {
            throw new IllegalArgumentException("Los IDs de lote y depósito no pueden ser nulos");
        }
        
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad a incrementar debe ser mayor a cero");
        }
        
        Optional<Existencia> existente = existenciaRepo.findByLoteIdAndDepositoId(loteId, depositoId);
        
        if (existente.isPresent()) {
            Existencia ex = existente.get();
            ex.setCantidad(ex.getCantidad().add(cantidad));
            existenciaRepo.save(ex);
        } else {
            throw new IllegalArgumentException("No existe stock para este lote en este depósito");
        }
    }

    @Override
    @Transactional
    public void decrementarStock(UUID loteId, UUID depositoId, BigDecimal cantidad) {
        if (loteId == null || depositoId == null) {
            throw new IllegalArgumentException("Los IDs de lote y depósito no pueden ser nulos");
        }
        
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad a decrementar debe ser mayor a cero");
        }
        
        Optional<Existencia> existente = existenciaRepo.findByLoteIdAndDepositoId(loteId, depositoId);
        
        if (existente.isPresent()) {
            Existencia ex = existente.get();
            BigDecimal nuevoStock = ex.getCantidad().subtract(cantidad);
            
            if (nuevoStock.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Stock insuficiente. Disponible: " + ex.getCantidad());
            }
            
            ex.setCantidad(nuevoStock);
            existenciaRepo.save(ex);
        } else {
            throw new IllegalArgumentException("No existe stock para este lote en este depósito");
        }
    }

    @Override
    public BigDecimal obtenerCantidad(UUID loteId, UUID depositoId) {
        Optional<Existencia> existencia = buscarPorLoteYDeposito(loteId, depositoId);
        return existencia.map(Existencia::getCantidad).orElse(BigDecimal.ZERO);
    }
}
