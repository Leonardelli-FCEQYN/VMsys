package com.unam.mvmsys.config;

import com.unam.mvmsys.entidad.configuracion.*;
import com.unam.mvmsys.entidad.produccion.EtapaProceso;
import com.unam.mvmsys.entidad.produccion.ProcesoEstandar;
import com.unam.mvmsys.entidad.produccion.ProcesoEstandarInsumo;
import com.unam.mvmsys.entidad.seguridad.Persona;
import com.unam.mvmsys.entidad.seguridad.RolUsuario;
import com.unam.mvmsys.entidad.stock.Producto;
import com.unam.mvmsys.entidad.stock.Rubro;
import com.unam.mvmsys.repositorio.configuracion.*;
import com.unam.mvmsys.repositorio.produccion.ProcesoEstandarRepository;
import com.unam.mvmsys.repositorio.seguridad.PersonaRepository;
import com.unam.mvmsys.repositorio.seguridad.RolUsuarioRepository;
import com.unam.mvmsys.repositorio.stock.ProductoRepository;
import com.unam.mvmsys.repositorio.stock.RubroRepository;
import com.unam.mvmsys.servicio.stock.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    // Repositorios de Configuración
    private final EntidadSistemaRepository entidadRepo;
    private final EstadoRepository estadoRepo;
    private final UnidadMedidaRepository unidadRepo;
    private final TipoProductoRepository tipoRepo;
    private final CategoriaClienteRepository categoriaClienteRepo;
    private final RolUsuarioRepository rolRepo;
    private final PersonaRepository personaRepo;
    private final RubroRepository rubroRepo;
    private final ProductoRepository productoRepo;
    private final ProcesoEstandarRepository procesoRepo;

    // Servicios de Negocio (Para crear datos transaccionales)
    private final ProductoService productoService;

    @Bean
    @Transactional
    public CommandLineRunner initData() {
        return args -> {
            System.out.println("⚡ [DataInitializer] Iniciando verificación de datos maestros...");

            // 1. Unidades de Medida
            crearUnidadSiNoExiste("un", "Unidad", false);
            crearUnidadSiNoExiste("m", "Metro Lineal", true);
            crearUnidadSiNoExiste("m2", "Metro Cuadrado", true);
            crearUnidadSiNoExiste("m3", "Metro Cúbico", true);
            crearUnidadSiNoExiste("kg", "Kilogramo", true);
            crearUnidadSiNoExiste("pt", "Pie Tabla", true); // Fundamental para maderas
            crearUnidadSiNoExiste("l", "Litro", true);

            crearTipoSiNoExiste("Materia Prima");
            crearTipoSiNoExiste("Insumo");
            crearTipoSiNoExiste("Producto Semielaborado");
            crearTipoSiNoExiste("Producto Terminado");
            crearTipoSiNoExiste("Servicio");

            // 3. CATEGORÍAS DE CLIENTE (IRQ-06)
            crearCategoriaClienteSiNoExiste("MAYORISTA", "Compra en grandes volúmenes", "📦");
            crearCategoriaClienteSiNoExiste("MINORISTA", "Compra en menor cantidad", "🛒");
            crearCategoriaClienteSiNoExiste("EVENTUAL", "Compras ocasionales", "⏰");
            crearCategoriaClienteSiNoExiste("FRECUENTE", "Cliente habitual", "⭐");

            // 4. Rubros (Categorías de productos y proveedores)
            crearRubroSiNoExiste("Maderas Duras");
            crearRubroSiNoExiste("Maderas Blandas");
            crearRubroSiNoExiste("Tableros");
            crearRubroSiNoExiste("Químicos y Barnices");
            crearRubroSiNoExiste("Herrajes y Accesorios");
            crearRubroSiNoExiste("Abrasivos");
            crearRubroSiNoExiste("Logística y Transporte");
            crearRubroSiNoExiste("Herramientas");
            crearRubroSiNoExiste("Energía y Combustibles");

            // 4. Roles de Usuario
            // 3. Roles de Usuario
            crearRolSiNoExiste("ADMIN");
            crearRolSiNoExiste("VENTAS");
            crearRolSiNoExiste("PRODUCCION");
            crearRolSiNoExiste("GERENCIA");

            // 5. Estados del Sistema
            // Lotes
            EntidadSistema entLote = crearEntidadSiNoExiste("LOTE", "Lotes de producción/compra");
            crearEstadoSiNoExiste(entLote, "En Cuarentena", "#FFA500", true, false);
            crearEstadoSiNoExiste(entLote, "Disponible", "#008000", false, false);
            crearEstadoSiNoExiste(entLote, "Reservado", "#0000FF", false, false);
            crearEstadoSiNoExiste(entLote, "Agotado", "#808080", false, true);

            // Pedidos
            EntidadSistema entPedido = crearEntidadSiNoExiste("PEDIDO", "Pedidos de Compra/Venta");
            crearEstadoSiNoExiste(entPedido, "Pendiente", "#FFFF00", true, false);
            crearEstadoSiNoExiste(entPedido, "Confirmado", "#0000FF", false, false);
            crearEstadoSiNoExiste(entPedido, "Entregado", "#008000", false, true);
            crearEstadoSiNoExiste(entPedido, "Cancelado", "#FF0000", false, true);

            // Ordenes de Producción
            EntidadSistema entOrden = crearEntidadSiNoExiste("ORDEN_PROD", "Órdenes de Producción");
            crearEstadoSiNoExiste(entOrden, "Planificada", "#FFFF00", true, false);
            crearEstadoSiNoExiste(entOrden, "En Proceso", "#0000FF", false, false);
            crearEstadoSiNoExiste(entOrden, "Finalizada", "#008000", false, true);

            // 6. PERSONAS (Clientes y Proveedores) - AMPLIADO PARA PAGINACIÓN
            System.out.println("👥 [DataInitializer] Creando personas...");
            
            // === CLIENTES (15) ===
            crearPersonaSiNoExiste("20-12345678-9", "Mueblería Dedalier S.A.", "contacto@dedalier.com.ar", "011 4567-8901", "Av. Corrientes 1234", true, false);
            crearPersonaSiNoExiste("27-87654321-0", "Carpintería López", "info@carpinteria-lopez.com.ar", "011 2345-6789", "Calle Libertad 567", true, false);
            crearPersonaSiNoExiste("33-22334455-6", "Muebles del Norte S.R.L.", "ventas@mueblesdn.com.ar", "0381 445-6677", "Av. Mate de Luna 890", true, false);
            crearPersonaSiNoExiste("20-77665544-3", "Amoblamientos Rosario", "contacto@amoblamientosrosario.com", "0341 456-7788", "San Lorenzo 2345", true, false);
            crearPersonaSiNoExiste("30-88997766-5", "Casa Martínez Muebles", "ventas@casamartinez.com.ar", "0351 422-3344", "Dean Funes 678", true, false);
            crearPersonaSiNoExiste("27-11223344-7", "Carpintería El Roble", "info@carpinteriaelroble.com", "011 5544-3322", "Av. Rivadavia 4567", true, false);
            crearPersonaSiNoExiste("23-55443322-9", "Mueblería Moderna", "administracion@muebleriamoderna.com", "011 6677-8899", "Av. Avellaneda 789", true, false);
            crearPersonaSiNoExiste("20-99887766-1", "Diseño y Construcción SRL", "proyectos@disenoconstruccion.com.ar", "011 7788-9900", "Calle Belgrano 1122", true, false);
            crearPersonaSiNoExiste("33-44556677-2", "Carpintería Industrial del Sur", "ventas@carpinteriasur.com", "0291 455-6789", "Av. Colón 3344", true, false);
            crearPersonaSiNoExiste("27-66778899-4", "Muebles & Diseño Patagonia", "info@mueblepatagonia.com.ar", "0299 444-5566", "San Martín 567", true, false);
            crearPersonaSiNoExiste("30-22446688-8", "Fábrica de Muebles Cuyo", "fabricacuyo@gmail.com", "0261 433-4455", "Godoy Cruz 890", true, false);
            crearPersonaSiNoExiste("20-33557799-0", "Carpintería Artesanal La Plata", "artesanal@carpinterialp.com", "0221 456-7890", "Calle 7 n° 1234", true, false);
            crearPersonaSiNoExiste("27-77889900-3", "Mueblería Premium Design", "contacto@premiumdesign.com.ar", "011 4321-0987", "Av. Callao 2567", true, false);
            crearPersonaSiNoExiste("33-88990011-5", "Amoblamientos Litoral", "ventas@amobl-litoral.com", "0343 422-3344", "Urquiza 678", true, false);
            crearPersonaSiNoExiste("23-99001122-7", "Casa del Mueble Neuquén", "info@casadelmueblenqn.com.ar", "0299 442-5566", "Av. Argentina 4567", true, false);

            // === PROVEEDORES (15) ===
            crearPersonaSiNoExiste("30-11223344-5", "Aserradero Central S.A.", "ventas@aserradero-central.com", "011 8765-4321", "Ruta 5 km 45", false, true);
            crearPersonaSiNoExiste("20-55443322-1", "Insumos Industriales García", "compras@insumos-garcia.com.ar", "011 3456-7890", "Av. San Martín 789", false, true);
            crearPersonaSiNoExiste("33-44332211-9", "Maderas del Valle S.A.", "info@maderasdelvalle.com", "0261 445-6677", "Ruta 7 km 102", false, true);
            crearPersonaSiNoExiste("27-66554433-2", "Distribuidora Química del Norte", "ventas@quimicadelnorte.com.ar", "0381 456-7890", "Av. Independencia 234", false, true);
            crearPersonaSiNoExiste("30-77665544-4", "Herrajes y Tornillos Rosario", "pedidos@herrajesrosario.com", "0341 433-5566", "Sarmiento 1567", false, true);
            crearPersonaSiNoExiste("20-88776655-6", "Aserradero Patagónico", "ventas@aserraderopa.com.ar", "0299 445-6789", "Ruta 22 km 1234", false, true);
            crearPersonaSiNoExiste("27-99887766-8", "Importadora de Maderas Finas", "importadora@maderasfinas.com", "011 4567-8901", "Av. Callao 3456", false, true);
            crearPersonaSiNoExiste("33-00998877-0", "Barnices y Lacas Industriales", "ventas@barnicesindustriales.com.ar", "0351 422-3344", "Circunvalación 7890", false, true);
            crearPersonaSiNoExiste("30-11009988-1", "Tableros y Placas del Litoral", "info@tablerosdelitoral.com", "0343 456-7890", "Av. Ramírez 2345", false, true);
            crearPersonaSiNoExiste("20-22110099-3", "Maderas Tratadas del Sur", "comercial@maderasdelsur.com.ar", "0291 444-5566", "Ruta 3 km 567", false, true);
            crearPersonaSiNoExiste("27-33221100-5", "Herrajes Premium Import", "ventas@herrajespremium.com", "011 5678-9012", "Av. Córdoba 4567", false, true);
            crearPersonaSiNoExiste("33-44332200-7", "Abrasivos del Centro", "pedidos@abrasivoscentro.com.ar", "0351 433-4455", "Av. Colón 890", false, true);
            crearPersonaSiNoExiste("30-55443311-9", "Pegamentos Industriales SA", "ventas@pegamentosindustriales.com", "011 6789-0123", "Av. Warnes 2345", false, true);
            crearPersonaSiNoExiste("20-66554422-1", "Maderas Nativas del Chaco", "info@maderaschaco.com.ar", "0362 445-6677", "Ruta 11 km 1234", false, true);
            crearPersonaSiNoExiste("27-77665533-3", "Distribuidora de Insumos Cuyo", "comercial@insumoscuyo.com", "0261 456-7890", "San Martín 567", false, true);
            
            // === CLIENTES Y PROVEEDORES (5) ===
            crearPersonaSiNoExiste("23-99887766-4", "DuoPlex Distribuciones", "admin@duoplex.com.ar", "011 5555-5555", "Blvd. Industrial 999", true, true);
            crearPersonaSiNoExiste("30-12312312-5", "Maderas y Muebles Integral", "contacto@integralmaderas.com.ar", "011 4444-3333", "Av. Libertador 5678", true, true);
            crearPersonaSiNoExiste("27-23423423-7", "Carpintería y Aserradero del Centro", "ventas@carpinteriayaserradero.com", "0351 455-6677", "Av. Vélez Sarsfield 1234", true, true);
            crearPersonaSiNoExiste("33-34534534-9", "Muebles y Maderas Patagonia", "info@mueblesymp.com.ar", "0299 466-7788", "Ruta 40 km 2345", true, true);
            crearPersonaSiNoExiste("20-45645645-1", "Carpintería Industrial Buenos Aires", "comercial@carpinteriaba.com", "011 7777-8888", "Av. General Paz 6789", true, true);

            // 7. PRODUCTOS - AMPLIADO PARA PAGINACIÓN (30+ productos)
            System.out.println("📦 [DataInitializer] Creando productos...");
            
            UnidadMedida unidadUn = unidadRepo.findByCodigo("un").orElseThrow();
            UnidadMedida unidadM2 = unidadRepo.findByCodigo("m2").orElseThrow();
            UnidadMedida unidadL = unidadRepo.findByCodigo("l").orElseThrow();
            UnidadMedida unidadKg = unidadRepo.findByCodigo("kg").orElseThrow();
            
            TipoProducto tipoMateriaPrima = tipoRepo.findByNombre("Materia Prima").orElseThrow();
            TipoProducto tipoInsumo = tipoRepo.findByNombre("Insumo").orElseThrow();
            TipoProducto tipoSemielaborado = tipoRepo.findByNombre("Producto Semielaborado").orElseThrow();
            TipoProducto tipoTerminado = tipoRepo.findByNombre("Producto Terminado").orElseThrow();

            // === MATERIAS PRIMAS (8) ===
            crearProductoSiNoExiste("MAD-001", "Pino Radiata Tabla 25x100", "Tabla de pino radiata para construcción", unidadM2, tipoMateriaPrima, new BigDecimal("50"), new BigDecimal("850.00"), new BigDecimal("1200.00"));
            crearProductoSiNoExiste("MAD-002", "Pino Importado Premium 2x8", "Pino importado de grano fino", unidadM2, tipoMateriaPrima, new BigDecimal("20"), new BigDecimal("2500.00"), new BigDecimal("3500.00"));
            crearProductoSiNoExiste("MAD-003", "Eucalipto Colorado Tabla 2x6", "Madera dura para estructuras", unidadM2, tipoMateriaPrima, new BigDecimal("30"), new BigDecimal("1200.00"), new BigDecimal("1800.00"));
            crearProductoSiNoExiste("MAD-004", "Cedro Nacional Tabla 1x8", "Cedro aromático para muebles finos", unidadM2, tipoMateriaPrima, new BigDecimal("15"), new BigDecimal("3500.00"), new BigDecimal("5000.00"));
            crearProductoSiNoExiste("MAD-005", "Algarrobo Tabla 2x10", "Madera nativa de alta densidad", unidadM2, tipoMateriaPrima, new BigDecimal("12"), new BigDecimal("4200.00"), new BigDecimal("6000.00"));
            crearProductoSiNoExiste("MAD-006", "Guatambú Tabla 2x8", "Madera dura para pisos y estructuras", unidadM2, tipoMateriaPrima, new BigDecimal("25"), new BigDecimal("2800.00"), new BigDecimal("4000.00"));
            crearProductoSiNoExiste("MAD-007", "Paraíso Tabla 1x6", "Madera semidura para carpintería", unidadM2, tipoMateriaPrima, new BigDecimal("40"), new BigDecimal("950.00"), new BigDecimal("1400.00"));
            crearProductoSiNoExiste("MAD-008", "Lapacho Tabla 3x12", "Madera extremadamente dura", unidadM2, tipoMateriaPrima, new BigDecimal("8"), new BigDecimal("5500.00"), new BigDecimal("7800.00"));

            // === INSUMOS (10) ===
            crearProductoSiNoExiste("INS-001", "Lija Banda Grano 80", "Lija industrial para calibradora - 200x750mm", unidadUn, tipoInsumo, new BigDecimal("10"), new BigDecimal("1500.50"), new BigDecimal("2500.00"));
            crearProductoSiNoExiste("INS-002", "Pegamento Titebond III", "Adhesivo para maderas - Galón", unidadL, tipoInsumo, new BigDecimal("5"), new BigDecimal("3800.00"), new BigDecimal("5200.00"));
            crearProductoSiNoExiste("INS-003", "Acetona Pura Industrial", "Disolvente - Bidón 20L", unidadL, tipoInsumo, new BigDecimal("3"), new BigDecimal("2200.00"), new BigDecimal("3100.00"));
            crearProductoSiNoExiste("INS-004", "Barniz Poliuretánico Brillante", "Acabado profesional - 4L", unidadL, tipoInsumo, new BigDecimal("8"), new BigDecimal("2900.00"), new BigDecimal("4200.00"));
            crearProductoSiNoExiste("INS-005", "Sellador al Agua", "Sellador ecológico - 20L", unidadL, tipoInsumo, new BigDecimal("6"), new BigDecimal("1800.00"), new BigDecimal("2700.00"));
            crearProductoSiNoExiste("INS-006", "Laca Nitrocelulósica", "Acabado rápido profesional - 5L", unidadL, tipoInsumo, new BigDecimal("4"), new BigDecimal("4500.00"), new BigDecimal("6300.00"));
            crearProductoSiNoExiste("INS-007", "Tinte Nogal Oscuro", "Tinte al alcohol - 1L", unidadL, tipoInsumo, new BigDecimal("15"), new BigDecimal("850.00"), new BigDecimal("1200.00"));
            crearProductoSiNoExiste("INS-008", "Masilla para Madera Roble", "Masilla de alta adherencia - 500g", unidadKg, tipoInsumo, new BigDecimal("20"), new BigDecimal("650.00"), new BigDecimal("950.00"));
            crearProductoSiNoExiste("INS-009", "Cera en Pasta Natural", "Acabado tradicional - 500g", unidadKg, tipoInsumo, new BigDecimal("12"), new BigDecimal("980.00"), new BigDecimal("1450.00"));
            crearProductoSiNoExiste("INS-010", "Thinner Especial", "Diluyente para lacas - 20L", unidadL, tipoInsumo, new BigDecimal("7"), new BigDecimal("1600.00"), new BigDecimal("2400.00"));

            // === SEMIELABORADOS (7) ===
            crearProductoSiNoExiste("SEL-001", "Listón Cepillado 2x4", "Listón de pino radiata cepillado", unidadM2, tipoSemielaborado, new BigDecimal("15"), new BigDecimal("1200.00"), new BigDecimal("1800.00"));
            crearProductoSiNoExiste("SEL-002", "Tablero Compensado Espesor 15mm", "Tablero de compensado industrial", unidadM2, tipoSemielaborado, new BigDecimal("8"), new BigDecimal("3200.00"), new BigDecimal("4500.00"));
            crearProductoSiNoExiste("SEL-003", "Panel MDF 18mm Natural", "Panel de densidad media", unidadM2, tipoSemielaborado, new BigDecimal("12"), new BigDecimal("2400.00"), new BigDecimal("3500.00"));
            crearProductoSiNoExiste("SEL-004", "Machimbre Pino Finger 1x6", "Machimbre encolado fingerjointed", unidadM2, tipoSemielaborado, new BigDecimal("20"), new BigDecimal("1800.00"), new BigDecimal("2600.00"));
            crearProductoSiNoExiste("SEL-005", "Varillas Torneadas Pino 3cm", "Varillas decorativas torneadas", unidadUn, tipoSemielaborado, new BigDecimal("50"), new BigDecimal("450.00"), new BigDecimal("750.00"));
            crearProductoSiNoExiste("SEL-006", "Moldura Colonial 5cm", "Moldura decorativa colonial", unidadUn, tipoSemielaborado, new BigDecimal("30"), new BigDecimal("380.00"), new BigDecimal("620.00"));
            crearProductoSiNoExiste("SEL-007", "Tablero Aglomerado 25mm", "Tablero melamínico blanco", unidadM2, tipoSemielaborado, new BigDecimal("10"), new BigDecimal("2800.00"), new BigDecimal("4100.00"));

            // === PRODUCTOS TERMINADOS (10) ===
            crearProductoSiNoExiste("PRO-001", "Puerta Madera Pino Tablero 70x200", "Puerta de una hoja estándar", unidadUn, tipoTerminado, new BigDecimal("5"), new BigDecimal("8500.00"), new BigDecimal("12500.00"));
            crearProductoSiNoExiste("PRO-002", "Marco de Ventana Aluminio 50x80", "Marco de ventana aluminio anodizado", unidadUn, tipoTerminado, new BigDecimal("3"), new BigDecimal("6200.00"), new BigDecimal("9500.00"));
            crearProductoSiNoExiste("PRO-003", "Escritorio Madera Pino 1.40x60", "Escritorio completo con cajonera", unidadUn, tipoTerminado, new BigDecimal("2"), new BigDecimal("15000.00"), new BigDecimal("22500.00"));
            crearProductoSiNoExiste("PRO-004", "Biblioteca Modular 2 Cuerpos", "Biblioteca con 5 estantes - 180cm", unidadUn, tipoTerminado, new BigDecimal("4"), new BigDecimal("12500.00"), new BigDecimal("18000.00"));
            crearProductoSiNoExiste("PRO-005", "Mesa Comedor Cedro 180x90", "Mesa rectangular para 8 personas", unidadUn, tipoTerminado, new BigDecimal("2"), new BigDecimal("22000.00"), new BigDecimal("32000.00"));
            crearProductoSiNoExiste("PRO-006", "Silla Comedor Tapizada", "Silla con respaldo alto y tapizado", unidadUn, tipoTerminado, new BigDecimal("12"), new BigDecimal("4500.00"), new BigDecimal("7200.00"));
            crearProductoSiNoExiste("PRO-007", "Ropero 3 Puertas Pino", "Placard de 220cm con cajonera", unidadUn, tipoTerminado, new BigDecimal("3"), new BigDecimal("28000.00"), new BigDecimal("42000.00"));
            crearProductoSiNoExiste("PRO-008", "Cama Plaza y Media con Respaldo", "Cama 1.40 con cabecera tallada", unidadUn, tipoTerminado, new BigDecimal("4"), new BigDecimal("18500.00"), new BigDecimal("27500.00"));
            crearProductoSiNoExiste("PRO-009", "Mesa de Luz Moderna", "Mesa de noche con cajón y estante", unidadUn, tipoTerminado, new BigDecimal("8"), new BigDecimal("3800.00"), new BigDecimal("6200.00"));
            crearProductoSiNoExiste("PRO-010", "Rack TV Laqueado 120cm", "Mueble para TV con estantes laterales", unidadUn, tipoTerminado, new BigDecimal("5"), new BigDecimal("8900.00"), new BigDecimal("13500.00"));

            // 8. Recetas de Producción
            System.out.println("📋 [DataInitializer] Creando recetas de producción...");
            crearRecetasDeProduccion();

            System.out.println("✅ [DataInitializer] Datos maestros cargados correctamente.");
            System.out.println("🚀 [DataInitializer] Sistema listo y cargado.");
        };
    }

    // ===== MÉTODOS AUXILIARES =====

    private void crearUnidadSiNoExiste(String codigo, String nombre, boolean decimales) {
        if (unidadRepo.findByCodigo(codigo).isEmpty()) {
            UnidadMedida unidad = UnidadMedida.builder()
                    .codigo(codigo)
                    .nombre(nombre)
                    .permiteDecimales(decimales)
                    .build();
            @SuppressWarnings("unused")
            UnidadMedida saved = unidadRepo.save(unidad);
        }
    }

    private void crearTipoSiNoExiste(String nombre) {
        if (tipoRepo.findByNombre(nombre).isEmpty()) {
            TipoProducto tipo = TipoProducto.builder()
                    .nombre(nombre)
                    .build();
            @SuppressWarnings("unused")
            TipoProducto saved = tipoRepo.save(tipo);
        }
    }

    private void crearRolSiNoExiste(String nombre) {
        if (rolRepo.findByNombre(nombre).isEmpty()) {
            RolUsuario rol = RolUsuario.builder()
                    .nombre(nombre)
                    .build();
            @SuppressWarnings("unused")
            RolUsuario saved = rolRepo.save(rol);
        }
    }

    private EntidadSistema crearEntidadSiNoExiste(String codigo, String nombre) {
        return entidadRepo.findByCodigo(codigo)
                .orElseGet(() -> {
                    EntidadSistema entidad = EntidadSistema.builder()
                            .codigo(codigo)
                            .nombre(nombre)
                            .build();
                    @SuppressWarnings("null")
                    EntidadSistema saved = entidadRepo.save(entidad);
                    return saved;
                });
    }

    private void crearEstadoSiNoExiste(EntidadSistema entidad, String nombre, String color, boolean inicial, boolean esFinal) {
        if (estadoRepo.findByNombreAndEntidadSistema(nombre, entidad).isEmpty()) {
            Estado estado = Estado.builder()
                    .entidadSistema(entidad)
                    .nombre(nombre)
                    .colorHex(color)
                    .esInicial(inicial)
                    .esFinal(esFinal)
                    .build();
            @SuppressWarnings("unused")
            Estado saved = estadoRepo.save(estado);
        }
    }

    private Persona crearPersonaSiNoExiste(String cuitDni, String razonSocial, String email, 
                                           String telefono, String direccion, 
                                           boolean esCliente, boolean esProveedor) {
        return personaRepo.findByCuitDni(cuitDni)
                .orElseGet(() -> {
                    Persona persona = Persona.builder()
                            .cuitDni(cuitDni)
                            .razonSocial(razonSocial)
                            .email(email)
                            .telefono(telefono)
                            .direccionCalle(direccion)
                            .esCliente(esCliente)
                            .esProveedor(esProveedor)
                            .esEmpleado(false)
                            .activo(true)
                            .build();
                    @SuppressWarnings("null")
                    Persona saved = personaRepo.save(persona);
                    return saved;
                });
    }

    private void crearProductoSiNoExiste(String sku, String nombre, String descripcion,
                                        UnidadMedida unidad, TipoProducto tipo,
                                        BigDecimal stockMin, BigDecimal costo, BigDecimal precioVenta) {
        if (productoService.listarTodos().stream()
                .noneMatch(p -> p.getCodigoSku().equals(sku))) {
            
            Producto producto = Producto.builder()
                    .codigoSku(sku)
                    .nombre(nombre)
                    .descripcion(descripcion)
                    .unidadMedida(unidad)
                    .tipoProducto(tipo)
                    .stockMinimo(stockMin)
                    .costoReposicion(costo)
                    .precioVentaBase(precioVenta)
                    .activo(true)
                    .build();
            
            productoService.crearProducto(producto);
        }
    }

    private Rubro crearRubroSiNoExiste(String nombre) {
        return rubroRepo.findByNombreIgnoreCase(nombre)
                .orElseGet(() -> {
                    Rubro rubro = Rubro.builder()
                            .nombre(nombre)
                            .build();
                    return rubroRepo.save(rubro);
                });
    }

    private void crearCategoriaClienteSiNoExiste(String nombre, String descripcion, String icono) {
        if (categoriaClienteRepo.findByNombreIgnoreCase(nombre).isEmpty()) {
            CategoriaCliente categoria = CategoriaCliente.builder()
                    .nombre(nombre)
                    .descripcion(descripcion)
                    .icono(icono)
                    .colorHex("#6366F1")
                    .activa(true)
                    .build();
            @SuppressWarnings("unused")
            CategoriaCliente saved = categoriaClienteRepo.save(categoria);
        }
    }

            private void crearRecetasDeProduccion() {
            // Receta 1: Mesa Comedor Cedro 180x90
            crearRecetaSiNoExiste(
                "Mesa Comedor Cedro 180x90",
                "Proceso completo de fabricación de mesa de comedor en cedro con acabado natural",
                List.of(
                    new InsumoData("MAD-004", new BigDecimal("2.5")),
                    new InsumoData("INS-002", new BigDecimal("0.5")),
                    new InsumoData("INS-004", new BigDecimal("0.8")),
                    new InsumoData("INS-001", new BigDecimal("5"))
                ),
                List.of(
                    new EtapaData(1, "Corte y dimensionado de tablones", "Cortar tablones de cedro a medidas exactas", 120),
                    new EtapaData(2, "Cepillado y lijado inicial", "Cepillar y lijar para eliminar asperezas", 90),
                    new EtapaData(3, "Ensamblado de tablero", "Unir tablones con cola y prensas", 180),
                    new EtapaData(4, "Construcción de estructura y patas", "Fabricar estructura portante y patas", 240),
                    new EtapaData(5, "Lijado fino y preparación superficie", "Lijar fino y preparar para acabado", 150),
                    new EtapaData(6, "Aplicación de sellador", "Aplicar sellador para cerrar poros", 60),
                    new EtapaData(7, "Aplicación de barniz (3 manos)", "Barnizar en 3 capas con lija entre manos", 360),
                    new EtapaData(8, "Pulido final y control de calidad", "Pulido espejo y control de calidad", 90)
                )
            );

            // Receta 2: Puerta Madera Pino Tablero
            crearRecetaSiNoExiste(
                "Puerta Madera Pino Tablero 70x200",
                "Fabricación de puerta placa con marco de pino y tablero central",
                List.of(
                    new InsumoData("MAD-001", new BigDecimal("1.8")),
                    new InsumoData("SEL-003", new BigDecimal("1.5")),
                    new InsumoData("INS-002", new BigDecimal("0.3")),
                    new InsumoData("INS-005", new BigDecimal("0.5")),
                    new InsumoData("INS-001", new BigDecimal("3"))
                ),
                List.of(
                    new EtapaData(1, "Corte de marco perimetral", "Cortar perimetral de pino según medidas", 45),
                    new EtapaData(2, "Armado de bastidor", "Armar bastidor con escuadras", 90),
                    new EtapaData(3, "Corte y ajuste de tablero central", "Cortar y ajustar tablero central", 60),
                    new EtapaData(4, "Encolado y prensado", "Encolar y prensar conjunto", 180),
                    new EtapaData(5, "Lijado y preparación", "Lijar y preparar superficie", 75),
                    new EtapaData(6, "Aplicación de sellador y acabado", "Aplicar sellador y acabado", 120),
                    new EtapaData(7, "Control de calidad final", "Inspección final de calidad", 30)
                )
            );

            // Receta 3: Escritorio con Cajonera
            crearRecetaSiNoExiste(
                "Escritorio Madera Pino 1.40x60",
                "Fabricación de escritorio completo con cajonera lateral y estante",
                List.of(
                    new InsumoData("MAD-001", new BigDecimal("3.0")),
                    new InsumoData("SEL-003", new BigDecimal("1.2")),
                    new InsumoData("INS-002", new BigDecimal("0.4")),
                    new InsumoData("INS-004", new BigDecimal("0.6")),
                    new InsumoData("INS-001", new BigDecimal("4"))
                ),
                List.of(
                    new EtapaData(1, "Corte de componentes principales", "Cortar tablero, laterales y frente", 90),
                    new EtapaData(2, "Fabricación de cajones", "Fabricar 3 cajones con guías", 150),
                    new EtapaData(3, "Construcción de estructura y patas", "Construir estructura base y patas", 120),
                    new EtapaData(4, "Ensamblado de tapa y estructura", "Ensamblar tapa con estructura", 180),
                    new EtapaData(5, "Instalación de guías y herrajes", "Instalar guías de cajones y cerraduras", 90),
                    new EtapaData(6, "Lijado completo", "Lijar todo el conjunto", 120),
                    new EtapaData(7, "Aplicación de acabado", "Aplicar acabado y barniz", 240),
                    new EtapaData(8, "Montaje final y ajustes", "Montaje final y ajustes", 60)
                )
            );

            // Receta 4: Biblioteca Modular
            crearRecetaSiNoExiste(
                "Biblioteca Modular 2 Cuerpos",
                "Sistema de biblioteca con estantes ajustables y respaldo",
                List.of(
                    new InsumoData("MAD-001", new BigDecimal("4.5")),
                    new InsumoData("SEL-007", new BigDecimal("2.0")),
                    new InsumoData("INS-002", new BigDecimal("0.3")),
                    new InsumoData("INS-006", new BigDecimal("0.7"))
                ),
                List.of(
                    new EtapaData(1, "Corte de paneles laterales y estantes", "Cortar paneles laterales y 5 estantes", 120),
                    new EtapaData(2, "Perforado para tarugos regulables", "Perforar para tarugos de regulación", 90),
                    new EtapaData(3, "Armado de estructura principal", "Armar estructura con cola y escuadras", 150),
                    new EtapaData(4, "Instalación de respaldo y zócalo", "Instalar respaldo y zócalo", 120),
                    new EtapaData(5, "Lijado y preparación", "Lijar y preparar para laqueado", 90),
                    new EtapaData(6, "Laqueado y acabado", "Laquear en múltiples capas", 300),
                    new EtapaData(7, "Montaje final de accesorios", "Montar accesorios y control final", 60)
                )
            );

            // Receta 5: Ropero 3 Puertas
            crearRecetaSiNoExiste(
                "Ropero 3 Puertas Pino",
                "Placard de 220cm de alto con cajonera inferior y barral",
                List.of(
                    new InsumoData("MAD-001", new BigDecimal("6.0")),
                    new InsumoData("SEL-007", new BigDecimal("3.5")),
                    new InsumoData("INS-002", new BigDecimal("0.6")),
                    new InsumoData("INS-004", new BigDecimal("1.2")),
                    new InsumoData("INS-001", new BigDecimal("6"))
                ),
                List.of(
                    new EtapaData(1, "Corte de paneles y estructura", "Cortar laterales, techo y base", 180),
                    new EtapaData(2, "Fabricación de puertas", "Fabricar 3 puertas con bisagras", 240),
                    new EtapaData(3, "Construcción de cajones", "Construir cajonera inferior", 180),
                    new EtapaData(4, "Armado del cuerpo principal", "Armar cuerpo principal", 300),
                    new EtapaData(5, "Instalación de herrajes y bisagras", "Instalar bisagras y cerraduras", 120),
                    new EtapaData(6, "Lijado completo", "Lijar conjunto completo", 180),
                    new EtapaData(7, "Aplicación de acabado", "Aplicar acabado final 3 manos", 420),
                    new EtapaData(8, "Montaje de puertas y ajustes", "Montar puertas y hacer ajustes", 120),
                    new EtapaData(9, "Control de calidad y packaging", "Control final y empaque", 60)
                )
            );

            // Receta 6: Silla Comedor Tapizada
            crearRecetaSiNoExiste(
                "Silla Comedor Tapizada",
                "Silla con estructura de madera y asiento/respaldo tapizado",
                List.of(
                    new InsumoData("MAD-003", new BigDecimal("0.5")),
                    new InsumoData("INS-002", new BigDecimal("0.1")),
                    new InsumoData("INS-004", new BigDecimal("0.2"))
                ),
                List.of(
                    new EtapaData(1, "Corte de patas y travesaños", "Cortar madera maciza para patas", 45),
                    new EtapaData(2, "Torneado de patas", "Tornear patas decorativas", 90),
                    new EtapaData(3, "Ensamblado de estructura", "Ensamblar estructura de silla", 120),
                    new EtapaData(4, "Fabricación de asiento y respaldo", "Armar asiento y respaldo", 90),
                    new EtapaData(5, "Lijado y acabado de madera", "Lijar y preparar madera", 150),
                    new EtapaData(6, "Tapizado de asiento y respaldo", "Tapizar con tela y relleno", 120),
                    new EtapaData(7, "Montaje final", "Montaje y control final", 45)
                )
            );

            // Receta 7: Cama Plaza y Media
            crearRecetaSiNoExiste(
                "Cama Plaza y Media con Respaldo",
                "Cama de 1.40m con cabecera tallada y largueros reforzados",
                List.of(
                    new InsumoData("MAD-004", new BigDecimal("2.0")),
                    new InsumoData("MAD-001", new BigDecimal("2.5")),
                    new InsumoData("INS-002", new BigDecimal("0.4")),
                    new InsumoData("INS-004", new BigDecimal("0.8"))
                ),
                List.of(
                    new EtapaData(1, "Corte de largueros y travesaños", "Cortar largueros principales", 90),
                    new EtapaData(2, "Tallado de cabecera decorativa", "Tallar cabecera con motivos", 300),
                    new EtapaData(3, "Armado de estructura base", "Armar estructura base", 180),
                    new EtapaData(4, "Instalación de cabecera", "Instalar cabecera al marco", 120),
                    new EtapaData(5, "Refuerzo de uniones", "Reforzar uniones internas", 90),
                    new EtapaData(6, "Lijado completo", "Lijar todo el conjunto", 150),
                    new EtapaData(7, "Aplicación de acabado", "Aplicar barniz/cedro natural", 300),
                    new EtapaData(8, "Inspección y control final", "Inspección de calidad", 60)
                )
            );

        // Receta 8: Rack TV Laqueado
        crearRecetaSiNoExiste(
            "Rack TV Laqueado 120cm",
            "Mueble para TV con estantes laterales y acabado laqueado",
            List.of(
                new InsumoData("SEL-003", new BigDecimal("2.5")),
                new InsumoData("INS-002", new BigDecimal("0.3")),
                new InsumoData("INS-006", new BigDecimal("1.0")),
                new InsumoData("INS-008", new BigDecimal("0.5"))
            ),
            List.of(
                new EtapaData(1, "Corte de paneles CNC", "Corte preciso con CNC", 60),
                new EtapaData(2, "Ensamblado de estructura", "Ensamblar estructura principal", 120),
                new EtapaData(3, "Aplicación de masilla en bordes", "Aplicar masilla de poliuretano", 90),
                new EtapaData(4, "Lijado fino", "Lijar todo con grano fino", 120),
                new EtapaData(5, "Aplicación de fondos y masilla", "Aplicar fondo y masilla blanca", 180),
                new EtapaData(6, "Laqueado (3 manos)", "Laquear 3 capas espejo", 360),
                new EtapaData(7, "Pulido espejo", "Pulido final espejo", 150),
                new EtapaData(8, "Montaje de herrajes y accesorios", "Montaje de herrajes finales", 60)
            )
        );
    }

    private void crearRecetaSiNoExiste(String nombre, String descripcion,
                                       List<InsumoData> insumos, List<EtapaData> etapas) {
        if (!procesoRepo.existsByNombreIgnoreCase(nombre)) {
            ProcesoEstandar receta = new ProcesoEstandar();
            receta.setNombre(nombre);
            receta.setDescripcion(descripcion);
            receta.setActivo(true);

            List<ProcesoEstandarInsumo> insumosReceta = new ArrayList<>();
            for (InsumoData data : insumos) {
                productoRepo.findByCodigoSku(data.sku()).ifPresent(producto -> {
                    ProcesoEstandarInsumo insumo = new ProcesoEstandarInsumo();
                    insumo.setProcesoEstandar(receta);
                    insumo.setProducto(producto);
                    insumo.setCantidadBase(data.cantidad());
                    insumosReceta.add(insumo);
                });
            }
            receta.setInsumos(insumosReceta);

            List<EtapaProceso> etapasProduccion = new ArrayList<>();
            int tiempoTotal = 0;
            for (EtapaData data : etapas) {
                EtapaProceso etapa = new EtapaProceso();
                etapa.setProcesoEstandar(receta);
                etapa.setOrdenSecuencia(data.orden());
                etapa.setNombre(data.nombre());
                etapa.setDescripcion(data.descripcion());
                etapa.setTiempoEstimadoMinutos(data.tiempoMinutos());
                etapasProduccion.add(etapa);
                tiempoTotal += data.tiempoMinutos();
            }
            receta.setEtapas(etapasProduccion);
            receta.setTiempoEstimadoMinutos(tiempoTotal);

            procesoRepo.save(receta);
        }
    }

    private record InsumoData(String sku, BigDecimal cantidad) { }
    private record EtapaData(int orden, String nombre, String descripcion, int tiempoMinutos) { }
}