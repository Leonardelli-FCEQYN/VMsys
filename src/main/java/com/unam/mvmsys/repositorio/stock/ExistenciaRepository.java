package com.unam.mvmsys.repositorio.stock;

import com.unam.mvmsys.entidad.stock.Existencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExistenciaRepository extends JpaRepository<Existencia, UUID> {
    
    Optional<Existencia> findByLoteIdAndDepositoId(UUID loteId, UUID depositoId);
    
    List<Existencia> findByLoteId(UUID loteId);
    
    List<Existencia> findByDepositoId(UUID depositoId);
}
