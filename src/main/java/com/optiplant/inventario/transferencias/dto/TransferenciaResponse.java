package com.optiplant.inventario.transferencias.dto;

import com.optiplant.inventario.transferencias.entity.EstadoReserva;
import com.optiplant.inventario.transferencias.entity.EstadoTransferencia;
import com.optiplant.inventario.transferencias.entity.TratamientoFaltante;
import com.optiplant.inventario.transferencias.entity.UrgenciaTransferencia;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaResponse {

    private Long id;
    private String codigo;
    private Long productoId;
    private String sku;
    private String nombreProducto;
    private Long sucursalOrigenId;
    private String nombreSucursalOrigen;
    private Long sucursalDestinoId;
    private String nombreSucursalDestino;
    private Long usuarioSolicitanteId;
    private String nombreUsuarioSolicitante;
    private BigDecimal cantidadSolicitada;
    private BigDecimal cantidadDespachada;
    private BigDecimal cantidadRecibida;
    private UrgenciaTransferencia urgencia;
    private String transportista;
    private String guia;
    private LocalDateTime fechaEstimadaLlegada;
    private EstadoTransferencia estado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaDespacho;
    private LocalDateTime fechaRecepcion;
    private BigDecimal cantidadDisponibleOrigen;
    private ReservaResponse reserva;
    private List<AprobacionResponse> aprobaciones;
    private List<FaltanteResponse> faltantes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservaResponse {
        private Long id;
        private Long productoId;
        private Long sucursalId;
        private BigDecimal cantidad;
        private EstadoReserva estado;
        private LocalDateTime fechaCreacion;
        private LocalDateTime fechaLiberacion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AprobacionResponse {
        private Long id;
        private Long gerenteId;
        private String nombreGerente;
        private String rolAprobacion;
        private String decision;
        private LocalDateTime fecha;
        private String observacion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FaltanteResponse {
        private Long id;
        private BigDecimal cantidadFaltante;
        private TratamientoFaltante tratamiento;
        private Long usuarioId;
        private String nombreUsuario;
        private LocalDateTime fecha;
    }
}
