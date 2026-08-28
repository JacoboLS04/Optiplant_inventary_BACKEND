package com.optiplant.inventario.transferencias.entity;

import com.optiplant.inventario.identidad.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transferencia_aprobacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferenciaAprobacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transferencia_id", nullable = false)
    private Transferencia transferencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gerente_id", nullable = false)
    private Usuario gerente;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol_aprobacion", nullable = false, length = 20)
    private RolAprobacion rolAprobacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DecisionAprobacion decision;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(length = 500)
    private String observacion;
}
