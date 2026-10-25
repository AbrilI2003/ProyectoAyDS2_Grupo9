package com.aydsii.demo.service;

import com.aydsii.demo.dto.PromocionDTO;
import com.aydsii.demo.dto.PedidoDetalleDTO;
import com.aydsii.demo.entity.Producto;
import com.aydsii.demo.entity.Promocion;
import com.aydsii.demo.exception.PromocionVencidaException;
import com.aydsii.demo.exception.ResourceNotFoundException;
import com.aydsii.demo.repository.PromocionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PromocionService {
    private final PromocionRepository promocionRepository;

    public PromocionService(PromocionRepository promocionRepository, ProductoRepository productoRepository) {
        this.promocionRepository = promocionRepository;
        this.productoRepository = productoRepository;
    }

    public List<PromocionDTO> obtenerPromocionesVigentes() {
       LocalDate hoy = LocalDate.now();
       List<Promocion> promociones = promocionRepository.findPromocionesVigentes(hoy);

        // Flujo alternativo A1: no hay promociones vigentes
        if (promociones.isEmpty()) {
            throw new ResourceNotFoundException("No hay promociones vigentes.");
        }

        List<PromocionDTO> resultado = new ArrayList<>();

        for (Promocion promo : promociones) {
            // Se obtiene el producto con el productoRepository inyectado
            Producto prod = productoRepository.findById(promo.getProducto_id())
                    .orElse(null);

            String nombreProducto = (prod != null) ? prod.getNombre() : "Producto no disponible";
            
            // Flujo alternativo A3: Si no tiene stock ("Sin disponibilidad")
            if (prod != null && prod.getStock() <= 0) {
                nombreProducto += " (Sin disponibilidad)";
            }

            PromocionDTO dto = new PromocionDTO(
                promo.getNombre(),
                promo.getDescripcion(),
                promo.getTipoDescuento(),
                promo.getValorDescuento(),
                promo.getFechaFin(), 
                promo.getProducto().getNombre()
            );

            resultado.add(dto);
        }

        return resultado;
    }
}

