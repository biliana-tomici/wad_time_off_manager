package com.project.timeOffManager.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "balance")
public class Balance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private User employee;

    private Long daysLeft;
    private String name;
    private Long maxDays;

    @Version
    private Long version;

    public Balance() {}

    public Balance(Long id, User employee, Long daysLeft, String name, Long maxDays) {
        this.id = id;
        this.employee = employee;
        this.daysLeft = daysLeft;
        this.name = name;
        this.maxDays = maxDays;
    }

    @Override
    public String toString() {
        return String.format("Balance[bid=%d, eid=%d, daysLeft=%d, name='%s', maxDays=%d]", id, employee.getId(), daysLeft, name, maxDays);
    }

    public Long getId() {
        return id;
    }

    public User getEmployee() {
        return employee;
    }

    public Long getDaysLeft() {
        return daysLeft;
    }

    public String getName() {
        return name;
    }

    public Long getMaxDays() {
        return maxDays;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEmployee(User employee) {
        this.employee = employee;
    }

    public void setDaysLeft(Long daysLeft) {
        this.daysLeft = daysLeft;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMaxDays(Long maxDays) {
        this.maxDays = maxDays;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
