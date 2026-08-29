package com.optiplant.inventario.catalogo.service;

import com.optiplant.inventario.catalogo.dto.SucursalRequest;
import com.optiplant.inventario.catalogo.dto.SucursalResponse;
import com.optiplant.inventario.catalogo.entity.Sucursal;
import com.optiplant.inventario.catalogo.mapper.SucursalMapper;
import com.optiplant.inventario.catalogo.repository.SucursalRepository;
import com.optiplant.inventario.common.exception.BusinessRuleException;
import com.optiplant.inventario.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SucursalService {

    private final SucursalRepository repository;
    private final SucursalMapper mapper;

    @Transactional(readOnly = true)
    public List<SucursalResponse> findAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SucursalResponse getById(Long id) {
        return mapper.toResponse(obtener(id));
    }

    @Transactional
    public SucursalResponse create(SucursalRequest request) {
        validarNombreUnico(request.getNombre(), null);
        Sucursal sucursal = Sucursal.builder()
                .nombre(request.getNombre())
                .direccion(request.getDireccion())
                .build();
        return mapper.toResponse(repository.save(sucursal));
    }

    @Transactional
    public SucursalResponse update(Long id, SucursalRequest request) {
        Sucursal sucursal = obtener(id);
        validarNombreUnico(request.getNombre(), id);
        sucursal.setNombre(request.getNombre());
        sucursal.setDireccion(request.getDireccion());
        return mapper.toResponse(repository.save(sucursal));
    }

    @Transactional
    public SucursalResponse inactivar(Long id) {
        Sucursal sucursal = obtener(id);
        if (!"activa".equals(sucursal.getEstado())) {
            throw new BusinessRuleException("La sucursal ya está inactiva: " + id);
        }
        sucursal.setEstado("inactiva");
        return mapper.toResponse(repository.save(sucursal));
    }

    private Sucursal obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada: " + id));
    }

    private void validarNombreUnico(String nombre, Long idExcluida) {
        if (nombre == null || nombre.isBlank()) {
            return;
        }
        repository.findAll().stream()
                .filter(s -> !s.getId().equals(idExcluida))
                .filter(s -> s.getNombre().equalsIgnoreCase(nombre.trim()))
                .findFirst()
                .ifPresent(s -> {
                    throw new BusinessRuleException("Ya existe una sucursal con ese nombre: " + nombre);
                });
    }
}
