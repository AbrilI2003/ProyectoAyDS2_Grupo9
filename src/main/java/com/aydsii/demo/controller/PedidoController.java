package com.aydsii.demo.controller;

import com.aydsii.demo.dto.ApiResponse;
import com.aydsii.demo.dto.DetallePedidoDTO;
import com.aydsii.demo.service.DetallePedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@Tag(name = "Pedidos", description = "CU-02 Consultar detalle de pedido")
public class PedidoController {

    private final DetallePedidoService detallePedidoService;

    // Inyección de dependencias por constructor: Spring crea el service y lo pasa acá
    public PedidoController(DetallePedidoService detallePedidoService) {
        this.detallePedidoService = detallePedidoService;
    }


    // GET /detalle_pedido/{idPedido}
    @GetMapping("/detalle_pedido/{idPedido}")
    @Operation(
            summary = "Consultar el detalle de un pedido",
            description = "Devuelve los productos del pedido indicado, cada uno con su cantidad, "
                    + "precio unitario y subtotal (cantidad x precio unitario).")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Detalle del pedido encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "El id enviado no es válido (no es un número o no es positivo)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No existe un pedido con ese id"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error inesperado en el servidor")
    })
    public ResponseEntity<ApiResponse<List<DetallePedidoDTO>>> getDetallePedido(
            @Parameter(description = "Id del pedido a consultar", example = "2")
            @PathVariable
            @Positive(message = "El id del pedido debe ser un número positivo")     // 0 o negativo es 400 (lo resuelve el GlobalExceptionHandler)
            Long idPedido) {

        // Le pido al service el detalle completo del pedido (una lista, un elemento por cada producto)
        List<DetallePedidoDTO> detallesDTO = detallePedidoService.calcularDetalleCompleto(idPedido);

        ApiResponse<List<DetallePedidoDTO>> respuesta = ApiResponse.success(       // success(200, mensaje, detalleDTO)
                HttpStatus.OK.value(), "Detalle del pedido obtenido correctamente", detallesDTO);

        return ResponseEntity.ok(respuesta);        // HTTP 200 + ApiResponse<detalleDTO>
    }
}