package isec.tp.das.onlinecompiler.services;

import isec.tp.das.onlinecompiler.models.FileEntity;
import isec.tp.das.onlinecompiler.models.ProjectEntity;
import isec.tp.das.onlinecompiler.models.ResultEntity;
import isec.tp.das.onlinecompiler.repository.ProjectRepository;
import isec.tp.das.onlinecompiler.services.factories.ProjectEntityFactory;
import isec.tp.das.onlinecompiler.services.factories.ResultEntityFactory;
import isec.tp.das.onlinecompiler.util.BUILDSTATUS;
import isec.tp.das.onlinecompiler.util.Helper;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import static isec.tp.das.onlinecompiler.util.BUILDSTATUS.*;

public class DefaultProjectService implements ProjectService{
    private final ProjectRepository projectRepository;

    private final ProjectEntityFactory projectFactory;
    private final ResultEntityFactory resultFactory;
    private final BuildManager bm;

    public DefaultProjectService(ProjectRepository projectRepository,
                                 ProjectEntityFactory projectFactory,
                                 ResultEntityFactory resultFactory) {
        this.projectRepository = projectRepository;
        this.bm = BuildManager.getInstance();
        this.projectFactory = projectFactory;
        this.resultFactory = resultFactory;
    }

    public List<ProjectEntity> getAllProjects() {
        return projectRepository.findAll();
    }

    public ProjectEntity getProjectById(Long projectId) {
        return projectRepository.findById(projectId).orElse(null);
    }

    public ProjectEntity createProject(String name, String description, List<MultipartFile> files) throws IOException {
        List<FileEntity> fileEntities = Helper.createFileEntities(files);
        ResultEntity resultEntity = resultFactory.createResultEntity();
        ProjectEntity project = projectFactory.createProjectEntity(name, description, fileEntities, resultEntity);

        return projectRepository.save(project);
    }

    public ProjectEntity updateProject(Long projectId, String name, String description, List<MultipartFile> files) throws IOException {
        ProjectEntity project = projectRepository.findById(projectId).orElse(null);

        if (project != null) {
            project.setName(name);
            if (description != null) {
                project.setDescription(description);
            }
            List<FileEntity> fileEntities = Helper.createFileEntities(files);
            project.getCodeFiles().clear();
            project.getCodeFiles().addAll(fileEntities);
            return projectRepository.save(project);
        } else {
            return null;
        }
    }

    public ProjectEntity patchProject(Long projectId, String name, String description, List<MultipartFile> files) throws IOException {
        ProjectEntity project = projectRepository.findById(projectId).orElse(null);

        if (project != null) {
            if (name != null)
                project.setName(name);
            if (description != null) {
                project.setDescription(description);
            }
            if (files != null) {
                List<FileEntity> fileEntities = Helper.createFileEntities(files);
                project.getCodeFiles().clear();
                project.getCodeFiles().addAll(fileEntities);
            }
            return projectRepository.save(project);
        } else {
            return null;
        }
    }

    public boolean deleteProject(Long projectId) {
        if (projectRepository.existsById(projectId)) {
            // (talvez?) ao fazer delete do projeto pode ser necessario mexer na lista do build manager
            projectRepository.deleteById(projectId);
            return true;
        } else {
            return false;
        }
    }

    public ProjectEntity addToQueue(Long projectId) {
        ProjectEntity project = projectRepository.findById(projectId).orElse(null);

        if (project != null) {
            bm.addProject(project);
            return projectRepository.save(project);
        } else {
            return null;
        }
    }

    public ProjectEntity removeFromQueue(Long projectId) {
        ProjectEntity project = projectRepository.findById(projectId).orElse(null);

        if (project != null) {
            bm.abortProject(project);
            return projectRepository.save(project);
        } else {
            return null;
        }
    }

    @Async("asyncExecutor")
    @Transactional
    public CompletableFuture<ResultEntity> compileProject() throws IOException, InterruptedException {
        ProjectEntity nextProject = bm.processNextProject();
        if (nextProject == null) {
            ResultEntity result = resultFactory.createResultEntity(false, Helper.queueIsEmpty, Helper.noOutput);
            bm.notifyBuildCompleted(null, result);
            return  CompletableFuture.completedFuture(result);
        }

        Long nextProjectID = nextProject.getId();
        ProjectEntity projectEntity = projectRepository.findById(nextProjectID).orElse(null);
        if (projectEntity == null) {
            ResultEntity result = resultFactory.createResultEntity(false, Helper.projectNotFound, Helper.noOutput);
            bm.notifyBuildCompleted(null, result);
            return  CompletableFuture.completedFuture(result);
        } else {
            if (projectEntity.getBuildStatus() != IN_QUEUE) {
                ResultEntity result = resultFactory.createResultEntity(false, Helper.projectNotInQueue, Helper.noOutput);
                bm.notifyBuildCompleted(projectEntity, result);
                return  CompletableFuture.completedFuture(result);
            }
            ResultEntity result = bm.callStartCompilation();
            bm.putBuildTasks(nextProjectID, buildTask);
            bm.notifyBuildCompleted(projectEntity, result);
            return  CompletableFuture.completedFuture(result);
        }
    }

