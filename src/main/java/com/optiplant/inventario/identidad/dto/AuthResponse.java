package com.optiplant.inventario.identidad.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    @Builder.Default
    private String tipo = "Bearer";
    private Long usuarioId;
    private String email;
    private String nombre;
    private String rol;
}
