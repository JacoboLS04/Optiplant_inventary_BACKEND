package com.optiplant.inventario.compras.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DespachoRequest {

    /** Transportista que levantó el pedido (opcional). */
    private String transportista;

    /** Número de guía de remisión (opcional). */
    private String guia;

    /** Nueva fecha estimada de entrega informada por el proveedor (opcional). */
    private LocalDate fechaEntregaEstimada;
}