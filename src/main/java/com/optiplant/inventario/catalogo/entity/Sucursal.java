package com.optiplant.inventario.catalogo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sucursal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sucursal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 300)
    private String direccion;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "activa";
}
