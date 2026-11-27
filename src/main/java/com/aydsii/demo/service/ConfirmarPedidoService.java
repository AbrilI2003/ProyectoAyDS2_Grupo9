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

        // 2. Validación previa de stock
        validarStockSuficiente(request.getItems());

        // 3. Determinar si es cotización
        boolean pedidoEspecial = esCotizacion(request.getItems(), request.getPais(), request.getCodigoPostal());

        // 4. Procesar ítems. Los precios y montos solo se calculan si es pedido normal.
        Integer totalUnidades = 0;
        BigDecimal montoProductos = BigDecimal.ZERO;
        List<DetallePedido> detalles = new ArrayList<>();

        for (ItemPedidoRequest item : request.getItems()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + item.getProductoId()));

            Integer cantidad = item.getCantidad();

            if (cantidad < config.getStockMinimo()) {
                throw new CantidadMinimaException(
                        "El producto " + producto.getNombre() + " requiere un mínimo de " + config.getStockMinimo() + " unidades"
                );
            }

            DetallePedido detalle = new DetallePedido();
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);

            if (!pedidoEspecial) {
                // Calcular precio con promoción y acumular monto solo para pedidos normales
                BigDecimal precioConPromocion = calcularPrecioConPromocion(producto);
                detalle.setPrecioUnitario(precioConPromocion);
                montoProductos = montoProductos.add(precioConPromocion.multiply(BigDecimal.valueOf(cantidad)));
            }

            detalles.add(detalle);
            totalUnidades += cantidad;
        }

        PedidoConfirmarResponse respuesta = new PedidoConfirmarResponse();

        if (pedidoEspecial) {
            Cotizacion cotizacion = new Cotizacion();
            cotizacion.setPedido(null);
            cotizacion.setMontoCotizado(BigDecimal.ZERO);
            cotizacion.setFechaEmision(LocalDate.now());
            cotizacion.setFechaVencimiento(LocalDate.now().plusDays(config.getDiasVencimientoCotizacion()));
            cotizacion.setEstado("SOLICITADA");
            cotizacion.setEnlaceWhatsapp(generarLinkWhatsApp(null, detalles));
            cotizacion.setDiasParaResponder(config.getDiasVencimientoCotizacion());

            cotizacionRepository.save(cotizacion);

            String enlaceWhatsapp = generarLinkWhatsApp(cotizacion.getId(), detalles);

            respuesta.setId(cotizacion.getId());
            respuesta.setMensaje("Pedido que requiere cotización. Se generó una cotización en estado solicitada. Comuníquese con nuestros vendedores.");
            respuesta.setEnlaceWhatsapp(enlaceWhatsapp);
        } else {
            // Pedido normal (se calculan los montos y se reserva stock)
            BigDecimal montoEnvio = calcularCostoEnvio();
            BigDecimal montoTotal = montoProductos.add(montoEnvio);

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
                // Reservar stock
                Producto producto = d.getProducto();
                producto.setStock(producto.getStock() - d.getCantidad());
            }

            pedidoRepository.save(pedido);

            String linkPago = generarLinkPagoSimulado(pedido.getId());
            pedido.setLinkPago(linkPago);
            pedido.setFechaExpiracionPago(LocalDateTime.now().plusSeconds(60));
            pedidoRepository.save(pedido);
            log.debug("Link de pago con expiración en 60s guardado en BD para pedido " + pedido.getId());

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

    /*Determina si el pedido debe ir a cotización usando cantidades y destino.*/
    private boolean esCotizacion(List<ItemPedidoRequest> items, String pais, String codigoPostal) {
        Integer totalUnidades = 0;
        boolean excedeMaximoPorProducto = false;

        for (ItemPedidoRequest item : items) {
            totalUnidades += item.getCantidad();
            if (item.getCantidad() > config.getStockMaximoProducto()) {
                excedeMaximoPorProducto = true;
            }
        }

        boolean esMayoristaPorUnidades = totalUnidades > config.getLimiteUnidadesMayorista();
        boolean esDestinoEspecial = !"Argentina".equalsIgnoreCase(pais) || !esProvinciaPermitida(codigoPostal);

        return esMayoristaPorUnidades || esDestinoEspecial || excedeMaximoPorProducto;
    }

    /* Valida que todos los ítems tengan stock suficiente antes de procesar cualquier cambio.*/
    private void validarStockSuficiente(List<ItemPedidoRequest> items) {
        List<String> productosSinStock = new ArrayList<>();

        for (ItemPedidoRequest item : items) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + item.getProductoId()));

            Integer cantidad = item.getCantidad();

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

    /* Calcula el precio unitario aplicando la mejor promoción vigente del producto.*/
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

    /*Calcula descuento según el tipo.*/
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

    /*Genera el link de Whatsapp con el número de cotización y el detalle del pedido */
    private String generarLinkWhatsApp(Long cotizacionId, List<DetallePedido> detalles) {
        String numeroTelefono = "2302206670";
        StringBuilder mensaje = new StringBuilder();

        if (cotizacionId != null) {
            mensaje.append("Hola! Mi número de cotización es ").append(cotizacionId).append(".\n");
        } else {
            mensaje.append("Hola! Quisiera información sobre una cotización.\n");
        }

        if (detalles != null && !detalles.isEmpty()) {
            mensaje.append("Detalle del pedido:\n");
            for (DetallePedido d : detalles) {
                mensaje.append("- ")
                       .append(d.getProducto().getNombre())
                       .append(" x")
                       .append(d.getCantidad())
                       .append("\n");
            }
        }

        String mensajeCodificado = URLEncoder.encode(mensaje.toString(), StandardCharsets.UTF_8);
        return "https://wa.me/" + numeroTelefono + "?text=" + mensajeCodificado;
    }

    /*Genera el link de pago que apunta a la página de pago de este proyecto.*/
    private String generarLinkPagoSimulado(Long pedidoId) {
        return "http://localhost:8080/pedidos/" + pedidoId + "/pago";
    }

    /*El costo de envío lo determina el servidor (nunca el cliente).*/
    private BigDecimal calcularCostoEnvio() {
        return new BigDecimal("1500.00");
    }
}