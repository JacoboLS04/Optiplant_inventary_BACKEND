package com.optiplant.inventario.ventas.mapper;

import com.optiplant.inventario.ventas.dto.VentaResponse;
import com.optiplant.inventario.ventas.entity.Venta;
import com.optiplant.inventario.ventas.entity.VentaLinea;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VentaMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "sucursalId", source = "sucursal.id")
    @Mapping(target = "nombreSucursal", source = "sucursal.nombre")
    @Mapping(target = "usuarioId", expression = "java(v.getUsuario() != null ? v.getUsuario().getId() : null)")
    @Mapping(target = "nombreUsuario", expression = "java(v.getUsuario() != null ? v.getUsuario().getNombre() : null)")
    @Mapping(target = "fecha", source = "fecha")
    @Mapping(target = "lineas", expression = "java(toLineaResponseList(v))")
    VentaResponse toResponse(Venta v);

    default List<VentaResponse.LineaResponse> toLineaResponseList(Venta v) {
        return v.getLineas().stream().map(this::toLineaResponse).toList();
    }

    @Mapping(target = "id", source = "id")
    @Mapping(target = "productoId", source = "producto.id")
    @Mapping(target = "sku", source = "producto.sku")
    @Mapping(target = "nombreProducto", source = "producto.nombre")
    VentaResponse.LineaResponse toLineaResponse(VentaLinea linea);
}
