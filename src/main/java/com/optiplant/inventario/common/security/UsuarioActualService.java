package com.optiplant.inventario.common.security;

import com.optiplant.inventario.identidad.entity.Usuario;
import com.optiplant.inventario.identidad.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Resuelve el usuario autenticado desde el contexto de seguridad y aplica la
 * política RF-009: las operaciones de escritura (venta, ingreso, retiro) quedan
 * restringidas a la sucursal del usuario, salvo para el rol ADMINISTRADOR.
 */
@Service
@RequiredArgsConstructor
public class UsuarioActualService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Optional<Usuario> actual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetails userDetails)) {
            return Optional.empty();
        }
        Optional<Usuario> usuario = usuarioRepository.findByEmail(userDetails.getUsername());
        usuario.ifPresent(u -> {
            // Inicializa las relaciones LAZY dentro de la transacción para poder
            // leer sucursal/proveedor fuera de ella (evita LazyInitializationException).
            if (u.getSucursal() != null) {
                u.getSucursal().getNombre();
            }
            if (u.getProveedor() != null) {
                u.getProveedor().getNombre();
            }
        });
        return usuario;
    }

    public boolean esAdministrador() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
    }

    /**
     * Asegura que el usuario autenticado pueda operar sobre la sucursal dada.
     * El ADMINISTRADOR puede operar en cualquier sucursal; el resto solo en la
     * suya. Lanza 403 en caso contrario.
     */
    public void validarAccesoSucursal(Long sucursalId) {
        if (esAdministrador()) {
            return;
        }
        Usuario usuario = actual().orElse(null);
        Long sucursalUsuario = usuario != null && usuario.getSucursal() != null
                ? usuario.getSucursal().getId()
                : null;
        if (sucursalUsuario == null || !sucursalUsuario.equals(sucursalId)) {
            throw new AccessDeniedException(
                    "Solo puedes operar sobre tu propia sucursal (RF-009)");
        }
    }
}
