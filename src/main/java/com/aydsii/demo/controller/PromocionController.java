package com.aydsii.demo.controller;

import com.aydsii.demo.dto.ApiResponse;
import com.aydsii.demo.dto.PromocionDTO;
import com.aydsii.demo.service.PromocionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Promociones", description = "CU-01 Consultar promociones vigentes")
public class PromocionController {

    private final PromocionService promocionService;

    public PromocionController(PromocionService promocionService) {
        this.promocionService = promocionService;
    }


    // GET /promociones
    @GetMapping("/promociones")
    @Operation(
            summary = "Listar promociones vigentes",
            description = "Devuelve las promociones cuya fecha actual está entre su fecha de inicio y de fin, con el producto incluido en cada una. Si no hay promociones vigentes no es un error: responde 200 con la lista vacía.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta realizada. La lista puede venir vacía si no hay promociones vigentes (A1)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error inesperado en el servidor")
    })
    public ResponseEntity<ApiResponse<List<PromocionDTO>>> listarVigentes() {

        List<PromocionDTO> promocionesDTO = promocionService.obtenerPromocionesVigentes();

        // Sin promociones vigentes no es un error, se responde 200 con la lista vacía y el mensaje del CU
        String mensaje;

        if (promocionesDTO.isEmpty()) {
            mensaje = "No hay promociones vigentes en este momento";
        } else {
            mensaje = "Promociones vigentes obtenidas correctamente";
        }

        ApiResponse<List<PromocionDTO>> respuesta = ApiResponse.success(HttpStatus.OK.value(), mensaje, promocionesDTO);    // success(200, mensaje, promocionesDTO)

        return ResponseEntity.ok(respuesta);        // HTTP 200 + ResponseEntity<ApiResponse>
    }
}