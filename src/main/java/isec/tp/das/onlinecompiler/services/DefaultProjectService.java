package isec.tp.das.onlinecompiler.services;

import isec.tp.das.onlinecompiler.models.FileEntity;
import isec.tp.das.onlinecompiler.models.ProjectEntity;
import isec.tp.das.onlinecompiler.models.ResultEntity;
import isec.tp.das.onlinecompiler.repository.ProjectRepository;
import isec.tp.das.onlinecompiler.services.factories.ProjectEntityFactory;
import isec.tp.das.onlinecompiler.services.factories.ResultEntityFactory;
import isec.tp.das.onlinecompiler.util.BUILDSTATUS;
import isec.tp.das.onlinecompiler.util.Helper;
import isec.tp.das.onlinecompiler.util.LANGUAGE;
import jakarta.annotation.PreDestroy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static isec.tp.das.onlinecompiler.util.BUILDSTATUS.*;

public class DefaultProjectService implements ProjectService {
    private final ProjectRepository projectRepository;

    private final ProjectEntityFactory projectFactory;
    private final ResultEntityFactory resultFactory;
    private final BuildManager bm;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


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

        if (fileEntities.isEmpty())
            return null;

        LANGUAGE language = determineLanguage(fileEntities);
        ResultEntity resultEntity = resultFactory.createResultEntity();
        ProjectEntity project = projectFactory.createProjectEntity(name, description, fileEntities, resultEntity, language);

