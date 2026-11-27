package com.aydsii.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PedidoConfirmarResponse {
    private Long id; // puede ser pedidoId (normal) o cotizacionId (especial)
    private String mensaje;
    private String linkPago; // solo pedidos normales
    private BigDecimal montoProductos; // solo pedidos normales
    private BigDecimal montoEnvio; // solo pedidos normales
    private BigDecimal montoTotal; // solo pedidos normales
    private String enlaceWhatsapp; // solo pedidos especiales
    private List<DetallePedidoDTO> detalles; // detalle de productos del pedido

    public PedidoConfirmarResponse() {
    }

    public PedidoConfirmarResponse(Long id, String mensaje,
                                   String linkPago, BigDecimal montoProductos,
                                   BigDecimal montoEnvio, BigDecimal montoTotal,
                                   String enlaceWhatsapp, List<DetallePedidoDTO> detalles) {
        this.id = id;
        this.mensaje = mensaje;
        this.linkPago = linkPago;
        this.montoProductos = montoProductos;
        this.montoEnvio = montoEnvio;
        this.montoTotal = montoTotal;
        this.enlaceWhatsapp = enlaceWhatsapp;
        this.detalles = detalles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getLinkPago() {
        return linkPago;
    }

    public void setLinkPago(String linkPago) {
        this.linkPago = linkPago;
    }

    public BigDecimal getMontoProductos() {
        return montoProductos;
    }

    public void setMontoProductos(BigDecimal montoProductos) {
        this.montoProductos = montoProductos;
    }

    public BigDecimal getMontoEnvio() {
        return montoEnvio;
    }

    public void setMontoEnvio(BigDecimal montoEnvio) {
        this.montoEnvio = montoEnvio;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public String getEnlaceWhatsapp() {
        return enlaceWhatsapp;
    }

    public void setEnlaceWhatsapp(String enlaceWhatsapp) {
        this.enlaceWhatsapp = enlaceWhatsapp;
    }

    public List<DetallePedidoDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedidoDTO> detalles) {
        this.detalles = detalles;
    }
}
