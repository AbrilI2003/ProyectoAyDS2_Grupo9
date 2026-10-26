package com.aydsii.demo.exception;

import com.aydsii.demo.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);    // para dejar registro de los errores 500 en consola y en logs/


    // 404 - el recurso pedido no existe (ej: GET /detalle_pedido/999)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> manejarNoEncontrado(ResourceNotFoundException ex) {
        return construirRespuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 400 - el valor de la URL no es del tipo esperado (ej: GET /detalle_pedido/abc, cuando se espera un número)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> manejarTipoInvalido(MethodArgumentTypeMismatchException ex) {
        String mensaje = String.format("El valor '%s' no es válido para el parámetro '%s'", ex.getValue(), ex.getName());
        return construirRespuesta(HttpStatus.BAD_REQUEST, mensaje);
    }

    // 400 - falla una validación de un parámetro (ej: en el idPedido -> GET /detalle_pedido/-5)
    // Como puede haber más de un error se devuelven todos en una lista dentro de "data"
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<List<String>>> manejarParametroInvalido(HandlerMethodValidationException ex) {
        List<String> errores = new ArrayList<>();

        for (ParameterValidationResult resultado : ex.getParameterValidationResults()) {     // recorro cada parámetro que falló
            for (MessageSourceResolvable error : resultado.getResolvableErrors()) {         // cada parámetro puede tener varios errores
                errores.add(error.getDefaultMessage());                                      // me quedo con el mensaje de cada uno
            }
        }

        ApiResponse<List<String>> respuesta = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(), "Error de validación en los datos enviados", errores);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    // 400 - validaciones manuales hechas en el service (por si alguna lanza IllegalArgumentException)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> manejarArgumentoInvalido(IllegalArgumentException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 404 - la ruta no existe en la API (ej: GET /promocionesss)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> manejarRutaInexistente(NoResourceFoundException ex) {
        return construirRespuesta(HttpStatus.NOT_FOUND, "La ruta solicitada no existe");
    }

    // 405 - la ruta existe pero con otro método HTTP (ej: POST /promociones, cuando solo existe GET)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> manejarMetodoNoPermitido(HttpRequestMethodNotSupportedException ex) {
        String mensaje = String.format("El método %s no está permitido para esta ruta", ex.getMethod());
        return construirRespuesta(HttpStatus.METHOD_NOT_ALLOWED, mensaje);
    }

    // 500 - cualquier otro error que no se haya previsto (ej: se cae la conexión con la base de datos)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> manejarErrorGenerico(Exception ex) {
        log.error("Error no controlado", ex);      // el detalle técnico queda en el log, al cliente no se le muestra
        return construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado en el servidor");
    }


    // Arma la respuesta de error con el formato estándar. Está separado para no repetir
    // las mismas tres líneas en cada uno de los métodos de arriba.
    private ResponseEntity<ApiResponse<Void>> construirRespuesta(HttpStatus estado, String mensaje) {
        ApiResponse<Void> respuesta = ApiResponse.error(estado.value(), mensaje, null);     // en los errores "data" va en null
        return ResponseEntity.status(estado).body(respuesta);       // código HTTP real + el ApiResponse como cuerpo
    }
}