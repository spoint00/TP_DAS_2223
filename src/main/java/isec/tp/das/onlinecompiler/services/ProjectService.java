package isec.tp.das.onlinecompiler.services;

import isec.tp.das.onlinecompiler.models.ProjectEntity;
import isec.tp.das.onlinecompiler.models.ResultEntity;
import isec.tp.das.onlinecompiler.util.BUILDSTATUS;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public interface ProjectService {
    List<ProjectEntity> getAllProjects();

    ProjectEntity getProjectById(Long projectId);

    ProjectEntity createProject(String name, String description, List<MultipartFile> files) throws IOException;

    ProjectEntity updateProject(Long projectId, String name, String description, List<MultipartFile> files) throws IOException;

    ProjectEntity patchProject(Long projectId, String name, String description, List<MultipartFile> files) throws IOException;

    boolean deleteProject(Long projectId);

    ProjectEntity addToQueue(Long projectId);

    ProjectEntity removeFromQueue(Long projectId);

    @Async("asyncExecutor")
    CompletableFuture<ResultEntity> compileProject(Long projectId, boolean checkQueue);

    ResultEntity runProject(Long projectId) throws IOException, InterruptedException;

    boolean saveConfiguration(Long projectId, boolean output);

    boolean addListener();

    boolean removeListener(Long listenerId);

    boolean cancelCompilation(Long projectId);

    BUILDSTATUS checkStatus(Long projectId);

    List<String> checkQueue();

    List<String> listCompiling();

    void scheduleBuild(Long projectId, long initialDelay, TimeUnit unit);

}
