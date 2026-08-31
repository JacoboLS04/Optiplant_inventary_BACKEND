package com.optiplant.inventario.identidad.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponse {

    private Long id;
    private String email;
    private String nombre;
    private String rol;
    private Long sucursalId;
    private String sucursalNombre;
    private Long proveedorId;
    private String proveedorNombre;
    private Boolean activo;
}
