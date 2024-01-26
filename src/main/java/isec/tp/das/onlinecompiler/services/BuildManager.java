package isec.tp.das.onlinecompiler.services;

import isec.tp.das.onlinecompiler.models.ProjectEntity;
import isec.tp.das.onlinecompiler.models.ResultEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static isec.tp.das.onlinecompiler.util.BUILDSTATUS.AWAITING_QUEUE;
import static isec.tp.das.onlinecompiler.util.BUILDSTATUS.IN_QUEUE;

public class BuildManager {
    private static final BuildManager instance = new BuildManager();
    private final List<ProjectEntity> projectQueue = new LinkedList<>();
    private final List<BuildListener> listeners = new ArrayList<>();
    private final Map<Long, Thread> compilationThreads = new ConcurrentHashMap<>();

    private BuildManager() {
    }

    public static BuildManager getInstance() {
        return instance;
    }

    public List<ProjectEntity> getProjectQueue() {
        return projectQueue;
    }

    public void addProject(ProjectEntity project) {
        if (!projectQueue.contains(project)){
            project.setBuildStatus(IN_QUEUE);
            projectQueue.add(project);
        }
    }

    public ProjectEntity processNextProject() {
        if (!projectQueue.isEmpty()) {
            return projectQueue.removeFirst();
        }
        return null;
    }

    public void compilationCompleted(ProjectEntity project) {
        projectQueue.remove(project);
    }

    public void abortProject(ProjectEntity project) {
        project.setBuildStatus(AWAITING_QUEUE);
        projectQueue.remove(project);
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

    public void notifyBuildCompleted(ProjectEntity project, ResultEntity message) {
        if (project == null)
            return;

        for (BuildListener listener : listeners) {
            listener.onBuildCompleted(project, message);
        }
    }

    public void addThread(Long projectId, Thread compThread) {
        compilationThreads.put(projectId, compThread);
    }

    public void removeThread(Long projectId) {
        compilationThreads.remove(projectId);
    }

    public boolean cancelCompilation(Long projectId) {
        Thread compThread = compilationThreads.get(projectId);

        if (compThread != null && compThread.isAlive()) {
            compThread.interrupt();
            return true;
        }
        return false;
    }
    public Map<Long, Thread> getCompilationThreads() {
        return compilationThreads;
    }

}

