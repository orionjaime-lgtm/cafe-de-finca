package com.cafedefinca.backend.repository;

import com.cafedefinca.backend.entity.MedioPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedioPagoRepository extends JpaRepository<MedioPago, Integer> {

	List<MedioPago> findAllByOrderByIdMedioPagoAsc();
}
