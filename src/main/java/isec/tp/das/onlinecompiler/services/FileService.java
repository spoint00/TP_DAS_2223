package isec.tp.das.onlinecompiler.services;

import isec.tp.das.onlinecompiler.models.FileEntity;
import isec.tp.das.onlinecompiler.repository.FileEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FileService {
    private final FileEntityRepository fileEntityRepository;
//    private final BuildManager bm;

    @Autowired
    public FileService(FileEntityRepository fileEntityRepository) {
        this.fileEntityRepository = fileEntityRepository;
//        bm = BuildManager.getInstance();
    }

    public List<FileEntity> getAllFiles() {
        return fileEntityRepository.findAll();
    }

    public FileEntity getFileById(Long fileId) {
        Optional<FileEntity> fileOptional = fileEntityRepository.findById(fileId);

        // return file or null
        return fileOptional.orElse(null);
    }

    public FileEntity updateFile(Long fileId, FileEntity updatedFileEntity) {
        Optional<FileEntity> existingFile = fileEntityRepository.findById(fileId);

        if (existingFile.isPresent()) {
            return fileEntityRepository.save(updatedFileEntity);
        } else {
            return null;
        }
    }

    public boolean deleteFile(Long fileId) {
        if (fileEntityRepository.existsById(fileId)) {
            fileEntityRepository.deleteById(fileId);
            return true;
        } else {
            return false;
        }
    }
}
