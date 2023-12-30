package isec.tp.das.onlinecompiler.services;

import isec.tp.das.onlinecompiler.models.BuildManager;
import isec.tp.das.onlinecompiler.models.FileEntity;
import isec.tp.das.onlinecompiler.models.ProjectEntity;
import isec.tp.das.onlinecompiler.repository.ProjectRepository;
import isec.tp.das.onlinecompiler.services.factories.ProjectEntityFactory;
import isec.tp.das.onlinecompiler.util.Helper;
import isec.tp.das.onlinecompiler.util.Message;
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

        ProjectEntity project = factory.createProjectEntity(name,description,fileEntities);
        ProjectEntity savedProject = projectRepository.save(project);

        return savedProject;
    }

    public ProjectEntity updateProject(Long projectId,String name, String description) {
        Optional<ProjectEntity> existingProjectOptional = projectRepository.findById(projectId);
        if (existingProjectOptional.isPresent()) {
            ProjectEntity existingProject = existingProjectOptional.get();
            existingProject.setName(name);
            if(description!=null){
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
            bm.removeProject(existingProject);
            return  projectRepository.save(existingProject);
        } else {
            return null;
        }
    }

    public Message compile(Long projectId) throws IOException {
        Optional<ProjectEntity> existingProjectOptional = projectRepository.findById(projectId);
        if (existingProjectOptional.isPresent()) {
            ProjectEntity existingProject = existingProjectOptional.get();

            return compileProject(existingProject);
        } else {
            return new Message("Project Not Found", false);
        }
    }

    public ProjectEntity getResults(Long projectId) {
        Optional<ProjectEntity> existingProjectOptional = projectRepository.findById(projectId);
        if (existingProjectOptional.isPresent()) {
            ProjectEntity existingProject = existingProjectOptional.get();
            //vais buscar status e output
            return  projectRepository.save(existingProject);
        } else {
            return null;
        }
    }

    //    // Method to compile and execute files listed one per line in a text area
    // endpoint para queue order

    public Message compileProject(ProjectEntity project) throws IOException {

        StringBuilder finalOutput = new StringBuilder();
        List<FileEntity> files = project.getCodeFiles();
        List<String> pathNames = new ArrayList<>();
        for(FileEntity file : files) {
            String pathName = Helper.convertToFile(file.getContent(), file.getName());
            pathNames.add(pathName);
        }
        String args = String.join(",", pathNames);
        try {
            // Compile the code
            project.setBuildStatus(IN_PROGRESS);
            projectRepository.save(project);
            ProcessBuilder compileBuilder = new ProcessBuilder("g++", "-o", project.getName(), args);
            compileBuilder.redirectErrorStream(true);
            Process compileProcess = compileBuilder.start();
            int exitCode = compileProcess.waitFor();
            if (exitCode == 0) {
                project.setBuildStatus(SUCCESS_BUILD);
                projectRepository.save(project);
                return new Message(finalOutput.toString(), true);
            } else {
                project.setBuildStatus(FAILURE_BUILD);
                projectRepository.save(project);
                System.err.println("Compilation failed with exit code: " + exitCode);
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()));
                String line;
                while ((line = errorReader.readLine()) != null) {
                    System.err.println(line);
                }
                errorReader.close();
                return new Message(finalOutput.toString(), false);
            }
            /*
            // Run the compiled program
            ProcessBuilder runBuilder = new ProcessBuilder("./" + executable);
            runBuilder.redirectErrorStream(true);
            Process runProcess = runBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                finalOutput.append(line).append(System.lineSeparator());
            }
            int exitCode = runProcess.waitFor();
            if (exitCode != 0) {
                finalOutput.append("Execution of ").append(sourceFile).append(" failed.").append(System.lineSeparator());
            }

             */
        } catch (IOException | InterruptedException e) {
            return new Message("Compilation interrupted: " + e.getMessage(), false);
        }
    }
}


