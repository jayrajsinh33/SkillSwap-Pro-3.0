package com.skillswap.repo;
import com.skillswap.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ReviewRepository extends JpaRepository<Review,Long> {
    List<Review> findByCourseIdOrderByCreatedAtDesc(Long courseId);
    boolean existsByAuthorIdAndCourseId(Long authorId,Long courseId);
}
