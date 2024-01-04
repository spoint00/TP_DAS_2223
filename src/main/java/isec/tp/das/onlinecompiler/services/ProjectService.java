package isec.tp.das.onlinecompiler.services;

import isec.tp.das.onlinecompiler.models.FileEntity;
import isec.tp.das.onlinecompiler.models.ProjectEntity;
import isec.tp.das.onlinecompiler.repository.ProjectRepository;
import isec.tp.das.onlinecompiler.repository.ResultEntityRepository;
import isec.tp.das.onlinecompiler.services.factories.ProjectEntityFactory;
import isec.tp.das.onlinecompiler.services.factories.ResultEntityFactory;
import isec.tp.das.onlinecompiler.util.BUILDSTATUS;
import isec.tp.das.onlinecompiler.util.Helper;
import isec.tp.das.onlinecompiler.models.ResultEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public ProjectService(ProjectRepository projectRepository, ResultEntityRepository resultRepository, ProjectEntityFactory projectFactory, ResultEntityFactory resultFactory) {
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

    //TODO: usar uma factory para o Result
    //NOTE: Result provavelmente vai sofrer alteracoes
    public ResultEntity compileProject(Long projectId) throws IOException, InterruptedException {
        ProjectEntity project = getProjectById(projectId);
        if (project != null) {
            return startCompilation(project);
        } else {
            ResultEntity result = resultFactory.createResultEntity(false, "Project Not Found", null);
            return resultRepository.save(result);
        }
    }

    //colocar compilacao a correr numa thread?
    private ResultEntity startCompilation(ProjectEntity project) throws IOException, InterruptedException {
        if (project.getBuildStatus() != IN_QUEUE) {
            ResultEntity result = resultFactory.createResultEntity(false, "Project not in queue.", null);
            return resultRepository.save(result);
        }

        String projectName = project.getName().replace(" ", "_");
        Path exePath = Paths.get("./temp").resolve(projectName).resolve(projectName);
        List<String> filesPaths = Helper.getFilesPathsAsStrings(projectName, project.getCodeFiles());

        if (filesPaths.isEmpty()) {
            ResultEntity result = resultFactory.createResultEntity(false, "No source files to compile.", null);
            return resultRepository.save(result);
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

        if (exitCode == 0) {
            updateProjectBuildStatus(project, SUCCESS_BUILD);
            bm.compilationCompleted(project);

            String successMessage = "Compilation successful.";
            if (!output.isEmpty()) {
                successMessage += "\nOutput:\n" + output;
            }
            // Cleanup temporary files
            cleanupTempFiles(Paths.get("./temp").resolve(projectName));
            ResultEntity result = resultFactory.createResultEntity(true, successMessage, null);
            return resultRepository.save(result);
        } else {
            updateProjectBuildStatus(project, FAILURE_BUILD);
            bm.compilationCompleted(project);

            String failureMessage = "Compilation failed. Exit code: " + exitCode;
            if (!output.isEmpty()) {
                failureMessage += "\nOutput:\n" + output;
            }
            // Cleanup temporary files
            cleanupTempFiles(Paths.get("./temp").resolve(projectName));
            ResultEntity result = resultFactory.createResultEntity(false, failureMessage, null);
            return resultRepository.save(result);
        }
    }

    private void cleanupTempFiles(Path tempDirectoryPath) {
        try {
            Files.walk(tempDirectoryPath)
                    .filter(path -> !path.toString().endsWith(".exe"))
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            // Log the exception or handle it as per your requirement
            System.err.println("Error occurred while deleting temp files: " + e.getMessage());
        }
    }

    public ResultEntity runProject(Long projectId) throws IOException, InterruptedException {
        ProjectEntity project = getProjectById(projectId);
        if(project == null){
            ResultEntity result = resultFactory.createResultEntity(false, "Project is null", null);
            return resultRepository.save(result);

        }
        if (project.getBuildStatus() != SUCCESS_BUILD ) {
            ResultEntity result = resultFactory.createResultEntity(false, "Project not in queue.", null);
            return resultRepository.save(result);
        }

        // replace whitespaces with underscore
        String projectName = project.getName().replace(" ", "_");
        Path exePath = Paths.get("./temp").resolve(projectName).resolve(projectName);

        ProcessBuilder runnerProcessBuilder = new ProcessBuilder(exePath.toString());

        // redirect the error stream to be able to read the output and/or the error
        runnerProcessBuilder.redirectErrorStream(true);
        Process runnerProcess = runnerProcessBuilder.start();

        int exitCode = runnerProcess.waitFor();

        // read the output from the process
        String output = readProcessOutput(runnerProcess);

        if (exitCode == 0) {
            updateProjectBuildStatus(project, SUCCESS_RUN);
            String successMessage = "Run successful.";
            if (!output.isEmpty()) {
                successMessage += "\nOutput:\n" + output;
            }
            ResultEntity result = resultFactory.createResultEntity(true, successMessage, output);
            Optional<ResultEntity> resultEntityOptional = resultRepository.findById(projectId);
            if (resultEntityOptional.isEmpty()) {
                String failureMessage = "Run failed. Exit code: " + exitCode;
                ResultEntity returnSmthing = resultFactory.createResultEntity(false, failureMessage, null);
                return resultRepository.save(returnSmthing);
            }else{
                String success = "Run successful.";
                result.setMessage(success);
            }
            return resultRepository.save(result);
        } else {
            updateProjectBuildStatus(project, FAILURE_RUN);
            String failureMessage = "Run failed. Exit code: " + exitCode;
            if (!output.isEmpty()) {
                failureMessage += "\nOutput:\n" + output;
            }
            return resultFactory.createResultEntity(false, failureMessage, null);

        }
    }

    // update status and save in the db
    private void updateProjectBuildStatus(ProjectEntity project, BUILDSTATUS buildstatus) {
        project.setBuildStatus(buildstatus);
        projectRepository.save(project);
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

    //    public ProjectEntity getResults(Long projectId) {
//        Optional<ProjectEntity> existingProjectOptional = projectRepository.findById(projectId);
//        if (existingProjectOptional.isPresent()) {
//            ProjectEntity existingProject = existingProjectOptional.get();
//            //vais buscar status e output
//            return projectRepository.save(existingProject);
//        } else {
//            return null;
//        }
//    }
}


