package com.optiplant.inventario.inventario.mapper;

import com.optiplant.inventario.inventario.dto.ExistenciaResponse;
import com.optiplant.inventario.inventario.entity.Existencia;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface ExistenciaMapper {

    @Mapping(source = "producto.id", target = "productoId")
    @Mapping(source = "producto.sku", target = "sku")
    @Mapping(source = "producto.nombre", target = "nombreProducto")
    @Mapping(source = "sucursal.id", target = "sucursalId")
    @Mapping(source = "sucursal.nombre", target = "nombreSucursal")
    @Mapping(target = "cantidadDisponible", expression = "java(existencia.getCantidadFisica().subtract(existencia.getCantidadReservada()))")
    @Mapping(target = "estadoStock", expression = "java(calcularEstadoStock(existencia))")
    @Mapping(target = "precio", expression = "java(java.math.BigDecimal.ZERO)")
    ExistenciaResponse toResponse(Existencia existencia);

    default String calcularEstadoStock(Existencia existencia) {
        BigDecimal disponible = existencia.getCantidadFisica()
                .subtract(existencia.getCantidadReservada());
        BigDecimal minimo = existencia.getStockMinimo();

        if (minimo == null || minimo.compareTo(BigDecimal.ZERO) == 0) {
            return disponible.compareTo(BigDecimal.ZERO) > 0 ? "disponible" : "critico";
        }

        BigDecimal umbralCritico = minimo.multiply(new BigDecimal("0.3"));

        if (disponible.compareTo(minimo) >= 0) {
            return "disponible";
        } else if (disponible.compareTo(umbralCritico) >= 0) {
            return "bajo";
        } else {
            return "critico";
        }
    }
}
