package isec.tp.das.onlinecompiler.models;

import java.util.LinkedList;
import java.util.List;

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

    public synchronized boolean removeProject(int projectId) {
        ProjectEntity projectToRemove = null;
        for (ProjectEntity project : projectList) {
            if (project.getId() == projectId) {
                projectToRemove = project;
                break;
            }
        }

        if (projectToRemove != null) {
            projectList.remove(projectToRemove);
            return true;
        }

        return false;
    }
}

