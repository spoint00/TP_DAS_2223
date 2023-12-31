package isec.tp.das.onlinecompiler.services.factories;

import isec.tp.das.onlinecompiler.models.FileEntity;
import isec.tp.das.onlinecompiler.models.ProjectEntity;

import java.util.List;

public class ConcreteProjectEntityFactory implements ProjectEntityFactory {

    @Override
    public ProjectEntity createProjectEntity(String name, String description, List<FileEntity> fileEntities) {
        return new ProjectEntity(name, description, fileEntities);
    }
}
