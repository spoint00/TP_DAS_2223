package isec.tp.das.onlinecompiler.repository;

import isec.tp.das.onlinecompiler.models.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileEntityRepository extends JpaRepository<FileEntity, Long> {
    // custom query methods
}
