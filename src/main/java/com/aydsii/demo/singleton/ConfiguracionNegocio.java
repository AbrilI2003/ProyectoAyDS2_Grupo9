package com.aydsii.demo.singleton;

public class ConfiguracionNegocio {

    private static volatile ConfiguracionNegocio instancia;

    private final int stockMinimo = 50;
    private final int stockMaximoProducto = 160;
    private final int limiteUnidadesMayorista = 500;
    private final int diasVencimientoCotizacion = 5;

    private ConfiguracionNegocio() {}

    public static ConfiguracionNegocio getInstance() {
        if (instancia == null) {
            synchronized (ConfiguracionNegocio.class) {
                if (instancia == null) {
                    instancia = new ConfiguracionNegocio();
                }
            }
        }
        return instancia;
    }

    public int getStockMinimo() { return stockMinimo; }
    public int getStockMaximoProducto() { return stockMaximoProducto; }
    public int getLimiteUnidadesMayorista() { return limiteUnidadesMayorista; }
    public int getDiasVencimientoCotizacion() { return diasVencimientoCotizacion; }
}