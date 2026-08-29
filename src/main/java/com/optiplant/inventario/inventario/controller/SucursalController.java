package com.optiplant.inventario.inventario.controller;

import com.optiplant.inventario.catalogo.dto.SucursalRequest;
import com.optiplant.inventario.catalogo.dto.SucursalResponse;
import com.optiplant.inventario.catalogo.service.SucursalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sucursales")
@RequiredArgsConstructor
public class SucursalController {

    private final SucursalService service;

    @GetMapping
    public ResponseEntity<List<SucursalResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SucursalResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<SucursalResponse> create(
            @Valid @RequestBody SucursalRequest request) {
        SucursalResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/sucursales/" + response.getId()))
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SucursalResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SucursalRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<SucursalResponse> inactivar(@PathVariable Long id) {
        return ResponseEntity.ok(service.inactivar(id));
    }
}
