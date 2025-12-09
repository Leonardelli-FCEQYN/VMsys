package com.unam.mvmsys.repositorio.configuracion;

import com.unam.mvmsys.entidad.configuracion.CategoriaCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoriaClienteRepository extends JpaRepository<CategoriaCliente, UUID> {

    /**
     * Busca categoría por nombre (case-insensitive)
     */
    Optional<CategoriaCliente> findByNombreIgnoreCase(String nombre);

    /**
     * Lista categorías activas ordenadas por nombre
     */
    List<CategoriaCliente> findByActivaTrueOrderByNombreAsc();

    /**
     * Lista todas las categorías (incluyendo inactivas)
     */
    List<CategoriaCliente> findAllByOrderByActivaDescNombreAsc();

    /**
     * Verifica si existe una categoría activa con ese nombre
     */
    boolean existsByNombreIgnoreCaseAndActivaTrue(String nombre);

    /**
     * Busca por nombre parcial
     */
    List<CategoriaCliente> findByNombreContainingIgnoreCaseOrderByNombreAsc(String texto);
}
