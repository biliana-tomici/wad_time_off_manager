package com.project.timeOffManager.repository;

import com.project.timeOffManager.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByLastName(String lastName);

    @EntityGraph(attributePaths = {"projects"})
    User findCustomerById(Long customerId);

    List<User> findByManager_Id(Long managerId);

    @Query("SELECT u FROM User u JOIN u.projects p WHERE p.id = :projectId")
    List<User> findUsersByProjectId(Long projectId);

    @Override
    @EntityGraph(attributePaths = {"projects"})
    List<User> findAll();

    @EntityGraph(attributePaths = {"projects"})
    List<User> findByActiveTrue();

    @EntityGraph(attributePaths = {"projects"})
    java.util.Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
