package com.aydsii.demo.exception;

public class BusinessException extends RuntimeException {

    public BusinessException(String mensaje) {
        super(mensaje);
    }
}