package com.project.timeOffManager.repository;

import com.project.timeOffManager.entity.Request;
import com.project.timeOffManager.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {


    Request findRequestById(Long requestId);

    @Query("SELECT r FROM Request r WHERE (r.employee.manager.id = :managerId OR (:includeUnassigned = true AND r.employee.manager IS NULL)) AND r.status IN :statuses")
    Page<Request> findPendingRequestsByManagerId(@Param("managerId") Long managerId,
                                                 @Param("includeUnassigned") boolean includeUnassigned,
                                                 @Param("statuses") List<Status> statuses,
                                                 Pageable pageable);

    Page<Request> findAllByEmployee_Id_OrderByCreationDateDesc(Long employeeId, Pageable pageable);

    List<Request> findByEmployee_IdAndStatusIn(Long employeeId, List<Status> statuses);

    List<Request> findByBalance_Id(Long balanceId);

    List<Request> findAllByEmployee_IdAndCreationDateAfter(Long employeeId, LocalDate creationDateAfter);

    @Query("SELECT r FROM Request r WHERE r.employee.id IN :employeeIds AND r.startDate <= :windowEnd AND r.endDate >= :windowStart")
    List<Request> findRequestsOverlappingWindow(@Param("employeeIds") List<Long> employeeIds,
                                                @Param("windowStart") LocalDate windowStart,
                                                @Param("windowEnd") LocalDate windowEnd);

    @Query("SELECT r FROM Request r WHERE r.employee.id IN :employeeIds AND r.status IN :statuses AND r.startDate <= :windowEnd AND r.endDate >= :windowStart")
    List<Request> findRequestsOverlappingWindowWithStatuses(@Param("employeeIds") List<Long> employeeIds,
                                                           @Param("statuses") List<Status> statuses,
                                                           @Param("windowStart") LocalDate windowStart,
                                                           @Param("windowEnd") LocalDate windowEnd);

    @Query("SELECT r FROM Request r WHERE r.employee.id IN :employeeIds AND r.status = :status AND r.startDate <= :date AND r.endDate >= :date")
    List<Request> findApprovedRequestsCoveringDate(@Param("employeeIds") List<Long> employeeIds,
                                                   @Param("status") Status status,
                                                   @Param("date") LocalDate date);
}
