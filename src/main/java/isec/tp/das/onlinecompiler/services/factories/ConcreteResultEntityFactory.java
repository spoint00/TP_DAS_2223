package isec.tp.das.onlinecompiler.services.factories;

import isec.tp.das.onlinecompiler.models.ResultEntity;

public class ConcreteResultEntityFactory implements ResultEntityFactory {

    @Override
    public ResultEntity createResultEntity(Boolean success, String message, String output) {
        return new ResultEntity(success, message, output);
    }

    @Override
    public ResultEntity createResultEntity() {
        return new ResultEntity();
    }
}
