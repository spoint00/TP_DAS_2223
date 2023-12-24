package isec.tp.das.onlinecompiler.Services;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class CompilationService {
    public String compileCode(String sourceCode) {
        // Save sourceCode to a file here, then compile that file using gcc
        // Assuming sourceCode is the path to the C/C++ file, if not, save it to a file first
        try {
            ProcessBuilder compileBuilder = new ProcessBuilder("gcc", "-o", "outputExecutable", sourceCode);
            compileBuilder.redirectErrorStream(true);
            Process compileProcess = compileBuilder.start();
            compileProcess.waitFor();

            // Run the compiled program
            ProcessBuilder runBuilder = new ProcessBuilder("./outputExecutable");
            runBuilder.redirectErrorStream(true);
            Process runProcess = runBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                result.append(line).append(System.lineSeparator());
            }

            int exitCode = runProcess.waitFor();
            if (exitCode == 0) {
                return "Program executed successfully.\n" + result;
            } else {
                return "Program execution failed.\n" + result;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error during compilation: " + e.getMessage();
        }
    }
}
