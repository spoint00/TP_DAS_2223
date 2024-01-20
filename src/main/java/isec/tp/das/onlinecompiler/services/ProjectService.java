package isec.tp.das.onlinecompiler.services;

import isec.tp.das.onlinecompiler.models.ProjectEntity;
import isec.tp.das.onlinecompiler.models.ResultEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProjectService{
    ResultEntity runProject(Long projectId) throws IOException, InterruptedException;

    List<ProjectEntity> getAllProjects();

    ProjectEntity getProjectById(Long projectId);

    ProjectEntity createProject(String name, String description, List<MultipartFile> files) throws IOException;

    ProjectEntity updateProject(Long projectId, String name, String description, List<MultipartFile> files) throws IOException;

    ProjectEntity patchProject(Long projectId, String name, String description, List<MultipartFile> files) throws IOException;

    boolean deleteProject(Long projectId);

    ProjectEntity addToQueue(Long projectId);

    ProjectEntity removeFromQueue(Long projectId);

    ResultEntity compileProject() throws IOException, InterruptedException;

    boolean saveConfiguration(Long projectId, boolean change);

    boolean addListener();

    boolean removeListener(Long listenerId);
}
