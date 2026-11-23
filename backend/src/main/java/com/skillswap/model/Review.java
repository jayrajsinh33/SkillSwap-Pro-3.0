package com.skillswap.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="reviews",uniqueConstraints=@UniqueConstraint(columnNames={"author_id","course_id"}))
public class Review {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional=false) public AppUser author;
    @ManyToOne(optional=false) public Course course;
    public int stars;
    @Column(length=1000) public String comment;
    public Instant createdAt = Instant.now();
}
