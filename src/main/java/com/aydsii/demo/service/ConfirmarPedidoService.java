package com.aydsii.demo.service;

import com.aydsii.demo.dto.DetallePedidoDTO;
import com.aydsii.demo.dto.ItemPedidoRequest;
import com.aydsii.demo.dto.PedidoConfirmarRequest;
import com.aydsii.demo.dto.PedidoConfirmarResponse;
import com.aydsii.demo.entity.Cotizacion;
import com.aydsii.demo.entity.DetallePedido;
import com.aydsii.demo.entity.Pedido;
import com.aydsii.demo.entity.Producto;
import com.aydsii.demo.entity.Promocion;
import com.aydsii.demo.entity.Cliente;
import com.aydsii.demo.exception.CantidadMinimaException;
import com.aydsii.demo.exception.ResourceNotFoundException;
import com.aydsii.demo.exception.StockInsuficienteException;
import com.aydsii.demo.logging.LoggerFacade;
import com.aydsii.demo.repository.CotizacionRepository;
import com.aydsii.demo.repository.ClienteRepository;
import com.aydsii.demo.repository.PedidoRepository;
import com.aydsii.demo.repository.ProductoRepository;
import com.aydsii.demo.singleton.ConfiguracionNegocio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConfirmarPedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final CotizacionRepository cotizacionRepository;
    private final EmailService emailService;
    private final LoggerFacade log = LoggerFacade.getInstance();
    private final ConfiguracionNegocio config = ConfiguracionNegocio.getInstance();

    public ConfirmarPedidoService(PedidoRepository pedidoRepository,
                                  ClienteRepository clienteRepository,
                                  ProductoRepository productoRepository,
                                  CotizacionRepository cotizacionRepository,
                                  EmailService emailService) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
        this.cotizacionRepository = cotizacionRepository;
        this.emailService = emailService;
    }

    @Transactional
    public PedidoConfirmarResponse confirmar(PedidoConfirmarRequest request) {
        log.info("Creando y confirmando pedido para cliente ID: " + request.getClienteId());

        // 1. Buscar cliente existente
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + request.getClienteId()));

        // 2. VALIDACIÓN PREVIA DE STOCK (pase previo antes de tocar nada)
        validarStockSuficiente(request.getItems());

        // 3. Procesar ítems, reservar stock y calcular montos con promociones
        Integer totalUnidades = 0;
        BigDecimal montoProductos = BigDecimal.ZERO;
        List<DetallePedido> detalles = new ArrayList<>();
        boolean excedeMaximoPorProducto = false;

        for (ItemPedidoRequest item : request.getItems()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + item.getProductoId()));

            Integer cantidad = item.getCantidad();

            if (cantidad < config.getStockMinimo()) {
                throw new CantidadMinimaException(
                        "El producto " + producto.getNombre() + " requiere un mínimo de " + config.getStockMinimo() + " unidades"
                );
            }

            // Si supera el máximo por producto, NO es error: el pedido pasa a ser especial (cotización)
            if (cantidad > config.getStockMaximoProducto()) {
                excedeMaximoPorProducto = true;
            }

            // Calcular precio unitario con promoción vigente (si aplica)
            BigDecimal precioConPromocion = calcularPrecioConPromocion(producto);

            // Reservar stock
            producto.setStock(producto.getStock() - cantidad);

            DetallePedido detalle = new DetallePedido();
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(precioConPromocion);

            detalles.add(detalle);

            totalUnidades += cantidad;
            montoProductos = montoProductos.add(precioConPromocion.multiply(BigDecimal.valueOf(cantidad)));
        }

        // 4. Determinar si es pedido especial (mayorista)
        boolean esMayoristaPorUnidades = totalUnidades > config.getLimiteUnidadesMayorista();
        boolean esDestinoEspecial = !"Argentina".equalsIgnoreCase(request.getPais())
                || !esProvinciaPermitida(request.getCodigoPostal());

        boolean pedidoEspecial = esMayoristaPorUnidades || esDestinoEspecial || excedeMaximoPorProducto;

        // Monto total = productos + envío
        BigDecimal montoEnvio = calcularCostoEnvio();
        BigDecimal montoTotal = montoProductos.add(montoEnvio);

        // Detalle de productos para la respuesta
        List<DetallePedidoDTO> detallesDTO = new ArrayList<>();
        for (DetallePedido d : detalles) {
            BigDecimal subtotal = d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad()));
            detallesDTO.add(new DetallePedidoDTO(
                    d.getProducto().getNombre(),
                    d.getCantidad(),
                    d.getPrecioUnitario(),
                    subtotal
            ));
        }

        PedidoConfirmarResponse respuesta = new PedidoConfirmarResponse();

        if (pedidoEspecial) {
            // Pedido especial (mayorista): crear Cotización en BD, SIN crear pedido aún
            Cotizacion cotizacion = new Cotizacion();
            cotizacion.setPedido(null); // aún no hay pedido
            cotizacion.setMontoCotizado(montoTotal);
            cotizacion.setFechaEmision(LocalDate.now());
            cotizacion.setFechaVencimiento(LocalDate.now().plusDays(config.getDiasVencimientoCotizacion()));
            cotizacion.setEstado("SOLICITADA");
            cotizacion.setEnlaceWhatsapp(generarLinkWhatsApp(null, detallesDTO));
            cotizacion.setDiasParaResponder(config.getDiasVencimientoCotizacion());

            cotizacionRepository.save(cotizacion);

            // Generar enlace de WhatsApp con el número de cotización y el detalle del pedido
            String enlaceWhatsapp = generarLinkWhatsApp(cotizacion.getId(), detallesDTO);

            respuesta.setId(cotizacion.getId());
            respuesta.setMensaje("Pedido que requiere cotización. Se generó una cotización en estado solicitada. Comuníquese con nuestros vendedores.");
            respuesta.setEnlaceWhatsapp(enlaceWhatsapp);
            // No se setean linkPago ni montos ni detalles en la response para pedidos especiales
        } else {
            // Pedido normal: crear el pedido
            Pedido pedido = new Pedido();
            pedido.setCliente(cliente);
            pedido.setEstado("PENDIENTE_PAGO");
            pedido.setFechaPedido(LocalDate.now());
            pedido.setPais(request.getPais());
            pedido.setCalle(request.getCalle());
            pedido.setAltura(request.getAltura());
            pedido.setCodigoPostal(request.getCodigoPostal());
            pedido.setCostoEnvio(montoEnvio);
            pedido.setTotalUnidades(totalUnidades);
            pedido.setDetalles(detalles);

            for (DetallePedido d : detalles) {
                d.setPedido(pedido);
            }

            pedidoRepository.save(pedido);

            // Link de pago que apunta a la página de pago de este proyecto
            String linkPago = generarLinkPagoSimulado(pedido.getId());
            pedido.setLinkPago(linkPago);
            // Expiración en 60 segundos PERSISTIDA en BD (sobrevive reinicios)
            pedido.setFechaExpiracionPago(LocalDateTime.now().plusSeconds(60));
            pedidoRepository.save(pedido);
            log.debug("Link de pago con expiración en 60s guardado en BD para pedido " + pedido.getId());

            // Enviar email de confirmación al cliente
            emailService.enviarConfirmacionPedido(cliente.getEmail(), pedido.getId(), linkPago);

            respuesta.setId(pedido.getId());
            respuesta.setMensaje("Pedido creado. Queda pendiente de pago. Stock reservado.");
            respuesta.setLinkPago(linkPago);
            respuesta.setMontoProductos(montoProductos);
            respuesta.setMontoEnvio(montoEnvio);
            respuesta.setMontoTotal(montoTotal);
            respuesta.setDetalles(detallesDTO);
        }

        log.info("Pedido/Cotizacion creado. Estado: " + (pedidoEspecial ? "SOLICITADA" : "PENDIENTE_PAGO"));
        return respuesta;
    }

    /**
     * Valida que todos los ítems tengan stock suficiente ANTES de procesar cualquier cambio.
     * Si falta stock de algún producto, lanza excepción y no se modifica nada en la BD.
     */
    private void validarStockSuficiente(List<ItemPedidoRequest> items) {
        List<String> productosSinStock = new ArrayList<>();

        for (ItemPedidoRequest item : items) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + item.getProductoId()));

            Integer cantidad = item.getCantidad();

            if (cantidad < config.getStockMinimo()) {
                throw new CantidadMinimaException(
                        "El producto " + producto.getNombre() + " requiere un mínimo de " + config.getStockMinimo() + " unidades"
                );
            }

            if (cantidad > producto.getStock()) {
                productosSinStock.add(producto.getNombre() + " (solicitado: " + cantidad + ", disponible: " + producto.getStock() + ")");
            }
        }

        if (!productosSinStock.isEmpty()) {
            throw new StockInsuficienteException(
                    "No hay stock suficiente para los siguientes productos: " + String.join(", ", productosSinStock)
            );
        }
    }

    /**
     * Calcula el precio unitario aplicando la mejor promoción vigente del producto.
     * Si no hay promoción vigente, devuelve el precio base.
     */
    private BigDecimal calcularPrecioConPromocion(Producto producto) {
        LocalDate hoy = LocalDate.now();
        BigDecimal precioBase = producto.getPrecio();
        BigDecimal mejorPrecio = precioBase;

        List<Promocion> promociones = producto.getPromociones();
        if (promociones != null) {
            for (Promocion p : promociones) {
                boolean vigente = hoy.isAfter(p.getFechaInicio().minusDays(1))
                        && hoy.isBefore(p.getFechaFin().plusDays(1));
                if (vigente) {
                    BigDecimal precioConDescuento = aplicarDescuento(precioBase, p.getTipoDescuento(), p.getValorDescuento());
                    if (precioConDescuento.compareTo(mejorPrecio) < 0) {
                        mejorPrecio = precioConDescuento;
                    }
                }
            }
        }

        return mejorPrecio.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Aplica un descuento según el tipo.
     * "PORCENTAJE": valorDescuento es un porcentaje (ej: 10 = 10% descuento)
     * "MONTO_FIJO": valorDescuento se resta directamente del precio
     */
    private BigDecimal aplicarDescuento(BigDecimal precio, String tipoDescuento, BigDecimal valorDescuento) {
        if (tipoDescuento == null || valorDescuento == null) {
            return precio;
        }

        switch (tipoDescuento.toUpperCase()) {
            case "PORCENTAJE":
                return precio.multiply(BigDecimal.ONE.subtract(valorDescuento.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)));
            case "MONTO_FIJO":
                BigDecimal resultado = precio.subtract(valorDescuento);
                return resultado.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : resultado;
            default:
                return precio;
        }
    }

    private boolean esProvinciaPermitida(String codigoPostal) {
        if (codigoPostal == null || codigoPostal.isEmpty()) {
            return false;
        }
        String letraProvincia = codigoPostal.substring(0, 1).toUpperCase();
        return switch (letraProvincia) {
            case "B", "L", "Q", "R", "M", "D", "X" -> true;
            default -> false;
        };
    }

    /**
     * Genera el link de WhatsApp para contactar sobre una cotización.
     * El mensaje por defecto incluye el número de cotización y el detalle del pedido.
     */
    private String generarLinkWhatsApp(Long cotizacionId, List<DetallePedidoDTO> detallesDTO) {
        String numeroTelefono = "2302206670";
        StringBuilder mensaje = new StringBuilder();

        if (cotizacionId != null) {
            mensaje.append("Hola! Mi número de cotización es ").append(cotizacionId).append(".\n");
        } else {
            mensaje.append("Hola! Quisiera información sobre una cotización.\n");
        }

        if (detallesDTO != null && !detallesDTO.isEmpty()) {
            mensaje.append("Detalle del pedido:\n");
            for (DetallePedidoDTO d : detallesDTO) {
                mensaje.append("- ")
                       .append(d.getNombreProducto())
                       .append(" x")
                       .append(d.getCantidad())
                       .append(" ($")
                       .append(d.getTotal())
                       .append(")\n");
            }
        }

        String mensajeCodificado = URLEncoder.encode(mensaje.toString(), StandardCharsets.UTF_8);
        return "https://wa.me/" + numeroTelefono + "?text=" + mensajeCodificado;
    }

    /**
     * Genera el link de pago que apunta a la página de pago de este proyecto.
     * Al visitarlo se muestra una pantalla con el botón PAGAR y los datos del pedido.
     */
    private String generarLinkPagoSimulado(Long pedidoId) {
        return "http://localhost:8080/pedidos/" + pedidoId + "/pago";
    }

    /**
     * El costo de envío lo determina el servidor (nunca el cliente).
     * Por ahora es un valor fijo; en producción se calcularía según destino, peso, volumen, etc.
     */
    private BigDecimal calcularCostoEnvio() {
        return new BigDecimal("1500.00");
    }
}
