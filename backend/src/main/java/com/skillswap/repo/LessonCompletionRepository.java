package com.skillswap.repo;
import com.skillswap.model.LessonCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface LessonCompletionRepository extends JpaRepository<LessonCompletion,Long> {
 List<LessonCompletion> findByLearnerIdAndLessonCourseId(Long learnerId,Long courseId);
 Optional<LessonCompletion> findByLearnerIdAndLessonId(Long learnerId,Long lessonId);
}
