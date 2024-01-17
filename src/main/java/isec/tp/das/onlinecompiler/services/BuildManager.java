package isec.tp.das.onlinecompiler.services;

import isec.tp.das.onlinecompiler.models.ProjectEntity;
import isec.tp.das.onlinecompiler.models.ResultEntity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static isec.tp.das.onlinecompiler.util.BUILDSTATUS.AWAITING_QUEUE;
import static isec.tp.das.onlinecompiler.util.BUILDSTATUS.IN_QUEUE;

public class BuildManager {
    private static final BuildManager instance = new BuildManager();
    private final List<ProjectEntity> projectList = new LinkedList<>();
    private List<BuildListener> listeners = new ArrayList<>();

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

    public void addBuildListener(BuildListener listener) {
        listeners.add(listener);
    }

    public void removeBuildListener(BuildListener listener) {
        listeners.remove(listener);
    }

    protected void notifyBuildCompleted(ProjectEntity project, ResultEntity message) {
        for (BuildListener listener : listeners) {
            listener.onBuildCompleted(project, message);
        }
    }
}

