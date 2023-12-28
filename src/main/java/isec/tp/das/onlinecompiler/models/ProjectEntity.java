package isec.tp.das.onlinecompiler.models;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column
    private String description;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "project_id")
    private List<FileEntity> codeFiles;

    public ProjectEntity() {
    }

    public ProjectEntity(String name, String description, List<FileEntity> codeFiles) {
        this.name = name;
        this.description = description;
        this.codeFiles = codeFiles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<FileEntity> getCodeFiles() {
        return codeFiles;
    }

    public void setCodeFiles(List<FileEntity> codeFiles) {
        this.codeFiles = codeFiles;
    }
}
