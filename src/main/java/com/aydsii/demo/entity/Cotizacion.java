package com.aydsii.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cotizacion")
public class Cotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "pedido_id", nullable = true, unique = true)
    private Pedido pedido;

    @Column(name = "monto_cotizado", precision = 10, scale = 2, nullable = false)
    private BigDecimal montoCotizado;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(length = 20, nullable = false)
    private String estado; // SOLICITADA, ACEPTADA, RECHAZADA, VENCIDA

    @Column(columnDefinition = "TEXT")
    private String enlaceWhatsapp;

    @Column(name = "dias_para_responder", nullable = false)
    private Integer diasParaResponder = 5;

    public Cotizacion() {
    }

    public Cotizacion(Long id, Pedido pedido, BigDecimal montoCotizado, LocalDate fechaEmision,
                      LocalDate fechaVencimiento, String estado, String enlaceWhatsapp,
                      Integer diasParaResponder) {
        this.id = id;
        this.pedido = pedido;
        this.montoCotizado = montoCotizado;
        this.fechaEmision = fechaEmision;
        this.fechaVencimiento = fechaVencimiento;
        this.estado = estado;
        this.enlaceWhatsapp = enlaceWhatsapp;
        this.diasParaResponder = diasParaResponder;
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

    public BigDecimal getMontoCotizado() {
        return montoCotizado;
    }

    public void setMontoCotizado(BigDecimal montoCotizado) {
        this.montoCotizado = montoCotizado;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDate fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getEnlaceWhatsapp() {
        return enlaceWhatsapp;
    }

    public void setEnlaceWhatsapp(String enlaceWhatsapp) {
        this.enlaceWhatsapp = enlaceWhatsapp;
    }

    public Integer getDiasParaResponder() {
        return diasParaResponder;
    }

    public void setDiasParaResponder(Integer diasParaResponder) {
        this.diasParaResponder = diasParaResponder;
    }
}
