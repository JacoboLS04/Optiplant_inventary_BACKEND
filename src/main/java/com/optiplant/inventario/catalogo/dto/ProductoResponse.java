package com.optiplant.inventario.catalogo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponse {
    private Long id;
    private String sku;
    private String nombre;
    private String descripcion;
    private Long categoriaId;
    private String categoriaNombre;
    private Long unidadBaseId;
    private String estado;
}
