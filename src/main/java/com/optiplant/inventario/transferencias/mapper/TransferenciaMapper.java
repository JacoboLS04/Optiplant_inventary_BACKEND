package com.optiplant.inventario.transferencias.mapper;

import com.optiplant.inventario.transferencias.dto.TransferenciaResponse;
import com.optiplant.inventario.transferencias.entity.Transferencia;
import com.optiplant.inventario.transferencias.entity.TransferenciaFaltante;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransferenciaMapper {

    @Mapping(target = "productoId", source = "producto.id")
    @Mapping(target = "sku", source = "producto.sku")
    @Mapping(target = "nombreProducto", source = "producto.nombre")
    @Mapping(target = "sucursalOrigenId", source = "sucursalOrigen.id")
    @Mapping(target = "nombreSucursalOrigen", source = "sucursalOrigen.nombre")
    @Mapping(target = "sucursalDestinoId", source = "sucursalDestino.id")
    @Mapping(target = "nombreSucursalDestino", source = "sucursalDestino.nombre")
    @Mapping(target = "usuarioSolicitanteId",
            expression = "java(t.getUsuarioSolicitante() != null ? t.getUsuarioSolicitante().getId() : null)")
    @Mapping(target = "nombreUsuarioSolicitante",
            expression = "java(t.getUsuarioSolicitante() != null ? t.getUsuarioSolicitante().getNombre() : null)")
    @Mapping(target = "reserva", ignore = true)
    @Mapping(target = "aprobaciones", ignore = true)
    @Mapping(target = "faltantes", ignore = true)
    @Mapping(target = "cantidadDisponibleOrigen", ignore = true)
    TransferenciaResponse toResponse(Transferencia t);

    List<TransferenciaResponse> toResponseList(List<Transferencia> transferencias);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "cantidadFaltante", source = "cantidadFaltante")
    @Mapping(target = "tratamiento", source = "tratamiento")
    @Mapping(target = "usuarioId", expression = "java(f.getUsuario() != null ? f.getUsuario().getId() : null)")
    @Mapping(target = "nombreUsuario", expression = "java(f.getUsuario() != null ? f.getUsuario().getNombre() : null)")
    @Mapping(target = "fecha", source = "fecha")
    TransferenciaResponse.FaltanteResponse toFaltanteResponse(TransferenciaFaltante f);
}
