package isec.tp.das.onlinecompiler.controllers;

import isec.tp.das.onlinecompiler.models.ProjectEntity;
import isec.tp.das.onlinecompiler.services.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectRestController {

    private final ProjectService projectService;

    @Autowired
    public ProjectRestController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectEntity> getAllProjects() {
        return projectService.getAllProjects();
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectEntity> getProjectById(@PathVariable Long projectId) {
        ProjectEntity project = projectService.getProjectById(projectId);

        if (project != null) {
            return ResponseEntity.ok(project);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<ProjectEntity> createProject(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("files") List<MultipartFile> files
    ) {
        try {
            ProjectEntity project = projectService.createProject(name, description, files);

            return ResponseEntity.status(HttpStatus.CREATED).body(project);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // TODO: corrigir metodo de update (problema na parte dos ficheiros)
    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectEntity> updateProject(@PathVariable Long projectId, @RequestBody ProjectEntity updatedProject) {
        ProjectEntity project = projectService.updateProject(projectId, updatedProject);

        if (project != null) {
            return ResponseEntity.ok(project);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {
        if (projectService.deleteProject(projectId)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}