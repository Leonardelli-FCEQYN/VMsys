package com.unam.mvmsys.repositorio.seguridad;

import com.unam.mvmsys.entidad.seguridad.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, UUID> {
    Optional<Persona> findByCuitDni(String cuitDni);
    boolean existsByCuitDni(String cuitDni);
    long countByEsClienteTrueAndCategoriaClienteId(java.util.UUID categoriaClienteId);
}