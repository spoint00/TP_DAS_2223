package isec.tp.das.onlinecompiler.services;

import isec.tp.das.onlinecompiler.models.ResultEntity;
import isec.tp.das.onlinecompiler.repository.ResultEntityRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;

import java.beans.PropertyDescriptor;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResultService {

    private final ResultEntityRepository resultEntityRepository;

    public ResultService(ResultEntityRepository resultEntityRepository) {
        this.resultEntityRepository = resultEntityRepository;
    }

    public ResultEntity findById(Long id) {
        return resultEntityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Result not found with id: " + id));
    }

    public Map<String, Object> filterResultEntityFields(ResultEntity entity, String fields) {
        Map<String, Object> filteredResults = new LinkedHashMap<>(); // Use LinkedHashMap to maintain order
        BeanWrapper wrapper = new BeanWrapperImpl(entity);

        // Add 'id' field first if it exists in the ResultEntity
        if(wrapper.isReadableProperty("id")) {
            filteredResults.put("id", wrapper.getPropertyValue("id"));
        }

        Set<String> includeFields;

        if (fields == null || fields.trim().isEmpty()) {
            // Include all fields except 'class'
            includeFields = Arrays.stream(wrapper.getPropertyDescriptors())
                    .map(PropertyDescriptor::getName)
                    .filter(name -> !name.equals("class") && !name.equals("id")) // Skip 'class' and 'id' as it's already added
                    .collect(Collectors.toSet());
        } else {
            // Include specified fields, already added 'id'
            includeFields = Arrays.stream(fields.split(","))
                    .filter(name -> !name.equals("id")) // Skip 'id' as it's already added
                    .collect(Collectors.toSet());
        }

        // Add the remaining fields in the specified order
        for (String fieldName : includeFields) {
            if(wrapper.isReadableProperty(fieldName)) {
                filteredResults.put(fieldName, wrapper.getPropertyValue(fieldName));
            }
        }

        return filteredResults;
    }

    public List<Map<String, Object>> findAllResultsWithFields(String fields) {
        List<ResultEntity> allResults = resultEntityRepository.findAll(); // Assuming you have a findAll method
        List<Map<String, Object>> filteredResults = new ArrayList<>();

        for (ResultEntity entity : allResults) {
            filteredResults.add(filterResultEntityFields(entity, fields));
        }

        return filteredResults;
    }

}
