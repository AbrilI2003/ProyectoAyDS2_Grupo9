package com.aydsii.demo.service;

import com.aydsii.demo.dto.PromocionDTO;
import com.aydsii.demo.entity.Promocion;
import com.aydsii.demo.repository.PromocionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromocionService {
    private final PromocionRepository promocionRepository;

    public PromocionService(PromocionRepository promocionRepository) {
        this.promocionRepository = promocionRepository;
    }

    public List<PromocionDTO> obtenerPromocionesVigentes() {
    // 1. Buscamos las entidades en la BD pasándole la fecha de hoy
    LocalDate hoy = LocalDate.now();
    List<Promocion> promociones = promocionRepository.findPromocionesVigentes(hoy);

    // 2. Mapeamos la lista de entidades a una lista de DTOs
    return promociones.stream()
        .map(promo -> new PromocionDTO(
            promo.getNombre(),
            promo.getDescripcion(),
            promo.getTipoDescuento(),
            promo.getValorDescuento(),
            promo.getFechaFin(), 
            promo.getProducto().getNombre()
        ))
        .collect(Collectors.toList());
    }
    
}
