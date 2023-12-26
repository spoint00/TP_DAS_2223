package isec.tp.das.onlinecompiler.Resources;

import isec.tp.das.onlinecompiler.Models.Project;
import isec.tp.das.onlinecompiler.Services.BuildManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectRestController {
    private BuildManager bm;

    // nao dá para usar @Autowired num constructor sem args
    public ProjectRestController() {
        this.bm = BuildManager.getInstance();
        // Initialize three sample projects
        Project project1 = new Project(1, "Project 1", new ArrayList<>(), "Description 1");
        Project project2 = new Project(2, "Project 2", new ArrayList<>(), "Description 2");
        Project project3 = new Project(3, "Project 3", new ArrayList<>(), "Description 3");

        // Add projects to the BuildManager
        bm.addProject(project1);
        bm.addProject(project2);
        bm.addProject(project3);
    }

    @GetMapping("/")
    public List<Project> getAllProjects() {
        return bm.getAllProjects();
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<Project> getProjectById(@PathVariable int projectId) {
        Project project = bm.getProjectById(projectId);
        if (project != null) {
            return ResponseEntity.ok(project);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        bm.addProject(project);
        // You can customize the response as needed
        return ResponseEntity.ok(project);
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<Project> updateProject(@PathVariable int projectId, @RequestBody Project updatedProject) {
        Project existingProject = bm.getProjectById(projectId);
        if (existingProject != null) {
            // Update properties of the existing project
            existingProject.setName(updatedProject.getName());
            existingProject.setFiles(updatedProject.getFiles());
            existingProject.setDescription(updatedProject.getDescription());

            // You might want to perform additional validation or error handling here

            return ResponseEntity.ok(existingProject);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable int projectId) {
        boolean removed = bm.removeProject(projectId);
        if (removed) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

