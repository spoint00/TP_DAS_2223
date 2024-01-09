package isec.tp.das.onlinecompiler.services;

import isec.tp.das.onlinecompiler.models.ProjectEntity;

import java.util.LinkedList;
import java.util.List;

import static isec.tp.das.onlinecompiler.util.BUILDSTATUS.AWAITING_QUEUE;
import static isec.tp.das.onlinecompiler.util.BUILDSTATUS.IN_QUEUE;

public class BuildManager {
    private static final BuildManager instance = new BuildManager();
    private final List<ProjectEntity> projectList = new LinkedList<>();

    private BuildManager() {
    }

    public static synchronized BuildManager getInstance() {
        return instance;
    }

    public synchronized void addProject(ProjectEntity project) {
        project.setBuildStatus(IN_QUEUE);
        projectList.add(project);
    }

    public synchronized ProjectEntity processNextProject() {
        if (!projectList.isEmpty()) {
            return projectList.removeFirst();
        }
        return null;
    }

    public void compilationCompleted(ProjectEntity project) {
        projectList.remove(project);
    }

    public synchronized void abortProject(ProjectEntity project) {
        project.setBuildStatus(AWAITING_QUEUE);
        projectList.remove(project);
    }
}

