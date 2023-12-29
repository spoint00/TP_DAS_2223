package isec.tp.das.onlinecompiler.services;

import isec.tp.das.onlinecompiler.models.BuildManager;
import isec.tp.das.onlinecompiler.models.FileEntity;
import isec.tp.das.onlinecompiler.models.ProjectEntity;
import isec.tp.das.onlinecompiler.repository.ProjectRepository;
import isec.tp.das.onlinecompiler.util.Helper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final BuildManager bm;

    public ProjectService(ProjectRepository projectRepository) {
        this.bm = BuildManager.getInstance();
        this.projectRepository = projectRepository;
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

        ProjectEntity project = new ProjectEntity(name, description, fileEntities);

        ProjectEntity savedProject = projectRepository.save(project);

        bm.addProject(savedProject);

        return savedProject;
    }

    public ProjectEntity updateProject(Long projectId, ProjectEntity updatedProject) {
        Optional<ProjectEntity> existingProject = projectRepository.findById(projectId);

        if (existingProject.isPresent()) {
            return projectRepository.save(updatedProject);
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


    //    // Method to compile and execute files listed one per line in a text area
//    public String compileAndRunFiles(List<String> sourceFiles) {
//        StringBuilder finalOutput = new StringBuilder();
//
//        for (String sourceFile : sourceFiles) {
//            String compiler = sourceFile.endsWith(".cpp") ? "g++" : "gcc";
//            String executable = sourceFile.replaceAll("\\.\\w+$", ""); // Remove the file extension for the executable name
//
//            try {
//                // Compile the code
//                ProcessBuilder compileBuilder = new ProcessBuilder(compiler, "-o", executable, sourceFile);
//                compileBuilder.redirectErrorStream(true);
//                Process compileProcess = compileBuilder.start();
//                compileProcess.waitFor();
//
//                // Run the compiled program
//                ProcessBuilder runBuilder = new ProcessBuilder("./" + executable);
//                runBuilder.redirectErrorStream(true);
//                Process runProcess = runBuilder.start();
//
//                BufferedReader reader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    finalOutput.append(line).append(System.lineSeparator());
//                }
//                int exitCode = runProcess.waitFor();
//                if (exitCode != 0) {
//                    finalOutput.append("Execution of ").append(sourceFile).append(" failed.").append(System.lineSeparator());
//                }
//            } catch (IOException | InterruptedException e) {
//                e.printStackTrace();
//                finalOutput.append("Error during compilation or execution of ").append(sourceFile).append(": ").append(e.getMessage()).append(System.lineSeparator());
//            }
//        }
//
//        return finalOutput.toString();
//    }
}

