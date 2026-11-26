package com.aydsii.demo.exception;

public class StockInsuficienteException extends BusinessException {
    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }
}