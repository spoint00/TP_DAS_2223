package isec.tp.das.onlinecompiler.util;

import isec.tp.das.onlinecompiler.models.FileEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    // convert array of bytes to file
    public static String convertToFile(String projectName, String fileName, byte[] fileContent) {
        Path folderPath = Paths.get("./temp", projectName);
        Path filePath = folderPath.resolve(fileName);

        try {
            // create directories if they don't exist
            Files.createDirectories(folderPath);
            // write file
            try (FileOutputStream fileOutputStream = new FileOutputStream(filePath.toFile())) {
                fileOutputStream.write(fileContent);
                return filePath.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
