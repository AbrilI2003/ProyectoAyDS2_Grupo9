package com.aydsii.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Formato de las respuestas de la API {status, message, data}
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;

    // success(200, mensaje, data) ----> Es el "success(...)" de los diagramas de secuencia
    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return new ApiResponse<>(status, message, data);
    }

    // lo usa el GlobalExceptionHandler para armar las respuestas de error con el mismo formato
    public static <T> ApiResponse<T> error(int status, String message, T data) {
        return new ApiResponse<>(status, message, data);
    }
}