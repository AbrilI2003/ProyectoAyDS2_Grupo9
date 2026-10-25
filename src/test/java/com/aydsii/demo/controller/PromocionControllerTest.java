package com.aydsii.demo.controller;

import com.aydsii.demo.dto.PromocionDTO;
import com.aydsii.demo.service.PromocionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Tests de la capa web de CU-01: prueban el controller y el GlobalExceptionHandler SIN base de datos.
// @WebMvcTest levanta solo la parte web de Spring (controllers + manejo de errores), no los repositorios.
// El service se reemplaza por un "mock": un objeto falso al que le digo qué devolver en cada test.
@WebMvcTest(PromocionController.class)
class PromocionControllerTest {

    @Autowired
    private MockMvc mockMvc;        // simula los pedidos HTTP, como si fuera Swagger o el navegador

    @MockitoBean
    private PromocionService promocionService;      // service falso: no necesita base de datos


    @Test
    @DisplayName("Flujo principal: hay promociones vigentes, responde 200 con la lista")
    void conPromocionesVigentesDevuelveLaLista() throws Exception {
        PromocionDTO promocion = new PromocionDTO(          // armo a mano lo que devolvería el service
                "Semana del vaso", "15% off en vasos de 300 ml", "PORCENTAJE",
                new BigDecimal("15.00"), LocalDate.of(2026, 11, 4), "Vaso PET 300 ml");
        when(promocionService.obtenerPromocionesVigentes()).thenReturn(List.of(promocion));

        mockMvc.perform(get("/promociones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.messege").value("Promociones vigentes obtenidas correctamente"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].nombre").value("Semana del vaso"))
                .andExpect(jsonPath("$.data[0].nombreProducto").value("Vaso PET 300 ml"));
    }

    @Test
    @DisplayName("A1 - Sin promociones vigentes: responde 200 con lista vacía y el mensaje del CU")
    void sinPromocionesVigentesDevuelveListaVacia() throws Exception {
        when(promocionService.obtenerPromocionesVigentes()).thenReturn(List.of());      // el service "no encuentra" promociones

        mockMvc.perform(get("/promociones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.messege").value("No hay promociones vigentes en este momento"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Error inesperado en el service: responde 500 con el formato estándar")
    void errorInesperadoDevuelve500() throws Exception {
        when(promocionService.obtenerPromocionesVigentes()).thenThrow(new RuntimeException("Se cayó la base"));     // simulo una falla

        mockMvc.perform(get("/promociones"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.messege").value("Ocurrió un error inesperado en el servidor"))
                .andExpect(jsonPath("$.data").doesNotExist());     // en los errores data va en null
    }

    @Test
    @DisplayName("Método HTTP no permitido: POST /promociones responde 405")
    void metodoNoPermitidoDevuelve405() throws Exception {
        mockMvc.perform(post("/promociones"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    @DisplayName("Ruta inexistente: responde 404 con el formato estándar")
    void rutaInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/promocionesss"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}