package com.aydsii.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetallePedidoDTO {
    private String nombreProducto;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal total; // cantidad * precioUnitario
}

/*Método que va en el service para que matchee todo:
public List<DetallePedidoDTO> calcularDetalleCompleto(Long pedidoId) {
    
    // 1. El repositorio nos trae la lista de entidades DetallePedido
    List<DetallePedido> detalles = pedidoRepository.findDetallesPorPedidoId(pedidoId);

    // 2. Transformamos (mapeamos) cada DetallePedido a un DetallePedidoDTO
    return detalles.stream()
        .map(detalle -> {
            // Calculamos el total usando BigDecimal (NUNCA uses double o float para dinero)
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
} */