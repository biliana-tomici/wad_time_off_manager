package com.project.timeOffManager.repository;

import com.project.timeOffManager.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByManagerId(Long managerId);

    Project findProjectById(Long id);
}
