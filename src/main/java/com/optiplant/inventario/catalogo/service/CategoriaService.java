package com.optiplant.inventario.catalogo.service;

import com.optiplant.inventario.catalogo.dto.CategoriaResponse;
import com.optiplant.inventario.catalogo.entity.Categoria;
import com.optiplant.inventario.catalogo.mapper.CategoriaMapper;
import com.optiplant.inventario.catalogo.repository.CategoriaRepository;
import com.optiplant.inventario.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository repository;
    private final CategoriaMapper mapper;

    public List<CategoriaResponse> findAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    public Categoria findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada: " + id));
    }
}
