package isec.tp.das.onlinecompiler.controllers;

import isec.tp.das.onlinecompiler.models.ResultEntity;
import isec.tp.das.onlinecompiler.services.ResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/results")
public class ResultController {

    @Autowired
    private ResultService resultService;

    @GetMapping("/{id}")
    public ResponseEntity<ResultEntity> getResultById(@PathVariable Long id) {
        ResultEntity result = resultService.getResultById(id);
        return ResponseEntity.ok(result);
    }
}
