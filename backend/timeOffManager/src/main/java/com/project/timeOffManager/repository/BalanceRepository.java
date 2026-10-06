package com.project.timeOffManager.repository;

import com.project.timeOffManager.entity.Balance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BalanceRepository extends JpaRepository<Balance, Long> {

    List<Balance> findAllById(Long id);

    Balance findBalanceById(Long id);
    List<Balance> findByEmployeeId(Long employeeId);
    Page<Balance> findByEmployeeId(Long employeeId, Pageable pageable);

}
