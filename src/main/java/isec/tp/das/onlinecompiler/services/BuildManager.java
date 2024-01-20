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
    private final List<BuildListener> listeners = new ArrayList<>();

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

    public boolean addBuildListener(BuildListener listener) {
        return listeners.add(listener);
    }

    public boolean removeBuildListener(Long listenerId) {
        for (BuildListener listener : listeners)
            if (listenerId.equals(listener.getId())) {
                return listeners.remove(listener);
            }
        return false;
    }

    protected void notifyBuildCompleted(ProjectEntity project, ResultEntity message) {
        if (project == null)
            return;

        for (BuildListener listener : listeners) {
            listener.onBuildCompleted(project, message);
        }
    }
}

