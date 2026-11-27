package com.aydsii.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aydsii.demo.entity.Cotizacion;

@Repository
public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {
}