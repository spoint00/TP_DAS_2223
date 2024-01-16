package isec.tp.das.onlinecompiler.services;

import isec.tp.das.onlinecompiler.models.ProjectEntity;
import isec.tp.das.onlinecompiler.models.ResultEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Service
public class ProjectDecorator implements DecoratorInterface{
    public DecoratorInterface wrappedService;

    public ProjectDecorator(DecoratorInterface wrappedService) {
        this.wrappedService = wrappedService;
    }

    @Override
    public ResultEntity runProject(Long projectId) throws IOException, InterruptedException {
        ResultEntity result = wrappedService.runProject(projectId);
        if (result != null) {
            saveOutputToFile(result.getOutput());
        }

        return result;
    }

    @Override
    public List<ProjectEntity> getAllProjects() {
        return null;
    }

    @Override
    public ProjectEntity getProjectById(Long projectId) {
        return null;
    }

    @Override
    public ProjectEntity createProject(String name, String description, List<MultipartFile> files) throws IOException {
        return null;
    }

    @Override
    public ProjectEntity updateProject(Long projectId, String name, String description, List<MultipartFile> files) throws IOException {
        return null;
    }

    @Override
    public ProjectEntity patchProject(Long projectId, String name, String description, List<MultipartFile> files) throws IOException {
        return null;
    }

    @Override
    public boolean deleteProject(Long projectId) {
        return false;
    }

    @Override
    public ProjectEntity addToQueue(Long projectId) {
        return null;
    }

    @Override
    public ProjectEntity removeFromQueue(Long projectId) {
        return null;
    }

    @Override
    public ResultEntity compileProject() throws IOException, InterruptedException {
        return null;
    }

    @Override
    public boolean saveConfiguration(Long projectId, boolean change) {
        return false;
    }

    private void saveOutputToFile(String output) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("project_output.txt", true))) {
            writer.write(output);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
