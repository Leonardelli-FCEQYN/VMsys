package com.unam.mvmsys.repositorio.stock;

import com.unam.mvmsys.entidad.stock.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, UUID> {
    Optional<Producto> findByCodigoSku(String codigoSku);
    boolean existsByCodigoSku(String codigoSku);
}