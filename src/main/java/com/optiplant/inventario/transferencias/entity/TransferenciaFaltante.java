package com.optiplant.inventario.transferencias.entity;

import com.optiplant.inventario.identidad.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transferencia_faltante")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferenciaFaltante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transferencia_linea_id", nullable = false)
    private TransferenciaLinea linea;

    @Column(name = "cantidad_faltante", nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidadFaltante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TratamientoFaltante tratamiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();
}
