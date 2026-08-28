package com.optiplant.inventario.transferencias.entity;

import com.optiplant.inventario.catalogo.entity.Producto;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "transferencia_linea")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferenciaLinea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transferencia_id", nullable = false)
    private Transferencia transferencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "cantidad_solicitada", nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidadSolicitada;

    @Column(name = "cantidad_despachada", precision = 12, scale = 2)
    private BigDecimal cantidadDespachada;

    @Column(name = "cantidad_recibida", precision = 12, scale = 2)
    private BigDecimal cantidadRecibida;
}
