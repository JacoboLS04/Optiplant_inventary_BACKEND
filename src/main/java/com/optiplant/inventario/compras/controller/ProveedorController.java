package com.optiplant.inventario.compras.controller;

import com.optiplant.inventario.compras.dto.ProveedorRequest;
import com.optiplant.inventario.compras.dto.ProveedorResponse;
import com.optiplant.inventario.compras.service.ProveedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService service;

    @PostMapping
    public ResponseEntity<ProveedorResponse> crear(@Valid @RequestBody ProveedorRequest request) {
        ProveedorResponse response = service.crear(request);
        return ResponseEntity.created(
                        URI.create("/api/v1/proveedores/" + response.getId()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProveedorResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProveedorResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProveedorResponse> actualizar(
            @PathVariable Long id, @Valid @RequestBody ProveedorRequest request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
