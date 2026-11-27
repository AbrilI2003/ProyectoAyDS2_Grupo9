package com.aydsii.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pago")
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", referencedColumnName = "id", unique = true, nullable = false)
    private Pedido pedido;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal importe;

    @Column(nullable = false)
    private LocalDate fecha;

    public Pago() {
    }

    public Pago(Long id, Pedido pedido, BigDecimal importe, LocalDate fecha) {
        this.id = id;
        this.pedido = pedido;
        this.importe = importe;
        this.fecha = fecha;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
}
