package isec.tp.das.onlinecompiler.Resources;

import isec.tp.das.onlinecompiler.Models.Project;
import isec.tp.das.onlinecompiler.Repository.ProjectRepository;
import isec.tp.das.onlinecompiler.Services.BuildManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/projects")
public class ProjectRestController {

    private final ProjectRepository projectRepository;
    private final BuildManager bm;

    @Autowired
    public ProjectRestController(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
        this.bm = BuildManager.getInstance();
    }

    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file,
                                                   @RequestParam("name") String projectName,
                                                   @RequestParam("description") String projectDescription) {
        // Save the file to a storage location
//        String filePath = saveFile(file);
        String filePath = "";

        // Create a new project
        Project project = new Project();
        project.setName(projectName);
        project.setDescription(projectDescription);
        project.getFilePaths().add(filePath);

        // Save the project to the database
        projectRepository.save(project);

        // Use the BuildManager instance as needed

        return ResponseEntity.ok("File uploaded successfully!");
    }

    @GetMapping
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long projectId) {
        Optional<Project> projectOptional = projectRepository.findById(projectId);

        if (projectOptional.isPresent()) {
            Project foundProject = projectOptional.get();
            return ResponseEntity.ok(foundProject);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<String> updateProject(@PathVariable Long projectId,
                                                @RequestBody Project updatedProject) {
        if (!projectRepository.existsById(projectId)) {
            return ResponseEntity.notFound().build();
        }

        // Update the existing project
        updatedProject.setId(projectId);
        projectRepository.save(updatedProject);

        return ResponseEntity.ok("Project updated successfully!");
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<String> deleteProject(@PathVariable Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            return ResponseEntity.notFound().build();
        }

        // Delete the project
        projectRepository.deleteById(projectId);

        return ResponseEntity.ok("Project deleted successfully!");
    }

//    private String saveFile(MultipartFile file) {
//        // Logic to save the file to a specific location and return its path
//        // ...
//        return "path/to/saved/file";
//    }
}
