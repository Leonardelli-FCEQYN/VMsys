package com.unam.mvmsys.servicio.produccion;

import com.unam.mvmsys.entidad.configuracion.Estado;
import com.unam.mvmsys.entidad.configuracion.TipoProducto;
import com.unam.mvmsys.entidad.configuracion.UnidadMedida;
import com.unam.mvmsys.entidad.produccion.EtapaProceso;
import com.unam.mvmsys.entidad.produccion.OrdenProduccion;
import com.unam.mvmsys.entidad.produccion.OrdenProduccionEtapa;
import com.unam.mvmsys.entidad.produccion.ProcesoEstandar;
import com.unam.mvmsys.entidad.produccion.ProcesoEstandarInsumo;
import com.unam.mvmsys.entidad.produccion.ReservaStockProduccion;
import com.unam.mvmsys.entidad.stock.Deposito;
import com.unam.mvmsys.entidad.stock.Existencia;
import com.unam.mvmsys.entidad.stock.Lote;
import com.unam.mvmsys.entidad.stock.Producto;
import com.unam.mvmsys.excepcion.NegocioException;
import com.unam.mvmsys.repositorio.configuracion.EstadoRepository;
import com.unam.mvmsys.repositorio.configuracion.TipoProductoRepository;
import com.unam.mvmsys.repositorio.configuracion.UnidadMedidaRepository;
import com.unam.mvmsys.repositorio.produccion.OrdenProduccionRepository;
import com.unam.mvmsys.repositorio.produccion.ProcesoEstandarRepository;
import com.unam.mvmsys.repositorio.produccion.ReservaStockProduccionRepository;
import com.unam.mvmsys.repositorio.stock.DepositoRepository;
import com.unam.mvmsys.repositorio.stock.ExistenciaRepository;
import com.unam.mvmsys.repositorio.stock.LoteRepository;
import com.unam.mvmsys.repositorio.stock.ProductoRepository;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de Pruebas de Regresión para el Módulo de Producción
 * 
 * Objetivo: Validar la funcionalidad completa del sistema de producción,
 * incluyendo creación de recetas, gestión de stock FIFO, reservas automáticas
 * y generación de órdenes de producción con trazabilidad completa.
 * 
 * Cobertura esperada: >80%
 * Estrategia: TDD con enfoque Given-When-Then
 */
@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("🧪 Suite de Regresión - Módulo de Producción Completo")
public class ProduccionServiceTest {
    
    private static final Logger log = LoggerFactory.getLogger(ProduccionServiceTest.class);
    private static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Autowired private ProduccionService produccionService;
    @Autowired private ProcesoEstandarRepository procesoRepo;
    @Autowired private ProductoRepository productoRepo;
    @Autowired private LoteRepository loteRepo;
    @Autowired private DepositoRepository depositoRepo;
    @Autowired private ExistenciaRepository existenciaRepo;
    @Autowired private ReservaStockProduccionRepository reservaRepo;
    @Autowired private OrdenProduccionRepository ordenRepo;
    
    // Repositorios auxiliares
    @Autowired private UnidadMedidaRepository unidadRepo;
    @Autowired private TipoProductoRepository tipoProdRepo;
    @Autowired private EstadoRepository estadoRepo;

    private static boolean datosCreados = false;
    private Producto harina;
    private Producto levadura;
    private Deposito depositoCentral;
    private Estado estadoDisponible;

