package isec.tp.das.onlinecompiler.services;

import isec.tp.das.onlinecompiler.models.FileEntity;
import isec.tp.das.onlinecompiler.models.ProjectEntity;
import isec.tp.das.onlinecompiler.models.ResultEntity;
import isec.tp.das.onlinecompiler.repository.ProjectRepository;
import isec.tp.das.onlinecompiler.repository.ResultEntityRepository;
import isec.tp.das.onlinecompiler.services.factories.ProjectEntityFactory;
import isec.tp.das.onlinecompiler.services.factories.ResultEntityFactory;
import isec.tp.das.onlinecompiler.util.BUILDSTATUS;
import isec.tp.das.onlinecompiler.util.Helper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static isec.tp.das.onlinecompiler.util.BUILDSTATUS.*;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ResultEntityRepository resultRepository;

    private final ProjectEntityFactory projectFactory;
    private final ResultEntityFactory resultFactory;

    private final BuildManager bm;

    public ProjectService(ProjectRepository projectRepository,
                          ResultEntityRepository resultRepository,
                          ProjectEntityFactory projectFactory,
                          ResultEntityFactory resultFactory) {
        this.resultRepository = resultRepository;
        this.projectRepository = projectRepository;
        this.bm = BuildManager.getInstance();
        this.projectFactory = projectFactory;
        this.resultFactory = resultFactory;
    }

    public List<ProjectEntity> getAllProjects() {
        return projectRepository.findAll();
    }

    public ProjectEntity getProjectById(Long projectId) {
        Optional<ProjectEntity> projectOptional = projectRepository.findById(projectId);

        // return project or null
        return projectOptional.orElse(null);
    }

    public ProjectEntity createProject(String name, String description, List<MultipartFile> files) throws IOException {
        List<FileEntity> fileEntities = Helper.createFileEntities(files);
        ResultEntity resultEntity = resultFactory.createResultEntity();
        ProjectEntity project = projectFactory.createProjectEntity(name, description, fileEntities, resultEntity);

        return projectRepository.save(project);
    }

    public ProjectEntity updateProject(Long projectId, String name, String description) {
        ProjectEntity project = getProjectById(projectId);

        if (project != null) {
            project.setName(name);
            if (description != null) {
                project.setDescription(description);
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
        ProjectEntity project = getProjectById(projectId);

        if (project != null) {
            bm.addProject(project);
            return projectRepository.save(project);
        } else {
            return null;
        }
    }

    public ProjectEntity removeFromQueue(Long projectId) {
        ProjectEntity project = getProjectById(projectId);

        if (project != null) {
            bm.abortProject(project);
            return projectRepository.save(project);
        } else {
            return null;
        }
    }

    public ResultEntity compileProject() throws IOException, InterruptedException {
        ProjectEntity nextProject = bm.processNextProject();
        if (nextProject == null){
            return resultFactory.createResultEntity(false, Helper.queueIsEmpty, Helper.noOutput);
        }

        Long nextProjectID = nextProject.getId();
        ProjectEntity projectEntity = getProjectById(nextProjectID);
        if (projectEntity == null) {
            return resultFactory.createResultEntity(false, Helper.projectNotFound, Helper.noOutput);
        } else {
            if (projectEntity.getBuildStatus() != IN_QUEUE) {
                return resultFactory.createResultEntity(false, Helper.projectNotInQueue, Helper.noOutput);
            }

            return startCompilation(projectEntity);
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

    public ResultEntity runProject(Long projectId) throws IOException, InterruptedException {
        ProjectEntity project = getProjectById(projectId);
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
}


