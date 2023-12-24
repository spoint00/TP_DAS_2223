package isec.tp.das.onlinecompiler.Services;

import java.util.LinkedList;
import java.util.List;


public class BuildManager {
    private static BuildManager instance;
    private List<String> buildList = new LinkedList<>();

    private BuildManager() {
        // Private constructor to prevent instantiation
    }

    public static synchronized BuildManager getInstance() {
        if (instance == null) {
            instance = new BuildManager();
        }
        return instance;
    }
    public synchronized void addBuild(String build) {
        buildList.add(build);
    }
    public synchronized String processNextBuild() {
        if (!buildList.isEmpty()) {
            return buildList.removeFirst();
        }
        return null;
    }
}

