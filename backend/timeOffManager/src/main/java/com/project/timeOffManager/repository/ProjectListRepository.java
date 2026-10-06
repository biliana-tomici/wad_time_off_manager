package com.project.timeOffManager.repository;

import com.project.timeOffManager.entity.ProjectList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectListRepository extends JpaRepository<ProjectList, Long> {


}