    //colocar compilacao a correr numa thread?
    private ResultEntity startCompilation(ProjectEntity project) throws IOException, InterruptedException {
        String projectName = project.getName().replace(" ", "_");
        Path exePath = Helper.tempPath.resolve(projectName).resolve(projectName);
        List<String> filesPaths = Helper.getFilesPathsAsStrings(projectName, project.getCodeFiles());

        if (filesPaths.isEmpty()) {
            return updateProjectResult(project, false, Helper.noFilesToCompile, Helper.noOutput);
        }

        updateProjectBuildStatus(project, IN_PROGRESS);
        ProcessBuilder compilerProcessBuilder = new ProcessBuilder("g++", "-o", exePath.toString());
        compilerProcessBuilder.command().addAll(filesPaths);

        // redirect the error stream to be able to read the output and/or the error
        compilerProcessBuilder.redirectErrorStream(true);
        Process compilerProcess = compilerProcessBuilder.start();

        int exitCode = compilerProcess.waitFor();

        // read the output from the process
        String output = readProcessOutput(compilerProcess);
        if (output.isBlank())
            output = Helper.noOutput;

        bm.compilationCompleted(project);

        if (exitCode == 0) {
            String successMessage = "Compilation successful.";

            updateProjectBuildStatus(project, SUCCESS_BUILD);
            Helper.cleanupTempFiles(Helper.tempPath.resolve(projectName));

            return updateProjectResult(project, true, successMessage, output);
        } else {
            String failureMessage = "Compilation failed. Exit code: " + exitCode;

            updateProjectBuildStatus(project, FAILURE_BUILD);
            Helper.cleanupTempFiles(Helper.tempPath.resolve(projectName));

            return updateProjectResult(project, false, failureMessage, output);
        }
    }
    @Override
    public ResultEntity runProject(Long projectId) throws IOException, InterruptedException {
        ProjectEntity project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return resultFactory.createResultEntity(false, Helper.projectNotFound, Helper.noOutput);
        }

        if (project.getBuildStatus() != SUCCESS_BUILD) {
            return resultFactory.createResultEntity(false, Helper.projectNotCompiled, Helper.noOutput);
        }

        // replace whitespaces with underscore
        String projectName = project.getName().replace(" ", "_");
        Path exePath = Helper.tempPath.resolve(projectName).resolve(projectName);

        ProcessBuilder runnerProcessBuilder = new ProcessBuilder(exePath.toString());

        // redirect the error stream to be able to read the output and/or the error
        runnerProcessBuilder.redirectErrorStream(true);
        Process runnerProcess = runnerProcessBuilder.start();

        int exitCode = runnerProcess.waitFor();

        // read the output from the process
        String output = readProcessOutput(runnerProcess);
        if (output.isBlank())
            output = Helper.noOutput;

        if (exitCode == 0) {
            String successMessage = "Run successful.";

            updateProjectBuildStatus(project, SUCCESS_RUN);

            return updateProjectResult(project, true, successMessage, output);
        } else {
            String failureMessage = "Run failed. Exit code: " + exitCode;

            updateProjectBuildStatus(project, FAILURE_RUN);

            return updateProjectResult(project, false, failureMessage, output);
        }
    }

    // update status and save in the db
    private void updateProjectBuildStatus(ProjectEntity project, BUILDSTATUS buildstatus) {
        project.setBuildStatus(buildstatus);
        projectRepository.save(project);
    }

    // update project result and save in the db
    private ResultEntity updateProjectResult(ProjectEntity project, boolean success, String message, String output) {
        ResultEntity result = project.getResultEntity();

        result.setSuccess(success);
        result.setMessage(message);
        result.setOutput(output);

        projectRepository.save(project);

        return result;
    }

    // read the output from the process
    private String readProcessOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            return output.toString();
        }
    }

    public boolean saveConfiguration(Long projectId, boolean output) {
        ProjectEntity project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return false;
        }

        project.setSaveOutput(output);
        projectRepository.save(project);
        return true;
    }

    @Override
    public boolean addListener() {
        return bm.addBuildListener(new Observer());
    }

    @Override
    public boolean removeListener(Long listenerId) {
        return bm.removeBuildListener(listenerId);
    }

    @Override
    public String cancelBuild(Long projectId) {
        // Implementation to cancel the build
        Future<?> buildTask = buildTasks.get(projectId);
        if (buildTask != null && !buildTask.isDone()) {
            buildTask.cancel(true); // Attempt to cancel the build
            buildTasks.remove(projectId);
            return "Build cancelled for project " + projectId;
        }
        return "No active build to cancel for project " + projectId;
    }
}


