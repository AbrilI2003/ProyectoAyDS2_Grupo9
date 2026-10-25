package com.aydsii.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromocionDTO {
    private String nombre;
    private String descripcion;
    private String tipoDescuento;
    private BigDecimal valorDescuento;
    private LocalDate fechaFin;

    private String nombreProducto;
}

/*public List<PromocionDTO> obtenerPromocionesVigentes() {
    
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
}*/