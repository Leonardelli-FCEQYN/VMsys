# 🧪 GUÍA DE TESTS DE REGRESIÓN - MÓDULO DE PRODUCCIÓN MVMsys

## 📋 Resumen Ejecutivo

Este documento proporciona la estrategia completa de pruebas de regresión para el sistema MVMsys, basado en las mejores prácticas de testing en Java y Spring Boot.

## 🎯 Objetivos de las Pruebas de Regresión

1. **Validar funcionalidad completa** del módulo de producción
2. **Detectar regresiones** tempranamente en el ciclo de desarrollo
3. **Asegurar >80% de cobertura** de código
4. **Generar logs detallados** para debugging
5. **Automatizar ejecución** en CI/CD pipeline

## 📊 Análisis de Entidades del Sistema

### Entidades de Producción Implementadas

| Entidad | Campos Principales | Relaciones |
|---------|-------------------|------------|
| `ProcesoEstandar` | nombre, descripcion, tiempoEstimadoMinutos | OneToMany: insumos, etapas |
| `ProcesoEstandarInsumo` | producto, cantidadBase | ManyToOne: procesoEstandar, producto |
| `EtapaProceso` | nombre, descripcion, ordenSecuencia, tiempoEstimadoMinutos | ManyToOne: procesoEstandar |
| `OrdenProduccion` | fechaPlanificada, estado, cantidadAProd

ucer | ManyToOne: procesoEstandar |
| `ReservaStockProduccion` | cantidadReservada | ManyToOne: ordenProduccion, lote |

### Entidades de Stock

| Entidad | Campos Principales | Observaciones |
|---------|-------------------|---------------|
| `Producto` | codigoSku, nombre, marca, tipoProducto | Base para materias primas y productos terminados |
| `Lote` | codigo, producto, fechaVencimiento | Usa `codigo` (no `numeroLote`) |
| `Existencia` | lote, deposito, cantidad | NO tiene campo `activo` |
| `Deposito` | nombre, direccion, esPropio | Base para ubicación de stock |

### Entidades de Configuración

| Entidad | Estructura | Notas Importantes |
|---------|-----------|-------------------|
| `Estado` | entidadSistema, nombre, colorHex, esInicial, esFinal | NO tiene campo `codigo` |
| `TipoProducto` | nombre, descripcion, activo | NO tiene campo `codigo` |
| `UnidadMedida` | codigo, nombre, permiteDecimales | Tiene campo `codigo` |

## 🔍 Problemas Identificados en Tests Iniciales

### ❌ Errores de Compilación Detectados

1. **ProcesoEstandar**: No existe `cantidadProduccion` ni `productoFinal`
2. **ProcesoEstandarInsumo**: Campo es `cantidadBase` (no `cantidadRequerida`), relación es `producto` (no `insumo`)
3. **Lote**: Campo es `codigo` (no `numeroLote`), NO tiene método `getNumeroLote()`
4. **Existencia**: NO tiene campo `activo` en el builder
5. **Estado**: NO tiene campo `codigo`, solo `nombre`
6. **TipoProducto**: NO tiene campo `codigo`, solo `nombre`  
7. **Deposito**: NO tiene campo `codigo`, solo `nombre`
8. **EtapaProceso**: NO tiene campo `instrucciones`
9. **ExistenciaRepository**: NO tiene método `findByLoteIdAndDepositoId()`, usar query personalizado

## ✅ Estrategia Corregida de Testing

### Casos de Prueba Prioritarios

#### R001 - Creación de Proceso Estándar Completo
```java
Given: Receta con insumos (harina, levadura, sal, agua) y etapas (mezclado, amasado, fermentación, horneado)
When: Persistir proceso estándar con relaciones bidireccionales
Then: 
  - ID generado automáticamente
  - 4 insumos con cantidades correctas
  - 4 etapas ordenadas secuencialmente
  - Tiempo total = suma de tiempos de etapas
  - Relaciones bidireccionales intactas
```

#### R002 - Múltiples Recetas Coexistentes
```java
Given: Primera receta ya existe en sistema
When: Crear segunda receta diferente (medialunas)
Then:
  - Ambas recetas persisten sin conflictos
  - IDs únicos para cada receta
  - Insumos y etapas separados correctamente
```

#### R003 - Lógica FIFO de Stock
```java
Given: 3 lotes de harina con fechas de vencimiento: +15d, +30d, +60d
When: Consultar lotes ordenados por FIFO
Then:
  - Orden correcto: lote +15d → +30d → +60d
  - Stock total = suma de existencias
  - Fechas ordenadas ascendentemente
```

#### R004 - Stock en Múltiples Depósitos
```java
Given: Lote de levadura distribuido: 10kg en Central, 5kg en Sucursal
When: Consultar existencias por depósito
Then:
  - Depósito Central: 10.00 kg
  - Sucursal Norte: 5.00 kg
  - Ambos pertenecen al mismo lote
  - Stock total: 15.00 kg
```

