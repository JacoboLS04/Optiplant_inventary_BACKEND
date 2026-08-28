package com.optiplant.inventario.catalogo.controller;

import com.optiplant.inventario.catalogo.dto.PrecioProductoRequest;
import com.optiplant.inventario.catalogo.dto.PrecioProductoResponse;
import com.optiplant.inventario.catalogo.service.PrecioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/precios")
@RequiredArgsConstructor
public class PrecioController {

    private final PrecioService service;

    @PostMapping
    public ResponseEntity<PrecioProductoResponse> setPrecio(
            @Valid @RequestBody PrecioProductoRequest request) {
        PrecioProductoResponse response = service.setPrecio(request);
        return ResponseEntity.created(URI.create("/api/v1/precios/" + response.getId()))
                .body(response);
    }

    @PutMapping("/{productoId}")
    public ResponseEntity<PrecioProductoResponse> updatePrecio(
            @PathVariable Long productoId,
            @Valid @RequestBody PrecioProductoRequest request) {
        request.setProductoId(productoId);
        PrecioProductoResponse response = service.setPrecio(request);
        return ResponseEntity.ok(response);
    }
}
