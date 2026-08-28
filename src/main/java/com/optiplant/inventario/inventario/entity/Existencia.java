package com.optiplant.inventario.inventario.entity;

import com.optiplant.inventario.catalogo.entity.Producto;
import com.optiplant.inventario.catalogo.entity.Sucursal;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "existencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Existencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @Column(name = "cantidad_fisica", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal cantidadFisica = BigDecimal.ZERO;

    @Column(name = "cantidad_reservada", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal cantidadReservada = BigDecimal.ZERO;

    @Column(name = "stock_minimo", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal stockMinimo = BigDecimal.ZERO;

    @Column(name = "costo_promedio", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal costoPromedio = BigDecimal.ZERO;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
