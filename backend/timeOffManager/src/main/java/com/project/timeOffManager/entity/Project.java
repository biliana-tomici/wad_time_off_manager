package com.project.timeOffManager.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "project")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long minEmp;
    private String name;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User manager;

    @ManyToMany
    @JoinTable(
        name = "project_list",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    private List<User> employees;

    public Project() {}

    public Project(Long id, Long minEmp, String name, User manager) {
        this.id = id;
        this.minEmp = minEmp;
        this.name = name;
        this.manager = manager;
    }

    @Override
    public String toString() {
        return String.format("Project[pid=%d, minEmp=%d, name='%s', managerId=%d]", id, minEmp, name, manager.getId());
    }

    public Long getId() {
        return id;
    }

    public Long getMinEmp() {
        return minEmp;
    }

    public String getName() {
        return name;
    }

    public User getManager() {
        return manager;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMinEmp(Long minEmp) {
        this.minEmp = minEmp;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }

    public List<User> getEmployees() {
        return employees;
    }

    public void setEmployees(List<User> employees) {
        this.employees = employees;
    }
}

