package com.aydsii.demo.controller;

import com.aydsii.demo.dto.ApiResponse;
import com.aydsii.demo.dto.PromocionDTO;
import com.aydsii.demo.service.PromocionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// CU-01 Consultar promociones
// El controller solo recibe el pedido HTTP, le pide los datos al service y arma la respuesta.
// La lógica (qué promociones son vigentes, cálculo de precios) está en PromocionService.
//
// OJO con el nombre "ApiResponse": nuestra clase (dto.ApiResponse) se llama igual que la anotación
// de Swagger. Por eso la nuestra va en el import y la de Swagger se escribe con el nombre completo
// (@io.swagger.v3.oas.annotations.responses.ApiResponse) para que no choquen.

@RestController
@Tag(name = "Promociones", description = "CU-01 Consultar promociones vigentes")
public class PromocionController {

    private static final String MENSAJE_CON_PROMOCIONES = "Promociones vigentes obtenidas correctamente";
    private static final String MENSAJE_SIN_PROMOCIONES = "No hay promociones vigentes en este momento";     // texto del flujo alternativo A1

    // Ejemplo de respuesta de error que se muestra en Swagger, en la sección "Responses"
    private static final String EJEMPLO_ERROR_500 = """
            {
              "status": 500,
              "messege": "Ocurrió un error inesperado en el servidor",
              "data": null
            }
            """;

    private final PromocionService promocionService;

    // Inyección de dependencias por constructor: Spring crea el service y lo pasa acá
    public PromocionController(PromocionService promocionService) {
        this.promocionService = promocionService;
    }


    // GET /promociones -> mensaje 1 del diagrama de secuencia
    @GetMapping("/promociones")
    @Operation(
            summary = "Listar promociones vigentes",
            description = "Devuelve las promociones cuya fecha actual está entre su fecha de inicio y de fin, "
                    + "con el producto incluido en cada una. Si no hay promociones vigentes no es un error: "
                    + "responde 200 con la lista vacía.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Consulta realizada. La lista puede venir vacía si no hay promociones vigentes (A1)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Error inesperado en el servidor",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "Error interno", value = EJEMPLO_ERROR_500)))
    })
    public ResponseEntity<ApiResponse<List<PromocionDTO>>> listarVigentes() {

        List<PromocionDTO> promocionesDTO = promocionService.obtenerPromocionesVigentes();       // mensaje 2: listarVigentes()

        // A1 - Sin promociones vigentes: no es un error, se responde 200 con la lista vacía y el mensaje del CU
        String mensaje = promocionesDTO.isEmpty() ? MENSAJE_SIN_PROMOCIONES : MENSAJE_CON_PROMOCIONES;

        ApiResponse<List<PromocionDTO>> respuesta = ApiResponse.success(       // mensaje 6: success(200, mensaje, promocionesDTO)
                HttpStatus.OK.value(), mensaje, promocionesDTO);

        return ResponseEntity.ok(respuesta);        // HTTP 200 + ResponseEntity<ApiResponse>
    }
}