        return projectRepository.save(project);
    }

    private LANGUAGE determineLanguage(List<FileEntity> fileEntities) {
        if (fileEntities.stream().anyMatch(fileEntity -> fileEntity.getName().endsWith(".c"))) {
            return LANGUAGE.C;
        } else if (fileEntities.stream().anyMatch(fileEntity -> fileEntity.getName().endsWith(".cpp"))) {
            return LANGUAGE.CPP;
        } else if (fileEntities.stream().anyMatch(fileEntity -> fileEntity.getName().endsWith(".py"))) {
            return LANGUAGE.PYTHON;
        } else if (fileEntities.stream().anyMatch(fileEntity -> fileEntity.getName().endsWith(".java"))) {
            return LANGUAGE.JAVA;
        } else {
            return LANGUAGE.UNSUPPORTED_LANGUAGE;
        }
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
    public CompletableFuture<ResultEntity> compileProject(Long projectId, boolean checkQueue) {
        CompletableFuture<ResultEntity> future = new CompletableFuture<>();
        Thread compilationThread = new Thread(() -> {
            try {
                ProjectEntity project;
                if (checkQueue) {
                    project = bm.processNextProject();
                    // queue is empty
                    if (project == null) {
                        ResultEntity result = resultFactory.createResultEntity(false, Helper.queueIsEmpty, Helper.noOutput);
                        bm.notifyBuildCompleted(null, result);

                        future.complete(result);
                        return;
                    }
                } else {
                    project = projectRepository.findById(projectId).orElse(null);
                }

                // project not found
                if (project == null) {
                    ResultEntity result = resultFactory.createResultEntity(false, Helper.projectNotFound, Helper.noOutput);
                    bm.notifyBuildCompleted(null, result);

                    future.complete(result);
                } else {
                    if (checkQueue) {
                        // project not in queue
                        if (project.getBuildStatus() != IN_QUEUE) {
                            ResultEntity result = resultFactory.createResultEntity(false, Helper.projectNotInQueue, Helper.noOutput);
                            bm.notifyBuildCompleted(project, result);

                            future.complete(result);
                            return;
                        }
                    }
                    updateProjectBuildStatus(project, COMPILATION_IN_PROGRESS);

                    bm.addThread(project.getId(), Thread.currentThread());
                    // only for TESTING
                    //Thread.sleep(5000);
                    ResultEntity result = startCompilation(project);
                    future.complete(result);
                    compilationFinished(project, result);
                }
            } catch (IOException | InterruptedException e) {
                future.completeExceptionally(e);
            }
        });
        compilationThread.start();
        // return completable future reference after starting the thread
        return future;
    }

    private ResultEntity startCompilation(ProjectEntity project) throws IOException, InterruptedException {
        String projectName = project.getName().replace(" ", "_");
        Path projectFolder = Helper.tempPath.resolve(projectName);
        Path exePath = projectFolder.resolve(projectName);
        List<String> filesPaths = Helper.getFilesPathsAsStrings(projectName, project.getCodeFiles());

        if (filesPaths.isEmpty()) {
            updateProjectBuildStatus(project, FAILURE_BUILD);
            return updateProjectResult(project, false, Helper.noFilesToCompile, Helper.noOutput);
        }

        ProcessBuilder compilerProcessBuilder = createCompilerProcessBuilder(project.getLanguage(), exePath, filesPaths);
        if (compilerProcessBuilder == null) {
            return updateProjectResult(project, false, LANGUAGE.UNSUPPORTED_LANGUAGE.name().toLowerCase(), Helper.noOutput);
        }

        ResultEntity result = executeProcess(project, compilerProcessBuilder, SUCCESS_BUILD, FAILURE_BUILD,
                "Compilation successful for project id = " + project.getId(),
                "Compilation failed for project id = " + project.getId());
        Helper.cleanupTempFiles(Helper.tempPath.resolve(projectName));

        return result;
    }

    private ProcessBuilder createCompilerProcessBuilder(LANGUAGE language, Path exePath, List<String> filesPaths) {
        ProcessBuilder compilerProcessBuilder;

        switch (language) {
            case C:
                compilerProcessBuilder = new ProcessBuilder("gcc", "-o", exePath.toString());
                break;
            case CPP:
                compilerProcessBuilder = new ProcessBuilder("g++", "-o", exePath.toString());
                break;
            case JAVA:
                exePath = Path.of(filesPaths.getFirst());
                compilerProcessBuilder = new ProcessBuilder("javac", exePath.toString());
                break;
            default:
                return null;
        }

        compilerProcessBuilder.command().addAll(filesPaths);
        // redirect the error stream to be able to read the output and/or the error
        compilerProcessBuilder.redirectErrorStream(true);

        return compilerProcessBuilder;
    }

    private ResultEntity executeProcess(ProjectEntity project, ProcessBuilder processBuilder,
                                        BUILDSTATUS successBuildStatus, BUILDSTATUS failureBuildStatus,
                                        String successMessage, String failureMessage) throws IOException, InterruptedException {
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        String output = readProcessOutput(process);

        if (output.isBlank()) {
            output = Helper.noOutput;
        }

        if (exitCode == 0) {
            updateProjectBuildStatus(project, successBuildStatus);
            return updateProjectResult(project, true, successMessage, output);
        } else {
            updateProjectBuildStatus(project, failureBuildStatus);
            return updateProjectResult(project, false, failureMessage + ". Exit code: " + exitCode, output);
        }
    }


    private void compilationFinished(ProjectEntity project, ResultEntity result) {
        bm.compilationCompleted(project);
        bm.removeThread(project.getId());
        bm.notifyBuildCompleted(project, result);
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

    @Override
    public ResultEntity runProject(Long projectId) throws IOException, InterruptedException {
        ProjectEntity project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return resultFactory.createResultEntity(false, Helper.projectNotFound, Helper.noOutput);
        }

        if (project.getBuildStatus() != SUCCESS_BUILD) {
            return updateProjectResult(project, false, Helper.projectNotCompiled, Helper.noOutput);
        }

        ProcessBuilder runnerProcessBuilder = createRunnerProcessBuilder(project);
        if (runnerProcessBuilder == null) {
            return updateProjectResult(project, false, LANGUAGE.UNSUPPORTED_LANGUAGE.name().toLowerCase(), Helper.noOutput);
        }

        return executeProcess(project, runnerProcessBuilder, SUCCESS_RUN, FAILURE_RUN, "Run successful", "Run failed");
    }

    private ProcessBuilder createRunnerProcessBuilder(ProjectEntity project) {
        // replace whitespaces with underscore
        String projectName = project.getName().replace(" ", "_");
        Path projectFolder = Helper.tempPath.resolve(projectName);
        ProcessBuilder runnerProcessBuilder;

        switch (project.getLanguage()) {
            case C:
            case CPP:
                Path exePath = projectFolder.resolve(projectName);
                runnerProcessBuilder = new ProcessBuilder(exePath.toString());
                break;
            case JAVA:
                String exe = project.getCodeFiles().getFirst().getName().replace(".java", "");
                runnerProcessBuilder = new ProcessBuilder("java", exe);
                runnerProcessBuilder.directory(projectFolder.toFile());
                break;
            default:
                return null;
        }
        // redirect the error stream to be able to read the output and/or the error
        runnerProcessBuilder.redirectErrorStream(true);
        return runnerProcessBuilder;
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
    public boolean cancelCompilation(Long projectId) {
        boolean canceled = bm.cancelCompilation(projectId);

        if (canceled) {
            projectRepository.findById(projectId).ifPresent(project -> updateProjectBuildStatus(project, CANCELED));
        }
        return canceled;
    }

    @Override
    public BUILDSTATUS checkStatus(Long projectId) {
        ProjectEntity project = projectRepository.findById(projectId).orElse(null);

        if (project == null)
            return null;

        return project.getBuildStatus();
    }

    @Override
    public List<String> checkQueue() {
        List<ProjectEntity> projectsList = bm.getProjectQueue();
        List<String> projectIds = new LinkedList<>();

        for (ProjectEntity project : projectsList) {
            projectIds.add("Project Id: " + project.getId());
        }

        return projectIds;
    }

    @Override
    public List<String> listCompiling() {
        Map<Long, Thread> projectsList = bm.getCompilationThreads();
        List<String> projectIds = new LinkedList<>();
        Long[] ids = projectsList.keySet().toArray(new Long[0]);
        for (Long id : ids) {
            projectIds.add("Project Id: " + id);
        }

        return projectIds;
    }

    @Async("asyncExecutor")
    public void scheduleBuild(Long projectId, long initialDelay, TimeUnit unit) {
        Runnable buildTask = () -> {
            try {
                compileProject(projectId, false);
            } catch (Exception e) {
                System.err.println("Error in compileProject for project ID " + projectId + ": " + e.getMessage());
            }
        };
        scheduler.schedule(buildTask, initialDelay, unit);
        System.out.println("Scheduled a build for project ID " + projectId + " to start after " + initialDelay + " " + unit.toString().toLowerCase());
    }

    @PreDestroy
    public void shutDown() {
        scheduler.shutdown();
        Helper.deleteTempFolder();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}


