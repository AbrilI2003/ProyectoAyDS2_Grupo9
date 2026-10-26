package com.aydsii.demo.exception;

// Se lanza desde el service cuando se busca algo que no existe

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }
}