package com.aydsii.demo.controller;

import com.aydsii.demo.dto.DetallePedidoDTO;
import com.aydsii.demo.exception.ResourceNotFoundException;
import com.aydsii.demo.service.DetallePedidoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Tests de la capa web de CU-02: prueban el controller y el GlobalExceptionHandler SIN base de datos.
// El service se reemplaza por un mock, así puedo simular cada caso (pedido existente, inexistente, error, etc.)
@WebMvcTest(PedidoController.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DetallePedidoService detallePedidoService;


    @Test
    @DisplayName("Flujo principal: pedido existente, responde 200 con sus productos")
    void pedidoExistenteDevuelveSuDetalle() throws Exception {
        List<DetallePedidoDTO> detalles = List.of(          // los mismos datos que el pedido 1 de la base de prueba
                new DetallePedidoDTO("Vaso PET 300 ml", 100, new BigDecimal("48.00"), new BigDecimal("4800.00")),
                new DetallePedidoDTO("Bandeja chica 15x20 cm", 60, new BigDecimal("80.00"), new BigDecimal("4800.00")));
        when(detallePedidoService.calcularDetalleCompleto(1L)).thenReturn(detalles);

        mockMvc.perform(get("/detalle_pedido/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.messege").value("Detalle del pedido obtenido correctamente"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].nombreProducto").value("Vaso PET 300 ml"))
                .andExpect(jsonPath("$.data[0].cantidad").value(100));
    }

    @Test
    @DisplayName("Pedido inexistente: responde 404 con el mensaje que manda el service")
    void pedidoInexistenteDevuelve404() throws Exception {
        when(detallePedidoService.calcularDetalleCompleto(999L))
                .thenThrow(new ResourceNotFoundException("No se encontró el detalle para el pedido ID: 999"));

        mockMvc.perform(get("/detalle_pedido/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.messege").value("No se encontró el detalle para el pedido ID: 999"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("Id no numérico: responde 400 y el service ni se llama")
    void idNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(get("/detalle_pedido/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.messege").value("El valor 'abc' no es válido para el parámetro 'idPedido'"));

        verify(detallePedidoService, never()).calcularDetalleCompleto(anyLong());     // compruebo que el error se cortó antes de llegar al service
    }

    @Test
    @DisplayName("Id negativo: responde 400 con el error de validación")
    void idNegativoDevuelve400() throws Exception {
        mockMvc.perform(get("/detalle_pedido/-5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.data[0]").value("El id del pedido debe ser un número positivo"));

        verify(detallePedidoService, never()).calcularDetalleCompleto(anyLong());
    }

    @Test
    @DisplayName("Error inesperado en el service: responde 500 con el formato estándar")
    void errorInesperadoDevuelve500() throws Exception {
        when(detallePedidoService.calcularDetalleCompleto(1L)).thenThrow(new IllegalStateException("Se cayó la base"));

        mockMvc.perform(get("/detalle_pedido/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.messege").value("Ocurrió un error inesperado en el servidor"));
    }
}