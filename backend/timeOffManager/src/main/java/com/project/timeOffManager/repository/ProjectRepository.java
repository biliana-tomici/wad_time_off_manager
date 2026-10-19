package com.project.timeOffManager.repository;

import com.project.timeOffManager.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByManagerId(Long managerId);

    Page<Project> findByManager_Id(Long managerId, Pageable pageable);

    Page<Project> findByEmployees_Id(Long employeeId, Pageable pageable);

    @EntityGraph(attributePaths = {"employees"})
    List<Project> findAllByEmployees_Id(Long employeeId);

    Project findProjectById(Long id);
}
