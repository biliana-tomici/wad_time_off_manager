package com.project.timeOffManager.repository;

import com.project.timeOffManager.entity.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {


    Request findRequestById(Long requestId);

    @Query("SELECT r FROM Request r" +
            " WHERE r.employee.manager.id = :managerId AND r.status = 'Pending'")
    Page<Request> findPendingRequestsByManagerId(@Param("managerId") Long managerId, Pageable pageable);

    Page<Request> findAllByEmployee_Id_OrderByCreationDateDesc(Long employeeId, Pageable pageable);

    List<Request> findAllByEmployee_IdAndCreationDateAfter(Long employeeId, LocalDate creationDateAfter);
}