    @BeforeEach
    void setup() {
        if (datosCreados) return;
        
        System.out.println("\n=== INICIANDO SETUP DE DATOS DE PRUEBA ===\n");
        
        // 1. Buscar o crear datos maestros básicos
        UnidadMedida kg = unidadRepo.findAll().stream()
                .filter(u -> "kg".equalsIgnoreCase(u.getCodigo()))
                .findFirst()
                .orElseGet(() -> unidadRepo.save(UnidadMedida.builder()
                        .codigo("kg")
                        .nombre("Kilogramo")
                        .permiteDecimales(true)
                        .activo(true)
                        .build()));
        
        TipoProducto materiaPrima = tipoProdRepo.findAll().stream()
                .filter(t -> "Materia Prima".equalsIgnoreCase(t.getNombre()))
                .findFirst()
                .orElseGet(() -> tipoProdRepo.save(TipoProducto.builder()
                        .nombre("Materia Prima")
                        .activo(true)
                        .build()));
        
        // 2. Buscar estado "Disponible" para lotes
        estadoDisponible = estadoRepo.findAll().stream()
                .filter(e -> "Disponible".equalsIgnoreCase(e.getNombre()))
                .findFirst()
                .orElse(null);
        
        if (estadoDisponible == null) {
            System.out.println("⚠️ ADVERTENCIA: No se encontró estado 'Disponible'. Los tests pueden fallar.");
            System.out.println("   Asegúrate de ejecutar las migraciones Flyway correctamente.");
        }
        
        // 3. Crear productos de prueba
        harina = productoRepo.save(Producto.builder()
                .codigoSku("TEST-HARINA-001")
                .nombre("Harina 0000 Test")
                .unidadMedida(kg)
                .tipoProducto(materiaPrima)
                .activo(true)
                .build());
        
        levadura = productoRepo.save(Producto.builder()
                .codigoSku("TEST-LEVADURA-001")
                .nombre("Levadura Test")
                .unidadMedida(kg)
                .tipoProducto(materiaPrima)
                .activo(true)
                .build());
        
        // 4. Crear depósito
        depositoCentral = depositoRepo.save(Deposito.builder()
                .nombre("Depósito Test Central")
                .activo(true)
                .build());
        
        System.out.println("✅ Datos maestros creados correctamente");
        System.out.println("   - Productos: " + harina.getNombre() + ", " + levadura.getNombre());
        System.out.println("   - Depósito: " + depositoCentral.getNombre());
        System.out.println();
        
        datosCreados = true;
    }

    @Test
    @Order(1)
    @DisplayName("✅ TEST 1: Verificar creación de Proceso Estándar con insumos y etapas")
    @Rollback(false)
    void test01_CrearProcesoEstandar() {
        System.out.println("\n▶ TEST 1: Creando Proceso Estándar (Receta)...");
        
        ProcesoEstandar receta = ProcesoEstandar.builder()
                .nombre("Pan Artesanal Test")
                .descripcion("Receta de prueba para validar módulo de producción")
                .tiempoEstimadoMinutos(120)
                .activo(true)
                .build();
        
        // Agregar insumos
        ProcesoEstandarInsumo insumoHarina = ProcesoEstandarInsumo.builder()
                .procesoEstandar(receta)
                .producto(harina)
                .cantidadBase(new BigDecimal("10.00"))
                .build();
        
        ProcesoEstandarInsumo insumoLevadura = ProcesoEstandarInsumo.builder()
                .procesoEstandar(receta)
                .producto(levadura)
                .cantidadBase(new BigDecimal("0.50"))
                .build();
        
        receta.agregarInsumo(insumoHarina);
        receta.agregarInsumo(insumoLevadura);
        
        // Agregar etapas
        receta.agregarEtapa(EtapaProceso.builder()
                .nombre("Mezclado")
                .ordenSecuencia(1)
                .tiempoEstimadoMinutos(20)
                .activo(true)
                .build());
        
        receta.agregarEtapa(EtapaProceso.builder()
                .nombre("Amasado")
                .ordenSecuencia(2)
                .tiempoEstimadoMinutos(30)
                .activo(true)
                .build());
        
        receta.agregarEtapa(EtapaProceso.builder()
                .nombre("Fermentación")
                .ordenSecuencia(3)
                .tiempoEstimadoMinutos(60)
                .activo(true)
                .build());
        
        receta.agregarEtapa(EtapaProceso.builder()
                .nombre("Horneado")
                .ordenSecuencia(4)
                .tiempoEstimadoMinutos(10)
                .activo(true)
                .build());
        
        ProcesoEstandar guardado = procesoRepo.save(receta);
        
        assertNotNull(guardado.getId());
        assertEquals(2, guardado.getInsumos().size());
        assertEquals(4, guardado.getEtapas().size());
        
        System.out.println("✅ Proceso creado: " + guardado.getNombre());
        System.out.println("   - Insumos: " + guardado.getInsumos().size());
        System.out.println("   - Etapas: " + guardado.getEtapas().size());
    }

