package com.aydsii.demo.service;

import com.aydsii.demo.dto.DetallePedidoDTO;
import com.aydsii.demo.entity.DetallePedido;
import com.aydsii.demo.exception.ResourceNotFoundException;
import com.aydsii.demo.repository.PedidoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DetallePedidoService {

private final PedidoRepository pedidoRepository;

    public DetallePedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public List<DetallePedidoDTO> calcularDetalleCompleto(Long pedidoId) {
    // 1. El repositorio nos trae la lista de entidades DetallePedido
    List<DetallePedido> detalles = pedidoRepository.findDetallesPorPedidoId(pedidoId);

    // 2. Transformamos (mapeamos) cada DetallePedido a un DetallePedidoDTO
    return detalles.stream()
        .map(detalle -> {
            // Calculamos el total usando BigDecimal 
            BigDecimal total = detalle.getPrecioUnitario()
                                .multiply(new BigDecimal(detalle.getCantidad()));
            
            // Creamos el DTO con los datos extraídos
            return new DetallePedidoDTO(
                detalle.getProducto().getNombre(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                total
            );
        })
        .collect(Collectors.toList());

    }
}
