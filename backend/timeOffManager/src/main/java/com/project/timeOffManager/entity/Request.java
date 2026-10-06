package com.project.timeOffManager.entity;

import com.project.timeOffManager.enums.Status;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;

@Entity
@Table(name = "request")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private User employee;

    @OneToOne
    @JoinColumn(name = "balance_id")
    private Balance balance;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDate startDate;

    private LocalDate endDate;
    private String explanation;

    @CreatedDate
    private LocalDate creationDate;


    private LocalDate modifiedDate;

    public Request() {
    }

    public Request(Long id, User employee, Balance balance, Status status, LocalDate startDate, LocalDate endDate, String explanation, LocalDate creationDate, LocalDate modifiedDate) {
        this.id = id;
        this.employee = employee;
        this.balance = balance;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.explanation = explanation;
        this.creationDate = creationDate;
        this.modifiedDate = modifiedDate;
    }

    @Override
    public String toString() {
        return String.format("Request[rid=%d, eid=%d, bid=%d, status='%s', startDate=%s, endDate=%s]", id, employee.getId(), balance.getId(), status, startDate, endDate);
    }

    public Long getId() {
        return id;
    }

    public User getEmployee() {
        return employee;
    }

    public Balance getBalance() {
        return balance;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getExplanation() {
        return explanation;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public LocalDate getModifiedDate() {
        return modifiedDate;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEmployee(User employee) {
        this.employee = employee;
    }

    public void setBalance(Balance balance) {
        this.balance = balance;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public void setModifiedDate(LocalDate modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}
