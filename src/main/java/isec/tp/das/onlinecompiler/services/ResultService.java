package isec.tp.das.onlinecompiler.services;

import isec.tp.das.onlinecompiler.models.ResultEntity;
import isec.tp.das.onlinecompiler.repository.ResultEntityRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ResultService {

    private final ResultEntityRepository resultEntityRepository;

    public ResultService(ResultEntityRepository resultEntityRepository) {
        this.resultEntityRepository = resultEntityRepository;
    }

    public Map<String, String> getResultById(Long resultId, String fields) {
        ResultEntity result = resultEntityRepository.findById(resultId).orElse(null);
        if (result == null) {
            return null;
        }

        return convertResultToMap(result, fields);
    }

    public List<Map<String, String>> getAllResults(String fields) {
        List<ResultEntity> results = resultEntityRepository.findAll();
        List<Map<String, String>> resultsList = new ArrayList<>();

        results.forEach(result -> {
            Map<String, String> resultMap = convertResultToMap(result, fields);
            resultsList.add(resultMap);
        });

        return resultsList;
    }

    // convert ResultEntity to LinkedHashMap
    private Map<String, String> convertResultToMap(ResultEntity result, String fields) {
        if (fields != null && !fields.isEmpty()) {
            return applyFieldMask(result, fields);
        }

        LinkedHashMap<String, String> resultMap = new LinkedHashMap<>();
        resultMap.put("id", String.valueOf(result.getId()));
        resultMap.put("success", String.valueOf(result.isSuccess()));
        resultMap.put("message", result.getMessage());
        resultMap.put("output", result.getOutput());

        return resultMap;
    }

    // apply field masks
    private LinkedHashMap<String, String> applyFieldMask(ResultEntity result, String fields) {
        String[] fieldsArray = fields.split(",");
        List<String> fieldsList = Arrays.asList(fieldsArray);

        LinkedHashMap<String, String> resultMap = new LinkedHashMap<>();
        resultMap.put("id", String.valueOf(result.getId()));

        if (fieldsList.contains("success")) {
            resultMap.put("success", String.valueOf(result.isSuccess()));
        }

        if (fieldsList.contains("message")) {
            resultMap.put("message", result.getMessage());
        }

        if (fieldsList.contains("output")) {
            resultMap.put("output", result.getOutput());
        }

        return resultMap;
    }
}
