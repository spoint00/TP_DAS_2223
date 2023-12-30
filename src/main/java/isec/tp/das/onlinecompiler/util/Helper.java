package isec.tp.das.onlinecompiler.util;

import isec.tp.das.onlinecompiler.models.FileEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
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

            if (fileName.endsWith(".c") || fileName.endsWith(".cpp") || fileName.endsWith(".h")) {
                FileEntity fileEntity = new FileEntity(fileName, fileContent);
                fileEntities.add(fileEntity);
            } else {
                // You can either throw an exception or handle it based on your application's needs
                throw new IllegalArgumentException("Invalid file type: " + fileName);
            }
        }
        return fileEntities;
    }

    public static String convertToFile(byte[] fileBytes, String fileName) throws IOException {
        String path = "./temp/";
        String pathName = path + fileName;
        try (FileOutputStream fileOutputStream = new FileOutputStream(pathName)) {
            fileOutputStream.write(fileBytes);
            return pathName;
        }
    }
}
