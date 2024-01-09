package isec.tp.das.onlinecompiler.repository;

import isec.tp.das.onlinecompiler.models.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    // custom query methods
}