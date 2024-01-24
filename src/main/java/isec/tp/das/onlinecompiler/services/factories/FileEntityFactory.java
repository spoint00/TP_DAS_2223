package isec.tp.das.onlinecompiler.services.factories;

import isec.tp.das.onlinecompiler.models.FileEntity;

public interface FileEntityFactory {
    FileEntity createFileEntity();
    FileEntity createFileEntity(String name, byte[] content);
}
