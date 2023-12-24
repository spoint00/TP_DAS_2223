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
            ProcessBuilder builder = new ProcessBuilder("gcc", "-o", "outputExecutable", sourceCode);
            builder.redirectErrorStream(true); // Combine output and error streams
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                result.append(line).append(System.lineSeparator());
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return "Compilation successful.\n" + result.toString();
            } else {
                return "Compilation failed.\n" + result.toString();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error during compilation: " + e.getMessage();
        }
    }
}
