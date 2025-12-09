package com.unam.mvmsys.repositorio.stock;

import com.unam.mvmsys.entidad.stock.Deposito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepositoRepository extends JpaRepository<Deposito, UUID> {

    Optional<Deposito> findByNombre(String nombre);

    @Query("SELECT d FROM Deposito d WHERE d.activo = true ORDER BY d.nombre")
    List<Deposito> findAllActivos();

    @Query("SELECT d FROM Deposito d WHERE d.activo = true AND d.nombre LIKE %:nombre% ORDER BY d.nombre")
    List<Deposito> findActivosByNombreLike(@Param("nombre") String nombre);
}
