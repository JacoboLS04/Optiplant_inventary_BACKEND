package com.optiplant.inventario.catalogo.controller;

import com.optiplant.inventario.catalogo.dto.UnidadMedidaResponse;
import com.optiplant.inventario.catalogo.service.UnidadMedidaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/unidades-medida")
@RequiredArgsConstructor
public class UnidadMedidaController {

    private final UnidadMedidaService service;

    @GetMapping
    public ResponseEntity<List<UnidadMedidaResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }
}
