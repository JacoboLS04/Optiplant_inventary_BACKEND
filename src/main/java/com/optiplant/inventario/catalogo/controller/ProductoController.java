package com.optiplant.inventario.catalogo.controller;

import com.optiplant.inventario.catalogo.dto.ProductoRequest;
import com.optiplant.inventario.catalogo.dto.ProductoResponse;
import com.optiplant.inventario.catalogo.service.ProductoService;
import com.optiplant.inventario.common.dto.PaginatedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService service;

    @GetMapping
    public ResponseEntity<PaginatedResponse<ProductoResponse>> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.search(search, categoriaId, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<ProductoResponse> create(
            @Valid @RequestBody ProductoRequest request) {
        ProductoResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/productos/" + response.getId()))
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ProductoResponse> inactivar(@PathVariable Long id) {
        return ResponseEntity.ok(service.inactivar(id));
    }
}
