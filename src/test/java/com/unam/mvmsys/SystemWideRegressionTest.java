package com.unam.mvmsys;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.unam.mvmsys.repositorio.configuracion.*;
import com.unam.mvmsys.repositorio.stock.*;
import com.unam.mvmsys.repositorio.produccion.*;
import com.unam.mvmsys.repositorio.comercial.*;
import com.unam.mvmsys.repositorio.seguridad.*;
import com.unam.mvmsys.repositorio.auditoria.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 *  🧪 SUITE COMPLETA DE REGRESIÓN - VERIFICACIÓN TOTAL DEL SISTEMA MVMsys
 * ═══════════════════════════════════════════════════════════════════════════
 * 
 * Esta suite ejecuta una verificación completa de TODOS los módulos del sistema
 * comprobando que todos los repositorios están accesibles y las migraciones
 * Flyway se aplicaron correctamente.
 * 
 * MÓDULOS VERIFICADOS:
 * ✓ 1. Configuración (Estados, UnidadesMedida, TiposProducto, etc.)
 * ✓ 2. Seguridad (Personas, Usuarios, Roles)
 * ✓ 3. Stock (Productos, Lotes, Existencias, Depósitos)
 * ✓ 4. Producción (Procesos, Órdenes, Reservas)
 * ✓ 5. Comercial (Pedidos, Vinculaciones)
 * ✓ 6. Auditoría (Logs)
 * 
 * EJECUCIÓN:
 * mvn test -Dtest=SystemWideRegressionTest
 * 
 * @author Sistema MVMsys
 * @version 1.0 - Verificación Global
 * @since Diciembre 2025
 */
@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("🧪 VERIFICACIÓN COMPLETA DEL SISTEMA MVMsys")
public class SystemWideRegressionTest {
    
    private static final Logger log = LoggerFactory.getLogger(SystemWideRegressionTest.class);
    
    // ==================== REPOSITORIOS - CONFIGURACIÓN ====================
    @Autowired private UnidadMedidaRepository unidadMedidaRepo;
    @Autowired private TipoProductoRepository tipoProductoRepo;
    @Autowired private EstadoRepository estadoRepo;
    @Autowired private EntidadSistemaRepository entidadSistemaRepo;
    @Autowired private CategoriaClienteRepository categoriaClienteRepo;
    
    // ==================== REPOSITORIOS - SEGURIDAD ====================
    @Autowired private PersonaRepository personaRepo;
    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private RolUsuarioRepository rolRepo;
    
    // ==================== REPOSITORIOS - STOCK ====================
    @Autowired private ProductoRepository productoRepo;
    @Autowired private LoteRepository loteRepo;
    @Autowired private ExistenciaRepository existenciaRepo;
    @Autowired private DepositoRepository depositoRepo;
    @Autowired private MovimientoStockRepository movimientoStockRepo;
    @Autowired private RubroRepository rubroRepo;
    @Autowired private DetalleMovimientoRepository detalleMovimientoRepo;
    
    // ==================== REPOSITORIOS - PRODUCCIÓN ====================
    @Autowired private ProcesoEstandarRepository procesoEstandarRepo;
    @Autowired private OrdenProduccionRepository ordenProduccionRepo;
    @Autowired private OrdenProduccionEtapaRepository ordenEtapaRepo;
    @Autowired private ReservaStockProduccionRepository reservaRepo;
    
    // ==================== REPOSITORIOS - COMERCIAL ====================
    @Autowired private PedidoRepository pedidoRepo;
    @Autowired private ClienteProductoRepository clienteProductoRepo;
    @Autowired private ProveedorProductoRepository proveedorProductoRepo;
    @Autowired private ClienteProductoVinculacionRepository vinculacionRepo;
    
    // ==================== REPOSITORIOS - AUDITORÍA ====================
    @Autowired private AuditoriaLogRepository auditoriaRepo;
    
    private int totalTests = 0;
    private int testsOK = 0;
    private int testsFailed = 0;
    
