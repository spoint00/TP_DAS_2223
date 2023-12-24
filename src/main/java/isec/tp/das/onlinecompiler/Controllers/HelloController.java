package isec.tp.das.onlinecompiler.Controllers;

import isec.tp.das.onlinecompiler.Services.BuildManager;
import isec.tp.das.onlinecompiler.Services.CompilationService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Controller
public class HelloController {
    private final BuildManager buildManager;
    private final CompilationService compilationService;

    public HelloController(CompilationService compilationService) {
        this.compilationService = compilationService;
        this.buildManager = BuildManager.getInstance();
    }

    @GetMapping("/")
    public String home() {
        return "HomePage";  // Assuming your HTML file is named 'index.html'
    }

    @PostMapping("/CompileResults")
    public String compileCode(@RequestParam("code") List<String> code, Model model) {
        String result = compilationService.compileAndRunFiles(code);
        model.addAttribute("result", result);
        return "CompileResults"; // This should be the name of the HTML file to display the result
    }
}
