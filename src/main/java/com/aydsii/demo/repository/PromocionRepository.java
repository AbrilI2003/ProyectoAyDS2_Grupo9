package com.aydsii.demo.repository;

import com.aydsii.demo.entity.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PromocionRepository extends JpaRepository<Promocion, Long> {

    @Query("SELECT p FROM Promocion p JOIN FETCH p.producto " +
    "WHERE :fechaActual BETWEEN p.fechaInicio AND p.fechaFin")
    List<Promocion> findPromocionesVigentes(@Param("fechaActual") LocalDate fechaActual);
}