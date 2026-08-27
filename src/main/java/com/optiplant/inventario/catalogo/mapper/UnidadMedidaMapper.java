package com.optiplant.inventario.catalogo.mapper;

import com.optiplant.inventario.catalogo.dto.UnidadMedidaResponse;
import com.optiplant.inventario.catalogo.entity.UnidadMedida;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UnidadMedidaMapper {

    UnidadMedidaResponse toResponse(UnidadMedida unidadMedida);
}
