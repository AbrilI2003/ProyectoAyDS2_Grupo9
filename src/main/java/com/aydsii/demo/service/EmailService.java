package com.aydsii.demo.service;

import com.aydsii.demo.logging.LoggerFacade;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final LoggerFacade log = LoggerFacade.getInstance();

    @Value("${spring.mail.username:}")
    private String remitente;

    @Value("${spring.mail.password:}")
    private String password;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /** El mail NO es opcional: si faltan credenciales, la app no arranca. */
    @PostConstruct
    public void validarConfiguracion() {
        if (remitente == null || remitente.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalStateException(
                    "Falta configuración de email. Copiá application-example.properties a "
                    + "application-local.properties y completá spring.mail.username y spring.mail.password."
            );
        }
        log.info("Email configurado correctamente para " + remitente);
    }

    public void enviarConfirmacionPedido(String destinatario, Long pedidoId, String linkPago) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject("Confirmación de pedido #" + pedidoId);
        mensaje.setText(
                "Hola,\n\n" +
                "Tu pedido #" + pedidoId + " ha sido creado con éxito.\n" +
                "Estado: PENDIENTE DE PAGO\n" +
                "Link de pago: " + linkPago + "\n\n" +
                "Tenes 60 segundos para completar el pago desde el link de arriba.\n" +
                "Gracias por tu compra.\n"
        );

        try {
            mailSender.send(mensaje);
            log.info("Email de confirmación enviado para pedido " + pedidoId);
        } catch (Exception ex) {
            log.error("No se pudo enviar el email de confirmación del pedido " + pedidoId + ": " + ex.getMessage());
            throw new IllegalStateException("No se pudo enviar el email de confirmación del pedido " + pedidoId, ex);
        }
    }
}
