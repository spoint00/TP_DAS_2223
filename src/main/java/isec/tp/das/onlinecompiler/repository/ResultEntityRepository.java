package isec.tp.das.onlinecompiler.repository;

import isec.tp.das.onlinecompiler.models.ResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResultEntityRepository extends JpaRepository<ResultEntity, Long> {
    // custom query methods
}
