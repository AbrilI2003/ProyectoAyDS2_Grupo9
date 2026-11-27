package com.aydsii.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ItemPedidoRequest {
    @NotNull(message = "El id del producto es obligatorio")
    private Long productoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 50, message = "La cantidad mínima por producto es 50 unidades")
    private Integer cantidad;

    public ItemPedidoRequest() {
    }

    public ItemPedidoRequest(Long productoId, Integer cantidad) {
        this.productoId = productoId;
        this.cantidad = cantidad;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
