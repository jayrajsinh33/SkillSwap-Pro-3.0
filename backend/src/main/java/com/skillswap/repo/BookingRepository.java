package com.skillswap.repo;
import com.skillswap.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface BookingRepository extends JpaRepository<Booking,Long> {
    List<Booking> findByLearnerIdOrMentorIdOrderByStartAtDesc(Long learnerId,Long mentorId);
    List<Booking> findByMentorIdOrderByStartAtDesc(Long mentorId);
    Optional<Booking> findByLectureId(String lectureId);
}
