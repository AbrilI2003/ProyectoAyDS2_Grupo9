package com.aydsii.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cliente")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "razon_social", length = 150, nullable = false)
    private String razonSocial;

    @Column(length = 13, unique = true, nullable = false)
    private String cuit;

    @Column(length = 100, nullable = false)
    private String email;

    @Column(length = 30)
    private String telefono;

    @Column(name = "direccion_fiscal", length = 200, nullable = false)
    private String direccionFiscal;

    @Column(name = "fecha_alta", nullable = false)
    private LocalDate fechaAlta;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Pedido> pedidos = new ArrayList<>();

    public Cliente() {
    }

    public Cliente(Long id, String razonSocial, String cuit, String email, String telefono,
                   String direccionFiscal, LocalDate fechaAlta, List<Pedido> pedidos) {
        this.id = id;
        this.razonSocial = razonSocial;
        this.cuit = cuit;
        this.email = email;
        this.telefono = telefono;
        this.direccionFiscal = direccionFiscal;
        this.fechaAlta = fechaAlta;
        this.pedidos = pedidos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public void setCuit(String cuit) {
        this.cuit = cuit;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccionFiscal() {
        return direccionFiscal;
    }

    public void setDireccionFiscal(String direccionFiscal) {
        this.direccionFiscal = direccionFiscal;
    }

    public LocalDate getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(LocalDate fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public List<Pedido> getPedidos() {
        return pedidos;
    }

    public void setPedidos(List<Pedido> pedidos) {
        this.pedidos = pedidos;
    }
}
