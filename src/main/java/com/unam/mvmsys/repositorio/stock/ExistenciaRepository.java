package com.unam.mvmsys.repositorio.stock;

import com.unam.mvmsys.entidad.stock.Existencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExistenciaRepository extends JpaRepository<Existencia, UUID> {

    // Buscar dónde hay stock físico de un lote
    List<Existencia> findByLoteIdAndCantidadGreaterThan(UUID loteId, double cantidadMinima);

    // Buscar todas las existencias de un producto (para FIFO global)
    // Ordenamos por vencimiento del lote asociado
    @Query("SELECT e FROM Existencia e JOIN e.lote l " +
           "WHERE l.producto.id = :productoId AND l.estado.nombre = 'Disponible' AND e.cantidad > 0 " +
           "ORDER BY l.fechaVencimiento ASC")
    List<Existencia> findDisponiblesPorProductoFIFO(UUID productoId);
}