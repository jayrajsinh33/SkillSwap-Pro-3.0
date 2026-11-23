package com.skillswap.repo;
import com.skillswap.model.MaterialFile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface MaterialFileRepository extends JpaRepository<MaterialFile,Long> {
    List<MaterialFile> findByCourseIdOrderByCreatedAtDesc(Long courseId);
}
