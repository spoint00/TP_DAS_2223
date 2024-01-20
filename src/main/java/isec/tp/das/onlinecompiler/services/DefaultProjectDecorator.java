package isec.tp.das.onlinecompiler.services;

import isec.tp.das.onlinecompiler.models.ProjectEntity;
import isec.tp.das.onlinecompiler.models.ResultEntity;
import isec.tp.das.onlinecompiler.util.Helper;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class DefaultProjectDecorator implements ProjectDecorator {
    private final ProjectService projectService;

    public DefaultProjectDecorator(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public ResultEntity runProject(Long projectId) throws IOException, InterruptedException {
        ResultEntity result = projectService.runProject(projectId);
        ProjectEntity project = getProjectById(projectId);
        if (project != null) {
            if (project.isSaveOutput()) {
                saveOutputToFile(result.getOutput(), project.getName());
            }
        }
        return result;
    }

    @Override
    public List<ProjectEntity> getAllProjects() {
        return projectService.getAllProjects();
    }

    @Override
    public ProjectEntity getProjectById(Long projectId) {
        return projectService.getProjectById(projectId);

    }

    @Override
    public ProjectEntity createProject(String name, String description, List<MultipartFile> files) throws IOException {
        return projectService.createProject(name, description, files);
    }

    @Override
    public ProjectEntity updateProject(Long projectId, String name, String description, List<MultipartFile> files) throws IOException {
        return projectService.updateProject(projectId, name, description, files);
    }

    @Override
    public ProjectEntity patchProject(Long projectId, String name, String description, List<MultipartFile> files) throws IOException {
        return projectService.patchProject(projectId, name, description, files);
    }

    @Override
    public boolean deleteProject(Long projectId) {
        return projectService.deleteProject(projectId);
    }

    @Override
    public ProjectEntity addToQueue(Long projectId) {
        return projectService.addToQueue(projectId);
    }

    @Override
    public ProjectEntity removeFromQueue(Long projectId) {
        return projectService.removeFromQueue(projectId);
    }

    @Override
    @Async("asyncExecutor")
    @Transactional
    public CompletableFuture<ResultEntity> compileProject() throws IOException, InterruptedException {
        return projectService.compileProject();
    }

    @Override
    public boolean saveConfiguration(Long projectId, boolean output) {
        return projectService.saveConfiguration(projectId, output);
    }

    private void saveOutputToFile(String output, String projectName) {
        String pName = projectName.replace(" ", "_");
        String filename = pName + ".txt";
        Path folderPath = Helper.tempPath.resolve(pName);
        Path filePath = folderPath.resolve(filename);

        try {
            Files.createDirectories(folderPath);
            try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                writer.write(output);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean addListener() {
        return projectService.addListener();
    }

    @Override
    public boolean removeListener(Long listenerId) {
        return projectService.removeListener(listenerId);
    }

}
