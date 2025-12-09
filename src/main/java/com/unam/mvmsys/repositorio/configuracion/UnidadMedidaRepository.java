package com.unam.mvmsys.repositorio.configuracion;

import com.unam.mvmsys.entidad.configuracion.UnidadMedida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UnidadMedidaRepository extends JpaRepository<UnidadMedida, UUID> {
    Optional<UnidadMedida> findByCodigo(String codigo);
}