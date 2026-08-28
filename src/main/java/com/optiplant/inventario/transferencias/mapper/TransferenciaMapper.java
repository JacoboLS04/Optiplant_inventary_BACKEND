package com.optiplant.inventario.transferencias.mapper;

import com.optiplant.inventario.transferencias.dto.TransferenciaResponse;
import com.optiplant.inventario.transferencias.entity.Transferencia;
import com.optiplant.inventario.transferencias.entity.TransferenciaFaltante;
import com.optiplant.inventario.transferencias.entity.TransferenciaLinea;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransferenciaMapper {

    @Mapping(target = "sucursalOrigenId", source = "sucursalOrigen.id")
    @Mapping(target = "nombreSucursalOrigen", source = "sucursalOrigen.nombre")
    @Mapping(target = "sucursalDestinoId", source = "sucursalDestino.id")
    @Mapping(target = "nombreSucursalDestino", source = "sucursalDestino.nombre")
    @Mapping(target = "usuarioSolicitanteId",
            expression = "java(t.getUsuarioSolicitante() != null ? t.getUsuarioSolicitante().getId() : null)")
    @Mapping(target = "nombreUsuarioSolicitante",
            expression = "java(t.getUsuarioSolicitante() != null ? t.getUsuarioSolicitante().getNombre() : null)")
    @Mapping(target = "totalUnidades", ignore = true)
    @Mapping(target = "lineas", ignore = true)
    @Mapping(target = "aprobaciones", ignore = true)
    TransferenciaResponse toResponse(Transferencia t);

    List<TransferenciaResponse> toResponseList(List<Transferencia> transferencias);

    @Mapping(target = "id", source = "linea.id")
    @Mapping(target = "productoId", source = "linea.producto.id")
    @Mapping(target = "sku", source = "linea.producto.sku")
    @Mapping(target = "nombreProducto", source = "linea.producto.nombre")
    @Mapping(target = "cantidadSolicitada", source = "linea.cantidadSolicitada")
    @Mapping(target = "cantidadDespachada", source = "linea.cantidadDespachada")
    @Mapping(target = "cantidadRecibida", source = "linea.cantidadRecibida")
    @Mapping(target = "cantidadDisponibleOrigen", ignore = true)
    @Mapping(target = "reserva", ignore = true)
    @Mapping(target = "faltantes", ignore = true)
    TransferenciaResponse.LineaResponse toLineaResponse(TransferenciaLinea linea);

    List<TransferenciaResponse.LineaResponse> toLineaResponseList(List<TransferenciaLinea> lineas);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "cantidadFaltante", source = "cantidadFaltante")
    @Mapping(target = "tratamiento", source = "tratamiento")
    @Mapping(target = "usuarioId", expression = "java(f.getUsuario() != null ? f.getUsuario().getId() : null)")
    @Mapping(target = "nombreUsuario", expression = "java(f.getUsuario() != null ? f.getUsuario().getNombre() : null)")
    @Mapping(target = "fecha", source = "fecha")
    TransferenciaResponse.FaltanteResponse toFaltanteResponse(TransferenciaFaltante f);
}
