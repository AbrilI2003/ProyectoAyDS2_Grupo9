package com.aydsii.demo.service;

import com.aydsii.demo.dto.PagoResponse;
import com.aydsii.demo.dto.PagoVistaDTO;
import com.aydsii.demo.entity.DetallePedido;
import com.aydsii.demo.entity.Pago;
import com.aydsii.demo.entity.Pedido;
import com.aydsii.demo.exception.BusinessException;
import com.aydsii.demo.exception.ResourceNotFoundException;
import com.aydsii.demo.logging.LoggerFacade;
import com.aydsii.demo.repository.PagoRepository;
import com.aydsii.demo.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class PagoService {

    private final PedidoRepository pedidoRepository;
    private final PagoRepository pagoRepository;
    private final LoggerFacade log = LoggerFacade.getInstance();

    public PagoService(PedidoRepository pedidoRepository, PagoRepository pagoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.pagoRepository = pagoRepository;
    }

    /* Prepara los datos para la página de pago.*/
    @Transactional
    public PagoVistaDTO obtenerVistaPago(Long idPedido) {
        if (idPedido == null || idPedido <= 0) {
            throw new IllegalArgumentException("El id del pedido debe ser un número positivo");
        }

        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + idPedido));

        boolean linkExpirado = "PENDIENTE_PAGO".equalsIgnoreCase(pedido.getEstado())
                && pedido.getFechaExpiracionPago() != null
                && LocalDateTime.now().isAfter(pedido.getFechaExpiracionPago());

        if (linkExpirado) {
            cancelarPorExpiracion(pedido);
        }

        BigDecimal montoProductos = BigDecimal.ZERO;
        if (pedido.getDetalles() != null) {
            for (DetallePedido d : pedido.getDetalles()) {
                montoProductos = montoProductos.add(
                        d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad()))
                );
            }
        }
        BigDecimal montoEnvio = pedido.getCostoEnvio() != null ? pedido.getCostoEnvio() : BigDecimal.ZERO;

        log.info("Datos de pago preparados para pedido " + idPedido);
        return new PagoVistaDTO(
                pedido.getId(),
                pedido.getEstado(),
                montoProductos,
                montoEnvio,
                montoProductos.add(montoEnvio),
                linkExpirado
        );
    }

    /* Simula el pago exitoso de un pedido. Devuelve el resultado para que el controller lo muestre.*/
    @Transactional
    public PagoResponse simularPagoExitoso(Long pedidoId) {
        if (pedidoId == null || pedidoId <= 0) {
            throw new IllegalArgumentException("El id del pedido debe ser un número positivo");
        }

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + pedidoId));

        if (!"PENDIENTE_PAGO".equalsIgnoreCase(pedido.getEstado())) {
            log.warn("Intento de pago sobre pedido " + pedidoId + " en estado " + pedido.getEstado());
            throw new BusinessException(
                    "El pedido " + pedidoId + " no está en estado pendiente de pago. Estado actual: " + pedido.getEstado()
            );
        }

        if (pedido.getFechaExpiracionPago() != null
                && LocalDateTime.now().isAfter(pedido.getFechaExpiracionPago())) {
            cancelarPorExpiracion(pedido);
            throw new BusinessException(
                    "El link de pago del pedido " + pedidoId + " expiró. El pedido fue cancelado. Debe realizarse un nuevo pedido."
            );
        }

        BigDecimal monto = pedido.getDetalles().stream()
                .map(d -> d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // El importe del pago incluye el costo de envío (es el total que se cobra en la pasarela)
        if (pedido.getCostoEnvio() != null) {
            monto = monto.add(pedido.getCostoEnvio());
        }

        aprobarPago(pedidoId, monto);

        Pedido actualizado = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + pedidoId));

        PagoResponse respuesta = new PagoResponse();
        respuesta.setPedidoId(actualizado.getId());
        respuesta.setEstadoPedido(actualizado.getEstado());
        respuesta.setEstadoPago("APROBADO");
        respuesta.setMensaje("Pago exitoso para el pedido " + pedidoId);

        log.info("Pago simulado exitoso para pedido " + pedidoId);
        return respuesta;
    }

    private void aprobarPago(Long pedidoId, BigDecimal importe) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + pedidoId));

        Pago pago = new Pago();
        pago.setPedido(pedido);
        pago.setImporte(importe);
        pago.setFecha(LocalDate.now());
        pagoRepository.save(pago);

        pedido.setEstado("EN_PREPARACION");
        pedidoRepository.save(pedido);

        log.info("Pago aprobado. Pedido " + pedidoId + " pasó a EN_PREPARACION.");
    }

    private void cancelarPorExpiracion(Pedido pedido) {
        devolverStock(pedido);
        pedido.setEstado("CANCELADO");
        pedidoRepository.save(pedido);
        log.warn("Link expirado. Pedido " + pedido.getId() + " cancelado. Stock devuelto.");
    }

    /* Devuelve al stock las unidades reservadas por un pedido que se cancela. */
    private void devolverStock(Pedido pedido) {
        if (pedido.getDetalles() == null) {
            return;
        }
        for (DetallePedido d : pedido.getDetalles()) {
            if (d.getProducto() != null && d.getCantidad() != null) {
                d.getProducto().setStock(d.getProducto().getStock() + d.getCantidad());
            }
        }
        log.info("Stock devuelto por cancelación del pedido " + pedido.getId());
    }
}