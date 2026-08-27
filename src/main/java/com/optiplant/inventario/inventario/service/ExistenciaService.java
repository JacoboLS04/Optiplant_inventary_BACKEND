package com.optiplant.inventario.inventario.service;

import com.optiplant.inventario.common.dto.PaginatedResponse;
import com.optiplant.inventario.inventario.dto.ExistenciaResponse;
import com.optiplant.inventario.inventario.entity.Existencia;
import com.optiplant.inventario.inventario.mapper.ExistenciaMapper;
import com.optiplant.inventario.inventario.repository.ExistenciaRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExistenciaService {

    private final ExistenciaRepository repository;
    private final ExistenciaMapper mapper;

    @Transactional(readOnly = true)
    public PaginatedResponse<ExistenciaResponse> search(
            String search, Long categoriaId, Long sucursalId,
            String estadoStock, LocalDateTime actualizadoDesde,
            LocalDateTime actualizadoHasta, int page, int size) {

        Specification<Existencia> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("producto").get("nombre")),
                                "%" + search.toLowerCase() + "%"),
                        cb.like(cb.lower(root.get("producto").get("sku")),
                                "%" + search.toLowerCase() + "%")
                ));
            }

            if (categoriaId != null) {
                predicates.add(cb.equal(
                        root.get("producto").get("categoria").get("id"), categoriaId));
            }

            if (sucursalId != null) {
                predicates.add(cb.equal(root.get("sucursal").get("id"), sucursalId));
            }

            if (actualizadoDesde != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("updatedAt"), actualizadoDesde));
            }

            if (actualizadoHasta != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("updatedAt"), actualizadoHasta));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Existencia> existencias = repository.findAll(spec,
                PageRequest.of(page, size));

        Page<ExistenciaResponse> responsePage = existencias.map(mapper::toResponse);

        if (estadoStock != null && !estadoStock.isBlank()) {
            List<ExistenciaResponse> filtered = responsePage.getContent().stream()
                    .filter(e -> estadoStock.equals(e.getEstadoStock()))
                    .toList();
            return new PaginatedResponse<>(
                    filtered, page, size,
                    filtered.size(), 1);
        }

        return new PaginatedResponse<>(
                responsePage.getContent(), page, size,
                responsePage.getTotalElements(), responsePage.getTotalPages());
    }
}
