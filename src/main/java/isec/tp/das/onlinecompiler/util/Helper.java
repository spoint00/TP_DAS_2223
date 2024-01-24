package isec.tp.das.onlinecompiler.util;

import isec.tp.das.onlinecompiler.models.FileEntity;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Helper {
    public static String defaultMessage = "Default message";
    public static String defaultOutput = "Default output";
    public static String projectNotFound = "Project not found";
    public static String noOutput = "No output";
    public static String projectNotInQueue = "Project not in queue";
    public static String noFilesToCompile = "No source files to compile";
    public static String projectNotCompiled = "Compile project successfully before running";
    public static String queueIsEmpty = "Project queue is empty";

    public static Path tempPath = Paths.get("./temp");


    private Helper() {
    }

    // convert list of MultipartFile to FileEntity
    public static List<FileEntity> createFileEntities(List<MultipartFile> multipartFiles) throws IOException {
        List<FileEntity> fileEntities = new ArrayList<>();

        for (MultipartFile multipartFile : multipartFiles) {
            String fileName = multipartFile.getOriginalFilename();
            byte[] fileContent = multipartFile.getBytes();

            if (fileName != null){
                if (fileName.endsWith(".c") || fileName.endsWith(".cpp") || fileName.endsWith(".h")) {
                    //TODO: criar factory para file entity
                    FileEntity fileEntity = new FileEntity(fileName, fileContent);
                    fileEntities.add(fileEntity);
                } else {
                    // You can either throw an exception or handle it based on your application's needs
                    throw new IllegalArgumentException("Invalid file type: " + fileName);
                }
            }
        }
        return fileEntities;
    }

    // convert array of bytes to file
    public static String convertToFile(String projectName, String fileName, byte[] fileContent) {
        Path folderPath = tempPath.resolve(projectName);
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

    // return list of file paths as strings
    public static List<String> getFilesPathsAsStrings(String projectName, List<FileEntity> files) {
        List<String> filesPaths = new ArrayList<>();

        for (FileEntity file : files) {
            String path = Helper.convertToFile(projectName, file.getName(), file.getContent());
            if (path != null && !file.getName().endsWith(".h"))
                filesPaths.add(path);
        }
        return filesPaths;
    }


    // clean source code files from temp folder
    public static void cleanupTempFiles(Path tempDirectoryPath) throws IOException {
        try (Stream<Path> paths = Files.walk(tempDirectoryPath)) {
            paths.filter(path -> !path.toString().endsWith(".exe"))
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    // delete Temp folder and all files
    public static void deleteTempFolder() {
        FileSystemUtils.deleteRecursively(tempPath.toFile());
    }
}
