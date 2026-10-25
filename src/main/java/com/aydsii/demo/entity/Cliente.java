package com.aydsii.demo.entity;

import jakarta.persistence.*;
import lombok.Data; 
import java.time.LocalDate;
import java.util.ArrayList; 
import java.util.List;

@Entity
@Data 
@Table(name = "cliente")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "razon_social", length = 150, nullable = false)
    private String razonSocial;

    @Column(length = 13, unique = true, nullable = false)
    private String cuit;

    @Column(length = 100, nullable = false) 
    private String email;

    @Column(length = 30)
    private String telefono;

    @Column(name = "direccion_fiscal", length = 200, nullable = false) 
    private String direccionFiscal;

    @Column(name = "fecha_alta", nullable = false) 
    private LocalDate fechaAlta;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Pedido> pedidos = new ArrayList<>();
}