package isec.tp.das.onlinecompiler.models;

import isec.tp.das.onlinecompiler.util.BUILDSTATUS;
import isec.tp.das.onlinecompiler.util.LANGUAGE;
import jakarta.persistence.*;

import java.util.List;
import java.util.Objects;

import static isec.tp.das.onlinecompiler.util.BUILDSTATUS.AWAITING_QUEUE;

@Entity
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BUILDSTATUS buildStatus;

    @Column(nullable = false)
    private boolean saveOutput;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LANGUAGE language;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    private List<FileEntity> codeFiles;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "result_id")
    private ResultEntity resultEntity;

    public ProjectEntity() {
        this.buildStatus = AWAITING_QUEUE;
        this.saveOutput = false;
    }

    public ProjectEntity(String name, String description, List<FileEntity> codeFiles, ResultEntity resultEntity, LANGUAGE language) {
        this.name = name;
        this.description = description;
        this.codeFiles = codeFiles;
        this.buildStatus = AWAITING_QUEUE;
        this.resultEntity = resultEntity;
        this.saveOutput = false;
        this.language = language;
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

    public BUILDSTATUS getBuildStatus() {
        return buildStatus;
    }

    public void setBuildStatus(BUILDSTATUS buildStatus) {
        this.buildStatus = buildStatus;
    }

    public ResultEntity getResultEntity() {
        return resultEntity;
    }

    public void setResultEntity(ResultEntity resultEntity) {
        this.resultEntity = resultEntity;
    }

    public boolean isSaveOutput() {
        return saveOutput;
    }

    public void setSaveOutput(boolean saveOutput) {
        this.saveOutput = saveOutput;
    }

    public LANGUAGE getLanguage() {
        return language;
    }

    public void setLanguage(LANGUAGE language) {
        this.language = language;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProjectEntity that = (ProjectEntity) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, buildStatus, codeFiles, resultEntity, saveOutput, language);
    }
}
