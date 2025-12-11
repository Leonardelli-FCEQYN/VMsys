package com.unam.mvmsys.servicio.stock.impl;

import com.unam.mvmsys.entidad.stock.Existencia;
import com.unam.mvmsys.repositorio.stock.ExistenciaRepository;
import com.unam.mvmsys.servicio.stock.ExistenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExistenciaServiceImpl implements ExistenciaService {

    private final ExistenciaRepository existenciaRepo;

    @Override
    @Transactional
    public Existencia guardar(Existencia existencia) {
        return existenciaRepo.save(existencia);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Existencia> buscarDisponiblesPorProductoFIFO(UUID productoId) {
        // Llama al método optimizado del repositorio (Query JPQL)
        return existenciaRepo.findDisponiblesPorProductoFIFO(productoId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal consultarStockTotalProducto(UUID productoId) {
        // Sumar todas las existencias disponibles de ese producto
        return existenciaRepo.findDisponiblesPorProductoFIFO(productoId).stream()
                .map(Existencia::getCantidad)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public Existencia buscarPorLoteYDeposito(UUID loteId, UUID depositoId) {
        // Podrías necesitar agregar 'findByLoteIdAndDepositoId' en el repositorio
        // Si no existe, puedes implementar una búsqueda simple o lanzar excepción.
        // Por ahora retornamos null o implementamos la query en el repo.
        return null; 
    }
}