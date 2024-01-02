package isec.tp.das.onlinecompiler.services;

import isec.tp.das.onlinecompiler.models.BuildManager;
import isec.tp.das.onlinecompiler.models.FileEntity;
import isec.tp.das.onlinecompiler.models.ProjectEntity;
import isec.tp.das.onlinecompiler.repository.ProjectRepository;
import isec.tp.das.onlinecompiler.services.factories.ProjectEntityFactory;
import isec.tp.das.onlinecompiler.util.BUILDSTATUS;
import isec.tp.das.onlinecompiler.util.Helper;
import isec.tp.das.onlinecompiler.util.Result;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static isec.tp.das.onlinecompiler.util.BUILDSTATUS.*;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectEntityFactory factory;

    private final BuildManager bm;

    public ProjectService(ProjectRepository projectRepository, ProjectEntityFactory factory) {
        this.bm = BuildManager.getInstance();
        this.projectRepository = projectRepository;
        this.factory = factory;
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

        ProjectEntity project = factory.createProjectEntity(name, description, fileEntities);

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
    public Result compileProject(Long projectId) throws IOException, InterruptedException {
        ProjectEntity project = getProjectById(projectId);
        if (project != null) {
            return startCompilation(project);
        } else {
            return new Result(false, "Project Not Found");
        }
    }

    // colocar compilacao a correr numa thread?
    private Result startCompilation(ProjectEntity project) throws IOException, InterruptedException {
        if (project.getBuildStatus() != IN_QUEUE) {
            return new Result(false, "Project not in queue.");
        }

        // replace whitespaces with underscore
        String projectName = project.getName().replace(" ", "_");
        Path exePath = Paths.get("./temp").resolve(projectName).resolve(projectName);
        List<String> filesPaths = Helper.getFilesPathsAsStrings(projectName, project.getCodeFiles());

        if (filesPaths.isEmpty())
            return new Result(false, "No source files to compile.");

        updateProjectBuildStatus(project, IN_PROGRESS);
        ProcessBuilder compilerProcessBuilder = new ProcessBuilder("g++", "-o", exePath.toString());
        compilerProcessBuilder.command().addAll(filesPaths);
        Process compilerProcess = compilerProcessBuilder.start();

        int exitCode = compilerProcess.waitFor();
        if (exitCode == 0) {
            updateProjectBuildStatus(project, SUCCESS_BUILD);
            bm.compilationCompleted(project);
            return new Result(true, "Compilation successful");
        } else {
            updateProjectBuildStatus(project, FAILURE_BUILD);
            bm.compilationCompleted(project);
            return new Result(false, "Compilation failed. Exit code: " + exitCode);
        }
    }

    public Result runProject(Long projectId) throws IOException, InterruptedException {
        ProjectEntity project = getProjectById(projectId);

        if (project == null)
            return null;

        // replace whitespaces with underscore
        String projectName = project.getName().replace(" ", "_");
        Path exePath = Paths.get("./temp").resolve(projectName).resolve(projectName);

        ProcessBuilder runnerProcessBuilder = new ProcessBuilder(exePath.toString());

        // redirect the standard output and error to capture the output
        runnerProcessBuilder.redirectErrorStream(true);
        Process runnerProcess = runnerProcessBuilder.start();

        int exitCode = runnerProcess.waitFor();

        // read the output from the process
        String output = readProcessOutput(runnerProcess);

        if (exitCode == 0) {
            updateProjectBuildStatus(project, SUCCESS_RUN);
            return new Result(true, "Run successful. Output:\n" + output);
        } else {
            updateProjectBuildStatus(project, FAILURE_RUN);
            return new Result(false, "Run failed. Exit code: " + exitCode + "\nOutput:\n" + output);
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


