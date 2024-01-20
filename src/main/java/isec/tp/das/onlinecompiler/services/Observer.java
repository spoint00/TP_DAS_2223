package isec.tp.das.onlinecompiler.services;

import isec.tp.das.onlinecompiler.models.ProjectEntity;
import isec.tp.das.onlinecompiler.models.ResultEntity;

public class Observer implements BuildListener {
    private static long nextId = 1;
    private final long id;

    public Observer() {
        this.id = nextId++;
    }

    public Long getId() {
        return id;
    }

    @Override
    public void onBuildCompleted(ProjectEntity project, ResultEntity result) {
        System.out.println("Listener[id=" + id + "] ");
        System.out.println("Project [id=" + project.getId() +
                ", name=" + project.getName() + ", result=" + result.getMessage() + "]");
    }
}
