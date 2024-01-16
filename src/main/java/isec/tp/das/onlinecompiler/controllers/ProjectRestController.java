package isec.tp.das.onlinecompiler.controllers;

import isec.tp.das.onlinecompiler.models.ProjectEntity;
import isec.tp.das.onlinecompiler.services.ProjectDecorator;
import isec.tp.das.onlinecompiler.services.ProjectService;
import isec.tp.das.onlinecompiler.models.ResultEntity;
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

    private final ProjectDecorator projectDecorator;
    @Autowired
    public ProjectRestController(ProjectDecorator projectDecorator) {
        this.projectDecorator = projectDecorator;
    }

    @GetMapping
    public List<ProjectEntity> getAllProjects() {
        return projectDecorator.wrappedService.getAllProjects();
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectEntity> getProjectById(@PathVariable Long projectId) {
        ProjectEntity project = projectDecorator.wrappedService.getProjectById(projectId);

        if (project != null) {
            return ResponseEntity.ok(project);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // TODO: verificaçao: (no project e no file) permitir apenas guardar ficheiros com extensao .c, .cpp, .h
    // TODO: verificaçao: os projetos devem conter pelo menos 1 ficheiro .c ou .cpp
    @PostMapping
    public ResponseEntity<ProjectEntity> createProject(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("files") List<MultipartFile> files
    ) {
        try {
            ProjectEntity project = projectDecorator.wrappedService.createProject(name, description, files);

            return ResponseEntity.status(HttpStatus.CREATED).body(project);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    //Update Project (whole)
    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectEntity> updateProject(
            @PathVariable Long projectId,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("files") List<MultipartFile> files){
        try {
            ProjectEntity project = projectDecorator.wrappedService.updateProject(projectId,name, description, files);

            if (project != null) {
                return ResponseEntity.ok(project);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/{projectId}")
    public ResponseEntity<?> patchProject(
            @PathVariable Long projectId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value ="files",required = false) List<MultipartFile> files){
        try {
            ProjectEntity project = projectDecorator.wrappedService.patchProject(projectId,name, description, files);
            if (project != null) {
                return ResponseEntity.ok(project);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {
        if (projectDecorator.wrappedService.deleteProject(projectId)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{projectId}/addToQueue")
    public ResponseEntity<ProjectEntity> addToQueue(@PathVariable Long projectId){
        ProjectEntity project = projectDecorator.wrappedService.addToQueue(projectId);
        if (project != null) {
            return ResponseEntity.ok(project);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{projectId}/removeFromQueue")
    public ResponseEntity<ProjectEntity> removeFromQueue(@PathVariable Long projectId) {
        ProjectEntity project = projectDecorator.wrappedService.removeFromQueue(projectId);
        if (project != null) {
            return ResponseEntity.ok(project);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping("/compile")
    public ResponseEntity<String> compile() {
        try {
            ResultEntity compilationResult = projectDecorator.wrappedService.compileProject();
            String response = compilationResult.getMessage() + "\n" + compilationResult.getOutput();
            if (compilationResult.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during compilation: " + e.getMessage());
        }
    }

    @PostMapping("/{projectId}/run")
    public ResponseEntity<String> run(@PathVariable Long projectId){
        try {
            ResultEntity runResult = projectDecorator.runProject(projectId);
            String response = runResult.getMessage() + "\n" + runResult.getOutput();
            if (runResult.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error running the project: " + e.getMessage());
        }
    }

    @PostMapping("/{projectId}/saveOutput")
    public ResponseEntity<String> saveOuput(@PathVariable Long projectId,@RequestParam boolean change){
        boolean saveOutput = projectDecorator.wrappedService.saveConfiguration(projectId, change);
        if(saveOutput){
            return ResponseEntity.status(HttpStatus.OK).body("updated with success");
        }else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }
}
