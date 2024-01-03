package isec.tp.das.onlinecompiler.models;

import jakarta.persistence.*;

@Entity
public class ResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private boolean success;
    @Column
    private String message;
    @Column
    private String output;
    public ResultEntity() {

    }
    public ResultEntity(boolean success, String message, String output) {
        this.success = success;
        this.message = message;
        this.output = output;
    }

    public boolean isSuccess() {
        return success;
    }
    public String getMessage() {
        return message;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getOutput() {return output;}
    public void setOutput(String output) {this.output = output;}

}

