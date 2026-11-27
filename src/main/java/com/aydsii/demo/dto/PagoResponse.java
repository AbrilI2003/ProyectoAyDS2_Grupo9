package com.aydsii.demo.dto;

public class PagoResponse {
    private Long pedidoId;
    private String estadoPedido;
    private String estadoPago;
    private String mensaje;

    public PagoResponse() {
    }

    public PagoResponse(Long pedidoId, String estadoPedido, String estadoPago, String mensaje) {
        this.pedidoId = pedidoId;
        this.estadoPedido = estadoPedido;
        this.estadoPago = estadoPago;
        this.mensaje = mensaje;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getEstadoPedido() {
        return estadoPedido;
    }

    public void setEstadoPedido(String estadoPedido) {
        this.estadoPedido = estadoPedido;
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
