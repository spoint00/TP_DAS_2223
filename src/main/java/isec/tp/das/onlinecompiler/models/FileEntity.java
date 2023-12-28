package isec.tp.das.onlinecompiler.models;

import jakarta.persistence.*;

@Entity
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    @Column(nullable = false)
    private String name;

    @Lob
    @Column(nullable = false)
    private byte[] content;

    public FileEntity() {

    }

    public FileEntity(String name, byte[] content) {
        this.name = name;
        this.content = content;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long fileId) {
        this.Id = fileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
