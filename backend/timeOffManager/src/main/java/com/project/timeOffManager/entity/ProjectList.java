package com.project.timeOffManager.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "project_list")
public class ProjectList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private User employee;

    public ProjectList() {
    }

    public ProjectList(Project project, User employee) {
        this.project = project;
        this.employee = employee;
    }

    @Override
    public String toString() {
        return String.format("ProjectList[plid=%d, pid=%d, eid=%d]", this.id, project.getId(), employee.getId());
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public User getEmployee() {
        return employee;
    }
}
