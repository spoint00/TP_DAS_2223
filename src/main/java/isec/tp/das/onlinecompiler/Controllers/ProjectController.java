package isec.tp.das.onlinecompiler.Controllers;

import isec.tp.das.onlinecompiler.Services.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/CompileResults")
    public String compileCode(@RequestParam("projectName") String projectName,
                              @RequestParam("filesNames") String filesNames,
                              Model model) {
        String result = projectService.runCompilationForFiles(projectName, filesNames);
        model.addAttribute("result", result);

        return "CompileResults";
    }
}
