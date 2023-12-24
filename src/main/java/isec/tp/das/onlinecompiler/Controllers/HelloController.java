package isec.tp.das.onlinecompiler.Controllers;

import isec.tp.das.onlinecompiler.Services.CompilationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class HelloController {

    @Autowired
    private CompilationService compilationService;
    @GetMapping("/")
    public String home() {
        return "HomePage";  // Assuming your HTML file is named 'index.html'
    }

    @PostMapping("/CompileResults")
    public String compileCode(@RequestParam("code") String code, Model model) {
        String result = compilationService.compileCode(code);
        model.addAttribute("result", result);
        return "CompileResults"; // This should be the name of the HTML file to display the result
    }
}
