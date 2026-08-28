package com.optiplant.inventario.identidad.controller;

import com.optiplant.inventario.common.security.CustomUserDetailsService;
import com.optiplant.inventario.common.security.JwtUtil;
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

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;

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
}
