package isec.tp.das.onlinecompiler.Models;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Project {
    private int id;
    private String name;
    private List<File> files = new ArrayList<>();
    private String description;

    public Project(int id, String name, List<File> files, String description) {
        this.id = id;
        this.name = name;
        this.files = files;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
