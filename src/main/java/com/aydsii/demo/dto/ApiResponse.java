package com.aydsii.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Formato estándar de TODAS las respuestas de la API, tanto de éxito como de error: {status, messege, data}
// <T> es genérico: data puede ser un DTO, una lista de DTOs, una lista de errores o null
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Formato estándar de respuesta de la API")
public class ApiResponse<T> {

    @Schema(description = "Código de estado HTTP", example = "200")
    private int status;

    @Schema(description = "Mensaje descriptivo de lo que pasó", example = "Operación realizada con éxito")
    private String messege;     // se escribe "messege" porque así lo pide el enunciado del TP

    @Schema(description = "Contenido real de la respuesta (null en los errores)")
    private T data;


    // success(200, mensaje, data) -> es el mensaje "success(...)" de los dos diagramas de secuencia
    // static: se llama sobre la clase (ApiResponse.success(...)) sin tener que hacer un new antes
    public static <T> ApiResponse<T> success(int status, String messege, T data) {
        return new ApiResponse<>(status, messege, data);
    }

    // error(...) lo usa el GlobalExceptionHandler para que los errores salgan con el mismo formato
    public static <T> ApiResponse<T> error(int status, String messege, T data) {
        return new ApiResponse<>(status, messege, data);
    }
}