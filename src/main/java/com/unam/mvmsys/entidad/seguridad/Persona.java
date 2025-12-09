package com.unam.mvmsys.entidad.seguridad;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.unam.mvmsys.entidad.base.BaseEntity;
import com.unam.mvmsys.entidad.configuracion.CategoriaCliente;
import com.unam.mvmsys.entidad.financiero.CuentaCorriente;
import com.unam.mvmsys.entidad.stock.Rubro;

@Entity
@Table(name = "personas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Persona extends BaseEntity {

    @Column(name = "cuit_dni", nullable = false, unique = true, length = 13)
    private String cuitDni;

    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    @Column(length = 100)
    private String email;

    @Column(length = 50)
    private String telefono;

    @Column(name = "direccion_calle", length = 200)
    private String direccionCalle;

    @Column(name = "direccion_numero", length = 20)
    private String direccionNumero;

    @ManyToOne
    @JoinColumn(name = "localidad_id")
    private Localidad localidad;

    // Flags de Roles
    @Column(name = "es_cliente", nullable = false)
    private boolean esCliente;

    @Column(name = "es_proveedor", nullable = false)
    private boolean esProveedor;

    @Column(name = "es_empleado", nullable = false)
    private boolean esEmpleado;

    // Categoría de cliente (IRQ-06) - FK a tabla categorias_cliente
    @ManyToOne
    @JoinColumn(name = "categoria_cliente_id")
    private CategoriaCliente categoriaCliente;

    // Datos comerciales para proveedores
    @ManyToOne
    @JoinColumn(name = "rubro_id")
    private Rubro rubro;

    @Column(name = "plazo_pago_dias", nullable = false)
    @Builder.Default
    private int plazoPagoDias = 0;

    @Column(name = "bonificacion_porcentaje", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal bonificacionPorcentaje = BigDecimal.ZERO;

    @Column(name = "interes_mora_porcentaje", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal interesMoraPorcentaje = BigDecimal.ZERO;

    @Column(name = "fecha_alta", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaAlta = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    // Relación Bidireccional con Cuenta Corriente (Opcional pero útil)
    @OneToOne(mappedBy = "persona", cascade = CascadeType.ALL)
    private CuentaCorriente cuentaCorriente;
}