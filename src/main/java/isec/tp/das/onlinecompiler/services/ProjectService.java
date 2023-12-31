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
import java.util.ArrayList;
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
        Optional<ProjectEntity> existingProjectOptional = projectRepository.findById(projectId);

        if (existingProjectOptional.isPresent()) {
            ProjectEntity existingProject = existingProjectOptional.get();
            existingProject.setName(name);
            if (description != null) {
                existingProject.setDescription(description);
            }
            return projectRepository.save(existingProject);
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
        Optional<ProjectEntity> existingProjectOptional = projectRepository.findById(projectId);
        if (existingProjectOptional.isPresent()) {
            ProjectEntity existingProject = existingProjectOptional.get();
            bm.addProject(existingProject);
            return projectRepository.save(existingProject);
        } else {
            return null;
        }
    }

    public ProjectEntity removeFromQueue(Long projectId) {
        Optional<ProjectEntity> existingProjectOptional = projectRepository.findById(projectId);
        if (existingProjectOptional.isPresent()) {
            ProjectEntity existingProject = existingProjectOptional.get();
            bm.abortProject(existingProject);
            return projectRepository.save(existingProject);
        } else {
            return null;
        }
    }

    public Result compileProject(Long projectId) throws IOException, InterruptedException {
        Optional<ProjectEntity> existingProjectOptional = projectRepository.findById(projectId);
        if (existingProjectOptional.isPresent()) {
            ProjectEntity existingProject = existingProjectOptional.get();

            return startCompilation(existingProject);
        } else {
            return new Result(false, "Project Not Found");
        }
    }

    public ProjectEntity getResults(Long projectId) {
        Optional<ProjectEntity> existingProjectOptional = projectRepository.findById(projectId);
        if (existingProjectOptional.isPresent()) {
            ProjectEntity existingProject = existingProjectOptional.get();
            //vais buscar status e output
            return projectRepository.save(existingProject);
        } else {
            return null;
        }
    }

    // TODO: fazer um metodo para no final remover os ficheiros da pasta temp
    // name of the file must have extension (.c or .cpp) otherwise gives error
    //TODO: adicionar verificaçao: correr apenas projetos que estejam com status in_queue
    private Result startCompilation(ProjectEntity project) throws IOException, InterruptedException {
        List<String> filesPaths = getFilesPaths(project.getName(), project.getCodeFiles());
        if (filesPaths.isEmpty())
            return new Result(false, "No source files to compile.");

        updateProjectBuildStatus(project, IN_PROGRESS);
        ProcessBuilder compilerProcessBuilder = new ProcessBuilder("g++", "-o", project.getName());
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

    private static List<String> getFilesPaths(String projectName, List<FileEntity> files) {
        List<String> filesPaths = new ArrayList<>();

        for (FileEntity file : files) {
            String path = Helper.convertToFile(projectName, file.getName(), file.getContent());
            if (path != null && !file.getName().endsWith(".h"))
                filesPaths.add(path);
        }
        return filesPaths;
    }


    // update status and save in the db
    private void updateProjectBuildStatus(ProjectEntity project, BUILDSTATUS buildstatus) {
        project.setBuildStatus(buildstatus);
        projectRepository.save(project);
    }


    // Run the compiled program
//            ProcessBuilder runBuilder = new ProcessBuilder("./" + executable);
//            runBuilder.redirectErrorStream(true);
//            Process runProcess = runBuilder.start();
//
//            BufferedReader reader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
//            String line;
//            while ((line = reader.readLine()) != null) {
//                finalOutput.append(line).append(System.lineSeparator());
//            }
//            int exitCode = runProcess.waitFor();
//            if (exitCode != 0) {
//                finalOutput.append("Execution of ").append(sourceFile).append(" failed.").append(System.lineSeparator());
//            }
}


