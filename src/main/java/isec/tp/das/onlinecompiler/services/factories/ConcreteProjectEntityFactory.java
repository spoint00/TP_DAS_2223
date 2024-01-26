package isec.tp.das.onlinecompiler.services.factories;

import isec.tp.das.onlinecompiler.models.FileEntity;
import isec.tp.das.onlinecompiler.models.ProjectEntity;
import isec.tp.das.onlinecompiler.models.ResultEntity;
import isec.tp.das.onlinecompiler.util.LANGUAGE;

import java.util.List;

public class ConcreteProjectEntityFactory implements ProjectEntityFactory {
    @Override
    public ProjectEntity createProjectEntity(String name, String description, List<FileEntity> fileEntities, ResultEntity resultEntity, LANGUAGE language) {
        return new ProjectEntity(name, description, fileEntities, resultEntity, language);
    }

    @Override
    public ProjectEntity createProjectEntity() {
        return new ProjectEntity();
    }
}