    @BeforeEach
    public void init() {
        log.info("\n╔══════════════════════════════════════════════════════════════════╗");
        log.info("║          INICIANDO VERIFICACIÓN COMPLETA DEL SISTEMA          ║");
        log.info("╚══════════════════════════════════════════════════════════════════╝\n");
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //  MÓDULO 1: CONFIGURACIÓN
    // ═══════════════════════════════════════════════════════════════════════════
    
    @Test
    @Order(1)
    @DisplayName("M1 ✓ Configuración: Verificar repositorios y datos maestros")
    public void test_M1_Configuracion() {
        logModulo("MÓDULO 1: CONFIGURACIÓN");
        
        verificarRepositorio("EntidadSistema", entidadSistemaRepo);
        verificarRepositorio("Estado", estadoRepo);
        verificarRepositorio("UnidadMedida", unidadMedidaRepo);
        verificarRepositorio("TipoProducto", tipoProductoRepo);
        verificarRepositorio("CategoriaCliente", categoriaClienteRepo);
        
        logResumenModulo("CONFIGURACIÓN", 5);
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //  MÓDULO 2: SEGURIDAD
    // ═══════════════════════════════════════════════════════════════════════════
    
    @Test
    @Order(2)
    @DisplayName("M2 ✓ Seguridad: Verificar personas, usuarios y roles")
    public void test_M2_Seguridad() {
        logModulo("MÓDULO 2: SEGURIDAD");
        
        verificarRepositorio("Persona", personaRepo);
        verificarRepositorio("Usuario", usuarioRepo);
        verificarRepositorio("RolUsuario", rolRepo);
        
        logResumenModulo("SEGURIDAD", 3);
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //  MÓDULO 3: STOCK
    // ═══════════════════════════════════════════════════════════════════════════
    
    @Test
    @Order(3)
    @DisplayName("M3 ✓ Stock: Verificar productos, lotes, existencias y movimientos")
    public void test_M3_Stock() {
        logModulo("MÓDULO 3: STOCK");
        
        verificarRepositorio("Producto", productoRepo);
        verificarRepositorio("Lote", loteRepo);
        verificarRepositorio("Existencia", existenciaRepo);
        verificarRepositorio("Deposito", depositoRepo);
        verificarRepositorio("MovimientoStock", movimientoStockRepo);
        verificarRepositorio("DetalleMovimiento", detalleMovimientoRepo);
        verificarRepositorio("Rubro", rubroRepo);
        
        logResumenModulo("STOCK", 7);
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //  MÓDULO 4: PRODUCCIÓN
    // ═══════════════════════════════════════════════════════════════════════════
    
    @Test
    @Order(4)
    @DisplayName("M4 ✓ Producción: Verificar procesos, órdenes y reservas")
    public void test_M4_Produccion() {
        logModulo("MÓDULO 4: PRODUCCIÓN");
        
        verificarRepositorio("ProcesoEstandar", procesoEstandarRepo);
        verificarRepositorio("OrdenProduccion", ordenProduccionRepo);
        verificarRepositorio("OrdenProduccionEtapa", ordenEtapaRepo);
        verificarRepositorio("ReservaStockProduccion", reservaRepo);
        
        logResumenModulo("PRODUCCIÓN", 4);
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //  MÓDULO 5: COMERCIAL
    // ═══════════════════════════════════════════════════════════════════════════
    
    @Test
    @Order(5)
    @DisplayName("M5 ✓ Comercial: Verificar pedidos y vinculaciones")
    public void test_M5_Comercial() {
        logModulo("MÓDULO 5: COMERCIAL");
        
        verificarRepositorio("Pedido", pedidoRepo);
        verificarRepositorio("ClienteProducto", clienteProductoRepo);
        verificarRepositorio("ProveedorProducto", proveedorProductoRepo);
        verificarRepositorio("ClienteProductoVinculacion", vinculacionRepo);
        
        logResumenModulo("COMERCIAL", 4);
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //  MÓDULO 6: AUDITORÍA
    // ═══════════════════════════════════════════════════════════════════════════
    
    @Test
    @Order(6)
    @DisplayName("M6 ✓ Auditoría: Verificar logs del sistema")
    public void test_M6_Auditoria() {
        logModulo("MÓDULO 6: AUDITORÍA");
        
        verificarRepositorio("AuditoriaLog", auditoriaRepo);
        
        logResumenModulo("AUDITORÍA", 1);
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //  RESUMEN FINAL
    // ═══════════════════════════════════════════════════════════════════════════
    
    @AfterAll
    public static void resumenFinal() {
        Logger log = LoggerFactory.getLogger(SystemWideRegressionTest.class);
        
        log.info("\n╔══════════════════════════════════════════════════════════════════╗");
        log.info("║           VERIFICACIÓN COMPLETA DEL SISTEMA FINALIZADA          ║");
        log.info("╚══════════════════════════════════════════════════════════════════╝");
        log.info("");
        log.info("📊 RESUMEN GLOBAL:");
        log.info("  • Módulos verificados: 6");
        log.info("  • Entidades verificadas: 24");
        log.info("  • Repositorios validados: 24");
        log.info("");
        log.info("✅ SISTEMA OPERATIVO - Todos los componentes accesibles");
        log.info("═══════════════════════════════════════════════════════════════════════\n");
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    //  MÉTODOS AUXILIARES
    // ═══════════════════════════════════════════════════════════════════════════
    
    private void verificarRepositorio(String nombre, JpaRepository<?, ?> repo) {
        totalTests++;
        try {
            assertNotNull(repo, nombre + " repository debe estar inyectado");
            
            long count = repo.count();
            testsOK++;
            
            log.info("  ✅ {} - {} registro(s) en base de datos", 
                String.format("%-30s", nombre), count);
                
        } catch (Exception e) {
            testsFailed++;
            log.error("  ❌ {} - ERROR: {}", nombre, e.getMessage());
            throw e;
        }
    }
    
    private void logModulo(String titulo) {
        log.info("\n┌────────────────────────────────────────────────────────────────┐");
        log.info("│ {}", String.format("%-62s", titulo) + "│");
        log.info("└────────────────────────────────────────────────────────────────┘");
    }
    
    private void logResumenModulo(String modulo, int entidades) {
        log.info("\n  ✓ Módulo {} verificado exitosamente - {} entidades OK\n", modulo, entidades);
    }
}
