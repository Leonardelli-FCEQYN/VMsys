package com.unam.mvmsys.servicio.produccion;

import com.unam.mvmsys.entidad.produccion.*;
import com.unam.mvmsys.entidad.stock.Existencia;
import com.unam.mvmsys.excepcion.NegocioException;
import com.unam.mvmsys.repositorio.produccion.*;
import com.unam.mvmsys.servicio.stock.ExistenciaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProduccionService {

    private final ProcesoEstandarRepository procesoRepo;
    private final OrdenProduccionRepository ordenRepo;
    private final ReservaStockProduccionRepository reservaRepo;
    private final ExistenciaService existenciaService;

    @Transactional
    public OrdenProduccion crearOrden(UUID procesoEstandarId, BigDecimal cantidadAProducir, LocalDateTime fechaInicioEstimada) {
        
        // 1. Validar Receta
        ProcesoEstandar receta = procesoRepo.findById(procesoEstandarId)
                .orElseThrow(() -> new NegocioException("La receta no existe."));

        if (!receta.isActivo()) throw new NegocioException("Receta inactiva.");

        // 2. Crear Orden
        OrdenProduccion orden = OrdenProduccion.builder()
                .codigo(generarCodigoOrden())
                .procesoEstandar(receta)
                .cantidadPlanificada(cantidadAProducir)
                .fechaEmision(LocalDateTime.now())
                .fechaInicioEstimada(fechaInicioEstimada)
                .estado(OrdenProduccion.EstadoOrden.PLANIFICADA)
                .build();

        // 3. Copiar Etapas
        for (EtapaProceso pasoReceta : receta.getEtapas()) {
            orden.getEtapasSeguimiento().add(OrdenProduccionEtapa.builder()
                    .ordenProduccion(orden)
                    .etapaProceso(pasoReceta)
                    .estado(OrdenProduccionEtapa.EstadoEtapa.PENDIENTE)
                    .build());
        }

        orden = ordenRepo.save(orden);

        // 4. Reservar Stock (FIFO Multi-Depósito)
        reservarInsumos(orden, receta, cantidadAProducir);

        return orden;
    }

    private void reservarInsumos(OrdenProduccion orden, ProcesoEstandar receta, BigDecimal cantidadAProducir) {
        for (ProcesoEstandarInsumo insumo : receta.getInsumos()) {
            BigDecimal cantidadNecesaria = insumo.getCantidadBase().multiply(cantidadAProducir);
            reservarStockProducto(orden, insumo.getProducto().getId(), cantidadNecesaria);
        }
    }

    private void reservarStockProducto(OrdenProduccion orden, UUID productoId, BigDecimal cantidadNecesaria) {
        // Obtenemos las EXISTENCIAS físicas reales ordenadas por vencimiento del lote
        List<Existencia> existenciasDisponibles = existenciaService.buscarDisponiblesPorProductoFIFO(productoId);
        BigDecimal cantidadFaltante = cantidadNecesaria;

        for (Existencia existencia : existenciasDisponibles) {
            if (cantidadFaltante.compareTo(BigDecimal.ZERO) <= 0) break;

            // Calculamos cuánto de esta existencia física ya está "prometida" a otras órdenes
            BigDecimal reservadoEnEsteLote = calcularReservasActivasLote(existencia.getLote().getId());
            
            // Disponible Real = Físico - Reservado
            BigDecimal disponibleReal = existencia.getCantidad().subtract(reservadoEnEsteLote);

            if (disponibleReal.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal aReservar = disponibleReal.min(cantidadFaltante);

                ReservaStockProduccion reserva = ReservaStockProduccion.builder()
                        .ordenProduccion(orden)
                        .lote(existencia.getLote()) // Vinculamos al LOTE (Trazabilidad)
                        .cantidadReservada(aReservar)
                        .activo(true)
                        .build();

                reservaRepo.save(reserva);
                cantidadFaltante = cantidadFaltante.subtract(aReservar);
            }
        }

        if (cantidadFaltante.compareTo(BigDecimal.ZERO) > 0) {
            throw new NegocioException("Stock insuficiente para el producto ID: " + productoId + ". Faltan: " + cantidadFaltante);
        }
    }

    private BigDecimal calcularReservasActivasLote(UUID loteId) {
        return reservaRepo.findByLoteIdAndActivoTrue(loteId).stream()
                .map(ReservaStockProduccion::getCantidadReservada)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generarCodigoOrden() {
        return "OP-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd-HHmm"));
    }
}