#### R005 - Inventario Global Multi-Producto
```java
Given: Stock completo de 5 materias primas (harina, levadura, sal, agua, manteca)
When: Calcular totales por producto
Then:
  - Cada producto tiene stock > 0
  - Cantidades específicas validadas
  - Inventario global calculado correctamente
```

## 🛠️ Métodos Auxiliares Necesarios

### Creación de Datos Maestros

```java
// ✓ CORRECTO
UnidadMedida kg = unidadRepo.save(UnidadMedida.builder()
    .codigo("KG")
    .nombre("Kilogramo")
    .permiteDecimales(true)
    .activo(true)
    .build());

// ✓ CORRECTO  
TipoProducto materiaPrima = tipoProductoRepo.save(TipoProducto.builder()
    .nombre("Materia Prima")  // NO .codigo()
    .descripcion("Materias primas para producción")
    .activo(true)
    .build());

// ✓ CORRECTO - Estados requieren EntidadSistema
// Primero debes buscar el EntidadSistema existente creado por Flyway
EntidadSistema entidadLote = entidadSistemaRepo.findByNombre("Lote")
    .orElseThrow(() -> new RuntimeException("EntidadSistema 'Lote' no encontrado"));

Estado estadoDisponible = estadoRepo.save(Estado.builder()
    .entidadSistema(entidadLote)
    .nombre("Disponible")  // NO .codigo()
    .colorHex("#28a745")
    .esInicial(true)
    .esFinal(false)
    .build());

// ✓ CORRECTO
Deposito depositoCentral = depositoRepo.save(Deposito.builder()
    .nombre("Depósito Central")  // NO .codigo()
    .direccion("Av. Principal 123")
    .esPropio(true)
    .activo(true)
    .build());

// ✓ CORRECTO
Producto harina = productoRepo.save(Producto.builder()
    .codigoSku("MP-HARINA-001")
    .nombre("Harina 000")
    .marca("Morixe")  // Opcional
    .tipoProducto(materiaPrima)
    .unidadMedida(kg)
    .descripcion("Harina de trigo refinada")
    .activo(true)
    .build());

// ✓ CORRECTO
Lote loteHarina = loteRepo.save(Lote.builder()
    .codigo("LOTE-H-001")  // NO .numeroLote()
    .producto(harina)
    .estado(estadoDisponible)
    .fechaCreacion(LocalDateTime.now())
    .fechaVencimiento(LocalDateTime.now().plusDays(30))
    .costoUnitarioPromedio(new BigDecimal("150.00"))
    .build());

// ✓ CORRECTO
Existencia existencia = existenciaRepo.save(Existencia.builder()
    .lote(loteHarina)
    .deposito(depositoCentral)
    .cantidad(new BigDecimal("100.00"))
    // NO .activo() - no existe este campo
    .build());

// ✓ CORRECTO - Proceso Estándar (sin productoFinal ni cantidadProduccion)
ProcesoEstandar receta = procesoRepo.save(ProcesoEstandar.builder()
    .nombre("Pan Francés Artesanal")
    .descripcion("Receta tradicional")
    .tiempoEstimadoMinutos(150)
    .activo(true)
    .build());

// ✓ CORRECTO - Agregar insumo
ProcesoEstandarInsumo insumo = ProcesoEstandarInsumo.builder()
    .procesoEstandar(receta)
    .producto(harina)  // NO .insumo()
    .cantidadBase(new BigDecimal("5.000"))  // NO .cantidadRequerida()
    .build();
receta.agregarInsumo(insumo);

// ✓ CORRECTO - Agregar etapa (sin campo instrucciones)
EtapaProceso etapa = EtapaProceso.builder()
    .procesoEstandar(receta)
    .nombre("Mezclado")
    .descripcion("Mezclar ingredientes hasta masa homogénea")
    .ordenSecuencia(1)
    .tiempoEstimadoMinutos(15)
    // NO .instrucciones() - no existe este campo
    .activo(true)
    .build();
receta.agregarEtapa(etapa);
```

### Consultas de Stock

```java
// ✓ CORRECTO - Consulta FIFO usando repositorio de Lote
List<Lote> lotesFIFO = loteRepo.findByProductoIdAndEstadoNombreOrderByFechaVencimientoAsc(
    harina.getId(), "Disponible");

// ✓ CORRECTO - Obtener stock usando ExistenciaRepository
List<Existencia> existencias = existenciaRepo.findDisponiblesPorProductoFIFO(harina.getId());

// ❌ INCORRECTO - No existe este método
// existenciaRepo.findByLoteIdAndDepositoId(loteId, depositoId);

// ✓ CORRECTO - Query manual alternativa
@Query("SELECT e FROM Existencia e WHERE e.lote.id = :loteId AND e.deposito.id = :depositoId")
Optional<Existencia> findByLoteAndDeposito(@Param("loteId") UUID loteId, @Param("depositoId") UUID depositoId);
```

## 📝 Estructura de Logging Recomendada

### Formato de Logs para Tests

