package com.optiplant.inventario.identidad.controller;

import com.optiplant.inventario.common.exception.BusinessRuleException;
import com.optiplant.inventario.common.security.CustomUserDetailsService;
import com.optiplant.inventario.common.security.JwtUtil;
import com.optiplant.inventario.catalogo.entity.Sucursal;
import com.optiplant.inventario.catalogo.repository.SucursalRepository;
import com.optiplant.inventario.identidad.dto.AuthResponse;
import com.optiplant.inventario.identidad.dto.LoginRequest;
import com.optiplant.inventario.identidad.dto.RegisterRequest;
import com.optiplant.inventario.identidad.dto.UsuarioResponse;
import com.optiplant.inventario.identidad.entity.Usuario;
import com.optiplant.inventario.identidad.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;
    private final SucursalRepository sucursalRepository;
    private final PasswordEncoder passwordEncoder;

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

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("El email ya está registrado: " + request.getEmail());
        }

        Sucursal sucursal = null;
        if (request.getSucursalId() != null) {
            sucursal = sucursalRepository.findById(request.getSucursalId())
                    .orElseThrow(() -> new com.optiplant.inventario.common.exception.ResourceNotFoundException(
                            "Sucursal no encontrada: " + request.getSucursalId()));
        }

        Usuario usuario = Usuario.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nombre(request.getNombre())
                .rol(request.getRol())
                .sucursal(sucursal)
                .build();

        usuarioRepository.save(usuario);

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        String token = jwtUtil.generateToken(userDetails, usuario.getId());

        return ResponseEntity.created(URI.create("/api/v1/auth/" + usuario.getId()))
                .body(AuthResponse.builder()
                        .token(token)
                        .usuarioId(usuario.getId())
                        .email(usuario.getEmail())
                        .nombre(usuario.getNombre())
                        .rol(usuario.getRol())
                        .build());
    }
}
