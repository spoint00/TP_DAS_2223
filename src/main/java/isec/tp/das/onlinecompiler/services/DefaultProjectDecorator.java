package isec.tp.das.onlinecompiler.services;

import isec.tp.das.onlinecompiler.models.ProjectEntity;
import isec.tp.das.onlinecompiler.models.ResultEntity;
import isec.tp.das.onlinecompiler.util.Helper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
        if (result != null) {
            if(project.isSaveOutput()){
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
        return projectService.updateProject(projectId,name, description, files);
    }

    @Override
    public ProjectEntity patchProject(Long projectId, String name, String description, List<MultipartFile> files) throws IOException {
        return projectService.patchProject(projectId,name, description, files);
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
    public ResultEntity compileProject() throws IOException, InterruptedException {
        return projectService.compileProject();
    }

    @Override
    public boolean saveConfiguration(Long projectId, boolean change) {
        return projectService.saveConfiguration(projectId,change);
    }

    private void saveOutputToFile(String output, String projectName) throws IOException {
        String filename = projectName + ".txt";
        Path folderPath = Helper.tempPath.resolve(projectName);
        Path filePath = folderPath.resolve(filename);

        if (!Files.exists(folderPath)) {
            Files.createDirectories(folderPath);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write(output);
        } catch (IOException e) {
            throw new IOException("Failed to write output to file: " + filePath, e);
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
