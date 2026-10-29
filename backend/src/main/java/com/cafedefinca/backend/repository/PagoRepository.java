package com.cafedefinca.backend.repository;

import com.cafedefinca.backend.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Integer> {
}
