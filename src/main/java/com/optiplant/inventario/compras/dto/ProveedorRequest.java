package com.optiplant.inventario.compras.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200)
    private String nombre;

    @Size(max = 200)
    private String contacto;

    private String condicionesGenerales;
}
