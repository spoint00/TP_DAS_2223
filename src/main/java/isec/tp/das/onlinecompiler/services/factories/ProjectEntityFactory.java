package isec.tp.das.onlinecompiler.services.factories;

import isec.tp.das.onlinecompiler.models.FileEntity;
import isec.tp.das.onlinecompiler.models.ProjectEntity;
import isec.tp.das.onlinecompiler.models.ResultEntity;

import java.util.List;

public interface ProjectEntityFactory {
    ProjectEntity createProjectEntity(String name, String description, List<FileEntity> fileEntities, ResultEntity resultEntity);

    ProjectEntity createProjectEntity();

}
