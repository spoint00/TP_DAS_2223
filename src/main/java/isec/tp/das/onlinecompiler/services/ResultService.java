package isec.tp.das.onlinecompiler.services;

import isec.tp.das.onlinecompiler.models.ResultEntity;
import isec.tp.das.onlinecompiler.repository.ResultEntityRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResultService {

    private final ResultEntityRepository resultEntityRepository;

    public ResultService(ResultEntityRepository resultEntityRepository) {
        this.resultEntityRepository = resultEntityRepository;
    }

    public ResultEntity getResultById(Long id) {
        return resultEntityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Result not found with id: " + id));
    }
}
