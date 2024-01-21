package isec.tp.das.onlinecompiler.controllers;

import isec.tp.das.onlinecompiler.models.ProjectEntity;
import isec.tp.das.onlinecompiler.models.ResultEntity;
import isec.tp.das.onlinecompiler.services.ProjectDecorator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
        return projectDecorator.getAllProjects();
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectEntity> getProjectById(@PathVariable Long projectId) {
        ProjectEntity project = projectDecorator.getProjectById(projectId);

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
            ProjectEntity project = projectDecorator.createProject(name, description, files);

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
            @RequestParam("files") List<MultipartFile> files) {
        try {
            ProjectEntity project = projectDecorator.updateProject(projectId, name, description, files);

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
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        try {
            ProjectEntity project = projectDecorator.patchProject(projectId, name, description, files);
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
        if (projectDecorator.deleteProject(projectId)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{projectId}/addToQueue")
    public ResponseEntity<ProjectEntity> addToQueue(@PathVariable Long projectId) {
        ProjectEntity project = projectDecorator.addToQueue(projectId);
        if (project != null) {
            return ResponseEntity.ok(project);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{projectId}/removeFromQueue")
    public ResponseEntity<ProjectEntity> removeFromQueue(@PathVariable Long projectId) {
        ProjectEntity project = projectDecorator.removeFromQueue(projectId);
        if (project != null) {
            return ResponseEntity.ok(project);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/compile")
    public CompletableFuture<ResponseEntity<String>> compile() throws IOException, InterruptedException {
        CompletableFuture<ResultEntity> completableFuture = projectDecorator.compileProject();

        return completableFuture.thenApply(resultEntity -> {
            String response = resultEntity.getMessage() + "\n" + resultEntity.getOutput();
            if (resultEntity.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }).exceptionally(ex -> {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during compilation: " + ex.getMessage());
        });
    }

    @PostMapping("/{projectId}/run")
    public ResponseEntity<String> run(@PathVariable Long projectId) {
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

    //TODO apagar ficheiros do temp?
    @PostMapping("/{projectId}/saveOutput")
    public ResponseEntity<String> saveOuput(@PathVariable Long projectId, @RequestParam boolean output) {
        boolean saveOutput = projectDecorator.saveConfiguration(projectId, output);
        if (saveOutput) {
            return ResponseEntity.status(HttpStatus.OK).body("updated with success");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }

    @PostMapping("/addListener")
    public ResponseEntity<String> addListener() {
        boolean result = projectDecorator.addListener();
        if (result) {
            return ResponseEntity.status(HttpStatus.OK).body("Listener added");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }

    @PostMapping("/{listenerId}/removeListener")
    public ResponseEntity<String> removeListener(@PathVariable Long listenerId) {
        boolean result = projectDecorator.removeListener(listenerId);
        if (result) {
            return ResponseEntity.status(HttpStatus.OK).body("Listener removed");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }

    //TODO
    @PostMapping("/{projectId}/cancelBuild")
    public ResponseEntity<String> cancelBuild(@PathVariable Long projectId) {
//        String result = projectDecorator.cancelBuild(projectId);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("WIP");
    }

    //TODO
    @PostMapping("/{projectId}/checkStatus")
    public ResponseEntity<String> checkStatus(@PathVariable Long projectId) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("WIP");
    }
}
