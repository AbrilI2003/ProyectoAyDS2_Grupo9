package com.aydsii.demo.entity;

import jakarta.persistence.*;
import lombok.Data; 
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "promocion")
public class Promocion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(length = 100, nullable = false)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(name = "tipo_descuento", length = 20, nullable = false)
    private String tipoDescuento; 

    @Column(name = "valor_descuento", precision = 10, scale = 2, nullable = false)
    private BigDecimal valorDescuento;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;
}