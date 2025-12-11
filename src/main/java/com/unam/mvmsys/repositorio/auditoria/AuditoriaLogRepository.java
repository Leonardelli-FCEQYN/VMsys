package com.unam.mvmsys.repositorio.auditoria;

import com.unam.mvmsys.entidad.auditoria.AuditoriaLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditoriaLogRepository extends JpaRepository<AuditoriaLog, UUID> {

    // Ver el historial completo de una entidad específica (Ej: Todos los cambios del Pedido X)
    List<AuditoriaLog> findByEntidadNombreAndEntidadIdOrderByFechaDesc(String entidadNombre, UUID entidadId);

    // Ver qué hizo un usuario específico
    List<AuditoriaLog> findByUsuarioIdOrderByFechaDesc(UUID usuarioId);

    // Logs de un rango de fechas (para reportes de seguridad)
    List<AuditoriaLog> findByFechaBetweenOrderByFechaDesc(LocalDateTime inicio, LocalDateTime fin);
}