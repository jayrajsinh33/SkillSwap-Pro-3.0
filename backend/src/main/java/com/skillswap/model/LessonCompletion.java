package com.skillswap.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="lesson_completions",uniqueConstraints=@UniqueConstraint(columnNames={"learner_id","lesson_id"}))
public class LessonCompletion {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(optional=false) public AppUser learner;
 @ManyToOne(optional=false) public Lesson lesson;
 public Instant completedAt = Instant.now();
}
