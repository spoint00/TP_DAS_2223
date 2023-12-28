package isec.tp.das.onlinecompiler.controllers;

import isec.tp.das.onlinecompiler.models.FileEntity;
import isec.tp.das.onlinecompiler.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/files")
public class FileRestController {

    private final FileService fileService;

    @Autowired
    public FileRestController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping
    public List<FileEntity> getAllFiles() {
        return fileService.getAllFiles();
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<FileEntity> getFileById(@PathVariable Long fileId) {
        FileEntity file = fileService.getFileById(fileId);

        if (file != null) {
            return ResponseEntity.ok(file);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<String> createFile() {
        String errorMessage = "Not allowed to create a single file. Try to create a project.";
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorMessage);
    }

    // TODO: corrigir metodo de update
    @PutMapping("/{fileId}")
    public ResponseEntity<FileEntity> updateFile(@PathVariable Long fileId, @RequestBody FileEntity updatedFileEntity) {
        FileEntity file = fileService.updateFile(fileId, updatedFileEntity);

        if (file != null) {
            return ResponseEntity.ok(file);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long fileId) {
        if (fileService.deleteFile(fileId)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

