package isec.tp.das.onlinecompiler.controllers;

import isec.tp.das.onlinecompiler.models.ResultEntity;
import isec.tp.das.onlinecompiler.services.ResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/results")
public class ResultController {

    private final ResultService resultService;

    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getResultById(
            @PathVariable Long id,
            @RequestParam(required = false) String fields) {
        ResultEntity result = resultService.findById(id); // Assumes this method exists to find the result by ID
        Map<String, Object> filteredResult = resultService.filterResultEntityFields(result, fields);
        return ResponseEntity.ok(filteredResult);
    }

    @GetMapping("")
    public ResponseEntity<List<Map<String, Object>>> getAllResults(
            @RequestParam(required = false) String fields) {
        List<Map<String, Object>> allFilteredResults = resultService.findAllResultsWithFields(fields);
        return ResponseEntity.ok(allFilteredResults);
    }
}
