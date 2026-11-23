package com.skillswap.repo;
import com.skillswap.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface EnrollmentRepository extends JpaRepository<Enrollment,Long> {
    boolean existsByLearnerIdAndCourseId(Long learnerId,Long courseId);
    Optional<Enrollment> findByLearnerIdAndCourseId(Long learnerId, Long courseId);
    List<Enrollment> findByLearnerIdOrderByEnrolledAtDesc(Long learnerId);
    List<Enrollment> findByCourseId(Long courseId);
}
