package com.optiplant.inventario.compras.service;

import com.optiplant.inventario.common.exception.ResourceNotFoundException;
import com.optiplant.inventario.compras.dto.ProveedorRequest;
import com.optiplant.inventario.compras.dto.ProveedorResponse;
import com.optiplant.inventario.compras.entity.Proveedor;
import com.optiplant.inventario.compras.mapper.ComprasMapper;
import com.optiplant.inventario.compras.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProveedorService {

    private final ProveedorRepository repository;
    private final ComprasMapper mapper;

    @Transactional
    public ProveedorResponse crear(ProveedorRequest request) {
        Proveedor proveedor = mapper.toEntity(request);
        return mapper.toResponse(repository.save(proveedor));
    }

    @Transactional(readOnly = true)
    public List<ProveedorResponse> listar() {
        return mapper.toProveedorResponseList(repository.findAll());
    }

    @Transactional(readOnly = true)
    public ProveedorResponse obtener(Long id) {
        return mapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public ProveedorResponse actualizar(Long id, ProveedorRequest request) {
        Proveedor proveedor = findOrThrow(id);
        mapper.updateProveedor(request, proveedor);
        return mapper.toResponse(repository.save(proveedor));
    }

    @Transactional
    public void eliminar(Long id) {
        Proveedor proveedor = findOrThrow(id);
        repository.delete(proveedor);
    }

    private Proveedor findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado: " + id));
    }
}
