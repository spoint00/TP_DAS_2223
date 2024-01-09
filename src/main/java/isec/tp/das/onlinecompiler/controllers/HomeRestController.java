package isec.tp.das.onlinecompiler.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HomeRestController {
    @GetMapping("/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok().build();
    }
}
