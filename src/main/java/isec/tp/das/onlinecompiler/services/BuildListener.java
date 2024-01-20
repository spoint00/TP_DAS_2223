package isec.tp.das.onlinecompiler.services;

import isec.tp.das.onlinecompiler.models.ProjectEntity;
import isec.tp.das.onlinecompiler.models.ResultEntity;

public interface BuildListener {
    void onBuildCompleted(ProjectEntity project, ResultEntity message);

    Long getId();
}
