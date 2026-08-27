package com.optiplant.inventario.catalogo.service;

import com.optiplant.inventario.catalogo.dto.UnidadMedidaResponse;
import com.optiplant.inventario.catalogo.entity.UnidadMedida;
import com.optiplant.inventario.catalogo.mapper.UnidadMedidaMapper;
import com.optiplant.inventario.catalogo.repository.UnidadMedidaRepository;
import com.optiplant.inventario.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UnidadMedidaService {

    private final UnidadMedidaRepository repository;
    private final UnidadMedidaMapper mapper;

    public List<UnidadMedidaResponse> findAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    public UnidadMedida findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unidad de medida no encontrada: " + id));
    }
}
