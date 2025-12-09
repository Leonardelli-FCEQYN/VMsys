package com.unam.mvmsys.repositorio.configuracion;

import com.unam.mvmsys.entidad.configuracion.TipoProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TipoProductoRepository extends JpaRepository<TipoProducto, UUID> {
    Optional<TipoProducto> findByNombre(String nombre);
}