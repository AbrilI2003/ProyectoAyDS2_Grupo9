package com.aydsii.demo.controller;

import com.aydsii.demo.dto.ApiResponse;
import com.aydsii.demo.dto.DetallePedidoDTO;
import com.aydsii.demo.service.DetallePedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

// CU-02 Consultar detalle de pedido
// El controller recibe el id del pedido por la URL, le pide el detalle al service y arma la respuesta.
// Los cálculos (subtotales, envío, total) están en PedidoService.
//
// Igual que en PromocionController: la anotación de Swagger se escribe con el nombre completo
// porque se llama igual que nuestra clase ApiResponse.

@RestController
@Tag(name = "Pedidos", description = "CU-02 Consultar detalle de pedido")
public class PedidoController {

    private static final String MENSAJE_OK = "Detalle del pedido obtenido correctamente";

    // Ejemplos de respuestas de error que se muestran en Swagger, en la sección "Responses"
    private static final String EJEMPLO_ERROR_400_TIPO = """
            {
              "status": 400,
              "messege": "El valor 'abc' no es válido para el parámetro 'idPedido'",
              "data": null
            }
            """;

    private static final String EJEMPLO_ERROR_400_VALIDACION = """
            {
              "status": 400,
              "messege": "Error de validación en los datos enviados",
              "data": ["El id del pedido debe ser un número positivo"]
            }
            """;

    private static final String EJEMPLO_ERROR_404 = """
            {
              "status": 404,
              "messege": "No existe un pedido con id 999",
              "data": null
            }
            """;

    private static final String EJEMPLO_ERROR_500 = """
            {
              "status": 500,
              "messege": "Ocurrió un error inesperado en el servidor",
              "data": null
            }
            """;

    private final DetallePedidoService pedidoService;

    // Inyección de dependencias por constructor: Spring crea el service y lo pasa acá
    public PedidoController(DetallePedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }


    // GET /detalle_pedido/{idPedido} -> mensaje 1 del diagrama de secuencia
    @GetMapping("/detalle_pedido/{idPedido}")
    @Operation(
            summary = "Consultar el detalle de un pedido",
            description = "Devuelve la información completa del pedido: datos generales, productos con su "
                    + "cantidad, precio unitario y subtotal, costo de envío y total.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Detalle del pedido encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "El id enviado no es válido (no es un número o no es positivo)",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "Id no numérico", value = EJEMPLO_ERROR_400_TIPO),
                                    @ExampleObject(name = "Id negativo o cero", value = EJEMPLO_ERROR_400_VALIDACION)
                            })),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "No existe un pedido con ese id",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "Pedido inexistente", value = EJEMPLO_ERROR_404))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Error inesperado en el servidor",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "Error interno", value = EJEMPLO_ERROR_500)))
    })
    public ResponseEntity<ApiResponse<DetallePedidoDTO>> getDetallePedido(
            @Parameter(description = "Id del pedido a consultar", example = "2")
            @PathVariable
            @Positive(message = "El id del pedido debe ser un número positivo")     // si llega 0 o negativo -> 400 (lo resuelve el GlobalExceptionHandler)
            Long idPedido) {

        // mensaje 2: getDetallePedido(idPedido)
        // Si el pedido no existe, el service lanza ResourceNotFoundException y el GlobalExceptionHandler responde 404.
        // Por eso acá no hace falta ningún if ni try/catch.
        List<DetallePedidoDTO> detallesDTO = pedidoService.calcularDetalleCompleto(idPedido);

        ApiResponse<DetallePedidoDTO> respuesta = ApiResponse.success(         // mensaje 5: success(200, mensaje, detalleDTO)
                HttpStatus.OK.value(), MENSAJE_OK, detallesDTO);

        return ResponseEntity.ok(respuesta);        // HTTP 200 + ApiResponse<detalleDTO>
    }
}