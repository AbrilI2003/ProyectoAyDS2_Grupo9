package com.aydsii.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(length = 30, nullable = false)
    private String estado; // PENDIENTE_PAGO, SOLICITUD_COTIZACION, EN_PREPARACION, CANCELADO, etc.

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

    @Column(name = "pedido_especial", nullable = false)
    private Boolean pedidoEspecial = false;

    @Column(name = "total_unidades")
    private Integer totalUnidades;

    @Column(length = 200)
    private String linkPago;

    @Column(name = "fecha_expiracion_pago")
    private LocalDateTime fechaExpiracionPago;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    private List<DetallePedido> detalles = new ArrayList<>();

    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL, optional = true)
    private Pago pago;

    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL, optional = true)
    private Cotizacion cotizacion;

    public Pedido() {
    }

    public Pedido(Long id, Cliente cliente, String estado, LocalDate fechaPedido,
                  String pais, String codigoPostal, String calle, Integer altura,
                  BigDecimal costoEnvio, Boolean pedidoEspecial, Integer totalUnidades,
                  String linkPago, List<DetallePedido> detalles, Pago pago, Cotizacion cotizacion) {
        this.id = id;
        this.cliente = cliente;
        this.estado = estado;
        this.fechaPedido = fechaPedido;
        this.pais = pais;
        this.codigoPostal = codigoPostal;
        this.calle = calle;
        this.altura = altura;
        this.costoEnvio = costoEnvio;
        this.pedidoEspecial = pedidoEspecial;
        this.totalUnidades = totalUnidades;
        this.linkPago = linkPago;
        this.detalles = detalles;
        this.pago = pago;
        this.cotizacion = cotizacion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDate getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDate fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public Integer getAltura() {
        return altura;
    }

    public void setAltura(Integer altura) {
        this.altura = altura;
    }

    public BigDecimal getCostoEnvio() {
        return costoEnvio;
    }

    public void setCostoEnvio(BigDecimal costoEnvio) {
        this.costoEnvio = costoEnvio;
    }

    public Boolean getPedidoEspecial() {
        return pedidoEspecial;
    }

    public void setPedidoEspecial(Boolean pedidoEspecial) {
        this.pedidoEspecial = pedidoEspecial;
    }

    public Integer getTotalUnidades() {
        return totalUnidades;
    }

    public void setTotalUnidades(Integer totalUnidades) {
        this.totalUnidades = totalUnidades;
    }

    public String getLinkPago() {
        return linkPago;
    }

    public void setLinkPago(String linkPago) {
        this.linkPago = linkPago;
    }

    public LocalDateTime getFechaExpiracionPago() {
        return fechaExpiracionPago;
    }

    public void setFechaExpiracionPago(LocalDateTime fechaExpiracionPago) {
        this.fechaExpiracionPago = fechaExpiracionPago;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedido> detalles) {
        this.detalles = detalles;
    }

    public Pago getPago() {
        return pago;
    }

    public void setPago(Pago pago) {
        this.pago = pago;
    }

    public Cotizacion getCotizacion() {
        return cotizacion;
    }

    public void setCotizacion(Cotizacion cotizacion) {
        this.cotizacion = cotizacion;
    }
}
