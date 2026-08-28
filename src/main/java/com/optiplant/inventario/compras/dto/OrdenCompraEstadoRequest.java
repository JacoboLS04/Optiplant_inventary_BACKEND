package com.optiplant.inventario.compras.dto;

import com.optiplant.inventario.compras.entity.EstadoOrdenCompra;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdenCompraEstadoRequest {

    @NotNull(message = "El estado destino es obligatorio")
    private EstadoOrdenCompra estado;
}
