package com.aydsii.demo.controller;

import com.aydsii.demo.dto.ApiResponse;
import com.aydsii.demo.dto.PedidoConfirmarRequest;
import com.aydsii.demo.dto.PedidoConfirmarResponse;
import com.aydsii.demo.service.ConfirmarPedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pedidos")
@Validated
@Tag(name = "Pedidos", description = "CU-03 Confirmar pedido")
public class ConfirmarPedidoController {

    private final ConfirmarPedidoService confirmarPedidoService;

    public ConfirmarPedidoController(ConfirmarPedidoService confirmarPedidoService) {
        this.confirmarPedidoService = confirmarPedidoService;
    }

    @PostMapping("/confirmar")
    @Operation(summary = "Confirmar pedido",
            description = "Recibe el carrito completo (ítems + dirección del cliente), crea el pedido, " +
                    "reserva stock y determina si es normal o especial (mayorista). Todo en un solo endpoint.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pedido confirmado correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Error de validación (stock, cantidades mínimas, etc.)")
    })
    public ResponseEntity<ApiResponse<PedidoConfirmarResponse>> confirmar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Pedido con ítems y datos de entrega",
                    required = true
            )
            @Valid @RequestBody PedidoConfirmarRequest request) {

        PedidoConfirmarResponse respuesta = confirmarPedidoService.confirmar(request);
        ApiResponse<PedidoConfirmarResponse> respuestaApi = ApiResponse.success(       // success(200, mensaje, pedido o cotización)
                HttpStatus.OK.value(), respuesta.getMensaje(), respuesta);
        return ResponseEntity.ok(respuestaApi);        // HTTP 200 + ApiResponse<PedidoConfirmarResponse>
    }
}