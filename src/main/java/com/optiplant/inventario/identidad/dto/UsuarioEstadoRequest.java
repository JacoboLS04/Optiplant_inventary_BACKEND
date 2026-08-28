package com.optiplant.inventario.identidad.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioEstadoRequest {

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo;
}