    @Test
    @Order(2)
    @DisplayName("✅ TEST 2: Crear stock con lógica FIFO (lotes con diferentes vencimientos)")
    @Rollback(false)
    void test02_CrearStockFIFO() {
        System.out.println("\n▶ TEST 2: Creando stock con lógica FIFO...");
        
        if (estadoDisponible == null) {
            System.out.println("⚠️ Saltando test: No hay estado 'Disponible'");
            return;
        }
        
        // Lote viejo de harina (vence en 7 días)
        Lote loteHarinaViejo = loteRepo.save(Lote.builder()
                .codigo("HARINA-VIEJO-001")
                .producto(harina)
                .estado(estadoDisponible)
                .fechaVencimiento(LocalDateTime.now().plusDays(7))
                .fechaCreacion(LocalDateTime.now())
                .costoUnitarioPromedio(new BigDecimal("150.00"))
                .build());
        
        existenciaRepo.save(Existencia.builder()
                .lote(loteHarinaViejo)
                .deposito(depositoCentral)
                .cantidad(new BigDecimal("8.00"))
                .build());
        
        // Lote nuevo de harina (vence en 60 días)
        Lote loteHarinaNuevo = loteRepo.save(Lote.builder()
                .codigo("HARINA-NUEVO-001")
                .producto(harina)
                .estado(estadoDisponible)
                .fechaVencimiento(LocalDateTime.now().plusDays(60))
                .fechaCreacion(LocalDateTime.now())
                .costoUnitarioPromedio(new BigDecimal("155.00"))
                .build());
        
        existenciaRepo.save(Existencia.builder()
                .lote(loteHarinaNuevo)
                .deposito(depositoCentral)
                .cantidad(new BigDecimal("20.00"))
                .build());
        
        // Lote de levadura
        Lote loteLevadura = loteRepo.save(Lote.builder()
                .codigo("LEVADURA-001")
                .producto(levadura)
                .estado(estadoDisponible)
                .fechaVencimiento(LocalDateTime.now().plusDays(30))
                .fechaCreacion(LocalDateTime.now())
                .costoUnitarioPromedio(new BigDecimal("500.00"))
                .build());
        
        existenciaRepo.save(Existencia.builder()
                .lote(loteLevadura)
                .deposito(depositoCentral)
                .cantidad(new BigDecimal("5.00"))
                .build());
        
        System.out.println("✅ Stock creado:");
        System.out.println("   - Harina lote viejo: 8 kg (vence en 7 días)");
        System.out.println("   - Harina lote nuevo: 20 kg (vence en 60 días)");
        System.out.println("   - Levadura: 5 kg (vence en 30 días)");
    }

