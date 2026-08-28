package com.optiplant.inventario.identidad.controller;

import com.optiplant.inventario.common.dto.PaginatedResponse;
import com.optiplant.inventario.identidad.dto.ActualizarUsuarioRequest;
import com.optiplant.inventario.identidad.dto.CrearUsuarioRequest;
import com.optiplant.inventario.identidad.dto.UsuarioEstadoRequest;
import com.optiplant.inventario.identidad.dto.UsuarioResponse;
import com.optiplant.inventario.identidad.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(
            @Valid @RequestBody CrearUsuarioRequest request) {
        UsuarioResponse response = service.crear(request);
        return ResponseEntity.created(
                        URI.create("/api/v1/usuarios/" + response.getId()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<UsuarioResponse>> listar(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.listar(busqueda, rol, activo, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUsuarioRequest request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<UsuarioResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioEstadoRequest request) {
        return ResponseEntity.ok(service.cambiarEstado(id, request));
    }
}
