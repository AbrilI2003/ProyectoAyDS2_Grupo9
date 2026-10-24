package com.aydsii.demo.entity;

import jakarta.persistence.*;
import lombok.Data; 
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList; 
import java.util.List;

@Entity
@Data 
@Table(name = "pedido")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(length = 30, nullable = false)
    private String estado; 

    @Column(name = "fecha_pedido", nullable = false) 
    private LocalDate fechaPedido;

    @Column(length = 60)
    private String pais;

    @Column(name = "codigo_postal", length = 10)
    private String codigoPostal;

    @Column(length = 100)
    private String calle;

    private Integer altura;

    @Column(name = "costo_envio", precision = 10, scale = 2)
    private BigDecimal costoEnvio;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)     
    private List<DetallePedido> detalles = new ArrayList<>();

    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL, optional = true)
    private Pago pago;
}