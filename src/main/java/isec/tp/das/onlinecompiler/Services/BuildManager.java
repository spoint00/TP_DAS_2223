package isec.tp.das.onlinecompiler.Services;

import isec.tp.das.onlinecompiler.Models.Project;

import java.util.LinkedList;
import java.util.List;

public class BuildManager {
    private static BuildManager instance;
    private List<Project> projectList = new LinkedList<>();

    private BuildManager() {
    }

    public static synchronized BuildManager getInstance() {
        if (instance == null) {
            instance = new BuildManager();
        }
        return instance;
    }

    public synchronized void addProject(Project p) {
        projectList.add(p);
    }

    public synchronized Project processNextProject() {
        if (!projectList.isEmpty()) {
            return projectList.removeFirst();
        }
        return null;
    }

    public synchronized List<Project> getAllProjects() {
        return new LinkedList<>(projectList);
    }

    public synchronized Project getProjectById(int projectId) {
        for (Project project : projectList) {
            if (project.getId() == projectId) {
                return project;
            }
        }
        return null;
    }

    public synchronized boolean removeProject(int projectId) {
        Project projectToRemove = null;
        for (Project project : projectList) {
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

