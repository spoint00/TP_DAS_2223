package isec.tp.das.onlinecompiler.controllers;

import isec.tp.das.onlinecompiler.services.ResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/results")
public class ResultRestController {

    private final ResultService resultService;

    @Autowired
    public ResultRestController(ResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping("")
    public ResponseEntity<List<Map<String, String>>> getAllResults(
            @RequestParam(required = false) String fields) {
        List<Map<String, String>> results = resultService.getAllResults(fields);

        if (results.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(results);
        }
    }

    @GetMapping("/{resultId}")
    public ResponseEntity<Map<String, String>> getResultById(
            @PathVariable Long resultId,
            @RequestParam(required = false) String fields) {
        Map<String, String> result = resultService.getResultById(resultId, fields);
        if (result != null) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
