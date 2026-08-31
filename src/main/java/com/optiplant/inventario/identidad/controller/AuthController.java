package com.optiplant.inventario.identidad.controller;

import com.optiplant.inventario.common.security.CustomUserDetailsService;
import com.optiplant.inventario.common.security.JwtUtil;
import com.optiplant.inventario.common.security.TokenRevocationService;
import com.optiplant.inventario.common.security.UsuarioActualService;
import com.optiplant.inventario.identidad.dto.AuthResponse;
import com.optiplant.inventario.identidad.dto.LoginRequest;
import com.optiplant.inventario.identidad.dto.UsuarioResponse;
import com.optiplant.inventario.identidad.entity.Usuario;
import com.optiplant.inventario.identidad.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;
    private final TokenRevocationService tokenRevocationService;
    private final UsuarioActualService usuarioActualService;

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> perfilActual() {
        Usuario usuario = usuarioActualService.actual()
                .orElseThrow(() -> new com.optiplant.inventario.common.exception.ResourceNotFoundException(
                        "Usuario autenticado no encontrado"));
        return ResponseEntity.ok(UsuarioResponse.builder()
                .id(usuario.getId())
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .rol(usuario.getRol())
                .sucursalId(usuario.getSucursal() != null ? usuario.getSucursal().getId() : null)
                .sucursalNombre(usuario.getSucursal() != null ? usuario.getSucursal().getNombre() : null)
                .proveedorId(usuario.getProveedor() != null ? usuario.getProveedor().getId() : null)
                .proveedorNombre(usuario.getProveedor() != null ? usuario.getProveedor().getNombre() : null)
                .activo(usuario.getActivo() != null ? usuario.getActivo() : true)
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail()).orElseThrow();

        String token = jwtUtil.generateToken(userDetails, usuario.getId());

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .usuarioId(usuario.getId())
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .rol(usuario.getRol())
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            tokenRevocationService.revoke(authorization.substring(7));
        }
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente"));
    }
}
