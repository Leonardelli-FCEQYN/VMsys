package com.unam.mvmsys.repositorio.configuracion;

import com.unam.mvmsys.entidad.configuracion.EntidadSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EntidadSistemaRepository extends JpaRepository<EntidadSistema, UUID> {
    Optional<EntidadSistema> findByCodigo(String codigo);
}