package com.project.timeOffManager.controller;

import com.project.timeOffManager.dto.ProjectDto;
import com.project.timeOffManager.entity.Project;
import com.project.timeOffManager.mapper.ProjectMapper;
import com.project.timeOffManager.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        ProjectDto project = projectService.getProjectById(id);

        if (project == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(project);
    }

    @Operation(summary = "Create a new project")
    @ApiResponse(responseCode = "201", description = "Project created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid project data")
    @PostMapping
    public ResponseEntity<ProjectDto> createProject(
            @Parameter(description = "The Project object to be created", required = true)
            @RequestBody ProjectDto projectDto) {

        ProjectDto createdProject = projectService.createProject(projectDto);

        if (createdProject == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.status(201).body(createdProject);
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
            @RequestBody ProjectDto projectDto) {

        ProjectDto updatedProject = projectService.updateProject(projectDto, id);

        if (updatedProject == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedProject);
    }

    @Operation(summary = "Delete a project by ID")
    @ApiResponse(responseCode = "204", description = "Project deleted successfully")
    @ApiResponse(responseCode = "404", description = "Project not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @Parameter(description = "ID of the project to delete", required = true)
            @PathVariable("id") Long id) {

        if (!projectService.deleteProject(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all projects (paginated)")
    @ApiResponse(responseCode = "200", description = "Projects found")
    @ApiResponse(responseCode = "404", description = "No projects found")
    @GetMapping
    public ResponseEntity<Page<ProjectDto>> getAllProjects(
            @Parameter(description = "Pagination information", required = false)
            Pageable pageable) {

        Page<ProjectDto> projects = projectService.getAllProjects(pageable);

        if (projects.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(projects);
    }
}