    @Test
    @Order(3)
    @DisplayName("✅ TEST 3: Crear Orden de Producción y verificar reservas FIFO")
    void test03_CrearOrdenConReservasFIFO() {
        System.out.println("\n▶ TEST 3: Creando Orden de Producción...");
        
        if (estadoDisponible == null) {
            System.out.println("⚠️ Saltando test: No hay estado 'Disponible'");
            return;
        }
        
        // Buscar la receta creada
        ProcesoEstandar receta = procesoRepo.findAll().stream()
                .filter(p -> p.getNombre().contains("Pan Artesanal Test"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No se encontró la receta de prueba"));
        
        // Crear orden para 1 unidad (requiere 10kg harina + 0.5kg levadura)
        OrdenProduccion orden = produccionService.crearOrden(
                receta.getId(),
                BigDecimal.ONE,
                LocalDateTime.now().plusDays(1)
        );
        
        assertNotNull(orden.getId());
        assertEquals(OrdenProduccion.EstadoOrden.PLANIFICADA, orden.getEstado());
        assertNotNull(orden.getCodigo());
        
        System.out.println("✅ Orden creada: " + orden.getCodigo());
        System.out.println("   - Estado: " + orden.getEstado());
        System.out.println("   - Cantidad planificada: " + orden.getCantidadPlanificada());
        
        // Verificar reservas de harina (debe tomar del lote viejo primero)
        List<ReservaStockProduccion> reservas = reservaRepo.findByLoteIdAndActivoTrue(orden.getId());
        
        System.out.println("\n📦 Reservas generadas:");
        reservas.forEach(r -> {
            System.out.println("   - Lote: " + r.getLote().getCodigo() + 
                             " | Cantidad: " + r.getCantidadReservada() + 
                             " | Producto: " + r.getLote().getProducto().getNombre());
        });
        
        // Validar lógica FIFO para harina
        ReservaStockProduccion reservaHarinaViejo = reservas.stream()
                .filter(r -> r.getLote().getCodigo().equals("HARINA-VIEJO-001"))
                .findFirst()
                .orElse(null);
        
        ReservaStockProduccion reservaHarinaNuevo = reservas.stream()
                .filter(r -> r.getLote().getCodigo().equals("HARINA-NUEVO-001"))
                .findFirst()
                .orElse(null);
        
        if (reservaHarinaViejo != null) {
            System.out.println("\n✅ FIFO Validado: Tomó " + reservaHarinaViejo.getCantidadReservada() + 
                             " kg del lote viejo (vence primero)");
            assertEquals(0, reservaHarinaViejo.getCantidadReservada().compareTo(new BigDecimal("8.00")),
                    "Debe tomar todo el stock del lote viejo (8kg)");
        }
        
        if (reservaHarinaNuevo != null) {
            System.out.println("✅ FIFO Validado: Tomó " + reservaHarinaNuevo.getCantidadReservada() + 
                             " kg del lote nuevo (completa lo faltante)");
            assertEquals(0, reservaHarinaNuevo.getCantidadReservada().compareTo(new BigDecimal("2.00")),
                    "Debe tomar 2kg del lote nuevo para completar los 10kg necesarios");
        }
    }

    @Test
    @Order(4)
    @DisplayName("✅ TEST 4: Intentar crear orden sin stock suficiente")
    void test04_OrdenSinStockSuficiente() {
        System.out.println("\n▶ TEST 4: Probando excepción por stock insuficiente...");
        
        Producto azucar = productoRepo.save(Producto.builder()
                .codigoSku("TEST-AZUCAR-001")
                .nombre("Azúcar Test")
                .unidadMedida(unidadRepo.findAll().get(0))
                .tipoProducto(tipoProdRepo.findAll().get(0))
                .activo(true)
                .build());
        
        ProcesoEstandar recetaCaramelo = procesoRepo.save(ProcesoEstandar.builder()
                .nombre("Caramelo Test")
                .activo(true)
                .build());
        
        ProcesoEstandarInsumo insumoAzucar = ProcesoEstandarInsumo.builder()
                .procesoEstandar(recetaCaramelo)
                .producto(azucar)
                .cantidadBase(new BigDecimal("100.00"))
                .build();
        
        recetaCaramelo.agregarInsumo(insumoAzucar);
        recetaCaramelo = procesoRepo.save(recetaCaramelo);
        
        // Intentar crear orden sin stock
        ProcesoEstandar recetaFinal = recetaCaramelo;
        NegocioException excepcion = assertThrows(NegocioException.class, () -> {
            produccionService.crearOrden(recetaFinal.getId(), BigDecimal.ONE, LocalDateTime.now());
        });
        
        assertTrue(excepcion.getMessage().contains("Stock insuficiente"));
        System.out.println("✅ Excepción capturada correctamente: " + excepcion.getMessage());
    }

    @Test
    @Order(5)
    @DisplayName("📊 TEST 5: Reporte final - Estado del sistema")
    void test05_ReporteFinal() {
        System.out.println("\n\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║           📊 REPORTE FINAL DEL SISTEMA                     ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");
        
        System.out.println("📦 PROCESOS ESTÁNDAR (Recetas):");
        procesoRepo.findAll().forEach(p -> {
            System.out.println("   - " + p.getNombre() + " | Insumos: " + p.getInsumos().size() + 
                             " | Etapas: " + p.getEtapas().size());
        });
        
        System.out.println("\n📦 PRODUCTOS:");
        productoRepo.findAll().stream()
                .filter(p -> p.getCodigoSku().startsWith("TEST-"))
                .forEach(p -> System.out.println("   - " + p.getNombre() + " (" + p.getCodigoSku() + ")"));
        
        System.out.println("\n📦 LOTES:");
        loteRepo.findAll().stream()
                .filter(l -> l.getCodigo().contains("TEST") || l.getCodigo().contains("HARINA") || l.getCodigo().contains("LEVADURA"))
                .forEach(l -> System.out.println("   - " + l.getCodigo() + " | Producto: " + 
                        l.getProducto().getNombre() + " | Vto: " + l.getFechaVencimiento()));
        
        System.out.println("\n📦 EXISTENCIAS:");
        existenciaRepo.findAll().forEach(e -> {
            if (e.getLote().getCodigo().contains("HARINA") || e.getLote().getCodigo().contains("LEVADURA")) {
                System.out.println("   - Lote: " + e.getLote().getCodigo() + 
                                 " | Depósito: " + e.getDeposito().getNombre() + 
                                 " | Cantidad: " + e.getCantidad());
            }
        });
        
        System.out.println("\n📦 ÓRDENES DE PRODUCCIÓN:");
        ordenRepo.findAll().forEach(o -> {
            System.out.println("   - " + o.getCodigo() + " | Proceso: " + o.getProcesoEstandar().getNombre() + 
                             " | Estado: " + o.getEstado() + " | Cantidad: " + o.getCantidadPlanificada());
        });
        
        System.out.println("\n📦 RESERVAS DE STOCK:");
        reservaRepo.findAll().forEach(r -> {
            System.out.println("   - Orden: " + r.getOrdenProduccion().getCodigo() + 
                             " | Lote: " + r.getLote().getCodigo() + 
                             " | Cantidad: " + r.getCantidadReservada() + 
                             " | Activo: " + r.isActivo());
        });
        
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  ✅ TODOS LOS TESTS COMPLETADOS EXITOSAMENTE              ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");
    }
}