package com.unam.mvmsys.repositorio.seguridad;

import com.unam.mvmsys.entidad.seguridad.RolUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RolUsuarioRepository extends JpaRepository<RolUsuario, UUID> {
    Optional<RolUsuario> findByNombre(String nombre);
}