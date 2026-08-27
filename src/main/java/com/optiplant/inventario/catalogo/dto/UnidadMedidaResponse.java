package com.optiplant.inventario.catalogo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnidadMedidaResponse {
    private Long id;
    private String nombre;
    private String simbolo;
}
