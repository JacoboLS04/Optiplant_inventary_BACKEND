package com.optiplant.inventario.transferencias.dto;

import com.optiplant.inventario.transferencias.entity.UrgenciaTransferencia;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaRequest {

    @NotNull(message = "El producto es obligatorio")
    private Long productoId;

    @NotNull(message = "La sucursal origen es obligatoria")
    private Long sucursalOrigenId;

    @NotNull(message = "La sucursal destino es obligatoria")
    private Long sucursalDestinoId;

    @NotNull(message = "La cantidad solicitada es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
    private BigDecimal cantidadSolicitada;

    private UrgenciaTransferencia urgencia;

    private String transportista;

    private String guia;

    private LocalDateTime fechaEstimadaLlegada;
}
