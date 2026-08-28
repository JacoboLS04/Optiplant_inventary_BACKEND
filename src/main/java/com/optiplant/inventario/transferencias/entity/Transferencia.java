package com.optiplant.inventario.transferencias.entity;

import com.optiplant.inventario.catalogo.entity.Sucursal;
import com.optiplant.inventario.identidad.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transferencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transferencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_origen_id", nullable = false)
    private Sucursal sucursalOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_destino_id", nullable = false)
    private Sucursal sucursalDestino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_solicitante_id")
    private Usuario usuarioSolicitante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UrgenciaTransferencia urgencia = UrgenciaTransferencia.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoTransferencia estado = EstadoTransferencia.SOLICITADA;

    @Column(length = 200)
    private String transportista;

    @Column(length = 100)
    private String guia;

    @Column(name = "fecha_estimada_llegada")
    private LocalDateTime fechaEstimadaLlegada;

    @Column(name = "fecha_solicitud", nullable = false)
    @Builder.Default
    private LocalDateTime fechaSolicitud = LocalDateTime.now();

    @Column(name = "fecha_despacho")
    private LocalDateTime fechaDespacho;

    @Column(name = "fecha_recepcion")
    private LocalDateTime fechaRecepcion;

    @OneToMany(mappedBy = "transferencia", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TransferenciaLinea> lineas = new ArrayList<>();
}
