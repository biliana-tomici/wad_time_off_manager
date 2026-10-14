package com.project.timeOffManager.controller;

import com.project.timeOffManager.dto.ProjectDto;
import com.project.timeOffManager.dto.UserDto;
import com.project.timeOffManager.dto.command.CreateProjectCommand;
import com.project.timeOffManager.dto.command.UpdateProjectCommand;
import jakarta.validation.Valid;
import com.project.timeOffManager.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import com.project.timeOffManager.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Operation(summary = "Get project by ID")
    @ApiResponse(responseCode = "200", description = "Project found")
    @ApiResponse(responseCode = "404", description = "Project not found")
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> getProjectById(
            @Parameter(description = "ID of the project", required = true)
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @Operation(summary = "Create a new project")
    @ApiResponse(responseCode = "201", description = "Project created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid project data")
    @PostMapping
    public ResponseEntity<ProjectDto> createProject(
            @Parameter(description = "The Project object to be created", required = true)
            @Valid @RequestBody CreateProjectCommand command) {
        return ResponseEntity.status(201).body(projectService.createProject(command));
    }

    @Operation(summary = "Update an existing project")
    @ApiResponse(responseCode = "200", description = "Project updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid project data")
    @ApiResponse(responseCode = "404", description = "Project not found")
    @PutMapping("/{id}")
    public ResponseEntity<ProjectDto> updateProject(
            @Parameter(description = "ID of the project to update", required = true)
            @PathVariable("id") Long id,
            @Parameter(description = "Updated Project object", required = true)
            @Valid @RequestBody UpdateProjectCommand command) {
        return ResponseEntity.ok(projectService.updateProject(command, id));
    }

    @Operation(summary = "Delete a project by ID")
    @ApiResponse(responseCode = "204", description = "Project deleted successfully")
    @ApiResponse(responseCode = "404", description = "Project not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @Parameter(description = "ID of the project to delete", required = true)
            @PathVariable("id") Long id) {

        if (!projectService.deleteProject(id)) {
            throw new ResourceNotFoundException("Project not found with id: " + id);
        }

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all projects (paginated)")
    @ApiResponse(responseCode = "200", description = "Projects found")
    @GetMapping
    public ResponseEntity<Page<ProjectDto>> getAllProjects(
            @Parameter(description = "Pagination information", required = false)
            Pageable pageable) {

        Page<ProjectDto> projects = projectService.getAllProjects(pageable);
        return ResponseEntity.ok(projects);
    }

    @Operation(summary = "Get project members")
    @ApiResponse(responseCode = "200", description = "Project members retrieved")
    @ApiResponse(responseCode = "404", description = "Project not found")
    @GetMapping("/{id}/members")
    public ResponseEntity<List<UserDto>> getProjectMembers(@PathVariable("id") Long id) {
        return ResponseEntity.ok(projectService.getProjectMembers(id));
    }

    @Operation(summary = "Add an employee to a project")
    @ApiResponse(responseCode = "200", description = "Employee added successfully")
    @ApiResponse(responseCode = "400", description = "Invalid project or employee ID")
    @PostMapping("/{id}/members/{employeeId}")
    public ResponseEntity<Void> addProjectMember(@PathVariable("id") Long id, @PathVariable("employeeId") Long employeeId) {
        return projectService.addMember(id, employeeId) ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @Operation(summary = "Remove an employee from a project")
    @ApiResponse(responseCode = "204", description = "Employee removed successfully")
    @ApiResponse(responseCode = "404", description = "Project or employee not found")
    @DeleteMapping("/{id}/members/{employeeId}")
    public ResponseEntity<Void> removeProjectMember(@PathVariable("id") Long id, @PathVariable("employeeId") Long employeeId) {
        return projectService.removeMember(id, employeeId) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

