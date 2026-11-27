package com.aydsii.demo.logging;

import org.slf4j.LoggerFactory;

public class LoggerFacade {

    private static volatile LoggerFacade instancia;
    // El nombre empieza con "com.aydsii" para que tome la configuración de log4j2-spring.xml
    // (nivel debug, consola y archivo logs/demo.log). Con otro nombre los debug no se mostraban.
    private final org.slf4j.Logger logger = LoggerFactory.getLogger("com.aydsii.demo");

    private LoggerFacade() {} // privado = Singleton

    public static LoggerFacade getInstance() {
        if (instancia == null) {
            synchronized (LoggerFacade.class) {
                if (instancia == null) {
                    instancia = new LoggerFacade();
                }
            }
        }
        return instancia;
    }

    public void info(String mensaje) { logger.info(mensaje); }
    public void warn(String mensaje) { logger.warn(mensaje); }
    public void error(String mensaje) { logger.error(mensaje); }
    public void error(String mensaje, Throwable ex) { logger.error(mensaje, ex); }
    public void debug(String mensaje) { logger.debug(mensaje); }
}
