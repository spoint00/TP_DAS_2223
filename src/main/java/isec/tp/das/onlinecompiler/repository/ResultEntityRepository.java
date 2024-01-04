package isec.tp.das.onlinecompiler.repository;

import isec.tp.das.onlinecompiler.models.ResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResultEntityRepository extends JpaRepository<ResultEntity, Long> {
    Optional<ResultEntity> findById(Long id);
}