```java
private static final Logger log = LoggerFactory.getLogger(ProduccionRegressionTest.class);
private static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

// ═══════════ SEPARADORES VISUALES ═══════════

private void logSeparador(String titulo) {
    log.info("\n╔═══════════════════════════════════════════════════════════════════╗");
    log.info("║  {}", String.format("%-62s", titulo) + "║");
    log.info("╚═══════════════════════════════════════════════════════════════════╝");
}

private void logTestHeader(String codigo, String descripcion) {
    log.info("\n┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓");
    log.info("┃  TEST {} - {}", codigo, descripcion);
    log.info("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛");
}

private void logTestSuccess(String codigo, String mensaje) {
    log.info("\n┌────────────────────────────────────────────────────────────────┐");
    log.info("│ ✅ TEST {} EXITOSO", codigo);
    log.info("│ {}", mensaje);
    log.info("└────────────────────────────────────────────────────────────────┘\n");
}

private void logTestFailure(String codigo, Exception e) {
    log.error("\n┌────────────────────────────────────────────────────────────────┐");
    log.error("│ ❌ TEST {} FALLIDO", codigo);
    log.error("│ Error: {}", e.getMessage());
    log.error("└────────────────────────────────────────────────────────────────┘");
    log.error("Stacktrace completo:", e);
}

// Ejemplo de uso en test
@Test
@Order(1)
@DisplayName("R001 - Given receta completa | When persistir | Then guardado exitoso")
public void givenRecetaCompleta_whenPersistir_thenGuardadoExitoso() {
    logTestHeader("R001", "Creación de Receta Estándar");
    
    try {
        log.info("Given: Construyendo receta...");
        // ... código del test
        
        log.info("\nWhen: Guardando en base de datos...");
        // ... guardar
        
        log.info("\nThen: Validando resultado...");
        // ... assertions
        
        logTestSuccess("R001", "Receta creada exitosamente - ID: " + receta.getId());
        
    } catch (Exception e) {
        logTestFailure("R001", e);
        throw e;
    }
}
```

## 🚀 Integración con CI/CD

### GitHub Actions Workflow

```yaml
name: Regression Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'
    
    - name: Run Regression Tests
      run: mvn test -Dtest=ProduccionRegressionTest
    
    - name: Generate Test Report
      if: always()
      uses: actions/upload-artifact@v3
      with:
        name: test-results
        path: target/surefire-reports/
```

## 📈 Métricas de Calidad Esperadas

### Cobertura de Código (JaCoCo)

```xml
<!-- Agregar a pom.xml -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>jacoco-check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>PACKAGE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Comando para generar reporte de cobertura:

```bash
mvn clean test jacoco:report
# Ver reporte en: target/site/jacoco/index.html
```

## 🔧 Troubleshooting

### Problemas Comunes y Soluciones

| Problema | Causa | Solución |
|----------|-------|----------|
| `cannot find symbol: method cantidadProduccion()` | ProcesoEstandar no tiene ese campo | Eliminar referencias a `cantidadProduccion` y `productoFinal` |
| `cannot find symbol: method getNumeroLote()` | Lote usa `codigo` en lugar de `numeroLote` | Cambiar a `lote.getCodigo()` |
| `cannot find symbol: method activo()` en Existencia | Existencia no tiene campo activo | Eliminar `.activo(true)` del builder de Existencia |
| Estado/TipoProducto sin `codigo` | Estas entidades solo tienen `nombre` | Usar `.nombre()` en lugar de `.codigo()` |
| `NullPointerException` al crear Estado | Falta `EntidadSistema` requerida | Buscar EntidadSistema existente antes de crear Estado |
| Tests fallan con datos duplicados | @Transactional no hace rollback | Agregar `@Rollback` o usar `@DirtiesContext` |

## 📚 Referencias y Recursos

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Test-Driven Development (TDD)](https://martinfowler.com/bliki/TestDrivenDevelopment.html)
- [Given-When-Then Pattern](https://martinfowler.com/bliki/GivenWhenThen.html)

## ✅ Checklist de Implementación

- [ ] Revisar y corregir todas las entidades según schema real
- [ ] Implementar métodos auxiliares de creación de datos
- [ ] Crear tests R001-R005 con patrón Given-When-Then
- [ ] Agregar logging detallado en cada test
- [ ] Configurar JaCoCo para métricas de cobertura
- [ ] Integrar tests en GitHub Actions CI/CD
- [ ] Generar reporte HTML de resultados
- [ ] Validar cobertura >80%
- [ ] Documentar casos de test adicionales necesarios
- [ ] Ejecutar suite completa y verificar logs

---

**Próximos Pasos:**

1. Corregir archivo `ProduccionRegressionTest.java` con los campos correctos
2. Ejecutar `mvn clean test -Dtest=ProduccionRegressionTest`
3. Analizar logs generados para identificar fallos restantes
4. Iterar hasta lograr 100% de tests pasando
5. Generar reporte de cobertura con JaCoCo
