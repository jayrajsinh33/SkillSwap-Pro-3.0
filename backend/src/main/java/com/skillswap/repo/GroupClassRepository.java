package com.skillswap.repo;
import com.skillswap.model.GroupClass;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface GroupClassRepository extends JpaRepository<GroupClass,Long> {
    java.util.Optional<GroupClass> findByLectureId(String lectureId);
 List<GroupClass> findAllByOrderByStartAtAsc();
 List<GroupClass> findByMentorIdOrderByStartAtAsc(Long id);
}
