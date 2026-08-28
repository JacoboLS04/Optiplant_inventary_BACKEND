package com.optiplant.inventario.identidad.service;

import com.optiplant.inventario.catalogo.entity.Sucursal;
import com.optiplant.inventario.catalogo.repository.SucursalRepository;
import com.optiplant.inventario.common.dto.PaginatedResponse;
import com.optiplant.inventario.common.exception.BusinessRuleException;
import com.optiplant.inventario.common.exception.ResourceNotFoundException;
import com.optiplant.inventario.identidad.dto.ActualizarUsuarioRequest;
import com.optiplant.inventario.identidad.dto.CrearUsuarioRequest;
import com.optiplant.inventario.identidad.dto.UsuarioEstadoRequest;
import com.optiplant.inventario.identidad.dto.UsuarioResponse;
import com.optiplant.inventario.identidad.entity.Usuario;
import com.optiplant.inventario.identidad.repository.UsuarioRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    public static final Set<String> ROLES_VALIDOS =
            Set.of("ADMINISTRADOR", "GERENTE", "OPERADOR");

    private final UsuarioRepository usuarioRepository;
    private final SucursalRepository sucursalRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponse crear(CrearUsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("El email ya está registrado: " + request.getEmail());
        }

        validarRol(request.getRol());
        Sucursal sucursal = resolverSucursalObligatoria(request.getRol(), request.getSucursalId());

        Usuario usuario = Usuario.builder()
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .nombre(request.getNombre())
                .rol(rolNormalizado(request.getRol()))
                .sucursal(sucursal)
                .activo(true)
                .build();

        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<UsuarioResponse> listar(
            String busqueda, String rol, Boolean activo, int page, int size) {

        Specification<Usuario> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (busqueda != null && !busqueda.isBlank()) {
                String patron = "%" + busqueda.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("nombre")), patron),
                        cb.like(cb.lower(root.get("email")), patron)
                ));
            }
            if (rol != null && !rol.isBlank()) {
                predicates.add(cb.equal(root.get("rol"), rolNormalizado(rol)));
            }
            if (activo != null) {
                predicates.add(cb.equal(root.get("activo"), activo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Usuario> usuarios = usuarioRepository.findAll(spec,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "nombre")));

        return new PaginatedResponse<>(
                usuarios.getContent().stream().map(this::toResponse).toList(),
                page, size, usuarios.getTotalElements(), usuarios.getTotalPages());
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest request) {
        Usuario usuario = findOrThrow(id);

        validarRol(request.getRol());
        Sucursal sucursal = resolverSucursalObligatoria(request.getRol(), request.getSucursalId());

        usuario.setNombre(request.getNombre());
        usuario.setRol(rolNormalizado(request.getRol()));
        usuario.setSucursal(sucursal);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse cambiarEstado(Long id, UsuarioEstadoRequest request) {
        Usuario usuario = findOrThrow(id);

        usuario.setActivo(request.getActivo());
        return toResponse(usuarioRepository.save(usuario));
    }

    private void validarRol(String rol) {
        if (rol == null || !ROLES_VALIDOS.contains(rolNormalizado(rol))) {
            throw new BusinessRuleException(
                    "Rol inválido. Debe ser uno de: " + ROLES_VALIDOS);
        }
    }

    private String rolNormalizado(String rol) {
        return rol == null ? "" : rol.trim().toUpperCase();
    }

    private Sucursal resolverSucursalObligatoria(String rol, Long sucursalId) {
        String rolNorm = rolNormalizado(rol);

        if ("ADMINISTRADOR".equals(rolNorm)) {
            if (sucursalId != null) {
                throw new BusinessRuleException(
                        "El rol ADMINISTRADOR no se asocia a una sucursal específica.");
            }
            return null;
        }

        if (sucursalId == null) {
            throw new BusinessRuleException(
                    "El rol " + rolNorm + " requiere una sucursal.");
        }
        return sucursalRepository.findById(sucursalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sucursal no encontrada: " + sucursalId));
    }

    private Usuario findOrThrow(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .rol(usuario.getRol())
                .sucursalId(usuario.getSucursal() != null ? usuario.getSucursal().getId() : null)
                .sucursalNombre(usuario.getSucursal() != null ? usuario.getSucursal().getNombre() : null)
                .activo(usuario.getActivo() != null ? usuario.getActivo() : true)
                .build();
    }
}
