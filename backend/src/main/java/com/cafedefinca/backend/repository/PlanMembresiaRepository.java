package com.cafedefinca.backend.repository;

import com.cafedefinca.backend.entity.PlanMembresia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanMembresiaRepository extends JpaRepository<PlanMembresia, Integer> {

	List<PlanMembresia> findAllByActivoTrueOrderByIdPlanAsc();
}
