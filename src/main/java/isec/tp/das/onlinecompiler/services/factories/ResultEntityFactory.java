package isec.tp.das.onlinecompiler.services.factories;

import isec.tp.das.onlinecompiler.models.FileEntity;
import isec.tp.das.onlinecompiler.models.ProjectEntity;
import isec.tp.das.onlinecompiler.models.ResultEntity;

import java.util.List;

public interface ResultEntityFactory {
    ResultEntity createResultEntity( Boolean success,String message, String output);
    ResultEntity createResultEntity();

}
