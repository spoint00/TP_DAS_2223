package isec.tp.das.onlinecompiler.services.factories;

import isec.tp.das.onlinecompiler.models.FileEntity;

public class ConcreteFileEntityFactory implements FileEntityFactory{
    @Override
    public FileEntity createFileEntity() {
        return new FileEntity();
    }

    @Override
    public FileEntity createFileEntity(String name, byte[] content) {
        return new FileEntity(name, content);
    }
}
