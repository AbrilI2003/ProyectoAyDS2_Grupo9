package com.aydsii.demo.repository;

import com.aydsii.demo.entity.DetallePedido;
import com.aydsii.demo.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Query("SELECT d FROM Pedido p " +
       "JOIN p.detalles d " +
       "JOIN FETCH d.producto " +
       "WHERE p.id = :id")
    List<DetallePedido> findDetallesPorPedidoId(@Param("id") Long id);
}