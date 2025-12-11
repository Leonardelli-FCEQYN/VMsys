package com.unam.mvmsys.repositorio.stock;

import com.unam.mvmsys.entidad.stock.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoteRepository extends JpaRepository<Lote, UUID> {
    Optional<Lote> findByCodigo(String codigo);
    // Para buscar todos los lotes de un producto específico
    Iterable<Lote> findByProductoId(UUID productoId);
    List<Lote> findByProductoIdAndEstadoNombreOrderByFechaVencimientoAsc(UUID productoId, String nombreEstado);
}