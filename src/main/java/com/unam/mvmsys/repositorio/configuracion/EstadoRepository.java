package com.unam.mvmsys.repositorio.configuracion;

import com.unam.mvmsys.entidad.configuracion.EntidadSistema;
import com.unam.mvmsys.entidad.configuracion.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, UUID> {
    Optional<Estado> findByNombreAndEntidadSistema(String nombre, EntidadSistema entidad);
    List<Estado> findByEntidadSistemaCodigo(String codigoEntidad); // Ej: Buscar estados de 'PEDIDO'
}