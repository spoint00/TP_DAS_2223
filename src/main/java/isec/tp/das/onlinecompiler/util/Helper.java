package isec.tp.das.onlinecompiler.util;

import isec.tp.das.onlinecompiler.models.FileEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Helper {
    private Helper() {
    }

    // convert list of MultipartFile to FileEntity
    public static List<FileEntity> createFileEntities(List<MultipartFile> multipartFiles) throws IOException {
        List<FileEntity> fileEntities = new ArrayList<>();

        for (MultipartFile multipartFile : multipartFiles) {
            String fileName = multipartFile.getOriginalFilename();
            byte[] fileContent = multipartFile.getBytes();

            FileEntity fileEntity = new FileEntity(fileName, fileContent);
            fileEntities.add(fileEntity);
        }

        return fileEntities;
    }
}
