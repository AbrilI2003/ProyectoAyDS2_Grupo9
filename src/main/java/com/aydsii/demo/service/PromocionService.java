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

    LocalDate hoy = LocalDate.now();
    // consulta las promociones activas a la fecha actual
    List<Promocion> promociones = promocionRepository.findPromocionesVigentes(hoy);

    // 
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
