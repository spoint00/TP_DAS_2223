package isec.tp.das.onlinecompiler.services;

import isec.tp.das.onlinecompiler.models.ResultEntity;
import isec.tp.das.onlinecompiler.repository.ResultEntityRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResultService {

    @Autowired
    private ResultEntityRepository resultEntityRepository;

    public ResultEntity getResultById(Long id) {
        return resultEntityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Result not found with id: " + id));
    }

    public ResultEntityRepository getResultEntityRepository() {
        return resultEntityRepository;
    }

    public void setResultEntityRepository(ResultEntityRepository resultEntityRepository) {
        this.resultEntityRepository = resultEntityRepository;
    }
}
