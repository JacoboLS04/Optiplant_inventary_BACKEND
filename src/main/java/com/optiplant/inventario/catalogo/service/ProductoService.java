package com.optiplant.inventario.catalogo.service;

import com.optiplant.inventario.catalogo.dto.ProductoRequest;
import com.optiplant.inventario.catalogo.dto.ProductoResponse;
import com.optiplant.inventario.catalogo.entity.Categoria;
import com.optiplant.inventario.catalogo.entity.Producto;
import com.optiplant.inventario.catalogo.entity.UnidadMedida;
import com.optiplant.inventario.catalogo.mapper.ProductoMapper;
import com.optiplant.inventario.catalogo.repository.ProductoRepository;
import com.optiplant.inventario.common.dto.PaginatedResponse;
import com.optiplant.inventario.common.exception.ResourceNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository repository;
    private final ProductoMapper mapper;
    private final CategoriaService categoriaService;
    private final UnidadMedidaService unidadMedidaService;

    private static final String SKU_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional(readOnly = true)
    public PaginatedResponse<ProductoResponse> search(String search, Long categoriaId, int page, int size) {

        Specification<Producto> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase().trim() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("nombre")), pattern),
                        cb.like(cb.lower(root.get("sku")), pattern)
                ));
            }

            if (categoriaId != null) {
                predicates.add(cb.equal(root.get("categoria").get("id"), categoriaId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ProductoResponse> pageResult = repository.findAll(spec,
                        PageRequest.of(page, size, Sort.by("nombre")))
                .map(mapper::toResponse);
        return new PaginatedResponse<>(
                pageResult.getContent(), page, size,
                pageResult.getTotalElements(), pageResult.getTotalPages());
    }

    @Transactional(readOnly = true)
    public ProductoResponse getById(Long id) {
        Producto producto = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + id));
        return mapper.toResponse(producto);
    }

    @Transactional
    public ProductoResponse create(ProductoRequest request) {
        Categoria categoria = categoriaService.findById(request.getCategoriaId());
        UnidadMedida unidadBase = unidadMedidaService.findById(request.getUnidadBaseId());

        String sku = request.getSku() != null && !request.getSku().isBlank()
                ? request.getSku()
                : generarSkuUnico();

        Producto producto = Producto.builder()
                .sku(sku)
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .categoria(categoria)
                .unidadBase(unidadBase)
                .build();

        return mapper.toResponse(repository.save(producto));
    }

    @Transactional
    public ProductoResponse update(Long id, ProductoRequest request) {
        Producto producto = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + id));

        Categoria categoria = categoriaService.findById(request.getCategoriaId());
        UnidadMedida unidadBase = unidadMedidaService.findById(request.getUnidadBaseId());

        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setCategoria(categoria);
        producto.setUnidadBase(unidadBase);

        if (request.getSku() != null && !request.getSku().isBlank()
                && !request.getSku().equals(producto.getSku())) {
            if (repository.existsBySku(request.getSku())) {
                throw new com.optiplant.inventario.common.exception.BusinessRuleException(
                        "El SKU ya existe: " + request.getSku());
            }
            producto.setSku(request.getSku());
        }

        return mapper.toResponse(repository.save(producto));
    }

    private String generarSkuUnico() {
        String sku;
        int maxAttempts = 10;
        do {
            StringBuilder sb = new StringBuilder("PRD-");
            for (int i = 0; i < 8; i++) {
                sb.append(SKU_CHARS.charAt(RANDOM.nextInt(SKU_CHARS.length())));
            }
            sku = sb.toString();
            maxAttempts--;
        } while (repository.existsBySku(sku) && maxAttempts > 0);

        if (maxAttempts == 0) {
            throw new com.optiplant.inventario.common.exception.BusinessRuleException(
                    "No se pudo generar un SKU único. Intente nuevamente.");
        }
        return sku;
    }
}
