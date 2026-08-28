package com.optiplant.inventario.compras.entity;

import com.optiplant.inventario.catalogo.entity.Producto;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "orden_compra_linea")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenCompraLinea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orden_compra_id", nullable = false)
    private OrdenCompra ordenCompra;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "cantidad_ordenada", nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidadOrdenada;

    @Column(name = "cantidad_recibida", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal cantidadRecibida = BigDecimal.ZERO;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal precioUnitario = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal descuento = BigDecimal.ZERO;

    public BigDecimal getCantidadPendiente() {
        return cantidadOrdenada.subtract(cantidadRecibida);
    }
}
