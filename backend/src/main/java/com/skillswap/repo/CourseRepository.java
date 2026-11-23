package com.skillswap.repo;
import com.skillswap.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface CourseRepository extends JpaRepository<Course,Long> {
    List<Course> findByPublishedTrueOrderByCreatedAtDesc();
    List<Course> findByMentorIdOrderByCreatedAtDesc(Long mentorId);
}
