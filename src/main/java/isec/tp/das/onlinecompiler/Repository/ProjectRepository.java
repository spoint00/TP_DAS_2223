package isec.tp.das.onlinecompiler.Repository;

import isec.tp.das.onlinecompiler.Models.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    // Custom query methods can be defined here
}