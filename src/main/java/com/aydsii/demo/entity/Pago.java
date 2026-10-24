package com.aydsii.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pago")
@Data
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "pedido_id", referencedColumnName = "id", unique = true, nullable = false) 
    private Pedido pedido;

    @Column(precision = 10, scale = 2, nullable = false) 
    private BigDecimal importe;

    @Column(length = 30, nullable = false) 
    private String metodo;

    @Column(nullable = false) 
    private LocalDate fecha;
}