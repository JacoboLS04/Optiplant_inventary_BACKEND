package com.optiplant.inventario.common.security;

import com.optiplant.inventario.catalogo.entity.Sucursal;
import com.optiplant.inventario.identidad.entity.Usuario;
import com.optiplant.inventario.identidad.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioActualServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioActualService service;

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    private void autenticar(String email, Collection<? extends GrantedAuthority> authorities) {
        Authentication auth = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        lenient().when(userDetails.getUsername()).thenReturn(email);
        lenient().when(auth.getPrincipal()).thenReturn(userDetails);
        lenient().when(auth.getAuthorities()).thenAnswer(inv -> authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private Usuario usuarioConSucursal(Long sucursalId) {
        Sucursal s = Sucursal.builder().id(sucursalId).nombre("Central").build();
        return Usuario.builder().id(5L).email("op@x.com").sucursal(s).build();
    }

    @Test
    void actualDevuelveUsuarioDelContexto() {
        autenticar("op@x.com", List.of(new SimpleGrantedAuthority("ROLE_OPERADOR")));
        Usuario u = usuarioConSucursal(1L);
        when(usuarioRepository.findByEmail("op@x.com")).thenReturn(Optional.of(u));

        assertEquals(Optional.of(u), service.actual());
    }

    @Test
    void actualDevuelveVacioSinAutenticacion() {
        assertEquals(Optional.empty(), service.actual());
    }

    @Test
    void esAdministradorReconoceElRol() {
        autenticar("admin@x.com",
                List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")));

        assertTrue(service.esAdministrador());
    }

    @Test
    void esAdministradorDevuelveFalsoParaOtrosRoles() {
        autenticar("op@x.com", List.of(new SimpleGrantedAuthority("ROLE_OPERADOR")));

        assertFalse(service.esAdministrador());
    }

    @Test
    void validarAccesoSucursalPermiteAlAdministradorCualquierSucursal() {
        autenticar("admin@x.com",
                List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")));

        service.validarAccesoSucursal(99L);
    }

    @Test
    void validarAccesoSucursalPermiteOperarSobreLaPropiaSucursal() {
        autenticar("op@x.com", List.of(new SimpleGrantedAuthority("ROLE_OPERADOR")));
        when(usuarioRepository.findByEmail("op@x.com"))
                .thenReturn(Optional.of(usuarioConSucursal(1L)));

        service.validarAccesoSucursal(1L);
    }

    @Test
    void validarAccesoSucursalRechazaSucursalAjena() {
        autenticar("op@x.com", List.of(new SimpleGrantedAuthority("ROLE_OPERADOR")));
        when(usuarioRepository.findByEmail("op@x.com"))
                .thenReturn(Optional.of(usuarioConSucursal(1L)));

        assertThrows(AccessDeniedException.class, () -> service.validarAccesoSucursal(2L));
    }

    @Test
    void validarAccesoSucursalRechazaSiElUsuarioNoTieneSucursal() {
        autenticar("op@x.com", List.of(new SimpleGrantedAuthority("ROLE_OPERADOR")));
        Usuario sinSucursal = Usuario.builder().id(5L).email("op@x.com").build();
        when(usuarioRepository.findByEmail("op@x.com")).thenReturn(Optional.of(sinSucursal));

        assertThrows(AccessDeniedException.class, () -> service.validarAccesoSucursal(1L));
    }
}
