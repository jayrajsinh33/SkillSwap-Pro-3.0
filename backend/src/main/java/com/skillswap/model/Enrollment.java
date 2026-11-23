package com.skillswap.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="enrollments",uniqueConstraints=@UniqueConstraint(columnNames={"learner_id","course_id"}))
public class Enrollment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional=false) public AppUser learner;
    @ManyToOne(optional=false) public Course course;
    public int progress = 0;
    public long amountPaidPaise;
    public Instant enrolledAt = Instant.now();
}
