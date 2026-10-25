package com.aydsii.demo.exception;

// Se lanza desde el service cuando se busca algo que no existe (ej: un pedido con un id inexistente).
// No se atrapa con try/catch en el controller: el GlobalExceptionHandler la intercepta y responde 404.
public class ResourceNotFoundException extends RuntimeException {       // RuntimeException: no obliga a declararla con "throws"

    public ResourceNotFoundException(String mensaje) {
        super(mensaje);     // el mensaje es el que después se muestra en "messege"
    }
}