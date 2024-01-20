package isec.tp.das.onlinecompiler.services.factories;

import isec.tp.das.onlinecompiler.models.ResultEntity;

public interface ResultEntityFactory {
    ResultEntity createResultEntity(Boolean success, String message, String output);

    ResultEntity createResultEntity();

}
