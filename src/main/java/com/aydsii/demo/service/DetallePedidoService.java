package com.aydsii.demo.service;

import com.aydsii.demo.dto.DetallePedidoDTO;
import com.aydsii.demo.entity.DetallePedido;
import com.aydsii.demo.repository.PedidoRepository;
import com.aydsii.demo.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DetallePedidoService { 

    private final PedidoRepository pedidoRepository;

    public DetallePedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public List<DetallePedidoDTO> calcularDetalleCompleto(Long pedidoId) {

        // Recupera los items de un pedido segun el ID
        List<DetallePedido> detalles = pedidoRepository.findDetallesPorPedidoId(pedidoId);

        // Valida si existe 
        if (detalles == null || detalles.isEmpty()) {
            throw new ResourceNotFoundException("No se encontró el detalle para el pedido ID: " + pedidoId);
        }

        return detalles.stream()
                .map(detalle -> {
                    // Se calcula el subtotal de cada producto
                    BigDecimal subtotalItem = detalle.getPrecioUnitario()
                            .multiply(BigDecimal.valueOf(detalle.getCantidad()))
                            .setScale(2, RoundingMode.HALF_UP);

                    return new DetallePedidoDTO(
                            detalle.getProducto().getNombre(),
                            detalle.getCantidad(),
                            detalle.getPrecioUnitario(),
                            subtotalItem
                    );
                })
                .collect(Collectors.toList());
    }
}