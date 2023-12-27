package isec.tp.das.onlinecompiler.Services;

import isec.tp.das.onlinecompiler.Models.Project;
import isec.tp.das.onlinecompiler.Repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private BuildManager bm;

    public ProjectService(ProjectRepository projectRepository) {
        this.bm = BuildManager.getInstance();
        this.projectRepository = projectRepository;
    }

    public Project saveProject(Project project) {
        return projectRepository.save(project);
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

    public String runCompilationForFiles(String projectName, String filesNames) {
        StringBuilder finalOutput = new StringBuilder();
        List<String> sourceFiles = List.of(filesNames.split("\\r?\\n")); // split by newlines
        sourceFiles = sourceFiles.stream().map(String::trim).toList(); // trim each file name

        for (String sourceFile : sourceFiles) {
            String result = compileAndRunFile(sourceFile);
            finalOutput.append(result);
        }

        return finalOutput.toString();
    }

    // compile and run 1 file
    public String compileAndRunFile(String sourceFile) {
        StringBuilder finalOutput = new StringBuilder();

        if (!sourceFile.isEmpty()) {
            String compiler = sourceFile.endsWith(".cpp") ? "g++" : "gcc";
            String executable = sourceFile.replaceAll("\\.\\w+$", ""); // remove the file extension for the executable name

            try {
                // run compiler
                ProcessBuilder compileBuilder = new ProcessBuilder(compiler, "-o", executable, sourceFile);
                compileBuilder.redirectErrorStream(true);
                Process compileProcess = compileBuilder.start();
                compileProcess.waitFor();

                // run the compiled program (if compilation succeeds)
                if (compileProcess.exitValue() == 0) {
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
                        finalOutput.append("Execution of ").append(executable).append(" failed.").append(System.lineSeparator());
                    }
                } else {
                    finalOutput.append("Compilation of ").append(sourceFile).append(" failed.").append(System.lineSeparator());
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                finalOutput.append("Error during compilation or execution of ").append(sourceFile).append(": ").append(e.getMessage()).append(System.lineSeparator());
            }
        }

        return finalOutput.toString();
    }
}


