package com.unam.mvmsys.repositorio.comercial;

import com.unam.mvmsys.entidad.comercial.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, UUID> {
    Optional<Pedido> findByCodigo(String codigo);
}