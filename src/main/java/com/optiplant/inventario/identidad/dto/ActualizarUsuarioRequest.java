package com.optiplant.inventario.identidad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarUsuarioRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El rol es obligatorio")
    private String rol;

    private Long sucursalId;

    /** Obligatorio solo para el rol PROVEEDOR: el proveedor al que pertenece. */
    private Long proveedorId;

    /** Contraseña opcional: si viene en blanco se conserva la actual. */
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
}
