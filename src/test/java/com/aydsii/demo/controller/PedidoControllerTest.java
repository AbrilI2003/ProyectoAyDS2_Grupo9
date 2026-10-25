package com.aydsii.demo.controller;

import com.aydsii.demo.exception.ResourceNotFoundException;
import com.aydsii.demo.service.PedidoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Tests de la capa web de CU-02: prueban el controller y el GlobalExceptionHandler SIN base de datos.
// El service se reemplaza por un mock, así puedo simular cada caso (pedido inexistente, error, etc.)
@WebMvcTest(PedidoController.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoService pedidoService;


    @Test
    @DisplayName("Pedido inexistente: responde 404 con el mensaje que manda el service")
    void pedidoInexistenteDevuelve404() throws Exception {
        when(pedidoService.getDetallePedido(999L))
                .thenThrow(new ResourceNotFoundException("No existe un pedido con id 999"));

        mockMvc.perform(get("/detalle_pedido/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.messege").value("No existe un pedido con id 999"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("Id no numérico: responde 400 y el service ni se llama")
    void idNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(get("/detalle_pedido/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.messege").value("El valor 'abc' no es válido para el parámetro 'idPedido'"));

        verify(pedidoService, never()).getDetallePedido(anyLong());       // compruebo que el error se cortó antes de llegar al service
    }

    @Test
    @DisplayName("Id negativo: responde 400 con el error de validación")
    void idNegativoDevuelve400() throws Exception {
        mockMvc.perform(get("/detalle_pedido/-5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.data[0]").value("El id del pedido debe ser un número positivo"));

        verify(pedidoService, never()).getDetallePedido(anyLong());
    }

    @Test
    @DisplayName("Error inesperado en el service: responde 500 con el formato estándar")
    void errorInesperadoDevuelve500() throws Exception {
        when(pedidoService.getDetallePedido(1L)).thenThrow(new RuntimeException("Se cayó la base"));

        mockMvc.perform(get("/detalle_pedido/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.messege").value("Ocurrió un error inesperado en el servidor"));
    }
}