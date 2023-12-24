package isec.tp.das.onlinecompiler.Services;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Service
public class CompilationService {

    // Method to compile and execute files listed one per line in a text area
    public String compileAndRunFiles(List<String> sourceFiles) {
        StringBuilder finalOutput = new StringBuilder();

        for (String sourceFile : sourceFiles) {
            String compiler = sourceFile.endsWith(".cpp") ? "g++" : "gcc";
            String executable = sourceFile.replaceAll("\\.\\w+$", ""); // Remove the file extension for the executable name

            try {
                // Compile the code
                ProcessBuilder compileBuilder = new ProcessBuilder(compiler, "-o", executable, sourceFile);
                compileBuilder.redirectErrorStream(true);
                Process compileProcess = compileBuilder.start();
                compileProcess.waitFor();

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
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                finalOutput.append("Error during compilation or execution of ").append(sourceFile).append(": ").append(e.getMessage()).append(System.lineSeparator());
            }
        }

        return finalOutput.toString();
    }
}


