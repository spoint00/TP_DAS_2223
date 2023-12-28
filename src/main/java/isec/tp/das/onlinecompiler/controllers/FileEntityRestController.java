package isec.tp.das.onlinecompiler.controllers;

import isec.tp.das.onlinecompiler.models.FileEntity;
import isec.tp.das.onlinecompiler.repository.FileEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/files")
public class FileEntityRestController {

    private final FileEntityRepository fileEntityRepository;

    @Autowired
    public FileEntityRestController(FileEntityRepository fileEntityRepository) {
        this.fileEntityRepository = fileEntityRepository;
    }

    @GetMapping
    public List<FileEntity> getAllFiles() {
        return fileEntityRepository.findAll();
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<FileEntity> getFileById(@PathVariable Long fileId) {
        Optional<FileEntity> fileEntityOptional = fileEntityRepository.findById(fileId);

        if (fileEntityOptional.isPresent()) {
            FileEntity fileEntity = fileEntityOptional.get();
            return ResponseEntity.ok(fileEntity);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<String> createFile() {
        String errorMessage = "Creating files through POST is not allowed. Use the appropriate endpoint for file upload.";
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorMessage);
    }

    @PutMapping("/{fileId}")
    public ResponseEntity<FileEntity> updateFile(@PathVariable Long fileId, @RequestBody FileEntity updatedFileEntity) {
        Optional<FileEntity> existingFileEntity = fileEntityRepository.findById(fileId);
        if (existingFileEntity.isPresent()) {
            updatedFileEntity.setFileId(fileId);
            FileEntity savedFileEntity = fileEntityRepository.save(updatedFileEntity);
            return ResponseEntity.ok(savedFileEntity);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long fileId) {
        if (fileEntityRepository.existsById(fileId)) {
            fileEntityRepository.deleteById(fileId);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

