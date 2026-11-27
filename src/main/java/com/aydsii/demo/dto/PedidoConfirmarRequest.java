package com.aydsii.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class PedidoConfirmarRequest {
    @NotNull(message = "El id del cliente es obligatorio")
    private Long clienteId;

    @NotBlank(message = "El país es obligatorio")
    private String pais;

    @NotBlank(message = "La calle es obligatoria")
    private String calle;

    private Integer altura;

    @NotBlank(message = "El código postal es obligatorio")
    private String codigoPostal;

    @NotNull(message = "Los ítems son obligatorios")
    @Size(min = 1, message = "Debe haber al menos un ítem en el pedido")
    private List<ItemPedidoRequest> items;

    public PedidoConfirmarRequest() {
    }

    public PedidoConfirmarRequest(Long clienteId, String pais, String calle, Integer altura,
                                  String codigoPostal, List<ItemPedidoRequest> items) {
        this.clienteId = clienteId;
        this.pais = pais;
        this.calle = calle;
        this.altura = altura;
        this.codigoPostal = codigoPostal;
        this.items = items;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
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

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public List<ItemPedidoRequest> getItems() {
        return items;
    }

    public void setItems(List<ItemPedidoRequest> items) {
        this.items = items;
    }
}
