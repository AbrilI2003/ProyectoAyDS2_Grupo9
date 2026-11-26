package com.aydsii.demo.service;

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

    /**
     * Simula el pago exitoso de un pedido.
     * Solo funciona si el pedido está en estado PENDIENTE_PAGO.
     */
    @Transactional
    public void simularPagoExitoso(Long pedidoId) {
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

        // Validar expiración del link (60 segundos) desde la BD
        if (pedido.getFechaExpiracionPago() != null
                && LocalDateTime.now().isAfter(pedido.getFechaExpiracionPago())) {
            devolverStock(pedido);
            pedido.setEstado("CANCELADO");
            pedido.setLinkPago(null);
            pedidoRepository.save(pedido);
            log.warn("Link expirado. Pedido " + pedidoId + " cancelado. Stock devuelto.");
            throw new BusinessException(
                    "El link de pago del pedido " + pedidoId + " expiró. El pedido fue cancelado. Debe realizarse un nuevo pedido."
            );
        }

        BigDecimal monto = pedido.getDetalles().stream()
                .map(d -> d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        aprobarPago(pedidoId, monto);

        log.info("Pago simulado exitoso para pedido " + pedidoId);
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

    /** Devuelve al stock las unidades reservadas por un pedido que se cancela. */
    public void devolverStock(Pedido pedido) {
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
