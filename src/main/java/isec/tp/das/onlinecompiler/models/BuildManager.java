package isec.tp.das.onlinecompiler.models;

import java.util.LinkedList;
import java.util.List;

import static isec.tp.das.onlinecompiler.util.BUILDSTATUS.*;

// not sure se isto fica neste package
public class BuildManager {
    private static BuildManager instance;
    private List<ProjectEntity> projectList = new LinkedList<>();

    private BuildManager() {
    }

    public static synchronized BuildManager getInstance() {
        if (instance == null) {
            instance = new BuildManager();
        }
        return instance;
    }

    public synchronized void addProject(ProjectEntity p) {
        p.setBuildStatus(IN_QUEUE);
        projectList.add(p);
    }

    public synchronized ProjectEntity processNextProject() {
        if (!projectList.isEmpty()) {
            return projectList.removeFirst();
        }
        return null;
    }

    public synchronized List<ProjectEntity> getAllProjects() {
        return new LinkedList<>(projectList);
    }

    public synchronized ProjectEntity getProjectById(int projectId) {
        for (ProjectEntity project : projectList) {
            if (project.getId() == projectId) {
                return project;
            }
        }
        return null;
    }

    public synchronized boolean removeProject(ProjectEntity project) {
        if (project != null) {
            project.setBuildStatus(AWAITING_QUEUE);
            projectList.remove(project);
            return true;
        }
        return false;
    }
}

