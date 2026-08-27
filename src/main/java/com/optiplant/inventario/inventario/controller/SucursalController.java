package com.optiplant.inventario.inventario.controller;

import com.optiplant.inventario.catalogo.dto.SucursalResponse;
import com.optiplant.inventario.catalogo.entity.Sucursal;
import com.optiplant.inventario.catalogo.mapper.SucursalMapper;
import com.optiplant.inventario.catalogo.repository.SucursalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sucursales")
@RequiredArgsConstructor
public class SucursalController {

    private final SucursalRepository repository;
    private final SucursalMapper mapper;

    @GetMapping
    public ResponseEntity<List<SucursalResponse>> findAll() {
        List<SucursalResponse> sucursales = repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(sucursales);
    }
}
