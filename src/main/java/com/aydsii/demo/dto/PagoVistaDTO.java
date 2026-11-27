package com.aydsii.demo.dto;

import java.math.BigDecimal;

/** Datos que necesita la página de pago. Ya calculados por el service. */
public class PagoVistaDTO {

    private Long pedidoId;
    private String estado;
    private BigDecimal montoProductos;
    private BigDecimal montoEnvio;
    private BigDecimal montoTotal;
    private boolean linkExpirado;

    public PagoVistaDTO() {
    }

    public PagoVistaDTO(Long pedidoId, String estado, BigDecimal montoProductos,
                        BigDecimal montoEnvio, BigDecimal montoTotal, boolean linkExpirado) {
        this.pedidoId = pedidoId;
        this.estado = estado;
        this.montoProductos = montoProductos;
        this.montoEnvio = montoEnvio;
        this.montoTotal = montoTotal;
        this.linkExpirado = linkExpirado;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public String getEstado() {
        return estado;
    }

    public BigDecimal getMontoProductos() {
        return montoProductos;
    }

    public BigDecimal getMontoEnvio() {
        return montoEnvio;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public boolean isLinkExpirado() {
        return linkExpirado;
    }
}