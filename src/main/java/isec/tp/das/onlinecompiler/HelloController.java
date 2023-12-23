package isec.tp.das.onlinecompiler;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api") // Define the base path for your API
public class HelloController {

    @GetMapping("/hello") // Define the endpoint path for your GET method
    public String sayHello() {
        return "Hello, World!";
    }
}
